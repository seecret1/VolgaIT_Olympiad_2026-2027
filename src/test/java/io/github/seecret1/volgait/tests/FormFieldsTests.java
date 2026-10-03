package io.github.seecret1.volgait.tests;

import io.github.seecret1.volgait.constants.TestMetadata;
import io.github.seecret1.volgait.pages.FormFieldsPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic(TestMetadata.EPIC)
@Feature("Поля формы")
@DisplayName("Страница «Поля формы»")
class FormFieldsTests extends BaseUiTest {

    private FormFieldsPage form;

    @BeforeEach
    void openFormFieldsPage() {
        open(URL_FORM_FIELDS);
        form = new FormFieldsPage(driver);
    }

    @Test
    @Story("Отображение формы")
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("На странице отображается заголовок «Form Fields»")
    void page_has_expected_heading() {
        assertEquals(FORM_FIELDS_HEADING, form.heading());
    }

    @Test
    @Story("Automation Tools")
    @Tag(TestMetadata.POSITIVE)
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Selenium читает Automation Tools и записывает их через запятую в Message")
    void automation_tools_are_copied_to_message() {
        List<String> expectedTools = AUTOMATION_TOOLS;
        String message = form.fillMessageWithAutomationTools();

        assertAll(
                () -> assertEquals(expectedTools, form.automationTools()),
                () -> assertEquals(String.join(AUTOMATION_TOOLS_SEPARATOR, expectedTools), message),
                () -> assertEquals(message, form.messageValue())
        );
    }

    @ParameterizedTest(name = "{0}: поле Name сохраняет «{1}»")
    @MethodSource("spacedNumberValues")
    @Story("Текстовые поля")
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("Поле Name сохраняет числа и повторяющиеся пробелы")
    void name_preserves_numbers_and_repeated_spaces(String caseName, String value) {
        form.enterName(value);
        assertEquals(value, form.nameValue());
    }

    @ParameterizedTest(name = "{0}: поле Message сохраняет «{1}»")
    @MethodSource("spacedNumberValues")
    @Story("Текстовые поля")
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("Поле Message сохраняет числа и повторяющиеся пробелы")
    void message_preserves_numbers_and_repeated_spaces(String caseName, String value) {
        form.enterMessage(value);
        assertEquals(value, form.messageValue());
    }

    @Test
    @Story("Текстовые поля")
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("Поле Password маскирует и сохраняет введённое значение")
    void password_is_masked_and_preserves_value() {
        form.enterPassword(FORM_FIELDS_PASSWORD);
        assertAll(
                () -> assertEquals("password", form.passwordType()),
                () -> assertEquals(FORM_FIELDS_PASSWORD, form.passwordValue())
        );
    }

    @Test
    @Story("Флажки")
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("Можно одновременно выбрать несколько любимых напитков")
    void several_drinks_can_be_selected() {
        form.selectDrinks(FORM_FIELDS_DRINKS);
        assertEquals(FORM_FIELDS_DRINKS, form.selectedDrinks());
    }

    @ParameterizedTest(name = "Выбор цвета {0} снимает выбор с остальных цветов")
    @MethodSource("colors")
    @Story("Переключатели")
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("Radio-кнопки позволяют выбрать только один цвет")
    void color_selection_is_exclusive(String color) {
        form.selectColor(FORM_FIELDS_COLORS.get(0)).selectColor(color);
        assertEquals(color, form.selectedColor());
    }

    @Test
    @Story("Выпадающий список")
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("Список Automation содержит все варианты и позволяет выбрать Yes")
    void automation_select_has_expected_options() {
        form.selectAutomation(FORM_FIELDS_AUTOMATION_SELECTION);
        assertAll(
                () -> assertEquals(FORM_FIELDS_AUTOMATION_OPTIONS, form.automationOptions()),
                () -> assertEquals(FORM_FIELDS_AUTOMATION_SELECTION, form.selectedAutomation())
        );
    }

    @Test
    @Story("Валидация")
    @Tag(TestMetadata.NEGATIVE)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Пустое обязательное поле Name блокирует отправку формы")
    void empty_required_name_blocks_submission() {
        assertTrue(form.nameIsRequired());
        form.enterMessage(FORM_FIELDS_NUMBERS);
        form.submitWithoutWaitingForAlert();

        assertAll(
                () -> assertFalse(form.nameValidationMessage().isBlank()),
                () -> assertThrows(NoAlertPresentException.class, () -> driver.switchTo().alert())
        );
    }

    @Test
    @Story("Отправка формы")
    @Tag(TestMetadata.POSITIVE)
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Submit показывает сообщение и очищает заполненные поля")
    void successful_submit_shows_alert_and_resets_form() {
        form.enterName(FORM_FIELDS_NUMBERS)
                .enterPassword(FORM_FIELDS_PASSWORD)
                .enterEmail(FORM_FIELDS_EMAIL)
                .enterMessage(FORM_FIELDS_TRIPLE_SPACE)
                .selectDrinks(FORM_FIELDS_DRINKS)
                .selectColor(FORM_FIELDS_COLORS.get(0))
                .selectAutomation(FORM_FIELDS_AUTOMATION_SELECTION);

        Alert alert = form.submit();
        assertEquals(FORM_FIELDS_ALERT_TEXT, alert.getText());
        alert.accept();

        assertAll(
                () -> assertEquals("", form.nameValue()),
                () -> assertEquals("", form.passwordValue()),
                () -> assertEquals("", form.emailValue()),
                () -> assertEquals("", form.messageValue()),
                () -> assertTrue(form.selectedDrinks().isEmpty()),
                () -> assertEquals("", form.selectedColor()),
                () -> assertEquals("", form.selectedAutomation())
        );
    }

    static Stream<Arguments> spacedNumberValues() {
        return Stream.of(
                Arguments.of("только цифры", FORM_FIELDS_NUMBERS),
                Arguments.of("двойной пробел", FORM_FIELDS_DOUBLE_SPACE),
                Arguments.of("тройной пробел", FORM_FIELDS_TRIPLE_SPACE)
        );
    }

    static Stream<String> colors() {
        return FORM_FIELDS_COLORS.stream();
    }
}
