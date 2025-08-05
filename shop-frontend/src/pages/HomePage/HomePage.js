import React from 'react';
import {Container} from 'react-bootstrap';
import styled from 'styled-components';
import Slider from '../../components/Slider/Slider';
import ProductGrid from '../../components/ProductGrid/ProductGrid';

const HomeContainer = styled.div`
  background-color: ${props => props.theme.colors.background};
  padding-top: ${props => props.theme.spacing.lg};
  padding-bottom: ${props => props.theme.spacing.xl};
  
  @media (max-width: 767.98px) {
    padding-top: ${props => props.theme.spacing.md};
    padding-bottom: ${props => props.theme.spacing.lg};
  }
  
  @media (max-width: 575.98px) {
    padding-top: ${props => props.theme.spacing.sm};
    padding-bottom: ${props => props.theme.spacing.md};
  }
`;

const HomePage = () => {
    return (
        <HomeContainer>
            <Container>
                <Slider maxItems={5} />
                <ProductGrid
                    title="Новые товары"
                    maxItems={16}
                    productType="latest"
                />
            </Container>
        </HomeContainer>
    );
};

export default HomePage;