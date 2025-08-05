#!/bin/bash
# Проверка существования необходимых директорий и файлов
check_files() {
    local missing=0
    # Проверка необходимых директорий
    for dir in "admin-frontend" "shop-frontend" "nginx" "sql/models"; do
        if [ ! -d "$dir" ]; then
            echo "Директория $dir не найдена"
            missing=1
        fi
    done
    # Проверка необходимых файлов конфигурации
    if [ ! -f "nginx/nginx.conf" ]; then
        echo "Файл nginx/nginx.conf не найден"
        missing=1
    fi

    if [ ! -f "docker-compose.yml" ]; then
        echo "Файл docker-compose.yml не найден"
        missing=1
    fi
    # Проверка наличия SQL файлов в директории sql/models
    if [ ! "$(ls -A sql/models 2>/dev/null)" ]; then
        echo "Директория sql/models пуста или не содержит SQL файлы"
        missing=1
    fi

    if [ $missing -eq 1 ]; then
        echo "Некоторые необходимые файлы или директории не найдены. Проверьте структуру проекта."
        exit 1
    fi
}

check_files

echo "Останавливаем и удаляем старые контейнеры и volumes..."
docker-compose down --volumes --remove-orphans

echo "Удаляем старые данные PostgreSQL..."
docker volume rm -f $(docker-compose config --volumes | grep postgres_data) 2>/dev/null || true

echo "Удаляем старые образы для пересборки..."
docker-compose build --no-cache

# Пересоздаем volumes
echo "Создаем новые volumes..."
docker-compose up --no-start
docker-compose start postgres

# Ждем инициализации БД
echo "Ожидание инициализации PostgreSQL..."
timeout=60
counter=0
while ! docker exec diploma-postgres pg_isready -U shopbase > /dev/null 2>&1; do
    sleep 2
    counter=$((counter + 2))
    if [ $counter -ge $timeout ]; then
        echo "Timeout: PostgreSQL не запустился за $timeout секунд"
        docker logs diploma-postgres
        exit 1
    fi
    echo "  Ожидание PostgreSQL... ($counter/$timeout сек)"
done

echo "PostgreSQL успешно инициализирован"

# Запуск контейнеров
echo "Запускаем остальные контейнеры..."
docker-compose up -d

echo "Ожидание запуска всех сервисов (30 секунд)..."
sleep 30

# Проверка состояния
echo "Проверка состояния контейнеров:"
docker ps -a | grep diploma

# Проверяем, что схемы БД созданы
echo "Проверка созданных таблиц в базе данных:"
docker exec diploma-postgres psql -U shopbase -d shopbase -c "\dt" 2>/dev/null || echo "Не удалось подключиться к базе данных"