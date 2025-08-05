import React from 'react';
import { Spinner } from 'react-bootstrap';
import styled, { keyframes } from 'styled-components';

const fadeIn = keyframes`
    from { opacity: 0; }
    to { opacity: 1; }
`;

const pulse = keyframes`
    0% { transform: scale(1); }
    50% { transform: scale(1.1); }
    100% { transform: scale(1); }
`;

const LoadingContainer = styled.div`
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg,
    ${props => props.theme.colors.primary}22,
    ${props => props.theme.colors.accent}22
    );
    backdrop-filter: blur(10px);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    z-index: 9999;
    animation: ${fadeIn} 0.3s ease-in-out;
`;

const LoadingContent = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: ${props => props.theme.colors.background};
  padding: ${props => props.theme.spacing.xxl};
  border-radius: ${props => props.theme.borderRadius.card};
  box-shadow: ${props => props.theme.shadows.large};
  border: 1px solid #eee;
  animation: ${pulse} 2s ease-in-out infinite;
  
  @media (max-width: 767.98px) {
    padding: ${props => props.theme.spacing.xl};
    margin: 0 ${props => props.theme.spacing.md};
  }
  
  @media (max-width: 575.98px) {
    padding: ${props => props.theme.spacing.lg};
  }
`;

const LogoContainer = styled.div`
  margin-bottom: ${props => props.theme.spacing.lg};
  
  @media (max-width: 767.98px) {
    margin-bottom: ${props => props.theme.spacing.md};
  }
`;

const Logo = styled.h1`
  font-family: ${props => props.theme.typography.headingFontFamily};
  font-size: ${props => props.theme.typography.fontSize.display};
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  color: ${props => props.theme.colors.primary};
  margin: 0;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.heading};
  }
  
  @media (max-width: 575.98px) {
    font-size: ${props => props.theme.typography.fontSize.large};
  }
`;

const SpinnerContainer = styled.div`
  margin-bottom: ${props => props.theme.spacing.lg};
  
  @media (max-width: 767.98px) {
    margin-bottom: ${props => props.theme.spacing.md};
  }
`;

const StyledSpinner = styled(Spinner)`
  color: ${props => props.theme.colors.primary} !important;
  width: 3rem;
  height: 3rem;
  
  @media (max-width: 767.98px) {
    width: 2.5rem;
    height: 2.5rem;
  }
  
  @media (max-width: 575.98px) {
    width: 2rem;
    height: 2rem;
  }
`;

const LoadingText = styled.p`
  font-family: ${props => props.theme.typography.bodyFontFamily};
  font-size: ${props => props.theme.typography.fontSize.base};
  color: ${props => props.theme.colors.text};
  margin: 0;
  text-align: center;
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.small};
  }
`;

const LoadingSubtext = styled.p`
  font-family: ${props => props.theme.typography.bodyFontFamily};
  font-size: ${props => props.theme.typography.fontSize.small};
  color: ${props => props.theme.colors.text}99; /* 60% opacity */
  margin: ${props => props.theme.spacing.sm} 0 0;
  text-align: center;
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.tiny};
  }
`;

const ProgressBar = styled.div`
  width: 200px;
  height: 4px;
  background-color: ${props => props.theme.colors.text}22; /* 13% opacity */
  border-radius: ${props => props.theme.borderRadius.small};
  margin-top: ${props => props.theme.spacing.lg};
  overflow: hidden;
  
  @media (max-width: 575.98px) {
    width: 150px;
    margin-top: ${props => props.theme.spacing.md};
  }
`;

const ProgressFill = styled.div`
  height: 100%;
  background: linear-gradient(90deg, 
    ${props => props.theme.colors.primary}, 
    ${props => props.theme.colors.accent}
  );
  border-radius: ${props => props.theme.borderRadius.small};
  animation: ${keyframes`
    0% { width: 0%; }
    50% { width: 70%; }
    100% { width: 100%; }
  `} 2s ease-in-out infinite;
`;

const LoadingScreen = ({
                           text = "Загрузка...",
                           subtext = "Пожалуйста, подождите",
                           showProgress = true,
                           showLogo = true
                       }) => {
    return (
        <LoadingContainer>
            <LoadingContent>
                {showLogo && (
                    <LogoContainer>
                        <Logo>ShopApp</Logo>
                    </LogoContainer>
                )}

                <SpinnerContainer>
                    <StyledSpinner animation="border" role="status">
                        <span className="visually-hidden">Загрузка...</span>
                    </StyledSpinner>
                </SpinnerContainer>

                <LoadingText>{text}</LoadingText>
                {subtext && <LoadingSubtext>{subtext}</LoadingSubtext>}

                {showProgress && (
                    <ProgressBar>
                        <ProgressFill />
                    </ProgressBar>
                )}
            </LoadingContent>
        </LoadingContainer>
    );
};

export default LoadingScreen;