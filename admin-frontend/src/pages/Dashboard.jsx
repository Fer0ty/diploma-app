import React, {useEffect, useState} from 'react';
import {Link} from 'react-router-dom';
import {CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis} from 'recharts';
import {useAuth} from '../hooks/useAuth';
import '../pages/pages.css';

const tempChartData = [
    {name: 'Янв', orders: 4, amount: 240},
    {name: 'Фев', orders: 3, amount: 180},
    {name: 'Мар', orders: 5, amount: 320},
    {name: 'Апр', orders: 6, amount: 380},
    {name: 'Май', orders: 8, amount: 520},
    {name: 'Июн', orders: 7, amount: 460},
];

const Dashboard = () => {
    const {user} = useAuth();
    const [stats, setStats] = useState({
        totalOrders: 0,
        totalProducts: 0,
        recentOrders: [],
        loading: true
    });

    useEffect(() => {
        // В будущем здесь будет запрос к API для получения статистики
        setTimeout(() => {
            setStats({
                totalOrders: 33,
                totalProducts: 42,
                recentOrders: [],
                loading: false
            });
        }, 1000);
    }, []);

    if (stats.loading) {
        return (
            <div className="page-container">
                <div className="loading-spinner">
                    <div className="spin">⟳</div>
                    Загрузка...
                </div>
            </div>
        );
    }

    return (
        <div className="page-container">
            <div className="page-header slide-in-top">
                <h1 className="page-title">Добро пожаловать, {user?.username || 'Владелец Магазина'}</h1>
            </div>

            <p className="text-secondary mb-lg slide-in-top delay-100">
                Вот обзор производительности вашего магазина
            </p>

            <div className="card-grid">
                {/* Stats Card - Orders */}
                <div className="stats-card slide-in-left delay-200">
                    <div className="stats-card-icon">🛒</div>
                    <div className="stats-card-title">Всего заказов</div>
                    <div className="stats-card-value">{stats.totalOrders}</div>
                    <Link to="/orders" className="form-link mt-sm">Посмотреть все заказы</Link>
                </div>

                {/* Stats Card - Products */}
                <div className="stats-card slide-in-left delay-300">
                    <div className="stats-card-icon">📦</div>
                    <div className="stats-card-title">Товары</div>
                    <div className="stats-card-value">{stats.totalProducts}</div>
                    <Link to="/products" className="form-link mt-sm">Управление товарами</Link>
                </div>

                {/* Stats Card - Placeholder for future use */}
                <div className="stats-card slide-in-left delay-400">
                    <div className="stats-card-icon">💰</div>
                    <div className="stats-card-title">Общая выручка</div>
                    <div className="stats-card-value">₽92 500</div>
                    <span className="text-success">↑ 15% по сравнению с прошлым месяцем</span>
                </div>

                {/* Stats Card - Placeholder for future use */}
                <div className="stats-card slide-in-left delay-500">
                    <div className="stats-card-icon">👥</div>
                    <div className="stats-card-title">Клиенты</div>
                    <div className="stats-card-value">24</div>
                    <span className="text-success">↑ 8% по сравнению с прошлым месяцем</span>
                </div>
            </div>

            {/* Sales Chart */}
            <div className="chart-container slide-in-bottom delay-200">
                <h2 className="chart-title">Обзор продаж</h2>
                <div className="divider"></div>
                <ResponsiveContainer width="100%" height={300}>
                    <LineChart
                        data={tempChartData}
                        margin={{
                            top: 20,
                            right: 30,
                            left: 20,
                            bottom: 20,
                        }}
                    >
                        <CartesianGrid strokeDasharray="3 3" stroke="var(--divider-color)"/>
                        <XAxis
                            dataKey="name"
                            tick={{fill: 'var(--text-secondary)'}}
                            axisLine={{stroke: 'var(--divider-color)'}}
                        />
                        <YAxis
                            yAxisId="left"
                            tick={{fill: 'var(--text-secondary)'}}
                            axisLine={{stroke: 'var(--divider-color)'}}
                        />
                        <YAxis
                            yAxisId="right"
                            orientation="right"
                            tick={{fill: 'var(--text-secondary)'}}
                            axisLine={{stroke: 'var(--divider-color)'}}
                        />
                        <Tooltip
                            contentStyle={{
                                backgroundColor: 'var(--paper-color)',
                                border: '1px solid var(--divider-color)',
                                color: 'var(--text-primary)'
                            }}
                            formatter={(value, name) => {
                                if (name === 'orders') return [value, 'Заказы'];
                                if (name === 'amount') return [`₽${value}`, 'Сумма'];
                                return [value, name];
                            }}
                        />
                        <Line
                            yAxisId="left"
                            type="monotone"
                            dataKey="orders"
                            name="Заказы"
                            stroke="var(--primary-color)"
                            activeDot={{r: 8}}
                        />
                        <Line
                            yAxisId="right"
                            type="monotone"
                            dataKey="amount"
                            name="Сумма"
                            stroke="var(--secondary-color)"
                        />
                    </LineChart>
                </ResponsiveContainer>
            </div>

            {/* Quick Actions */}
            <div className="page-content slide-in-bottom delay-300">
                <h2 className="chart-title mb-md">Быстрые действия</h2>
                <div className="flex">
                    <Link to="/products/add" className="action-button mr-md">
                        Добавить товар
                    </Link>
                    <Link to="/orders" className="action-button mr-md">
                        Просмотреть последние заказы
                    </Link>
                    <Link to="/settings" className="action-button">
                        Настроить магазин
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;