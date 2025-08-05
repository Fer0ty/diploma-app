package diploma.ecommerce.backend.shopbase.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Настройки темы магазина")
public class ThemeResponse {
    @Schema(description = "ID темы", example = "1")
    private Long id;

    // Базовые цвета
    @Schema(description = "Основной цвет темы (HEX)", example = "#3498db")
    private String primaryColor;

    @Schema(description = "Вторичный цвет темы (HEX)", example = "#2ecc71")
    private String secondaryColor;

    @Schema(description = "Акцентный цвет темы (HEX)", example = "#e74c3c")
    private String accentColor;

    @Schema(description = "Цвет текста (HEX)", example = "#333333")
    private String textColor;

    @Schema(description = "Цвет фона (HEX)", example = "#ffffff")
    private String backgroundColor;

    // Шрифты
    @Schema(description = "Название основного шрифта", example = "Roboto, sans-serif")
    private String fontFamily;

    @Schema(description = "Шрифт для заголовков", example = "Montserrat, sans-serif")
    private String headingFontFamily;

    @Schema(description = "Шрифт для основного текста", example = "Open Sans, sans-serif")
    private String bodyFontFamily;

    // Изображения
    @Schema(description = "URL логотипа", example = "https://example.com/logo.png")
    private String logoUrl;

    @Schema(description = "URL изображения заголовка", example = "https://example.com/header.jpg")
    private String headerImageUrl;

    @Schema(description = "URL логотипа для футера", example = "https://example.com/footer-logo.png")
    private String footerLogoUrl;

    @Schema(description = "URL иконки сайта (favicon)", example = "https://example.com/favicon.ico")
    private String faviconUrl;

    // Скругления элементов
    @Schema(description = "Скругление кнопок", example = "4px")
    private String buttonRadius;

    @Schema(description = "Скругление карточек", example = "8px")
    private String cardRadius;

    @Schema(description = "Скругление полей ввода", example = "4px")
    private String inputRadius;

    // Цвета элементов
    @Schema(description = "Цвет текста на кнопках", example = "#ffffff")
    private String buttonTextColor;

    @Schema(description = "Цвет фона футера", example = "#2c3e50")
    private String footerBackgroundColor;

    @Schema(description = "Цвет текста в футере", example = "#ecf0f1")
    private String footerTextColor;

    // Цвета статусов
    @Schema(description = "Цвет для успешных действий", example = "#27ae60")
    private String successColor;

    @Schema(description = "Цвет для ошибок", example = "#c0392b")
    private String errorColor;

    @Schema(description = "Цвет для предупреждений", example = "#f39c12")
    private String warningColor;

    @Schema(description = "Цвет для информационных сообщений", example = "#3498db")
    private String infoColor;

    // Интерактивные цвета
    @Schema(description = "Цвет при наведении на элементы", example = "#34495e")
    private String hoverColor;

    @Schema(description = "Цвет при активации элементов", example = "#2980b9")
    private String activeColor;

    // Тени
    @Schema(description = "Тень для элементов", example = "0 2px 4px rgba(0,0,0,0.1)")
    private String boxShadow;

    @Schema(description = "Тень для карточек", example = "0 4px 8px rgba(0,0,0,0.1)")
    private String cardShadow;
}