package diploma.ecommerce.backend.shopbase.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на обновление настроек темы магазина")
public class ThemeUpdateRequest {
    // Базовые цвета
    @Schema(description = "Основной цвет темы (HEX)", example = "#3498db")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Неверный формат цвета HEX")
    private String primaryColor;

    @Schema(description = "Вторичный цвет темы (HEX)", example = "#2ecc71")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Неверный формат цвета HEX")
    private String secondaryColor;

    @Schema(description = "Акцентный цвет темы (HEX)", example = "#e74c3c")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Неверный формат цвета HEX")
    private String accentColor;

    @Schema(description = "Цвет текста (HEX)", example = "#333333")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Неверный формат цвета HEX")
    private String textColor;

    @Schema(description = "Цвет фона (HEX)", example = "#ffffff")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Неверный формат цвета HEX")
    private String backgroundColor;

    // Шрифты
    @Schema(description = "Название основного шрифта", example = "Roboto, sans-serif")
    @Size(max = 100, message = "Название шрифта не может превышать 100 символов")
    private String fontFamily;

    @Schema(description = "Шрифт для заголовков", example = "Montserrat, sans-serif")
    @Size(max = 100, message = "Название шрифта не может превышать 100 символов")
    private String headingFontFamily;

    @Schema(description = "Шрифт для основного текста", example = "Open Sans, sans-serif")
    @Size(max = 100, message = "Название шрифта не может превышать 100 символов")
    private String bodyFontFamily;

    // Изображения
    @Schema(description = "URL логотипа", example = "https://example.com/logo.png")
    @Size(max = 500, message = "URL логотипа не может превышать 500 символов")
    private String logoUrl;

    @Schema(description = "URL изображения заголовка", example = "https://example.com/header.jpg")
    @Size(max = 500, message = "URL изображения заголовка не может превышать 500 символов")
    private String headerImageUrl;

    @Schema(description = "URL логотипа для футера", example = "https://example.com/footer-logo.png")
    @Size(max = 500, message = "URL логотипа не может превышать 500 символов")
    private String footerLogoUrl;

    @Schema(description = "URL иконки сайта (favicon)", example = "https://example.com/favicon.ico")
    @Size(max = 500, message = "URL иконки не может превышать 500 символов")
    private String faviconUrl;

    // Скругления элементов
    @Schema(description = "Скругление кнопок", example = "4px")
    @Size(max = 20, message = "Значение не может превышать 20 символов")
    private String buttonRadius;

    @Schema(description = "Скругление карточек", example = "8px")
    @Size(max = 20, message = "Значение не может превышать 20 символов")
    private String cardRadius;

    @Schema(description = "Скругление полей ввода", example = "4px")
    @Size(max = 20, message = "Значение не может превышать 20 символов")
    private String inputRadius;

    // Цвета элементов
    @Schema(description = "Цвет текста на кнопках", example = "#ffffff")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Неверный формат цвета HEX")
    private String buttonTextColor;

    @Schema(description = "Цвет фона футера", example = "#2c3e50")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Неверный формат цвета HEX")
    private String footerBackgroundColor;

    @Schema(description = "Цвет текста в футере", example = "#ecf0f1")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Неверный формат цвета HEX")
    private String footerTextColor;

    // Цвета статусов
    @Schema(description = "Цвет для успешных действий", example = "#27ae60")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Неверный формат цвета HEX")
    private String successColor;

    @Schema(description = "Цвет для ошибок", example = "#c0392b")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Неверный формат цвета HEX")
    private String errorColor;

    @Schema(description = "Цвет для предупреждений", example = "#f39c12")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Неверный формат цвета HEX")
    private String warningColor;

    @Schema(description = "Цвет для информационных сообщений", example = "#3498db")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Неверный формат цвета HEX")
    private String infoColor;

    // Интерактивные цвета
    @Schema(description = "Цвет при наведении на элементы", example = "#34495e")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Неверный формат цвета HEX")
    private String hoverColor;

    @Schema(description = "Цвет при активации элементов", example = "#2980b9")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Неверный формат цвета HEX")
    private String activeColor;

    // Тени
    @Schema(description = "Тень для элементов", example = "0 2px 4px rgba(0,0,0,0.1)")
    @Size(max = 100, message = "Значение не может превышать 100 символов")
    private String boxShadow;

    @Schema(description = "Тень для карточек", example = "0 4px 8px rgba(0,0,0,0.1)")
    @Size(max = 100, message = "Значение не может превышать 100 символов")
    private String cardShadow;
}