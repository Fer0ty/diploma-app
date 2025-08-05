import React from 'react';
import {Card} from 'react-bootstrap';
import {Link} from 'react-router-dom';
import styled from 'styled-components';
import {getImageUrl} from '../../services/api';
import {useCart} from '../../contexts/CartContext';

const StyledCard = styled(Card)`
    height: 100%;
    transition: transform 0.3s, box-shadow 0.3s;
    overflow: hidden;
    border-radius: ${props => props.theme.borderRadius?.card || '8px'};
    background-color: ${props => props.theme.colors.card?.background || props.theme.colors.background};
    box-shadow: ${props => props.theme.shadows?.card || '0 2px 8px rgba(0, 0, 0, 0.1)'};
    border: 1px solid ${props => props.theme.colors.card?.border || '#eee'};
    margin-bottom: ${props => props.theme.spacing?.lg || '20px'};

    &:hover {
        transform: translateY(-5px);
        box-shadow: ${props => props.theme.shadows?.cardHover || props.theme.shadows?.medium || '0 4px 16px rgba(0, 0, 0, 0.15)'};
    }
`;

const ProductLink = styled(Link)`
    text-decoration: none;
    color: ${props => props.theme.colors.card?.text || props.theme.colors.text};
    display: block;
`;

const CardImgContainer = styled.div`
    position: relative;
    width: 100%;
    padding-top: 100%;
    overflow: hidden;
`;

const CardImg = styled(Card.Img)`
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
`;

const NewProductBadge = styled.span`
    position: absolute;
    top: 10px;
    right: 10px;
    border-radius: ${props => props.theme.borderRadius?.small || '4px'};
    padding: 5px 10px;
    font-size: ${props => props.theme.typography?.fontSize?.small || '0.875rem'};
    font-family: ${props => props.theme.typography?.bodyFontFamily || 'inherit'};
    font-weight: ${props => props.theme.typography?.fontWeight?.medium || '500'};
    z-index: 2;
    box-shadow: ${props => props.theme.shadows?.small || '0 2px 5px rgba(0, 0, 0, 0.05)'};
    background-color: ${props => props.theme.colors.card?.badge || props.theme.colors.accent};
    color: ${props => props.theme.colors.card?.badgeText || props.theme.colors.buttonText};
`;

const ProductTitle = styled(Card.Title)`
    color: ${props => props.theme.colors.card?.title || props.theme.colors.text};
    font-family: ${props => props.theme.typography?.headingFontFamily || 'inherit'};
    font-weight: ${props => props.theme.typography?.fontWeight?.medium || '500'};
`;

const ProductPrice = styled.div`
    font-weight: ${props => props.theme.typography?.fontWeight?.bold || '700'};
    font-size: ${props => props.theme.typography?.fontSize?.large || '1.25rem'};
    color: ${props => props.theme.colors.card?.price || props.theme.colors.accent};
    margin-bottom: ${props => props.theme.spacing?.sm || '0.5rem'};

    @media (max-width: 767.98px) {
        font-size: ${props => props.theme.typography?.fontSize?.base || '1rem'};
    }
`;

const QuantityControl = styled.div`
    display: flex;
    align-items: center;
    justify-content: space-between;
    border: 1px solid ${props => props.theme.colors.card?.buttonBorder || props.theme.colors.primary};
    border-radius: ${props => props.theme.borderRadius?.button || '4px'};
    overflow: hidden;
`;

const QuantityButton = styled.button`
    background-color: ${props => props.theme.colors.card?.button || props.theme.colors.primary};
    color: ${props => props.theme.colors.card?.buttonText || props.theme.colors.buttonText};
    border: none;
    width: 30px;
    height: 30px;
    cursor: pointer;
    font-size: 1.1rem;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.2s;

    &:hover {
        background-color: ${props => props.theme.colors.card?.buttonHover || props.theme.colors.interactive?.hover || props.theme.colors.secondary};
    }

    &:active {
        background-color: ${props => props.theme.colors.card?.buttonActive || props.theme.colors.interactive?.active || props.theme.colors.primary};
    }
`;

const QuantityDisplay = styled.span`
    padding: 0 10px;
    font-weight: ${props => props.theme.typography?.fontWeight?.bold || '700'};
`;

const AddToCartButton = styled.button`
    width: 100%;
    padding: ${props => props.theme.spacing?.sm || '0.5rem'} ${props => props.theme.spacing?.md || '1rem'};
    background-color: ${props => props.theme.colors.card?.button || props.theme.colors.primary};
    color: ${props => props.theme.colors.card?.buttonText || props.theme.colors.buttonText};
    border: none;
    border-radius: ${props => props.theme.borderRadius?.button || '4px'};
    cursor: pointer;
    font-weight: ${props => props.theme.typography?.fontWeight?.medium || '500'};
    font-family: ${props => props.theme.typography?.bodyFontFamily || 'inherit'};
    transition: background-color 0.2s;

    &:hover {
        background-color: ${props => props.theme.colors.card?.buttonHover || props.theme.colors.interactive?.hover || props.theme.colors.secondary};
    }

    &:active {
        background-color: ${props => props.theme.colors.card?.buttonActive || props.theme.colors.interactive?.active || props.theme.colors.primary};
    }
`;

const ProductCard = ({product}) => {
    const {addToCart, isInCart, getItemQuantity, updateQuantity} = useCart();

    const productInCart = isInCart(product.id);
    const quantity = getItemQuantity(product.id);

    const handleAddToCart = (e) => {
        e.preventDefault();
        e.stopPropagation();
        addToCart(product);
    };

    const handleIncreaseQuantity = (e) => {
        e.preventDefault();
        e.stopPropagation();
        updateQuantity(product.id, quantity + 1);
    };

    const handleDecreaseQuantity = (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (quantity > 1) {
            updateQuantity(product.id, quantity - 1);
        } else {
            updateQuantity(product.id, 0);
        }
    };

    const getProductImageUrl = () => {
        if (product.photos && product.photos.length > 0) {
            const mainPhoto = product.photos.find(photo => photo.main);
            if (mainPhoto) {
                return getImageUrl(mainPhoto.filePath);
            } else {
                return getImageUrl(product.photos[0].filePath);
            }
        }
        return '/images/no-image.jpg';
    };

    const isNewProduct = () => {
        if (!product.createdAt) return false;
        const createdDate = new Date(product.createdAt);
        const now = new Date();
        const diffTime = Math.abs(now - createdDate);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDays <= 7;
    };

    return (
        <StyledCard>
            <ProductLink to={`/product/${product.id}`}>
                <CardImgContainer>
                    <CardImg variant="top" src={getProductImageUrl()} alt={product.name}/>
                    {isNewProduct() && (
                        <NewProductBadge>
                            Новинка
                        </NewProductBadge>
                    )}
                </CardImgContainer>
                <Card.Body>
                    <ProductTitle>{product.name}</ProductTitle>
                    <ProductPrice>{Number(product.price).toLocaleString()} ₽</ProductPrice>
                </Card.Body>
            </ProductLink>
            <Card.Footer className="bg-white border-0">
                {productInCart ? (
                    <QuantityControl>
                        <QuantityButton
                            onClick={handleDecreaseQuantity}
                            aria-label="Уменьшить количество"
                        >
                            −
                        </QuantityButton>
                        <QuantityDisplay>{quantity}</QuantityDisplay>
                        <QuantityButton
                            onClick={handleIncreaseQuantity}
                            aria-label="Увеличить количество"
                        >
                            +
                        </QuantityButton>
                    </QuantityControl>
                ) : (
                    <AddToCartButton onClick={handleAddToCart}>
                        Добавить в корзину
                    </AddToCartButton>
                )}
            </Card.Footer>
        </StyledCard>
    );
};

export default ProductCard;