import { createGlobalStyle } from 'styled-components';

const GlobalStyles = createGlobalStyle`
  body {
    font-family: ${props => props.theme.typography.bodyFontFamily};
    color: ${props => props.theme.colors.text};
    background-color: ${props => props.theme.colors.background};
    margin: 0;
    padding: 0;
    font-size: ${props => props.theme.typography.fontSize.base};
    overflow-x: hidden;
    width: 100%;
  }
  
  h1, h2, h3, h4, h5, h6 {
    font-family: ${props => props.theme.typography.headingFontFamily};
    color: ${props => props.theme.colors.text};
  }
  
  a {
    color: ${props => props.theme.colors.primary};
    text-decoration: none;
    
    &:hover {
      color: ${props => props.theme.colors.interactive.hover};
    }
  }
  
  /* Кнопки */
  .btn {
    border-radius: ${props => props.theme.borderRadius.button};
    font-weight: ${props => props.theme.typography.fontWeight.medium};
    font-family: ${props => props.theme.typography.bodyFontFamily};
    transition: all 0.2s ease;
  }
  
  .btn-primary {
    background-color: ${props => props.theme.colors.primary};
    border-color: ${props => props.theme.colors.primary};
    color: ${props => props.theme.colors.buttonText};
    
    &:hover, &:focus, &:active {
      background-color: ${props => props.theme.colors.interactive.hover};
      border-color: ${props => props.theme.colors.interactive.hover};
      color: ${props => props.theme.colors.buttonText};
    }
    
    &:active, &.active {
      background-color: ${props => props.theme.colors.interactive.active};
      border-color: ${props => props.theme.colors.interactive.active};
    }
  }
  
  .btn-secondary {
    background-color: ${props => props.theme.colors.secondary};
    border-color: ${props => props.theme.colors.secondary};
    color: ${props => props.theme.colors.buttonText};
    
    &:hover, &:focus, &:active {
      background-color: ${props => props.theme.colors.status.success};
      border-color: ${props => props.theme.colors.status.success};
      color: ${props => props.theme.colors.buttonText};
    }
  }
  
  .btn-outline-primary {
    color: ${props => props.theme.colors.primary};
    border-color: ${props => props.theme.colors.primary};
    
    &:hover, &:focus, &:active {
      background-color: ${props => props.theme.colors.primary};
      border-color: ${props => props.theme.colors.primary};
      color: ${props => props.theme.colors.buttonText};
    }
  }
  
  .btn-accent {
    background-color: ${props => props.theme.colors.accent};
    border-color: ${props => props.theme.colors.accent};
    color: ${props => props.theme.colors.buttonText};
    
    &:hover, &:focus, &:active {
      background-color: ${props => props.theme.colors.status.error};
      border-color: ${props => props.theme.colors.status.error};
      color: ${props => props.theme.colors.buttonText};
    }
  }
  
  /* Формы */
  .form-control, .form-select {
    border-radius: ${props => props.theme.borderRadius.input};
    font-family: ${props => props.theme.typography.bodyFontFamily};
    color: ${props => props.theme.colors.text};
    
    &:focus {
      border-color: ${props => props.theme.colors.primary};
      box-shadow: 0 0 0 0.2rem rgba(${props => props.theme.colors.primaryRgb}, 0.25);
    }
  }
  
  .form-label {
    color: ${props => props.theme.colors.text};
    font-family: ${props => props.theme.typography.bodyFontFamily};
    font-weight: ${props => props.theme.typography.fontWeight.medium};
  }
  
  /* Карточки */
  .card {
    border-radius: ${props => props.theme.borderRadius.card};
    box-shadow: ${props => props.theme.shadows.card};
    background-color: ${props => props.theme.colors.background};
    border-color: #eee;
  }
  
  /* Пагинация */
  .page-item.active .page-link {
    background-color: ${props => props.theme.colors.primary};
    border-color: ${props => props.theme.colors.primary};
  }
  
  .page-link {
    color: ${props => props.theme.colors.primary};
    
    &:hover {
      color: ${props => props.theme.colors.interactive.hover};
      background-color: rgba(${props => props.theme.colors.primaryRgb}, 0.1);
    }
  }
  
  /* Алерты */
  .alert-primary {
    color: ${props => props.theme.colors.primary};
    background-color: rgba(${props => props.theme.colors.primaryRgb}, 0.1);
    border-color: rgba(${props => props.theme.colors.primaryRgb}, 0.2);
  }
  
  .alert-danger {
    color: ${props => props.theme.colors.status.error};
    background-color: rgba(${props => props.theme.colors.accentRgb}, 0.1);
    border-color: rgba(${props => props.theme.colors.accentRgb}, 0.2);
  }
  
  .alert-success {
    color: ${props => props.theme.colors.status.success};
    background-color: rgba(${props => props.theme.colors.secondaryRgb}, 0.1);
    border-color: rgba(${props => props.theme.colors.secondaryRgb}, 0.2);
  }
  
  .alert-warning {
    color: ${props => props.theme.colors.status.warning};
    background-color: rgba(243, 156, 18, 0.1);
    border-color: rgba(243, 156, 18, 0.2);
  }
  
  .alert-info {
    color: ${props => props.theme.colors.status.info};
    background-color: rgba(52, 152, 219, 0.1);
    border-color: rgba(52, 152, 219, 0.2);
  }
  
  /* Spinner */
  .spinner-border {
    color: ${props => props.theme.colors.primary} !important;
  }
  
  /* Адаптивная типографика */
  @media (max-width: 767.98px) {
    body {
      font-size: ${props => props.theme.typography.fontSize.small};
    }
  }
  
  @media (max-width: 575.98px) {
    body {
      font-size: 14px;
    }
    
    .container {
      padding-left: ${props => props.theme.spacing.sm};
      padding-right: ${props => props.theme.spacing.sm};
    }
  }
`;

export default GlobalStyles;