import React from 'react';
import {Col, Container, Row} from 'react-bootstrap';
import styled from 'styled-components';
import {useTheme} from '../../contexts/ThemeContext';
import {getImageUrl} from '../../services/api';

const FooterWrapper = styled.footer`
    background-color: ${props => props.theme.colors.footer.background};
    color: ${props => props.theme.colors.footer.text};
    padding: 2rem 0;
    margin-top: auto;

    @media (max-width: 991.98px) {
        padding-top: 40px;
        padding-bottom: 20px;
    }

    @media (max-width: 767.98px) {
        padding-top: 30px;
        text-align: center;
    }

    @media (max-width: 575.98px) {
        padding-top: 20px;
    }
`;

const FooterTitle = styled.h5`
    font-family: ${props => props.theme.typography.headingFontFamily};
    font-weight: 600;
    margin-bottom: 1.2rem;
    color: ${props => props.theme.colors.footer.text};

    @media (max-width: 991.98px) {
        margin-bottom: 15px;
    }

    @media (max-width: 575.98px) {
        margin-bottom: 10px;
    }
`;

const FooterText = styled.p`
    margin-bottom: ${props => props.theme.spacing.sm};
    color: ${props => props.theme.colors.footer.text};
`;

const FooterColumn = styled(Col)`
    @media (max-width: 767.98px) {
        margin-bottom: 25px;
    }
`;

const FooterLogoContainer = styled.div`
    margin-bottom: ${props => props.theme.spacing.md};
    text-align: center;

    @media (max-width: 767.98px) {
        margin-bottom: 15px;
    }
`;

const FooterLogoImage = styled.img`
    max-height: 60px;
    width: auto;
    filter: brightness(0) invert(1); /* Делаем логотип белым для футера */

    @media (max-width: 575.98px) {
        max-height: 45px;
    }
`;

const SocialIcons = styled.div`
    margin-top: ${props => props.theme.spacing.md};

    a {
        font-size: 1.5rem;
        margin-right: 1rem;
        color: ${props => props.theme.colors.footer.text};
        transition: color 0.3s;

        &:hover {
            color: ${props => props.theme.colors.primary};
        }
    }

    @media (max-width: 767.98px) {
        margin-bottom: 20px;
        justify-content: center;
        display: flex;
    }

    @media (max-width: 575.98px) {
        a {
            font-size: 1.3rem;
        }
    }
`;

const FooterDivider = styled.hr`
  margin: ${props => props.theme.spacing.md} 0;
  border-color: rgba(255, 255, 255, 0.2);
`;

const Copyright = styled.div`
  text-align: center;
  color: ${props => props.theme.colors.footer.text};
`;

const Footer = () => {
    const {theme, rawTheme, tenantInfo} = useTheme();

    return (
        <FooterWrapper>
            <Container>
                <Row>
                    {/* Логотип в футере (если есть) */}
                    {theme.assets?.footerLogoUrl && (
                        <Col xs={12}>
                            <FooterLogoContainer>
                                <FooterLogoImage
                                    src={getImageUrl(theme.assets.footerLogoUrl)}
                                    alt={tenantInfo?.name || 'Footer Logo'}
                                />
                            </FooterLogoContainer>
                        </Col>
                    )}

                    <FooterColumn md={4}>
                        <FooterTitle>О магазине</FooterTitle>
                        <FooterText>{rawTheme?.description || 'Наш магазин предлагает широкий выбор качественных товаров по доступным ценам.'}</FooterText>
                    </FooterColumn>
                    <FooterColumn md={4}>
                        <FooterTitle>Контакты</FooterTitle>
                        <FooterText>
                            <i className="bi bi-geo-alt me-2"></i>
                            {rawTheme?.address || tenantInfo?.address || 'г. Москва, ул. Примерная, д. 1'}
                        </FooterText>
                        <FooterText>
                            <i className="bi bi-telephone me-2"></i>
                            {rawTheme?.phone || tenantInfo?.phone || '+7 (800) 123-45-67'}
                        </FooterText>
                        <FooterText>
                            <i className="bi bi-envelope me-2"></i>
                            {rawTheme?.email || tenantInfo?.email || 'info@example.com'}
                        </FooterText>
                    </FooterColumn>
                    <FooterColumn md={4}>
                        <FooterTitle>Мы в социальных сетях</FooterTitle>
                        <SocialIcons>
                            {rawTheme?.facebookUrl && (
                                <a href={rawTheme.facebookUrl} target="_blank" rel="noopener noreferrer">
                                    <i className="bi bi-facebook"></i>
                                </a>
                            )}
                            {rawTheme?.instagramUrl && (
                                <a href={rawTheme.instagramUrl} target="_blank" rel="noopener noreferrer">
                                    <i className="bi bi-instagram"></i>
                                </a>
                            )}
                            {rawTheme?.vkUrl && (
                                <a href={rawTheme.vkUrl} target="_blank" rel="noopener noreferrer">
                                    <i className="bi bi-vk"></i>
                                </a>
                            )}
                            {rawTheme?.telegramUrl && (
                                <a href={rawTheme.telegramUrl} target="_blank" rel="noopener noreferrer">
                                    <i className="bi bi-telegram"></i>
                                </a>
                            )}
                        </SocialIcons>
                    </FooterColumn>
                </Row>
                <FooterDivider />
                <Copyright>
                    <FooterText>
                        © {new Date().getFullYear()} {tenantInfo?.name || 'Интернет-магазин'}. Все права защищены.
                    </FooterText>
                </Copyright>
            </Container>
        </FooterWrapper>
    );
};

export default Footer;