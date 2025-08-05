import React, { useState, useEffect } from 'react';
import { Modal, Form, Button, Row, Col, Alert } from 'react-bootstrap';
import styled from 'styled-components';
import { orderApi, userApi, addressApi } from '../../services/api';
import { useCart } from "../../contexts/CartContext";

const StyledModal = styled(Modal)`
  .modal-header {
    border-bottom: 1px solid #eee;
    background-color: ${props => props.theme.colors.background};
    
    .modal-title {
      color: ${props => props.theme.colors.text};
      font-family: ${props => props.theme.typography.headingFontFamily};
      font-weight: ${props => props.theme.typography.fontWeight.bold};
    }
    
    .btn-close {
      color: ${props => props.theme.colors.text};
    }
  }
  
  .modal-body {
    background-color: ${props => props.theme.colors.background};
  }
  
  .modal-footer {
    border-top: 1px solid #eee;
    background-color: ${props => props.theme.colors.background};
  }
  
  .modal-content {
    background-color: ${props => props.theme.colors.background};
    border-radius: ${props => props.theme.borderRadius.card};
    box-shadow: ${props => props.theme.shadows.large};
  }
`;

const SectionTitle = styled.h5`
  margin: ${props => props.theme.spacing.lg} 0 ${props => props.theme.spacing.md};
  font-weight: ${props => props.theme.typography.fontWeight.bold};
  color: ${props => props.theme.colors.text};
  font-family: ${props => props.theme.typography.headingFontFamily};
  padding-bottom: ${props => props.theme.spacing.xs};
  border-bottom: 1px solid #eee;
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.base};
  }
`;

const StyledFormLabel = styled(Form.Label)`
  color: ${props => props.theme.colors.text};
  font-family: ${props => props.theme.typography.bodyFontFamily};
  font-weight: ${props => props.theme.typography.fontWeight.medium};
`;

const StyledFormControl = styled(Form.Control)`
  border-radius: ${props => props.theme.borderRadius.input};
  font-family: ${props => props.theme.typography.bodyFontFamily};
  color: ${props => props.theme.colors.text};

  &:focus {
    border-color: ${props => props.theme.colors.primary};
    box-shadow: 0 0 0 0.2rem rgba(${props => props.theme.colors.primaryRgb}, 0.25);
  }
`;

const PhoneHint = styled.div`
  font-size: ${props => props.theme.typography.fontSize.small};
  color: ${props => props.theme.colors.text}99; /* 60% opacity */
  font-family: ${props => props.theme.typography.bodyFontFamily};
  margin-top: ${props => props.theme.spacing.xs};
`;

const TotalAmount = styled.div`
  font-size: ${props => props.theme.typography.fontSize.large};
  font-weight: ${props => props.theme.typography.fontWeight.medium};
  color: ${props => props.theme.colors.text};
  font-family: ${props => props.theme.typography.bodyFontFamily};
  
  span {
    font-weight: ${props => props.theme.typography.fontWeight.bold};
    color: ${props => props.theme.colors.accent};
  }
  
  @media (max-width: 767.98px) {
    font-size: ${props => props.theme.typography.fontSize.base};
  }
`;

const StyledButton = styled(Button)`
  background-color: ${props => props.theme.colors.primary};
  border-color: ${props => props.theme.colors.primary};
  color: ${props => props.theme.colors.buttonText};
  font-family: ${props => props.theme.typography.bodyFontFamily};
  font-weight: ${props => props.theme.typography.fontWeight.medium};
  border-radius: ${props => props.theme.borderRadius.button};
  
  &:hover, &:focus, &:active {
    background-color: ${props => props.theme.colors.interactive.hover};
    border-color: ${props => props.theme.colors.interactive.hover};
    color: ${props => props.theme.colors.buttonText};
  }
  
  &:disabled {
    background-color: ${props => props.theme.colors.text}33; /* 20% opacity */
    border-color: ${props => props.theme.colors.text}33;
    opacity: 0.6;
  }
`;

const StyledAlert = styled(Alert)`
  font-family: ${props => props.theme.typography.bodyFontFamily};
  border-radius: ${props => props.theme.borderRadius.card};
`;

const CheckoutModal = ({ show, onHide, total, cartItems }) => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    patronymic: '',
    email: '',
    phone: '',
    country: '',
    city: '',
    street: '',
    houseNumber: '',
    apartment: '',
    postalCode: '',
    comment: ''
  });

  const [isValid, setIsValid] = useState(false);
  const [touched, setTouched] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);
  const [submitSuccess, setSubmitSuccess] = useState(null);
  const { clearCart } = useCart();

  const isValidPhone = (phone) => {
    const cleanPhone = phone.replace(/[\s-]/g, '');
    const phonePattern1 = /^\+7\d{10}$/;
    const phonePattern2 = /^8\d{10}$/;
    return phonePattern1.test(cleanPhone) || phonePattern2.test(cleanPhone);
  };

  useEffect(() => {
    const requiredFields = {
      firstName: true,
      lastName: true,
      email: true,
      phone: true,
      country: true,
      city: true,
      street: true,
      houseNumber: true
    };

    const isFormValid = Object.keys(requiredFields).every(field => {
      if (field === 'email') {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData[field]);
      }
      if (field === 'phone') {
        return isValidPhone(formData[field]);
      }
      return formData[field].trim() !== '';
    });

    setIsValid(isFormValid);
  }, [formData]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    setTouched(prev => ({
      ...prev,
      [name]: true
    }));
  };

  const handleSubmit = async () => {
    setIsSubmitting(true);
    setSubmitError(null);
    setSubmitSuccess(null);

    try {
      const userCreateRequest = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        patronymic: formData.patronymic || null,
        email: formData.email,
        phone: formData.phone.replace(/[^0-9+]/g, '')
      };
      const userResponse = await userApi.createUser(userCreateRequest);
      const customerId = userResponse.data.id;

      const addressCreateRequest = {
        country: formData.country,
        city: formData.city,
        street: formData.street,
        houseNumber: formData.houseNumber,
        apartment: formData.apartment || '',
        postalCode: formData.postalCode || '',
        comment: formData.comment || ''
      };
      const addressResponse = await addressApi.createAddress(addressCreateRequest);
      const addressId = addressResponse.data.id;

      const orderItems = cartItems.map(item => ({
        productId: item.id,
        quantity: item.quantity
      }));

      const orderCreateRequest = {
        customerId,
        addressId,
        orderItems
      };

      const orderResponse = await orderApi.createOrder(orderCreateRequest);

      clearCart();
      setSubmitSuccess(`Заказ №${orderResponse.data.id} успешно создан! Мы свяжемся с вами для подтверждения.`);

      setTimeout(() => {
        onHide();
      }, 3000);

    } catch (error) {
      console.error('Ошибка при создании заказа:', error);
      setSubmitError(
          error.response?.data?.message ||
          'Произошла ошибка при оформлении заказа. Пожалуйста, попробуйте позже.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
      <StyledModal
          show={show}
          onHide={onHide}
          size="lg"
          centered
      >
        <Modal.Header closeButton>
          <Modal.Title>Оформление заказа</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {submitError && (
              <StyledAlert variant="danger">{submitError}</StyledAlert>
          )}

          {submitSuccess && (
              <StyledAlert variant="success">{submitSuccess}</StyledAlert>
          )}

          {!submitSuccess && (
              <Form>
                <SectionTitle>Личные данные</SectionTitle>
                <Row className="mb-3">
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <StyledFormLabel>Имя *</StyledFormLabel>
                      <StyledFormControl
                          type="text"
                          name="firstName"
                          value={formData.firstName}
                          onChange={handleInputChange}
                          isInvalid={touched.firstName && !formData.firstName}
                      />
                      <Form.Control.Feedback type="invalid">
                        Введите имя
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <StyledFormLabel>Фамилия *</StyledFormLabel>
                      <StyledFormControl
                          type="text"
                          name="lastName"
                          value={formData.lastName}
                          onChange={handleInputChange}
                          isInvalid={touched.lastName && !formData.lastName}
                      />
                      <Form.Control.Feedback type="invalid">
                        Введите фамилию
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                </Row>

                <Form.Group className="mb-3">
                  <StyledFormLabel>Отчество</StyledFormLabel>
                  <StyledFormControl
                      type="text"
                      name="patronymic"
                      value={formData.patronymic}
                      onChange={handleInputChange}
                  />
                </Form.Group>

                <Row className="mb-3">
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <StyledFormLabel>Email *</StyledFormLabel>
                      <StyledFormControl
                          type="email"
                          name="email"
                          value={formData.email}
                          onChange={handleInputChange}
                          isInvalid={touched.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)}
                      />
                      <Form.Control.Feedback type="invalid">
                        Введите корректный email
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <StyledFormLabel>Телефон *</StyledFormLabel>
                      <StyledFormControl
                          type="tel"
                          name="phone"
                          value={formData.phone}
                          onChange={handleInputChange}
                          isInvalid={touched.phone && !isValidPhone(formData.phone)}
                          placeholder="+79XXXXXXXXXX или 89XXXXXXXXXX"
                      />
                      <PhoneHint>
                        Формат: +7 XXX XXX XX XX или 8 XXX XXX XX XX
                      </PhoneHint>
                      <Form.Control.Feedback type="invalid">
                        Введите номер телефона в правильном формате
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                </Row>

                <SectionTitle>Адрес доставки</SectionTitle>
                <Row className="mb-3">
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <StyledFormLabel>Страна *</StyledFormLabel>
                      <StyledFormControl
                          type="text"
                          name="country"
                          value={formData.country}
                          onChange={handleInputChange}
                          isInvalid={touched.country && !formData.country}
                      />
                      <Form.Control.Feedback type="invalid">
                        Введите страну
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <StyledFormLabel>Город *</StyledFormLabel>
                      <StyledFormControl
                          type="text"
                          name="city"
                          value={formData.city}
                          onChange={handleInputChange}
                          isInvalid={touched.city && !formData.city}
                      />
                      <Form.Control.Feedback type="invalid">
                        Введите город
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                </Row>

                <Row className="mb-3">
                  <Col md={8}>
                    <Form.Group className="mb-3">
                      <StyledFormLabel>Улица *</StyledFormLabel>
                      <StyledFormControl
                          type="text"
                          name="street"
                          value={formData.street}
                          onChange={handleInputChange}
                          isInvalid={touched.street && !formData.street}
                      />
                      <Form.Control.Feedback type="invalid">
                        Введите улицу
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                  <Col md={4}>
                    <Form.Group className="mb-3">
                      <StyledFormLabel>Дом *</StyledFormLabel>
                      <StyledFormControl
                          type="text"
                          name="houseNumber"
                          value={formData.houseNumber}
                          onChange={handleInputChange}
                          isInvalid={touched.houseNumber && !formData.houseNumber}
                      />
                      <Form.Control.Feedback type="invalid">
                        Введите номер дома
                      </Form.Control.Feedback>
                    </Form.Group>
                  </Col>
                </Row>

                <Row className="mb-3">
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <StyledFormLabel>Квартира</StyledFormLabel>
                      <StyledFormControl
                          type="text"
                          name="apartment"
                          value={formData.apartment}
                          onChange={handleInputChange}
                      />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-3">
                      <StyledFormLabel>Почтовый индекс</StyledFormLabel>
                      <StyledFormControl
                          type="text"
                          name="postalCode"
                          value={formData.postalCode}
                          onChange={handleInputChange}
                      />
                    </Form.Group>
                  </Col>
                </Row>

                <Form.Group className="mb-3">
                  <StyledFormLabel>Комментарий к заказу</StyledFormLabel>
                  <StyledFormControl
                      as="textarea"
                      rows={3}
                      name="comment"
                      value={formData.comment}
                      onChange={handleInputChange}
                  />
                </Form.Group>
              </Form>
          )}
        </Modal.Body>
        <Modal.Footer>
          {!submitSuccess && (
              <div className="d-flex justify-content-between align-items-center w-100">
                <TotalAmount>
                  Сумма заказа: <span>{total.toLocaleString()} ₽</span>
                </TotalAmount>
                <div>
                  <StyledButton
                      onClick={handleSubmit}
                      disabled={!isValid || isSubmitting}
                  >
                    {isSubmitting ? 'Отправка...' : 'Оформить заказ'}
                  </StyledButton>
                </div>
              </div>
          )}
        </Modal.Footer>
      </StyledModal>
  );
};

export default CheckoutModal;