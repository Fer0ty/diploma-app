import React from 'react';
import { Container, Row, Col, Breadcrumb } from 'react-bootstrap';
import styled from 'styled-components';
import { useLocation, Link } from 'react-router-dom';

const HeaderContainer = styled.div`
  background: linear-gradient(135deg,
  ${props => props.theme.colors.primary}ee,
  ${props => props.theme.colors.accent}ee
  );
  padding: ${props => props.theme.spacing.xl} 0;

  @media (max-width: 767.98px) {
    padding: ${props => props.theme.spacing.lg} 0;
  }

  @media (max-width: 575.98px) {
    padding: ${props => props.theme.spacing.md} 0;
  }
`;

const StyledBreadcrumb = styled(Breadcrumb)`
  background-color: transparent;
  padding: 0;
  margin-bottom: ${props => props.theme.spacing.md};

  .breadcrumb-item {
    font-family: ${props => props.theme.typography.bodyFontFamily};

    a {
      color: ${props => props.theme.colors.buttonText};
      text-decoration: none;
      font-weight: ${props => props.theme.typography.fontWeight.medium};

      &:hover {
        color: ${props => props.theme.colors.background};
        text-decoration: underline;
      }
    }

    &.active {
      color: ${props => props.theme.colors.background};
    }

    & + .breadcrumb-item::before {
      color: ${props => props.theme.colors.buttonText};
    }

    @media (max-width: 767.98px) {
      font-size: ${props => props.theme.typography.fontSize.small};
    }
  }
`;

const PageTitle = styled.h1`
  font-family: ${props => props.theme.typography.headingFontFamily};
  font-size: ${props => props.theme.typography.fontSize.display};
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  color: ${props => props.theme.colors.buttonText};
  margin: 0;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
  
  @media (max-width: 991.98px) {
    font-size: ${props => props.theme.typography.fontSize.heading};
  }
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.large};
  }
`;

const PageSubtitle = styled.p`
  font-family: ${props => props.theme.typography.bodyFontFamily};
  font-size: ${props => props.theme.typography.fontSize.base};
  color: ${props => props.theme.colors.buttonText}dd; /* 87% opacity */
  margin: ${props => props.theme.spacing.sm} 0 0;

  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.small};
  }
`;

const ProductHeader = ({ title, subtitle, showBreadcrumb = true }) => {
  const location = useLocation();

  const getBreadcrumbItems = () => {
    const pathSegments = location.pathname.split('/').filter(segment => segment);
    const items = [
      { label: 'Главная', path: '/' }
    ];

    if (pathSegments.includes('products')) {
      items.push({ label: 'Товары', path: '/products' });
    }

    if (pathSegments.includes('cart')) {
      items.push({ label: 'Корзина', path: '/cart' });
    }

    // Для страницы товара
    if (pathSegments.length === 2 && pathSegments[0] === 'products') {
      items.push({ label: title || 'Товар', path: location.pathname, active: true });
    }

    return items;
  };

  const getPageTitle = () => {
    if (title) return title;

    if (location.pathname === '/products') return 'Наши товары';
    if (location.pathname === '/cart') return 'Корзина покупок';
    if (location.pathname.startsWith('/products/')) return 'Товар';

    return 'Страница';
  };

  const getPageSubtitle = () => {
    if (subtitle) return subtitle;

    if (location.pathname === '/products') return 'Выберите товары из нашего каталога';
    if (location.pathname === '/cart') return 'Просмотр и управление выбранными товарами';

    return null;
  };

  const breadcrumbItems = getBreadcrumbItems();

  return (
      <HeaderContainer>
        <Container>
          <Row>
            <Col>
              {showBreadcrumb && (
                  <StyledBreadcrumb>
                    {breadcrumbItems.map((item, index) => (
                        <Breadcrumb.Item
                            key={index}
                            active={item.active}
                            linkAs={item.active ? 'span' : Link}
                            linkProps={item.active ? {} : { to: item.path }}
                        >
                          {item.label}
                        </Breadcrumb.Item>
                    ))}
                  </StyledBreadcrumb>
              )}

              <PageTitle>{getPageTitle()}</PageTitle>
              {getPageSubtitle() && (
                  <PageSubtitle>{getPageSubtitle()}</PageSubtitle>
              )}
            </Col>
          </Row>
        </Container>
      </HeaderContainer>
  );
};

export default ProductHeader;