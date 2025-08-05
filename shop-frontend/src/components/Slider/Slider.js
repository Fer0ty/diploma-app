import React, { useState, useEffect } from 'react';
import { Carousel, Container, Row, Col, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import styled from 'styled-components';
import { getImageUrl, productApi } from '../../services/api';

const SliderContainer = styled.div`
    margin-bottom: ${props => props.theme.spacing.xxl};

    @media (max-width: 767.98px) {
        margin-bottom: ${props => props.theme.spacing.xl};
    }
`;

const StyledCarousel = styled(Carousel)`
  .carousel-inner {
    border-radius: ${props => props.theme.borderRadius.card};
    overflow: hidden;
    box-shadow: ${props => props.theme.shadows.large};
  }
  
  .carousel-control-prev,
  .carousel-control-next {
    width: 5%;
    opacity: 0.8;
    
    &:hover {
      opacity: 1;
    }
    
    @media (max-width: 767.98px) {
      width: 8%;
    }
  }
  
  .carousel-control-prev-icon,
  .carousel-control-next-icon {
    background-color: ${props => props.theme.colors.primary};
    border-radius: 50%;
    padding: ${props => props.theme.spacing.md};
    
    @media (max-width: 767.98px) {
      padding: ${props => props.theme.spacing.sm};
    }
  }
  
  .carousel-indicators {
    margin-bottom: ${props => props.theme.spacing.md};
    
    button {
      background-color: ${props => props.theme.colors.background};
      border: 2px solid ${props => props.theme.colors.primary};
      opacity: 0.6;
      
      &.active {
        background-color: ${props => props.theme.colors.primary};
        opacity: 1;
      }
    }
    
    @media (max-width: 767.98px) {
      margin-bottom: ${props => props.theme.spacing.sm};
    }
  }
`;

const SlideContainer = styled.div`
    position: relative;
    height: 500px;
    background: linear-gradient(135deg,
    ${props => props.theme.colors.primary}22,
    ${props => props.theme.colors.accent}22
    );

    @media (max-width: 991.98px) {
        height: 400px;
    }

    @media (max-width: 767.98px) {
        height: 300px;
    }

    @media (max-width: 575.98px) {
        height: 250px;
    }
`;

const SlideBackground = styled.div`
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url(${props => props.image});
    background-size: cover;
    background-position: center;
    opacity: 0.3;
`;

const SlideContent = styled.div`
    position: relative;
    height: 100%;
    display: flex;
    align-items: center;
    z-index: 2;
`;

const SlideText = styled.div`
    color: ${props => props.theme.colors.text};

    @media (max-width: 767.98px) {
        text-align: center;
    }
`;

const SlideTitle = styled.h2`
  font-family: ${props => props.theme.typography.headingFontFamily};
  font-size: ${props => props.theme.typography.fontSize.display};
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  margin-bottom: ${props => props.theme.spacing.md};
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
  
  @media (max-width: 991.98px) {
    font-size: ${props => props.theme.typography.fontSize.heading};
  }
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.large};
    margin-bottom: ${props => props.theme.spacing.sm};
  }
`;

const SlideDescription = styled.p`
    font-family: ${props => props.theme.typography.bodyFontFamily};
    font-size: ${props => props.theme.typography.fontSize.large};
    margin-bottom: ${props => props.theme.spacing.lg};
    line-height: 1.6;

    @media (max-width: 767.98px) {
        font-size: ${props => props.theme.typography.fontSize.base};
        margin-bottom: ${props => props.theme.spacing.md};
    }
`;

const SlidePrice = styled.div`
  font-family: ${props => props.theme.typography.headingFontFamily};
  font-size: ${props => props.theme.typography.fontSize.heading};
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  color: ${props => props.theme.colors.accent};
  margin-bottom: ${props => props.theme.spacing.lg};
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.large};
    margin-bottom: ${props => props.theme.spacing.md};
  }
`;

const SlideButton = styled(Button)`
  background-color: ${props => props.theme.colors.primary};
  border-color: ${props => props.theme.colors.primary};
  color: ${props => props.theme.colors.buttonText};
  font-family: ${props => props.theme.typography.bodyFontFamily};
  font-weight: ${props => props.theme.typography.fontWeight.medium};
  padding: ${props => props.theme.spacing.sm} ${props => props.theme.spacing.lg};
  border-radius: ${props => props.theme.borderRadius.button};
  
  &:hover, &:focus, &:active {
    background-color: ${props => props.theme.colors.interactive.hover};
    border-color: ${props => props.theme.colors.interactive.hover};
    color: ${props => props.theme.colors.buttonText};
    transform: translateY(-2px);
    box-shadow: ${props => props.theme.shadows.medium};
    transition: all 0.3s ease;
  }
  
  @media (max-width: 767.98px) {
    padding: ${props => props.theme.spacing.xs} ${props => props.theme.spacing.md};
    font-size: ${props => props.theme.typography.fontSize.small};
  }
`;

const ProductImage = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  
  img {
    max-width: 100%;
    max-height: 80%;
    object-fit: contain;
    border-radius: ${props => props.theme.borderRadius.image};
    box-shadow: ${props => props.theme.shadows.card};
    
    @media (max-width: 767.98px) {
      max-height: 60%;
    }
  }
`;

const LoadingContainer = styled.div`
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, 
    ${props => props.theme.colors.primary}22, 
    ${props => props.theme.colors.accent}22
  );
  border-radius: ${props => props.theme.borderRadius.card};
  
  p {
    color: ${props => props.theme.colors.text};
    font-family: ${props => props.theme.typography.bodyFontFamily};
    margin-left: ${props => props.theme.spacing.md};
  }
  
  @media (max-width: 767.98px) {
    height: 300px;
  }
`;

const EmptySlider = styled.div`
  height: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, 
    ${props => props.theme.colors.primary}22, 
    ${props => props.theme.colors.accent}22
  );
  border-radius: ${props => props.theme.borderRadius.card};
  color: ${props => props.theme.colors.text};
  font-family: ${props => props.theme.typography.bodyFontFamily};
  font-size: ${props => props.theme.typography.fontSize.large};
  
  @media (max-width: 767.98px) {
    height: 300px;
    font-size: ${props => props.theme.typography.fontSize.base};
  }
`;

const Slider = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchFeaturedProducts = async () => {
            try {
                setLoading(true);
                // Получаем первые 3 товара для слайдера
                const productsData = await productApi.getProducts({ limit: 3 });

                // Для каждого товара получаем главное фото
                const productsWithPhotos = await Promise.all(
                    productsData.content.map(async (product) => {
                        try {
                            const photos = await productApi.getProductPhotos(product.id);
                            const mainPhoto = photos.find(photo => photo.main) || photos[0];
                            return {
                                ...product,
                                mainPhotoPath: mainPhoto ? getImageUrl(mainPhoto.filePath) : null
                            };
                        } catch (err) {
                            console.error(`Ошибка при загрузке фото для товара ${product.id}:`, err);
                            return { ...product, mainPhotoPath: null };
                        }
                    })
                );

                setProducts(productsWithPhotos);
                setError(null);
            } catch (err) {
                console.error('Ошибка при загрузке товаров для слайдера:', err);
                setError('Ошибка при загрузке товаров');
            } finally {
                setLoading(false);
            }
        };

        fetchFeaturedProducts();
    }, []);

    if (loading) {
        return (
            <SliderContainer>
                <LoadingContainer>
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">Загрузка...</span>
                    </div>
                    <p>Загрузка товаров...</p>
                </LoadingContainer>
            </SliderContainer>
        );
    }

    if (error || !products || products.length === 0) {
        return (
            <SliderContainer>
                <EmptySlider>
                    {error || 'Нет товаров для отображения'}
                </EmptySlider>
            </SliderContainer>
        );
    }

    return (
        <SliderContainer>
            <StyledCarousel indicators controls interval={5000}>
                {products.map((product, index) => (
                    <Carousel.Item key={product.id}>
                        <SlideContainer>
                            {product.mainPhotoPath && (
                                <SlideBackground image={product.mainPhotoPath} />
                            )}

                            <Container>
                                <SlideContent>
                                    <Row className="align-items-center w-100">
                                        <Col lg={6} md={6} sm={12}>
                                            <SlideText>
                                                <SlideTitle>{product.name}</SlideTitle>
                                                <SlideDescription>
                                                    {product.description
                                                        ? product.description.substring(0, 150) + '...'
                                                        : 'Качественный товар по отличной цене!'
                                                    }
                                                </SlideDescription>
                                                <SlidePrice>
                                                    {Number(product.price).toLocaleString()} ₽
                                                </SlidePrice>
                                                <SlideButton
                                                    as={Link}
                                                    to={`/products/${product.id}`}
                                                    size="lg"
                                                >
                                                    Подробнее
                                                </SlideButton>
                                            </SlideText>
                                        </Col>

                                        <Col lg={6} md={6} sm={12}>
                                            <ProductImage>
                                                <img
                                                    src={product.mainPhotoPath || '/images/no-image.jpg'}
                                                    alt={product.name}
                                                    onError={(e) => {
                                                        e.target.src = '/images/no-image.jpg';
                                                    }}
                                                />
                                            </ProductImage>
                                        </Col>
                                    </Row>
                                </SlideContent>
                            </Container>
                        </SlideContainer>
                    </Carousel.Item>
                ))}
            </StyledCarousel>
        </SliderContainer>
    );
};

export default Slider;