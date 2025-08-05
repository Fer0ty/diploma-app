import React from 'react';
import {Link} from 'react-router-dom';
import LandingNav from '../components/landing/LandingNav';
import '../styles/landing.css';

const LandingPage = () => {
    return (
        <div className="landing-container">
            <LandingNav/>

            {/* Hero Section */}
            <section className="hero-section">
                <div className="hero-content">
                    <h1 className="hero-title slide-in-top">Создайте свой интернет-магазин за считанные минуты</h1>
                    <p className="hero-subtitle slide-in-top delay-100">
                        Простая и мощная платформа для запуска собственного онлайн-бизнеса без навыков программирования
                    </p>
                    <div className="hero-buttons slide-in-top delay-200">
                        <Link to="/register" className="primary-button">Создать магазин</Link>
                        <Link to="/login" className="secondary-button">Войти в аккаунт</Link>
                    </div>
                </div>
                <div className="hero-image slide-in-right delay-300">
                    <div className="image-placeholder">
                        <span className="store-icon">🏪</span>
                    </div>
                </div>
            </section>
            {/* Features Section */}
            <section className="features-section" id="features">
                <h2 className="section-title">Почему стоит выбрать нашу платформу</h2>
                <div className="features-grid">
                    <div className="feature-card">
                        <div className="feature-icon">🚀</div>
                        <h3 className="feature-title">Быстрый запуск</h3>
                        <p className="feature-description">
                            Создайте и запустите свой магазин в течение нескольких минут. Никакого кодирования не
                            требуется.
                        </p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">🎨</div>
                        <h3 className="feature-title">Настраиваемый дизайн</h3>
                        <p className="feature-description">
                            Выбирайте из множества тем и настраивайте цвета, шрифты и стили вашего магазина.
                        </p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">📱</div>
                        <h3 className="feature-title">Адаптивный дизайн</h3>
                        <p className="feature-description">
                            Ваш магазин будет отлично выглядеть на всех устройствах — от мобильных телефонов до
                            настольных компьютеров.
                        </p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">📊</div>
                        <h3 className="feature-title">Аналитика в реальном времени</h3>
                        <p className="feature-description">
                            Отслеживайте продажи, посетителей и другие важные показатели в удобной панели управления.
                        </p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">🔒</div>
                        <h3 className="feature-title">Безопасность</h3>
                        <p className="feature-description">
                            Ваши данные и данные ваших клиентов надежно защищены современными методами шифрования.
                        </p>
                    </div>
                    <div className="feature-card">
                        <div className="feature-icon">💸</div>
                        <h3 className="feature-title">Гибкие платежи</h3>
                        <p className="feature-description">
                            Принимайте платежи различными способами, включая банковские карты и электронные кошельки.
                        </p>
                    </div>
                </div>
            </section>

            {/* How It Works Section */}
            <section className="how-it-works-section">
                <h2 className="section-title">Как это работает</h2>
                <div className="steps-container">
                    <div className="step">
                        <div className="step-number">1</div>
                        <h3 className="step-title">Регистрация</h3>
                        <p className="step-description">
                            Создайте аккаунт, указав название вашего магазина и контактную информацию.
                        </p>
                    </div>
                    <div className="step">
                        <div className="step-number">2</div>
                        <h3 className="step-title">Добавление товаров</h3>
                        <p className="step-description">
                            Добавьте ваши товары, загрузите изображения и укажите цены через простой интерфейс.
                        </p>
                    </div>
                    <div className="step">
                        <div className="step-number">3</div>
                        <h3 className="step-title">Настройка дизайна</h3>
                        <p className="step-description">
                            Выберите тему оформления и настройте внешний вид вашего магазина по своему вкусу.
                        </p>
                    </div>
                    <div className="step">
                        <div className="step-number">4</div>
                        <h3 className="step-title">Запуск</h3>
                        <p className="step-description">
                            Активируйте ваш магазин, и он сразу будет доступен для ваших клиентов!
                        </p>
                    </div>
                </div>
            </section>


            {/* Call to Action Section */}
            <section className="cta-section">
                <h2 className="cta-title">Готовы начать продавать онлайн?</h2>
                <Link to="/register" className="cta-button">Создать магазин</Link>
            </section>

            {/* Footer */}
            <footer className="landing-footer">
                <div className="footer-content">
                    <div className="footer-logo">Редактор Магазина</div>
                    <div className="footer-links">
                        <div className="footer-column">
                            <h4 className="footer-heading">Продукт</h4>
                            <ul>
                                <li><a href="#features">Возможности</a></li>
                                <li><a href="#pricing">Тарифы</a></li>
                                <li><a href="#faq">FAQ</a></li>
                            </ul>
                        </div>
                        <div className="footer-column">
                            <h4 className="footer-heading">Компания</h4>
                            <ul>
                                <li><a href="#about">О нас</a></li>
                                <li><a href="#blog">Блог</a></li>
                                <li><a href="#careers">Карьера</a></li>
                            </ul>
                        </div>
                        <div className="footer-column">
                            <h4 className="footer-heading">Поддержка</h4>
                            <ul>
                                <li><a href="#contact">Связаться с нами</a></li>
                                <li><a href="#docs">Документация</a></li>
                                <li><a href="#status">Статус системы</a></li>
                            </ul>
                        </div>
                        <div className="footer-column">
                            <h4 className="footer-heading">Правовая информация</h4>
                            <ul>
                                <li><a href="#terms">Условия использования</a></li>
                                <li><a href="#privacy">Политика конфиденциальности</a></li>
                                <li><a href="#cookies">Политика cookies</a></li>
                            </ul>
                        </div>
                    </div>
                </div>
                <div className="footer-bottom">
                    <p>© {new Date().getFullYear()} Редактор Магазина. Все права защищены.</p>
                </div>
            </footer>
        </div>
    );
};

export default LandingPage;