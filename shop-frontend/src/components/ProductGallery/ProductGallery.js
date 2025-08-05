import React, { useState } from 'react';
import { Modal } from 'react-bootstrap';
import styled from 'styled-components';

const GalleryContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: ${props => props.theme.spacing.md};

  @media (max-width: 767.98px) {
    gap: ${props => props.theme.spacing.sm};
  }
`;

const MainImageContainer = styled.div`
  position: relative;
  aspect-ratio: 1;
  border-radius: ${props => props.theme.borderRadius.image};
  overflow: hidden;
  box-shadow: ${props => props.theme.shadows.card};
  border: 1px solid #eee;
  cursor: pointer;

  &:hover {
    box-shadow: ${props => props.theme.shadows.medium};
    transform: translateY(-2px);
    transition: all 0.3s ease;
  }
`;

const MainImage = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;

  &:hover {
    transform: scale(1.05);
  }
`;

const ZoomIcon = styled.div`
  position: absolute;
  top: ${props => props.theme.spacing.md};
  right: ${props => props.theme.spacing.md};
  background-color: ${props => props.theme.colors.background}ee; /* 93% opacity */
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: ${props => props.theme.colors.text};
  font-size: ${props => props.theme.typography.fontSize.base};
  opacity: 0;
  transition: opacity 0.3s ease;

  ${MainImageContainer}:hover & {
    opacity: 1;
  }

  @media (max-width: 767.98px) {
    opacity: 1;
    width: 36px;
    height: 36px;
    font-size: ${props => props.theme.typography.fontSize.small};
  }
`;

const ThumbnailContainer = styled.div`
  display: flex;
  gap: ${props => props.theme.spacing.sm};
  overflow-x: auto;
  padding: ${props => props.theme.spacing.xs} 0;

  &::-webkit-scrollbar {
    height: 4px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: ${props => props.theme.borderRadius.small};
  }

  &::-webkit-scrollbar-thumb {
    background: ${props => props.theme.colors.primary};
    border-radius: ${props => props.theme.borderRadius.small};
  }

  @media (max-width: 767.98px) {
    gap: ${props => props.theme.spacing.xs};
  }
`;

const ThumbnailImage = styled.img`
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: ${props => props.theme.borderRadius.small};
  border: 2px solid ${props => props.active ? props.theme.colors.primary : '#eee'};
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.3s ease;

  &:hover {
    border-color: ${props => props.theme.colors.primary};
    transform: translateY(-2px);
    box-shadow: ${props => props.theme.shadows.small};
  }

  @media (max-width: 767.98px) {
    width: 60px;
    height: 60px;
  }

  @media (max-width: 575.98px) {
    width: 50px;
    height: 50px;
  }
`;

const StyledModal = styled(Modal)`
  .modal-dialog {
    max-width: 90vw;
    max-height: 90vh;
    margin: auto;
    display: flex;
    align-items: center;
  }

  .modal-content {
    background: transparent;
    border: none;
    box-shadow: none;
  }

  .modal-body {
    padding: 0;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .modal-header {
    border: none;
    position: absolute;
    top: ${props => props.theme.spacing.md};
    right: ${props => props.theme.spacing.md};
    z-index: 1051;
    padding: 0;

    .btn-close {
      background-color: ${props => props.theme.colors.background};
      border-radius: 50%;
      width: 40px;
      height: 40px;
      opacity: 0.8;

      &:hover {
        opacity: 1;
      }
    }
  }
`;

const ModalImage = styled.img`
  max-width: 100%;
  max-height: 85vh;
  object-fit: contain;
  border-radius: ${props => props.theme.borderRadius.image};
  box-shadow: ${props => props.theme.shadows.large};
`;

const ImageCounter = styled.div`
  position: absolute;
  bottom: ${props => props.theme.spacing.md};
  left: 50%;
  transform: translateX(-50%);
  background-color: ${props => props.theme.colors.background}ee; /* 93% opacity */
  color: ${props => props.theme.colors.text};
  padding: ${props => props.theme.spacing.xs} ${props => props.theme.spacing.sm};
  border-radius: ${props => props.theme.borderRadius.button};
  font-family: ${props => props.theme.typography.bodyFontFamily};
  font-size: ${props => props.theme.typography.fontSize.small};

  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.tiny};
  }
`;

const NavigationButton = styled.button`
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background-color: ${props => props.theme.colors.background}ee; /* 93% opacity */
  color: ${props => props.theme.colors.text};
  border: none;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: ${props => props.theme.typography.fontSize.large};
  cursor: pointer;
  opacity: 0.8;
  transition: all 0.3s ease;
  z-index: 1051;

  &:hover {
    opacity: 1;
    background-color: ${props => props.theme.colors.background};
    transform: translateY(-50%) scale(1.1);
  }

  &.prev {
    left: ${props => props.theme.spacing.lg};
  }

  &.next {
    right: ${props => props.theme.spacing.lg};
  }

  @media (max-width: 767.98px) {
    width: 44px;
    height: 44px;
    font-size: ${props => props.theme.typography.fontSize.base};

    &.prev {
      left: ${props => props.theme.spacing.md};
    }

    &.next {
      right: ${props => props.theme.spacing.md};
    }
  }
`;

const ProductGallery = ({ images = [] }) => {
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [showModal, setShowModal] = useState(false);

  const handleThumbnailClick = (index) => {
    setCurrentImageIndex(index);
  };

  const handleMainImageClick = () => {
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
  };

  const handlePrevImage = (e) => {
    e.stopPropagation();
    setCurrentImageIndex((prev) =>
        prev === 0 ? images.length - 1 : prev - 1
    );
  };

  const handleNextImage = (e) => {
    e.stopPropagation();
    setCurrentImageIndex((prev) =>
        prev === images.length - 1 ? 0 : prev + 1
    );
  };

  const handleKeyDown = (e) => {
    if (e.key === 'ArrowLeft') {
      handlePrevImage(e);
    } else if (e.key === 'ArrowRight') {
      handleNextImage(e);
    } else if (e.key === 'Escape') {
      handleCloseModal();
    }
  };

  React.useEffect(() => {
    if (showModal) {
      document.addEventListener('keydown', handleKeyDown);
      return () => document.removeEventListener('keydown', handleKeyDown);
    }
  }, [showModal, currentImageIndex]);

  if (!images || images.length === 0) {
    return (
        <GalleryContainer>
          <MainImageContainer onClick={handleMainImageClick}>
            <MainImage
                src="/images/no-image.jpg"
                alt="Изображение не найдено"
            />
          </MainImageContainer>
        </GalleryContainer>
    );
  }

  return (
      <>
        <GalleryContainer>
          <MainImageContainer onClick={handleMainImageClick}>
            <MainImage
                src={images[currentImageIndex]}
                alt={`Изображение ${currentImageIndex + 1}`}
                onError={(e) => {
                  e.target.src = '/images/no-image.jpg';
                }}
            />
            <ZoomIcon>
              <i className="bi bi-zoom-in"></i>
            </ZoomIcon>
          </MainImageContainer>

          {images.length > 1 && (
              <ThumbnailContainer>
                {images.map((image, index) => (
                    <ThumbnailImage
                        key={index}
                        src={image}
                        alt={`Миниатюра ${index + 1}`}
                        active={index === currentImageIndex}
                        onClick={() => handleThumbnailClick(index)}
                        onError={(e) => {
                          e.target.src = '/images/no-image.jpg';
                        }}
                    />
                ))}
              </ThumbnailContainer>
          )}
        </GalleryContainer>

        <StyledModal
            show={showModal}
            onHide={handleCloseModal}
            centered
            size="xl"
        >
          <Modal.Header closeButton />
          <Modal.Body onClick={handleCloseModal}>
            <ModalImage
                src={images[currentImageIndex]}
                alt={`Изображение ${currentImageIndex + 1}`}
                onClick={(e) => e.stopPropagation()}
                onError={(e) => {
                  e.target.src = '/images/no-image.jpg';
                }}
            />

            {images.length > 1 && (
                <>
                  <NavigationButton
                      className="prev"
                      onClick={handlePrevImage}
                      aria-label="Предыдущее изображение"
                  >
                    ‹
                  </NavigationButton>
                  <NavigationButton
                      className="next"
                      onClick={handleNextImage}
                      aria-label="Следующее изображение"
                  >
                    ›
                  </NavigationButton>
                  <ImageCounter>
                    {currentImageIndex + 1} из {images.length}
                  </ImageCounter>
                </>
            )}
          </Modal.Body>
        </StyledModal>
      </>
  );
};

export default ProductGallery;