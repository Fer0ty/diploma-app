import React, { useState } from 'react';
import { Button } from 'react-bootstrap';
import styled from 'styled-components';
import CheckoutModal from '../CheckoutModal/CheckoutModal';

const OrderSummaryContainer = styled.div`
  background-color: ${props => props.theme.colors.background};
  border-radius: ${props => props.theme.borderRadius.card};
  padding: ${props => props.theme.spacing.lg};
  box-shadow: ${props => props.theme.shadows.card};
  border: 1px solid #eee;
  position: sticky;
  top: 20px;

  @media (max-width: 991.98px) {
    position: static;
    margin-top: ${props => props.theme.spacing.lg};
  }

  @media (max-width: 767.98px) {
    padding: ${props => props.theme.spacing.md};
  }

  @media (max-width: 575.98px) {
    padding: ${props => props.theme.spacing.sm};
  }
`;

const SummaryTitle = styled.h2`
  font-family: ${props => props.theme.typography.headingFontFamily};
  font-size: ${props => props.theme.typography.fontSize.heading};
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  margin-bottom: ${props => props.theme.spacing.lg};
  color: ${props => props.theme.colors.text};
  text-align: center;
  padding-bottom: ${props => props.theme.spacing.sm};
  border-bottom: 2px solid ${props => props.theme.colors.primary};

  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.large};
    margin-bottom: ${props => props.theme.spacing.md};
  }
`;

const SummaryDetails = styled.div`
  margin-bottom: ${props => props.theme.spacing.lg};

  @media (max-width: 767.98px) {
    margin-bottom: ${props => props.theme.spacing.md};
  }
`;

const SummaryRow = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: ${props => props.theme.spacing.sm};
  padding: ${props => props.theme.spacing.sm} 0;

  &:not(:last-child) {
    border-bottom: 1px solid #f8f9fa;
  }

  @media (max-width: 767.98px) {
    margin-bottom: ${props => props.theme.spacing.xs};
  }
`;

const SummaryLabel = styled.span`
  color: ${props => props.theme.colors.text}99; /* 60% opacity */
  font-family: ${props => props.theme.typography.bodyFontFamily};
`;

const SummaryValue = styled.span`
  font-weight: ${props => props.theme.typography.fontWeight.medium};
  color: ${props => props.theme.colors.text};
  font-family: ${props => props.theme.typography.bodyFontFamily};
`;

const ItemCount = styled.div`
  text-align: center;
  font-size: ${props => props.theme.typography.fontSize.small};
  color: ${props => props.theme.colors.text}99; /* 60% opacity */
  margin-bottom: ${props => props.theme.spacing.md};
  padding: ${props => props.theme.spacing.sm};
  background-color: #f8f9fa;
  border-radius: ${props => props.theme.borderRadius.small};
  font-family: ${props => props.theme.typography.bodyFontFamily};
`;

const SummaryTotal = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: ${props => props.theme.spacing.md} 0;
  border-top: 2px solid ${props => props.theme.colors.primary};
  margin-bottom: ${props => props.theme.spacing.lg};

  @media (max-width: 767.98px) {
    margin-bottom: ${props => props.theme.spacing.md};
  }
`;

const TotalLabel = styled.span`
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  font-size: ${props => props.theme.typography.fontSize.large};
  color: ${props => props.theme.colors.text};
  font-family: ${props => props.theme.typography.headingFontFamily};
`;

const TotalValue = styled.span`
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  font-size: ${props => props.theme.typography.fontSize.heading};
  color: ${props => props.theme.colors.accent};
  font-family: ${props => props.theme.typography.headingFontFamily};
`;

const CheckoutButton = styled(Button)`
  width: 100%;
  font-weight: ${props => props.theme.typography.fontWeight.medium};
  font-family: ${props => props.theme.typography.bodyFontFamily};
  padding: ${props => props.theme.spacing.sm} ${props => props.theme.spacing.md};
  font-size: ${props => props.theme.typography.fontSize.base};
  border-radius: ${props => props.theme.borderRadius.button};
  background-color: ${props => props.theme.colors.primary};
  border-color: ${props => props.theme.colors.primary};
  color: ${props => props.theme.colors.buttonText};
  
  &:disabled {
    cursor: not-allowed;
    opacity: 0.6;
    background-color: ${props => props.theme.colors.text}33; /* 20% opacity */
    border-color: ${props => props.theme.colors.text}33;
  }
  
  &:not(:disabled):hover {
    background-color: ${props => props.theme.colors.interactive.hover};
    border-color: ${props => props.theme.colors.interactive.hover};
    transform: translateY(-2px);
    box-shadow: ${props => props.theme.shadows.medium};
    transition: all 0.3s ease;
  }
  
  &:not(:disabled):active {
    background-color: ${props => props.theme.colors.interactive.active};
    border-color: ${props => props.theme.colors.interactive.active};
  }
`;

const OrderSummary = ({ totalAmount, cartItems }) => {
  const [showCheckoutModal, setShowCheckoutModal] = useState(false);

  const totalItems = cartItems.reduce((sum, item) => sum + item.quantity, 0);

  const handleShowCheckout = () => {
    setShowCheckoutModal(true);
  };

  const handleCloseCheckout = () => {
    setShowCheckoutModal(false);
  };

  return (
      <>
        <OrderSummaryContainer>
          <SummaryTitle>Сумма заказа</SummaryTitle>

          {totalItems > 0 && (
              <ItemCount>
                Товаров в корзине: {totalItems} шт.
              </ItemCount>
          )}

          <SummaryDetails>
            <SummaryRow>
              <SummaryLabel>Стоимость товаров:</SummaryLabel>
              <SummaryValue>{totalAmount.toLocaleString()} ₽</SummaryValue>
            </SummaryRow>
            <SummaryRow>
              <SummaryLabel>Доставка:</SummaryLabel>
              <SummaryValue>Рассчитается при оформлении</SummaryValue>
            </SummaryRow>
          </SummaryDetails>

          <SummaryTotal>
            <TotalLabel>Итого:</TotalLabel>
            <TotalValue>{totalAmount.toLocaleString()} ₽</TotalValue>
          </SummaryTotal>

          <CheckoutButton
              size="lg"
              onClick={handleShowCheckout}
              disabled={totalAmount === 0}
          >
            {totalAmount === 0 ? 'Корзина пуста' : 'Оформить заказ'}
          </CheckoutButton>
        </OrderSummaryContainer>

        <CheckoutModal
            show={showCheckoutModal}
            onHide={handleCloseCheckout}
            total={totalAmount}
            cartItems={cartItems}
        />
      </>
  );
};

export default OrderSummary;