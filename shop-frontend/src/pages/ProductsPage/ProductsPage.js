import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Alert, Spinner } from 'react-bootstrap';
import styled from 'styled-components';
import ProductCard from '../../components/ProductCard/ProductCard';
import FilterSidebar from '../../components/FilterSidebar/FilterSidebar';
import { productApi } from '../../services/api';

const PageContainer = styled(Container)`
    padding-top: ${props => props.theme.spacing.xl};
    padding-bottom: ${props => props.theme.spacing.xl};
`;

const PageTitle = styled.h1`
    font-family: ${props => props.theme.typography.headingFontFamily};
    color: ${props => props.theme.colors.text};
    margin-bottom: ${props => props.theme.spacing.lg};
    text-align: center;
`;

const ProductsGrid = styled(Row)`
    margin-top: ${props => props.theme.spacing.lg};
`;

const LoadingContainer = styled.div`
    text-align: center;
    padding: ${props => props.theme.spacing.xl} 0;
`;

const StyledAlert = styled(Alert)`
    margin-top: ${props => props.theme.spacing.lg};
`;

const ProductsPage = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [searchTerm, setSearchTerm] = useState('');
    const [filters, setFilters] = useState({
        category: '',
        priceRange: { min: '', max: '' },
        sortBy: ''
    });

    const fetchProducts = async (params = {}) => {
        try {
            setLoading(true);
            setError(null);

            // Очищаем пустые параметры
            const cleanParams = Object.entries(params).reduce((acc, [key, value]) => {
                if (value !== undefined && value !== '' && value !== null) {
                    acc[key] = value;
                }
                return acc;
            }, {});

            const response = await productApi.getProducts(cleanParams);

            // Поддержка разных форматов ответа (пагинированный или простой массив)
            if (response.data.content) {
                setProducts(response.data.content);
                setTotalPages(response.data.totalPages || 0);
            } else if (Array.isArray(response.data)) {
                setProducts(response.data);
                setTotalPages(1);
            } else {
                setProducts([]);
                setTotalPages(0);
            }
        } catch (err) {
            console.error('Error fetching products:', err);
            setError(err.response?.data?.message || 'Ошибка при загрузке товаров');
            setProducts([]);
            setTotalPages(0);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchProducts({
            page: currentPage,
            size: 12,
            nameLike: searchTerm || undefined,
            category: filters.category || undefined,
            sort: getSortParam(filters.sortBy)
        });
    }, [currentPage]);

    const getSortParam = (sortBy) => {
        switch (sortBy) {
            case 'name_asc':
                return 'name,asc';
            case 'name_desc':
                return 'name,desc';
            case 'price_asc':
                return 'price,asc';
            case 'price_desc':
                return 'price,desc';
            default:
                return undefined;
        }
    };

    const handleSearch = (term) => {
        setSearchTerm(term);
        setCurrentPage(0);
        fetchProducts({
            page: 0,
            size: 12,
            nameLike: term || undefined,
            category: filters.category || undefined,
            sort: getSortParam(filters.sortBy)
        });
    };

    const handleApplyFilters = () => {
        setCurrentPage(0);

        const params = {
            page: 0,
            size: 12,
            nameLike: searchTerm || undefined,
            category: filters.category || undefined,
            minPrice: filters.priceRange.min || undefined,
            maxPrice: filters.priceRange.max || undefined,
            sort: getSortParam(filters.sortBy)
        };

        fetchProducts(params);
    };

    const handleResetFilters = () => {
        setFilters({
            category: '',
            priceRange: { min: '', max: '' },
            sortBy: ''
        });
        setSearchTerm('');
        setCurrentPage(0);
        fetchProducts({ page: 0, size: 12 });
    };

    const handlePageChange = (page) => {
        setCurrentPage(page);
        window.scrollTo(0, 0);
    };

    return (
        <PageContainer>
            <PageTitle>Наши товары</PageTitle>
            <Row>
                <Col lg={3}>
                    <FilterSidebar
                        filters={filters}
                        onFiltersChange={setFilters}
                        onApplyFilters={handleApplyFilters}
                        onResetFilters={handleResetFilters}
                    />
                </Col>
                <Col lg={9}>
                    {loading ? (
                        <LoadingContainer>
                            <Spinner animation="border" role="status">
                                <span className="visually-hidden">Загрузка...</span>
                            </Spinner>
                        </LoadingContainer>
                    ) : error ? (
                        <StyledAlert variant="danger">
                            {error}
                        </StyledAlert>
                    ) : products.length === 0 ? (
                        <StyledAlert variant="info">
                            Товары не найдены. Попробуйте изменить параметры поиска.
                        </StyledAlert>
                    ) : (
                        <>
                            <ProductsGrid>
                                {products.map(product => (
                                    <Col key={product.id} xs={12} sm={6} md={4} className="mb-4">
                                        <ProductCard product={product} />
                                    </Col>
                                ))}
                            </ProductsGrid>
                        </>
                    )}
                </Col>
            </Row>
        </PageContainer>
    );
};

export default ProductsPage;