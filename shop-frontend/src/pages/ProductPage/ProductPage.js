import React, { useEffect, useRef, useState } from 'react';
import { Alert, Button, Col, Container, Row, Spinner } from 'react-bootstrap';
import { useParams } from 'react-router-dom';
import styled from 'styled-components';
import ProductHeader from '../../components/ProductHeader/ProductHeader';
import ProductGallery from '../../components/ProductGallery/ProductGallery';
import { getImageUrl, productApi } from '../../services/api';
import { useCart } from "../../contexts/CartContext";

const PageContainer = styled.div`
    padding-bottom: ${props => props.theme.spacing.xxl};
    background-color: ${props => props.theme.colors.background};
`;

const ContentContainer = styled.div`
    margin-top: ${props => props.theme.spacing.lg};

    @media (max-width: 767.98px) {
        margin-top: ${props => props.theme.spacing.md};
    }
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

const ProductInfoContainer = styled.div`
    padding: 0 ${props => props.theme.spacing.md};

    @media (max-width: 767.98px) {
        padding: 0;
        margin-top: ${props => props.theme.spacing.lg};
    }
`;

const ProductTitle = styled.h1`
  font-size: ${props => props.theme.typography.fontSize.display};
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  font-family: ${props => props.theme.typography.headingFontFamily};
  color: ${props => props.theme.colors.text};
  margin-bottom: ${props => props.theme.spacing.sm};
  
  @media (max-width: 991.98px) {
    font-size: ${props => props.theme.typography.fontSize.heading};
  }
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.large};
  }
`;

const ProductPrice = styled.div`
  font-size: ${props => props.theme.typography.fontSize.display};
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  font-family: ${props => props.theme.typography.headingFontFamily};
  color: ${props => props.theme.colors.accent};
  margin-bottom: ${props => props.theme.spacing.lg};
  
  @media (max-width: 991.98px) {
    font-size: ${props => props.theme.typography.fontSize.heading};
  }
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.large};
    margin-bottom: ${props => props.theme.spacing.md};
  }
`;

const CartControl = styled.div`
    margin-bottom: ${props => props.theme.spacing.lg};

    @media (max-width: 767.98px) {
        margin-bottom: ${props => props.theme.spacing.md};
    }
`;

const QuantityControl = styled.div`
    display: flex;
    align-items: center;
    margin-bottom: ${props => props.theme.spacing.md};
`;

const CartStatus = styled.span`
    font-weight: ${props => props.theme.typography.fontWeight.medium};
    font-family: ${props => props.theme.typography.bodyFontFamily};
    color: ${props => props.theme.colors.text};
    margin-right: ${props => props.theme.spacing.md};
`;

const QuantityButtons = styled.div`
    display: flex;
    align-items: center;
    border: 1px solid ${props => props.theme.colors.primary};
    border-radius: ${props => props.theme.borderRadius.button};
    overflow: hidden;
`;

const QuantityButton = styled.button`
  background-color: ${props => props.theme.colors.primary};
  color: ${props => props.theme.colors.buttonText};
  border: none;
  width: 36px;
  height: 36px;
  font-size: ${props => props.theme.typography.fontSize.large};
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: ${props => props.theme.colors.interactive.hover};
  }
  
  &:active {
    background-color: ${props => props.theme.colors.interactive.active};
  }
`;

const QuantityDisplay = styled.span`
    padding: 0 ${props => props.theme.spacing.md};
    font-weight: ${props => props.theme.typography.fontWeight.bold};
    font-family: ${props => props.theme.typography.bodyFontFamily};
    color: ${props => props.theme.colors.text};
    line-height: 36px;
`;

const AddToCartButton = styled(Button)`
  width: 100%;
  font-weight: ${props => props.theme.typography.fontWeight.medium};
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
    padding: ${props => props.theme.spacing.sm} ${props => props.theme.spacing.md};
  }
`;

const ShortDescription = styled.div`
  margin-bottom: ${props => props.theme.spacing.lg};
  line-height: 1.6;
  color: ${props => props.theme.colors.text};
  font-family: ${props => props.theme.typography.bodyFontFamily};
  
  @media (max-width: 767.98px) {
    margin-bottom: ${props => props.theme.spacing.md};
    font-size: ${props => props.theme.typography.fontSize.small};
  }
`;

const ReadMoreButton = styled(Button)`
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: transparent;
  border-color: ${props => props.theme.colors.text};
  color: ${props => props.theme.colors.text};
  font-family: ${props => props.theme.typography.bodyFontFamily};
  border-radius: ${props => props.theme.borderRadius.button};
  
  &:hover, &:focus, &:active {
    background-color: ${props => props.theme.colors.text};
    border-color: ${props => props.theme.colors.text};
    color: ${props => props.theme.colors.background};
  }
  
  svg {
    margin-left: ${props => props.theme.spacing.sm};
  }
`;

const FullDescription = styled.div`
  margin-top: ${props => props.theme.spacing.xxl};
  padding-top: ${props => props.theme.spacing.xl};
  border-top: 1px solid #eee;
  
  h2 {
    font-size: ${props => props.theme.typography.fontSize.heading};
    font-family: ${props => props.theme.typography.headingFontFamily};
    color: ${props => props.theme.colors.text};
    margin-bottom: ${props => props.theme.spacing.md};
    
    @media (max-width: 767.98px) {
      font-size: ${props => props.theme.typography.fontSize.large};
    }
  }
  
  .description-content {
    line-height: 1.8;
    color: ${props => props.theme.colors.text};
    font-family: ${props => props.theme.typography.bodyFontFamily};
    
    @media (max-width: 767.98px) {
      font-size: ${props => props.theme.typography.fontSize.small};
    }
  }
`;

const ProductPage = () => {
    const { id } = useParams();
    const [product, setProduct] = useState(null);
    const [photos, setPhotos] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const descriptionRef = useRef(null);

    const { addToCart, updateQuantity, isInCart, getItemQuantity } = useCart();

    const [inCart, setInCart] = useState(false);
    const [quantity, setQuantity] = useState(0);

    useEffect(() => {
        const fetchProductData = async () => {
            try {
                setLoading(true);

                const productData = await productApi.getProductById(id);
                setProduct(productData);

                const photosData = await productApi.getProductPhotos(id);

                const sortedPhotos = [...photosData];
                const mainPhotoIndex = sortedPhotos.findIndex(photo => photo.main);
                if (mainPhotoIndex !== -1) {
                    const mainPhoto = sortedPhotos.splice(mainPhotoIndex, 1)[0];
                    sortedPhotos.unshift(mainPhoto);
                }

                const photosWithUrls = sortedPhotos.map(photo => getImageUrl(photo.filePath));

                setPhotos(photosWithUrls);
                setLoading(false);
            } catch (err) {
                setError('Ошибка при загрузке информации о товаре');
                setLoading(false);
                console.error(err);
            }
        };

        fetchProductData();
    }, [id]);

    useEffect(() => {
        if (product) {
            const productInCart = isInCart(parseInt(id));
            setInCart(productInCart);
            setQuantity(getItemQuantity(parseInt(id)));
        }
    }, [product, isInCart, getItemQuantity, id]);

    const handleAddToCart = () => {
        addToCart(product, 1);
        setInCart(true);
        setQuantity(1);
    };

    const truncateDescription = (text, maxLength = 100) => {
        if (!text) return '';
        if (text.length <= maxLength) return text;
        return text.substring(0, maxLength) + '...';
    };

    const handleIncreaseQuantity = () => {
        updateQuantity(parseInt(id), quantity + 1);
        setQuantity(prev => prev + 1);
    };

    const handleDecreaseQuantity = () => {
        if (quantity > 1) {
            updateQuantity(parseInt(id), quantity - 1);
            setQuantity(prev => prev - 1);
        } else {
            updateQuantity(parseInt(id), 0);
            setInCart(false);
            setQuantity(0);
        }
    };

    const scrollToDescription = () => {
        if (descriptionRef.current) {
            const yOffset = -80;
            const element = descriptionRef.current;
            const y = element.getBoundingClientRect().top + window.pageYOffset + yOffset;

            window.scrollTo({
                top: y,
                behavior: 'smooth'
            });
        }
    };

    if (loading) {
        return (
            <PageContainer>
                <ProductHeader />
                <Container>
                    <LoadingContainer>
                        <StyledSpinner animation="border" role="status">
                            <span className="visually-hidden">Загрузка...</span>
                        </StyledSpinner>
                        <p>Загрузка информации о товаре...</p>
                    </LoadingContainer>
                </Container>
            </PageContainer>
        );
    }

    if (error) {
        return (
            <PageContainer>
                <ProductHeader />
                <Container>
                    <StyledAlert variant="danger" className="my-4">
                        {error}
                    </StyledAlert>
                </Container>
            </PageContainer>
        );
    }

    if (!product) {
        return (
            <PageContainer>
                <ProductHeader />
                <Container>
                    <StyledAlert variant="warning" className="my-4">
                        Товар не найден
                    </StyledAlert>
                </Container>
            </PageContainer>
        );
    }

    const galleryImages = photos.length > 0 ? photos : ['/images/no-image.jpg'];

    return (
        <PageContainer>
            <ProductHeader />

            <Container>
                <ContentContainer>
                    <Row>
                        <Col lg={6} md={6} sm={12}>
                            <ProductGallery images={galleryImages} />
                        </Col>

                        <Col lg={6} md={6} sm={12}>
                            <ProductInfoContainer>
                                <ProductTitle>{product.name}</ProductTitle>
                                <ProductPrice>{Number(product.price).toLocaleString()} ₽</ProductPrice>

                                <CartControl>
                                    {inCart ? (
                                        <QuantityControl>
                                            <CartStatus>Добавлено в корзину:</CartStatus>
                                            <QuantityButtons>
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
                                            </QuantityButtons>
                                        </QuantityControl>
                                    ) : (
                                        <AddToCartButton
                                            size="lg"
                                            onClick={handleAddToCart}
                                        >
                                            Добавить в корзину
                                        </AddToCartButton>
                                    )}
                                </CartControl>

                                <ShortDescription>
                                    {truncateDescription(product.description, 200)}
                                </ShortDescription>

                                {product.description && product.description.length > 200 && (
                                    <ReadMoreButton
                                        variant="outline-secondary"
                                        onClick={scrollToDescription}
                                    >
                                        Подробное описание
                                        <i className="bi bi-arrow-down ms-2"></i>
                                    </ReadMoreButton>
                                )}
                            </ProductInfoContainer>
                        </Col>
                    </Row>

                    {product.description && (
                        <FullDescription ref={descriptionRef}>
                            <h2>Описание товара</h2>
                            <div className="description-content">
                                <p>{product.description}</p>
                            </div>
                        </FullDescription>
                    )}
                </ContentContainer>
            </Container>
        </PageContainer>
    );
};

export default ProductPage;