import React from 'react';
import { Row, Col, Button } from 'react-bootstrap';
import styled from 'styled-components';
import { getImageUrl } from '../../services/api';

const CartItemContainer = styled.div`
  padding: ${props => props.theme.spacing.lg};
  border-bottom: 1px solid #f8f9fa;

  &:last-child {
    border-bottom: none;
  }

  @media (max-width: 767.98px) {
    padding: ${props => props.theme.spacing.md};
  }

  @media (max-width: 575.98px) {
    padding: ${props => props.theme.spacing.sm};
  }
`;

const ProductImage = styled.img`
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: ${props => props.theme.borderRadius.image};
  border: 1px solid #eee;

  @media (max-width: 767.98px) {
    width: 80px;
    height: 80px;
  }

  @media (max-width: 575.98px) {
    width: 60px;
    height: 60px;
  }
`;

const ProductDetails = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding-left: ${props => props.theme.spacing.md};

  @media (max-width: 767.98px) {
    padding-left: ${props => props.theme.spacing.sm};
  }

  @media (max-width: 575.98px) {
    padding-left: ${props => props.theme.spacing.xs};
  }
`;

const ProductName = styled.h5`
  font-family: ${props => props.theme.typography.headingFontFamily};
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  color: ${props => props.theme.colors.text};
  margin-bottom: ${props => props.theme.spacing.sm};
  font-size: ${props => props.theme.typography.fontSize.base};
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.small};
    margin-bottom: ${props => props.theme.spacing.xs};
  }
`;

const ProductPrice = styled.div`
  font-family: ${props => props.theme.typography.bodyFontFamily};
  font-weight: ${props => props.theme.typography.fontWeight.medium};
  color: ${props => props.theme.colors.accent};
  font-size: ${props => props.theme.typography.fontSize.base};

  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.small};
  }
`;

const QuantityControls = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: ${props => props.theme.spacing.sm};

  @media (max-width: 767.98px) {
    gap: ${props => props.theme.spacing.xs};
  }
`;

const QuantityButton = styled(Button)`
  width: 36px;
  height: 36px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: ${props => props.theme.typography.fontSize.base};
  font-family: ${props => props.theme.typography.bodyFontFamily};
  background-color: ${props => props.theme.colors.primary};
  border-color: ${props => props.theme.colors.primary};
  color: ${props => props.theme.colors.buttonText};
  border-radius: ${props => props.theme.borderRadius.button};
  
  &:hover, &:focus, &:active {
    background-color: ${props => props.theme.colors.interactive.hover};
    border-color: ${props => props.theme.colors.interactive.hover};
    color: ${props => props.theme.colors.buttonText};
  }
  
  @media (max-width: 767.98px) {
    width: 32px;
    height: 32px;
    font-size: ${props => props.theme.typography.fontSize.small};
  }
  
  @media (max-width: 575.98px) {
    width: 28px;
    height: 28px;
    font-size: ${props => props.theme.typography.fontSize.tiny};
  }
`;

const QuantityDisplay = styled.span`
  font-family: ${props => props.theme.typography.bodyFontFamily};
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  color: ${props => props.theme.colors.text};
  min-width: 30px;
  text-align: center;
  font-size: ${props => props.theme.typography.fontSize.base};

  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.small};
    min-width: 25px;
  }
`;

const TotalPrice = styled.div`
  text-align: center;
  font-family: ${props => props.theme.typography.headingFontFamily};
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  color: ${props => props.theme.colors.text};
  font-size: ${props => props.theme.typography.fontSize.large};

  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.base};
  }
`;

const RemoveButton = styled(Button)`
  background-color: transparent;
  border-color: ${props => props.theme.colors.status.error};
  color: ${props => props.theme.colors.status.error};
  font-family: ${props => props.theme.typography.bodyFontFamily};
  border-radius: ${props => props.theme.borderRadius.button};
  
  &:hover, &:focus, &:active {
    background-color: ${props => props.theme.colors.status.error};
    border-color: ${props => props.theme.colors.status.error};
    color: ${props => props.theme.colors.buttonText};
  }
  
  @media (max-width: 575.98px) {
    font-size: ${props => props.theme.typography.fontSize.tiny};
    padding: ${props => props.theme.spacing.xs};
  }
`;

const MobileRow = styled.div`
  @media (max-width: 575.98px) {
    margin-top: ${props => props.theme.spacing.sm};
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
`;

const CartItem = ({ item, onIncrease, onDecrease, onRemove }) => {
  const totalPrice = item.price * item.quantity;
  const imageUrl = item.mainPhotoPath ? getImageUrl(item.mainPhotoPath) : '/images/no-image.jpg';

  return (
      <CartItemContainer>
        <Row className="align-items-center">
          <Col xs={3} sm={2} md={2}>
            <ProductImage
                src={imageUrl}
                alt={item.name}
                onError={(e) => {
                  e.target.src = '/images/no-image.jpg';
                }}
            />
          </Col>

          <Col xs={9} sm={4} md={3}>
            <ProductDetails>
              <ProductName>{item.name}</ProductName>
              <ProductPrice>{Number(item.price).toLocaleString()} ₽</ProductPrice>
            </ProductDetails>
          </Col>

          <Col xs={6} sm={3} md={3}>
            <QuantityControls>
              <QuantityButton
                  size="sm"
                  onClick={onDecrease}
                  aria-label="Уменьшить количество"
              >
                −
              </QuantityButton>
              <QuantityDisplay>{item.quantity}</QuantityDisplay>
              <QuantityButton
                  size="sm"
                  onClick={onIncrease}
                  aria-label="Увеличить количество"
              >
                +
              </QuantityButton>
            </QuantityControls>
          </Col>

          <Col xs={6} sm={2} md={2}>
            <TotalPrice>
              {totalPrice.toLocaleString()} ₽
            </TotalPrice>
          </Col>

          <Col xs={12} sm={1} md={2}>
            <MobileRow>
              <RemoveButton
                  variant="outline-danger"
                  size="sm"
                  onClick={onRemove}
              >
                Удалить
              </RemoveButton>
            </MobileRow>
          </Col>
        </Row>
      </CartItemContainer>
  );
};

export default CartItem;