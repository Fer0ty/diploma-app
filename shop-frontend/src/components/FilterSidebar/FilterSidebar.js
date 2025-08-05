import React, { useState } from 'react';
import { Form, Button } from 'react-bootstrap';
import styled from 'styled-components';

const FilterContainer = styled.div`
    background-color: ${props => props.theme.colors.background};
    border-radius: ${props => props.theme.borderRadius.card};
    padding: ${props => props.theme.spacing.lg};
    box-shadow: ${props => props.theme.shadows.card};
    border: 1px solid #eee;
    margin-bottom: ${props => props.theme.spacing.lg};
    position: sticky;
    top: 20px;

    @media (max-width: 991.98px) {
        position: static;
        margin-bottom: ${props => props.theme.spacing.md};
    }

    @media (max-width: 767.98px) {
        padding: ${props => props.theme.spacing.md};
    }

    @media (max-width: 575.98px) {
        padding: ${props => props.theme.spacing.sm};
    }
`;

const FilterTitle = styled.h3`
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

const FilterSection = styled.div`
    margin-bottom: ${props => props.theme.spacing.lg};

    &:last-child {
        margin-bottom: 0;
    }

    @media (max-width: 767.98px) {
        margin-bottom: ${props => props.theme.spacing.md};
    }
`;

const FilterSectionTitle = styled.h5`
    font-family: ${props => props.theme.typography.headingFontFamily};
    font-weight: ${props => props.theme.typography.fontWeight.bold};
    margin-bottom: ${props => props.theme.spacing.md};
    color: ${props => props.theme.colors.text};
    font-size: ${props => props.theme.typography.fontSize.base};

    @media (max-width: 767.98px) {
        margin-bottom: ${props => props.theme.spacing.sm};
        font-size: ${props => props.theme.typography.fontSize.small};
    }
`;

const StyledFormControl = styled(Form.Control)`
    border-radius: ${props => props.theme.borderRadius.input};
    font-family: ${props => props.theme.typography.bodyFontFamily};
    color: ${props => props.theme.colors.text};
    font-size: ${props => props.theme.typography.fontSize.small};

    &:focus {
        border-color: ${props => props.theme.colors.primary};
        box-shadow: 0 0 0 0.2rem rgba(${props => props.theme.colors.primaryRgb}, 0.25);
    }

    @media (max-width: 767.98px) {
        font-size: ${props => props.theme.typography.fontSize.tiny};
    }
`;

const StyledFormSelect = styled(Form.Select)`
    border-radius: ${props => props.theme.borderRadius.input};
    font-family: ${props => props.theme.typography.bodyFontFamily};
    color: ${props => props.theme.colors.text};
    font-size: ${props => props.theme.typography.fontSize.small};

    &:focus {
        border-color: ${props => props.theme.colors.primary};
        box-shadow: 0 0 0 0.2rem rgba(${props => props.theme.colors.primaryRgb}, 0.25);
    }

    @media (max-width: 767.98px) {
        font-size: ${props => props.theme.typography.fontSize.tiny};
    }
`;

const ButtonGroup = styled.div`
  display: flex;
  gap: ${props => props.theme.spacing.sm};
  margin-top: ${props => props.theme.spacing.lg};
  
  @media (max-width: 767.98px) {
    margin-top: ${props => props.theme.spacing.md};
    gap: ${props => props.theme.spacing.xs};
  }
  
  @media (max-width: 575.98px) {
    flex-direction: column;
  }
`;

const StyledButton = styled(Button)`
  flex: 1;
  font-family: ${props => props.theme.typography.bodyFontFamily};
  font-weight: ${props => props.theme.typography.fontWeight.medium};
  border-radius: ${props => props.theme.borderRadius.button};
  font-size: ${props => props.theme.typography.fontSize.small};
  
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
  
  &.btn-outline-secondary {
    color: ${props => props.theme.colors.text};
    border-color: ${props => props.theme.colors.text}66; /* 40% opacity */
    background-color: transparent;
    
    &:hover, &:focus, &:active {
      background-color: ${props => props.theme.colors.text};
      border-color: ${props => props.theme.colors.text};
      color: ${props => props.theme.colors.background};
    }
  }
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.tiny};
    padding: ${props => props.theme.spacing.xs} ${props => props.theme.spacing.sm};
  }
`;

const PriceInputGroup = styled.div`
  display: flex;
  align-items: center;
  gap: ${props => props.theme.spacing.sm};
  
  span {
    color: ${props => props.theme.colors.text};
    font-family: ${props => props.theme.typography.bodyFontFamily};
    font-size: ${props => props.theme.typography.fontSize.small};
    
    @media (max-width: 767.98px) {
      font-size: ${props => props.theme.typography.fontSize.tiny};
    }
  }
`;

const CategoryInput = styled(StyledFormControl)`
    width: 100%;
    margin-bottom: ${props => props.theme.spacing.sm};
`;

const FilterSidebar = ({ filters, onFiltersChange, onApplyFilters, onResetFilters }) => {
    const [categoryInput, setCategoryInput] = useState(filters.category || '');

    const handleCategoryChange = (e) => {
        setCategoryInput(e.target.value);
        onFiltersChange({
            ...filters,
            category: e.target.value
        });
    };

    const handleSortChange = (e) => {
        onFiltersChange({
            ...filters,
            sortBy: e.target.value
        });
    };

    const handlePriceChange = (field, value) => {
        onFiltersChange({
            ...filters,
            priceRange: {
                ...filters.priceRange,
                [field]: value
            }
        });
    };

    const handleReset = () => {
        setCategoryInput('');
        onResetFilters();
    };

    return (
        <FilterContainer>
            <FilterTitle>Фильтры</FilterTitle>

            <FilterSection>
                <FilterSectionTitle>Категория</FilterSectionTitle>
                <CategoryInput
                    type="text"
                    placeholder="Введите категорию для поиска"
                    value={categoryInput}
                    onChange={handleCategoryChange}
                    size="sm"
                />
            </FilterSection>

            <FilterSection>
                <FilterSectionTitle>Цена</FilterSectionTitle>
                <PriceInputGroup>
                    <StyledFormControl
                        type="number"
                        placeholder="От"
                        value={filters.priceRange.min}
                        onChange={(e) => handlePriceChange('min', e.target.value)}
                        min="0"
                        size="sm"
                    />
                    <span>—</span>
                    <StyledFormControl
                        type="number"
                        placeholder="До"
                        value={filters.priceRange.max}
                        onChange={(e) => handlePriceChange('max', e.target.value)}
                        min="0"
                        size="sm"
                    />
                </PriceInputGroup>
            </FilterSection>

            <FilterSection>
                <FilterSectionTitle>Сортировка</FilterSectionTitle>
                <StyledFormSelect
                    value={filters.sortBy}
                    onChange={handleSortChange}
                    size="sm"
                >
                    <option value="">По умолчанию</option>
                    <option value="name_asc">По названию (А-Я)</option>
                    <option value="name_desc">По названию (Я-А)</option>
                    <option value="price_asc">По цене (по возрастанию)</option>
                    <option value="price_desc">По цене (по убыванию)</option>
                </StyledFormSelect>
            </FilterSection>

            <ButtonGroup>
                <StyledButton
                    variant="primary"
                    size="sm"
                    onClick={onApplyFilters}
                >
                    Применить
                </StyledButton>
                <StyledButton
                    variant="outline-secondary"
                    size="sm"
                    onClick={handleReset}
                >
                    Сбросить
                </StyledButton>
            </ButtonGroup>
        </FilterContainer>
    );
};

export default FilterSidebar;