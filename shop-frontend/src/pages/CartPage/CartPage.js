import React from 'react';
import { Container, Row, Col, Button} from 'react-bootstrap';
import { Link } from 'react-router-dom';
import styled from 'styled-components';
import { useCart } from '../../contexts/CartContext';
import CartItem from '../../components/CartItem/CartItem';
import OrderSummary from '../../components/OrderSummary/OrderSummary';

const CartPageWrapper = styled.div`
  padding: ${props => props.theme.spacing.xl} 0;
  background-color: ${props => props.theme.colors.background};
  min-height: 60vh;

  @media (max-width: 767.98px) {
    padding: ${props => props.theme.spacing.lg} 0;
  }

  @media (max-width: 575.98px) {
    padding: ${props => props.theme.spacing.md} 0;
  }
`;

const CartTitle = styled.h1`
  text-align: center;
  margin-bottom: ${props => props.theme.spacing.xl};
  color: ${props => props.theme.colors.text};
  font-family: ${props => props.theme.typography.headingFontFamily};
  font-weight: ${props => props.theme.typography.fontWeight.bold};

  @media (max-width: 767.98px) {
    margin-bottom: ${props => props.theme.spacing.lg};
    font-size: ${props => props.theme.typography.fontSize.large};
  }
`;

const CartItemsContainer = styled.div`
  background-color: ${props => props.theme.colors.background};
  border-radius: ${props => props.theme.borderRadius.card};
  box-shadow: ${props => props.theme.shadows.card};
  padding: ${props => props.theme.spacing.lg};
  margin-bottom: ${props => props.theme.spacing.md};
  border: 1px solid #eee;

  @media (max-width: 767.98px) {
    padding: ${props => props.theme.spacing.md};
  }

  @media (max-width: 575.98px) {
    padding: ${props => props.theme.spacing.sm};
    box-shadow: ${props => props.theme.shadows.small};
    border: 1px solid #eee;
  }
`;

const EmptyCartContainer = styled.div`
  text-align: center;
  padding: ${props => props.theme.spacing.xxl} ${props => props.theme.spacing.lg};
  background-color: ${props => props.theme.colors.background};
  border-radius: ${props => props.theme.borderRadius.card};
  box-shadow: ${props => props.theme.shadows.card};
  border: 1px solid #eee;

  p {
    font-size: ${props => props.theme.typography.fontSize.large};
    color: ${props => props.theme.colors.text};
    margin-bottom: ${props => props.theme.spacing.lg};
    font-family: ${props => props.theme.typography.bodyFontFamily};
  }

  @media (max-width: 767.98px) {
    padding: ${props => props.theme.spacing.xl} ${props => props.theme.spacing.md};

    p {
      font-size: ${props => props.theme.typography.fontSize.base};
    }
  }

  @media (max-width: 575.98px) {
    padding: ${props => props.theme.spacing.lg} ${props => props.theme.spacing.sm};
    box-shadow: ${props => props.theme.shadows.small};
    border: 1px solid #eee;
  }
`;

const ClearCartButtonContainer = styled.div`
  margin-bottom: ${props => props.theme.spacing.md};
  text-align: right;

  @media (max-width: 767.98px) {
    text-align: center;
    margin-bottom: ${props => props.theme.spacing.sm};
  }
`;

const StyledClearButton = styled(Button)`
  color: ${props => props.theme.colors.status.error};
  border-color: ${props => props.theme.colors.status.error};
  background-color: transparent;
  font-family: ${props => props.theme.typography.bodyFontFamily};
  border-radius: ${props => props.theme.borderRadius.button};

  &:hover, &:focus, &:active {
    background-color: ${props => props.theme.colors.status.error};
    border-color: ${props => props.theme.colors.status.error};
    color: ${props => props.theme.colors.buttonText};
  }
`;

const StyledButton = styled(Button)`
  background-color: ${props => props.theme.colors.primary};
  border-color: ${props => props.theme.colors.primary};
  color: ${props => props.theme.colors.buttonText};
  font-family: ${props => props.theme.typography.bodyFontFamily};
  font-weight: ${props => props.theme.typography.fontWeight.medium};
  border-radius: ${props => props.theme.borderRadius.button};
  
  &:hover, &:focus, &:active {
    background-color: ${props => props.theme.colors.interactive.hover};
    border-color: ${props => props.theme.colors.interactive.hover};
    color: ${props => props.theme.colors.buttonText};
  }
`;

const CartPage = () => {
  const { cartItems, totalAmount, removeFromCart, updateQuantity, clearCart } = useCart();

  const handleIncreaseQuantity = (itemId) => {
    const item = cartItems.find(item => item.id === itemId);
    if (item) {
      updateQuantity(itemId, item.quantity + 1);
    }
  };

  const handleDecreaseQuantity = (itemId) => {
    const item = cartItems.find(item => item.id === itemId);
    if (item && item.quantity > 1) {
      updateQuantity(itemId, item.quantity - 1);
    } else {
      removeFromCart(itemId);
    }
  };

  const handleRemoveItem = (itemId) => {
    removeFromCart(itemId);
  };

  const handleClearCart = () => {
    if (window.confirm('Вы уверены, что хотите очистить корзину?')) {
      clearCart();
    }
  };

  return (
      <CartPageWrapper>
        <Container>
          <CartTitle>Корзина</CartTitle>

          {cartItems.length > 0 && (
              <ClearCartButtonContainer>
                <StyledClearButton
                    variant="outline-danger"
                    size="sm"
                    onClick={handleClearCart}
                >
                  Очистить корзину
                </StyledClearButton>
              </ClearCartButtonContainer>
          )}

          <Row>
            <Col lg={9} md={8}>
              {cartItems.length > 0 ? (
                  <CartItemsContainer>
                    {cartItems.map(item => (
                        <CartItem
                            key={item.id}
                            item={item}
                            onIncrease={() => handleIncreaseQuantity(item.id)}
                            onDecrease={() => handleDecreaseQuantity(item.id)}
                            onRemove={() => handleRemoveItem(item.id)}
                        />
                    ))}
                  </CartItemsContainer>
              ) : (
                  <EmptyCartContainer>
                    <p>Ваша корзина пуста</p>
                    <StyledButton
                        as={Link}
                        to="/products"
                        size="lg"
                    >
                      Перейти к покупкам
                    </StyledButton>
                  </EmptyCartContainer>
              )}
            </Col>

            <Col lg={3} md={4}>
              {cartItems.length > 0 && (
                  <OrderSummary
                      totalAmount={totalAmount}
                      cartItems={cartItems}
                  />
              )}
            </Col>
          </Row>
        </Container>
      </CartPageWrapper>
  );
};

export default CartPage;