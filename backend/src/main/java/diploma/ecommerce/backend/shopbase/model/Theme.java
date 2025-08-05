package diploma.ecommerce.backend.shopbase.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "themes")
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theme_id")
    private Long id;

    // Базовые цвета
    @Column(name = "primary_color")
    private String primaryColor;

    @Column(name = "secondary_color")
    private String secondaryColor;

    @Column(name = "accent_color")
    private String accentColor;

    @Column(name = "text_color")
    private String textColor;

    @Column(name = "background_color")
    private String backgroundColor;

    // Шрифты
    @Column(name = "font_family")
    private String fontFamily;

    @Column(name = "heading_font_family")
    private String headingFontFamily;

    @Column(name = "body_font_family")
    private String bodyFontFamily;

    // Изображения
    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "header_image_url")
    private String headerImageUrl;

    @Column(name = "footer_logo_url")
    private String footerLogoUrl;

    @Column(name = "favicon_url")
    private String faviconUrl;

    // Скругления элементов
    @Column(name = "button_radius")
    private String buttonRadius;

    @Column(name = "card_radius")
    private String cardRadius;

    @Column(name = "input_radius")
    private String inputRadius;

    // Цвета элементов
    @Column(name = "button_text_color")
    private String buttonTextColor;

    @Column(name = "footer_background_color")
    private String footerBackgroundColor;

    @Column(name = "footer_text_color")
    private String footerTextColor;

    // Цвета статусов
    @Column(name = "success_color")
    private String successColor;

    @Column(name = "error_color")
    private String errorColor;

    @Column(name = "warning_color")
    private String warningColor;

    @Column(name = "info_color")
    private String infoColor;

    // Интерактивные цвета
    @Column(name = "hover_color")
    private String hoverColor;

    @Column(name = "active_color")
    private String activeColor;

    // Тени
    @Column(name = "box_shadow")
    private String boxShadow;

    @Column(name = "card_shadow")
    private String cardShadow;

    // Временные метки
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Обратная связь с Tenant
    @OneToOne(mappedBy = "theme")
    private Tenant tenant;
}