import React from 'react';
import { Row, Col, Card, Button, Alert, Spinner } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import styled from 'styled-components';
import { getImageUrl } from '../../services/api';
import { useCart } from '../../contexts/CartContext';

const GridContainer = styled.div`
  margin-bottom: ${props => props.theme.spacing.xl};
  
  @media (max-width: 767.98px) {
    margin-bottom: ${props => props.theme.spacing.lg};
  }
`;

const StyledCard = styled(Card)`
  height: 100%;
  border: 1px solid #eee;
  border-radius: ${props => props.theme.borderRadius.card};
  box-shadow: ${props => props.theme.shadows.card};
  transition: all 0.3s ease;
  background-color: ${props => props.theme.colors.background};
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: ${props => props.theme.shadows.large};
    border-color: ${props => props.theme.colors.primary}66; /* 40% opacity */
  }
`;

const CardImageContainer = styled.div`
  position: relative;
  height: 250px;
  overflow: hidden;
  border-radius: ${props => props.theme.borderRadius.card} ${props => props.theme.borderRadius.card} 0 0;
  
  @media (max-width: 767.98px) {
    height: 200px;
  }
  
  @media (max-width: 575.98px) {
    height: 180px;
  }
`;

const CardImage = styled(Card.Img)`
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
  
  ${StyledCard}:hover & {
    transform: scale(1.05);
  }
`;

const CardBody = styled(Card.Body)`
  display: flex;
  flex-direction: column;
  padding: ${props => props.theme.spacing.lg};
  
  @media (max-width: 767.98px) {
    padding: ${props => props.theme.spacing.md};
  }
`;

const ProductTitle = styled(Card.Title)`
  font-family: ${props => props.theme.typography.headingFontFamily};
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  color: ${props => props.theme.colors.text};
  font-size: ${props => props.theme.typography.fontSize.base};
  margin-bottom: ${props => props.theme.spacing.sm};
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.small};
  }
`;

const ProductDescription = styled(Card.Text)`
  font-family: ${props => props.theme.typography.bodyFontFamily};
  color: ${props => props.theme.colors.text}99; /* 60% opacity */
  font-size: ${props => props.theme.typography.fontSize.small};
  margin-bottom: ${props => props.theme.spacing.md};
  flex-grow: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.tiny};
    -webkit-line-clamp: 2;
  }
`;

const ProductPrice = styled.div`
  font-family: ${props => props.theme.typography.headingFontFamily};
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  color: ${props => props.theme.colors.accent};
  font-size: ${props => props.theme.typography.fontSize.large};
  margin-bottom: ${props => props.theme.spacing.md};
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.base};
  }
`;

const ButtonGroup = styled.div`
  display: flex;
  gap: ${props => props.theme.spacing.sm};
  
  @media (max-width: 575.98px) {
    flex-direction: column;
    gap: ${props => props.theme.spacing.xs};
  }
`;

const StyledButton = styled(Button)`
  font-family: ${props => props.theme.typography.bodyFontFamily};
  font-weight: ${props => props.theme.typography.fontWeight.medium};
  border-radius: ${props => props.theme.borderRadius.button};
  
  &.btn-primary {
    background-color: ${props => props.theme.colors.primary};
    border-color: ${props => props.theme.colors.primary};
    color: ${props => props.theme.colors.buttonText};
    
    &:hover, &:focus, &:active {
      background-color: ${props => props.theme.colors.interactive.hover};
      border-color: ${props => props.theme.colors.interactive.hover};
      color: ${props => props.theme.colors.buttonText};
    }
  }
  
  &.btn-outline-primary {
    color: ${props => props.theme.colors.primary};
    border-color: ${props => props.theme.colors.primary};
    background-color: transparent;
    
    &:hover, &:focus, &:active {
      background-color: ${props => props.theme.colors.primary};
      border-color: ${props => props.theme.colors.primary};
      color: ${props => props.theme.colors.buttonText};
    }
  }
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.small};
    padding: ${props => props.theme.spacing.xs} ${props => props.theme.spacing.sm};
  }
`;

const QuantityControls = styled.div`
  display: flex;
  align-items: center;
  gap: ${props => props.theme.spacing.sm};
  margin-bottom: ${props => props.theme.spacing.sm};
  
  @media (max-width: 575.98px) {
    justify-content: center;
  }
`;

const QuantityButton = styled(Button)`
  width: 32px;
  height: 32px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: ${props => props.theme.typography.fontSize.small};
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
`;

const QuantityDisplay = styled.span`
  font-family: ${props => props.theme.typography.bodyFontFamily};
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  color: ${props => props.theme.colors.text};
  min-width: 30px;
  text-align: center;
  font-size: ${props => props.theme.typography.fontSize.small};
`;

const LoadingContainer = styled.div`
  text-align: center;
  padding: ${props => props.theme.spacing.xxl} 0;
  
  p {
    margin-top: ${props => props.theme.spacing.md};
    color: ${props => props.theme.colors.text};
    font-family: ${props => props.theme.typography.bodyFontFamily};
  }
`;

const StyledSpinner = styled(Spinner)`
  color: ${props => props.theme.colors.primary} !important;
`;

const StyledAlert = styled(Alert)`
  font-family: ${props => props.theme.typography.bodyFontFamily};
  border-radius: ${props => props.theme.borderRadius.card};
`;

const EmptyState = styled.div`
  text-align: center;
  padding: ${props => props.theme.spacing.xxl} 0;
  color: ${props => props.theme.colors.text};
  font-family: ${props => props.theme.typography.bodyFontFamily};
  
  h3 {
    font-family: ${props => props.theme.typography.headingFontFamily};
    margin-bottom: ${props => props.theme.spacing.md};
  }
`;

const ProductGrid = ({ products, loading, error }) => {
  const { addToCart, updateQuantity, isInCart, getItemQuantity } = useCart();

  const handleAddToCart = (product) => {
    addToCart(product, 1);
  };

  const handleIncreaseQuantity = (productId) => {
    const currentQuantity = getItemQuantity(productId);
    updateQuantity(productId, currentQuantity + 1);
  };

  const handleDecreaseQuantity = (productId) => {
    const currentQuantity = getItemQuantity(productId);
    if (currentQuantity > 1) {
      updateQuantity(productId, currentQuantity - 1);
    } else {
      updateQuantity(productId, 0);
    }
  };

  if (loading) {
    return (
        <GridContainer>
          <LoadingContainer>
            <StyledSpinner animation="border" role="status">
              <span className="visually-hidden">Загрузка...</span>
            </StyledSpinner>
            <p>Загрузка товаров...</p>
          </LoadingContainer>
        </GridContainer>
    );
  }

  if (error) {
    return (
        <GridContainer>
          <StyledAlert variant="danger">
            {error}
          </StyledAlert>
        </GridContainer>
    );
  }

  if (!products || products.length === 0) {
    return (
        <GridContainer>
          <EmptyState>
            <h3>Товары не найдены</h3>
            <p>Попробуйте изменить параметры поиска или сбросить фильтры</p>
          </EmptyState>
        </GridContainer>
    );
  }

  return (
      <GridContainer>
        <Row>
          {products.map(product => {
            const productInCart = isInCart(product.id);
            const quantity = getItemQuantity(product.id);
            const imageUrl = product.mainPhotoPath
                ? getImageUrl(product.mainPhotoPath)
                : '/images/no-image.jpg';

            return (
                <Col key={product.id} lg={4} md={6} sm={6} xs={12} className="mb-4">
                  <StyledCard>
                    <CardImageContainer>
                      <CardImage
                          variant="top"
                          src={imageUrl}
                          alt={product.name}
                          onError={(e) => {
                            e.target.src = '/images/no-image.jpg';
                          }}
                      />
                    </CardImageContainer>

                    <CardBody>
                      <ProductTitle>{product.name}</ProductTitle>

                      {product.description && (
                          <ProductDescription>
                            {product.description}
                          </ProductDescription>
                      )}

                      <ProductPrice>
                        {Number(product.price).toLocaleString()} ₽
                      </ProductPrice>

                      {productInCart ? (
                          <>
                            <QuantityControls>
                              <QuantityButton
                                  size="sm"
                                  onClick={() => handleDecreaseQuantity(product.id)}
                              >
                                −
                              </QuantityButton>
                              <QuantityDisplay>{quantity}</QuantityDisplay>
                              <QuantityButton
                                  size="sm"
                                  onClick={() => handleIncreaseQuantity(product.id)}
                              >
                                +
                              </QuantityButton>
                            </QuantityControls>
                            <StyledButton
                                as={Link}
                                to={`/products/${product.id}`}
                                variant="outline-primary"
                                size="sm"
                            >
                              Подробнее
                            </StyledButton>
                          </>
                      ) : (
                          <ButtonGroup>
                            <StyledButton
                                variant="primary"
                                size="sm"
                                onClick={() => handleAddToCart(product)}
                                style={{ flex: 1 }}
                            >
                              В корзину
                            </StyledButton>
                            <StyledButton
                                as={Link}
                                to={`/products/${product.id}`}
                                variant="outline-primary"
                                size="sm"
                                style={{ flex: 1 }}
                            >
                              Подробнее
                            </StyledButton>
                          </ButtonGroup>
                      )}
                    </CardBody>
                  </StyledCard>
                </Col>
            );
          })}
        </Row>
      </GridContainer>
  );
};

export default ProductGrid;