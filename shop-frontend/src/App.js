import React from 'react';
import {BrowserRouter as Router, Route, Routes, Navigate} from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-icons/font/bootstrap-icons.css';
import {ThemeProvider} from './contexts/ThemeContext';
import {CartProvider} from './contexts/CartContext';
import Header from './components/Header/Header';
import Footer from './components/Footer/Footer';
import HomePage from './pages/HomePage/HomePage';
import ProductsPage from './pages/ProductsPage/ProductsPage';
import ProductPage from './pages/ProductPage/ProductPage';
import CartPage from './pages/CartPage/CartPage';
import styled from 'styled-components';
import GlobalStyles from './styles/GlobalStyles';

const AppContainer = styled.div`
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    color: ${props => props.theme.colors.text};
    background-color: ${props => props.theme.colors.background};
`;

const Main = styled.main`
    flex-grow: 1;
`;

const NotFoundContainer = styled.div`
    margin: 3rem auto;
    text-align: center;
`;

const NotFound = () => (
    <NotFoundContainer className="container">
        <h1>404 - Страница не найдена</h1>
        <p>Запрашиваемая страница не существует.</p>
    </NotFoundContainer>
);

function App() {
    return (
        <ThemeProvider>
            <CartProvider>
                <Router>
                    <GlobalStyles /> {/* Добавляем глобальные стили */}
                    <AppContainer>
                        <Header/>
                        <Main>
                            <Routes>
                                <Route path="/" element={<HomePage/>}/>
                                <Route path="/products" element={<ProductsPage/>}/>
                                <Route path="/products/:id" element={<ProductPage/>}/> {/* Исправлен путь */}
                                <Route path="/cart" element={<CartPage/>}/>
                                <Route path="/404" element={<NotFound/>}/>
                                <Route path="*" element={<Navigate to="/404" replace />}/>
                            </Routes>
                        </Main>
                        <Footer/>
                    </AppContainer>
                </Router>
            </CartProvider>
        </ThemeProvider>
    );
}

export default App;