import React, { useState } from 'react';
import { Navbar, Nav, Container, Button, Badge } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { useCart } from '../../contexts/CartContext';
import { useTheme } from '../../contexts/ThemeContext';

const StyledNavbar = styled(Navbar)`
    background-color: ${props => props.theme.colors.background} !important;
    border-bottom: 1px solid ${props => props.theme.colors.text}22;
    box-shadow: ${props => props.theme.shadows.small};

    .navbar-brand {
        font-family: ${props => props.theme.typography.headingFontFamily};
        font-weight: ${props => props.theme.typography.fontWeight.bold};
        color: ${props => props.theme.colors.primary} !important;
        font-size: ${props => props.theme.typography.fontSize.large};

        &:hover {
            color: ${props => props.theme.colors.interactive.hover} !important;
        }
    }

    .navbar-nav .nav-link {
        font-family: ${props => props.theme.typography.bodyFontFamily};
        color: ${props => props.theme.colors.text} !important;
        font-weight: ${props => props.theme.typography.fontWeight.medium};
        margin-right: ${props => props.theme.spacing.sm};

        &:hover {
            color: ${props => props.theme.colors.primary} !important;
        }
    }

    .navbar-toggler {
        border-color: ${props => props.theme.colors.primary};

        &:focus {
            box-shadow: 0 0 0 0.2rem ${props => props.theme.colors.primary}44;
        }
    }
`;

const LogoImage = styled.img`
    height: 40px;
    width: auto;
    margin-right: ${props => props.theme.spacing.sm};

    @media (max-width: 767.98px) {
        height: 32px;
    }
`;

const StyledButton = styled(Button)`
    font-family: ${props => props.theme.typography.bodyFontFamily};
    font-weight: ${props => props.theme.typography.fontWeight.medium};
    border-radius: ${props => props.theme.borderRadius.button};
    margin-left: ${props => props.theme.spacing.xs};

    &.btn-primary {
        background-color: ${props => props.theme.colors.primary};
        border-color: ${props => props.theme.colors.primary};
        color: ${props => props.theme.colors.buttonText};

        &:hover {
            background-color: ${props => props.theme.colors.interactive.hover};
            border-color: ${props => props.theme.colors.interactive.hover};
        }
    }

    &.btn-outline-primary {
        color: ${props => props.theme.colors.primary};
        border-color: ${props => props.theme.colors.primary};

        &:hover {
            background-color: ${props => props.theme.colors.primary};
            border-color: ${props => props.theme.colors.primary};
            color: ${props => props.theme.colors.buttonText};
        }
    }
`;

const CartBadge = styled(Badge)`
    background-color: ${props => props.theme.colors.accent} !important;
    color: ${props => props.theme.colors.buttonText};
`;

const Header = () => {
    const [expanded, setExpanded] = useState(false);
    const { totalItems } = useCart();
    const { theme } = useTheme();
    const navigate = useNavigate();

    const handleNavClick = () => {
        setExpanded(false);
    };

    return (
        <StyledNavbar expand="lg" expanded={expanded} onToggle={setExpanded}>
            <Container>
                <Navbar.Brand as={Link} to="/" onClick={handleNavClick}>
                    {theme.assets.logoUrl ? (
                        <>
                            <LogoImage
                                src={theme.assets.logoUrl}
                                alt="Логотип"
                                onError={(e) => {
                                    e.target.style.display = 'none';
                                }}
                            />
                            ShopApp
                        </>
                    ) : (
                        'ShopApp'
                    )}
                </Navbar.Brand>

                <Navbar.Toggle aria-controls="basic-navbar-nav" />

                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="me-auto">
                        <Nav.Link as={Link} to="/" onClick={handleNavClick}>
                            Главная
                        </Nav.Link>
                        <Nav.Link as={Link} to="/products" onClick={handleNavClick}>
                            Товары
                        </Nav.Link>
                    </Nav>

                    <Nav className="ms-auto d-flex align-items-center">
                        <Nav.Link as={Link} to="/cart" onClick={handleNavClick} className="position-relative">
                            Корзина
                            {totalItems > 0 && (
                                <CartBadge
                                    pill
                                    className="position-absolute top-0 start-100 translate-middle"
                                >
                                    {totalItems}
                                </CartBadge>
                            )}
                        </Nav.Link>
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </StyledNavbar>
    );
};

export default Header;