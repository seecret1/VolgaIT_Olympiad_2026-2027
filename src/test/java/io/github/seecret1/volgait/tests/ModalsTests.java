package io.github.seecret1.volgait.tests;

import io.github.seecret1.volgait.constants.TestMetadata;
import io.github.seecret1.volgait.model.ContactField;

import io.github.seecret1.volgait.components.ContactFormComponent;
import io.github.seecret1.volgait.components.PopupComponent;
import io.github.seecret1.volgait.model.ContactData;
import io.github.seecret1.volgait.pages.ModalsPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Epic(TestMetadata.EPIC)
@Feature("Модальные окна")
@DisplayName("Страница «Модальные окна»")
class ModalsTests extends BaseUiTest {

    private ModalsPage modals;

    @BeforeEach
    void openModalsPage() {
        open(URL_MODALS);
        modals = new ModalsPage(driver);
    }

    @Nested
    @Story("Позитивные сценарии модальных окон")
    @Tag(TestMetadata.POSITIVE)
    class Positive {

        @Test
        @DisplayName("P01 — на странице отображается заголовок «Modals»")
        void page_has_expected_heading() {
            assertEquals(MODALS_HEADING, modals.heading());
        }

        @Test
        @Severity(SeverityLevel.BLOCKER)
        @DisplayName("P02 — кнопка «Simple Modal» открывает диалоговое окно")
        void simple_button_opens_dialog() {
            assertTrue(modals.openSimpleModal().isVisible());
        }

        @Test
        @DisplayName("P03 — простое диалоговое окно содержит ожидаемый заголовок")
        void simple_dialog_has_title() {
            assertEquals(SIMPLE_MODAL_TITLE, modals.openSimpleModal().title());
        }

        @Test
        @DisplayName("P04 — простое диалоговое окно содержит ожидаемый текст")
        void simple_dialog_has_text() {
            assertTrue(modals.openSimpleModal().content().contains(SIMPLE_MODAL_TEXT));
        }

        @Test
        @DisplayName("P05 — в простом диалоговом окне отображается кнопка закрытия")
        void simple_dialog_has_close_button() {
            assertTrue(modals.openSimpleModal().closeButtonIsDisplayed());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("P06 — кнопка закрытия скрывает простое диалоговое окно")
        void close_button_hides_simple_dialog() {
            PopupComponent simple = modals.openSimpleModal();
            simple.close();
            assertFalse(simple.isVisible());
        }

        @Test
        @DisplayName("P07 — кнопка «Form Modal» открывает окно с формой")
        void form_button_opens_form_dialog() {
            assertEquals(FORM_MODAL_TITLE, modals.openFormModal().title());
        }

        @Test
        @DisplayName("P08 — поле имени отмечено как обязательное")
        void name_is_required() {
            modals.openFormModal();
            assertTrue(modals.contactForm().requiredNameIsMarked());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("P09 — форма принимает имя, электронную почту и сообщение")
        void form_accepts_contact_data() {
            modals.openFormModal();
            ContactData data = ContactData.builder()
                    .name(FORM_NAME)
                    .email(FORM_EMAIL)
                    .message(FORM_MESSAGE)
                    .build();
            ContactFormComponent form = modals.contactForm().fill(data);

            assertAll(
                    () -> assertEquals(data.name(), form.valueOf(ContactField.NAME)),
                    () -> assertEquals(data.email(), form.valueOf(ContactField.EMAIL)),
                    () -> assertEquals(data.message(), form.valueOf(ContactField.MESSAGE))
            );
        }
    }

    @Nested
    @Story("Негативные сценарии модальных окон")
    @Tag(TestMetadata.NEGATIVE)
    class Negative {

        @Test
        @DisplayName("N01 — клавиша Escape не закрывает простое модальное окно")
        void escape_does_not_close_simple_modal() {
            PopupComponent simple = modals.openSimpleModal();
            simple.pressEscape();
            assertTrue(simple.isVisible());
        }

        @Test
        @DisplayName("N02 — закрытое простое модальное окно больше не отображается")
        void closed_simple_modal_is_not_visible() {
            PopupComponent simple = modals.openSimpleModal();
            simple.close();
            assertFalse(simple.isVisible());
        }

        @Test
        @Severity(SeverityLevel.BLOCKER)
        @DisplayName("N03 — пустое обязательное имя блокирует отправку формы")
        void empty_name_shows_validation_error() {
            modals.openFormModal();
            ContactFormComponent form = modals.contactForm();
            form.submit();
            assertFalse(form.errorText(ContactField.NAME).isBlank());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("N04 — форма отклоняет некорректный адрес электронной почты")
        void malformed_email_shows_validation_error() {
            modals.openFormModal();
            ContactFormComponent form = modals.contactForm().fill(ContactData.builder()
                    .name(INVALID_FORM_NAME)
                    .email(INVALID_FORM_EMAIL)
                    .message(INVALID_FORM_MESSAGE)
                    .build());
            form.submit();
            assertFalse(form.errorText(ContactField.EMAIL).isBlank());
        }
    }
}
