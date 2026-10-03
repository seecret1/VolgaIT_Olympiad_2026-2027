package io.github.seecret1.volgait.tests;

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

@Epic("VolgaIT semifinal")
@Feature("Modals")
@DisplayName("Modals page")
class ModalsTests extends BaseUiTest {
    private ModalsPage modals;

    @BeforeEach
    void openModalsPage() {
        open(URL_MODALS);
        modals = new ModalsPage(driver);
    }

    @Nested
    @Story("Positive modal scenarios")
    @Tag("positive")
    class Positive {
        @Test
        @DisplayName("P01 — page has the Modals heading")
        void page_has_expected_heading() {
            assertEquals(MODALS_HEADING, modals.heading());
        }

        @Test
        @Severity(SeverityLevel.BLOCKER)
        @DisplayName("P02 — Simple Modal button opens a dialog")
        void simple_button_opens_dialog() {
            assertTrue(modals.openSimpleModal().isVisible());
        }

        @Test
        @DisplayName("P03 — simple dialog has the expected title")
        void simple_dialog_has_title() {
            assertEquals(SIMPLE_MODAL_TITLE, modals.openSimpleModal().title());
        }

        @Test
        @DisplayName("P04 — simple dialog has the expected text")
        void simple_dialog_has_text() {
            assertTrue(modals.openSimpleModal().content().contains(SIMPLE_MODAL_TEXT));
        }

        @Test
        @DisplayName("P05 — simple dialog has an accessible close button")
        void simple_dialog_has_close_button() {
            assertTrue(modals.openSimpleModal().closeButtonIsDisplayed());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("P06 — close button hides the simple dialog")
        void close_button_hides_simple_dialog() {
            PopupComponent simple = modals.openSimpleModal();
            simple.close();
            assertFalse(simple.isVisible());
        }

        @Test
        @DisplayName("P07 — Form Modal button opens the form dialog")
        void form_button_opens_form_dialog() {
            assertEquals(FORM_MODAL_TITLE, modals.openFormModal().title());
        }

        @Test
        @DisplayName("P08 — name field is marked as required")
        void name_is_required() {
            modals.openFormModal();
            assertTrue(modals.contactForm().requiredNameIsMarked());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("P09 — form accepts name, email and message")
        void form_accepts_contact_data() {
            modals.openFormModal();
            ContactData data = ContactData.builder()
                    .name(FORM_NAME)
                    .email(FORM_EMAIL)
                    .message(FORM_MESSAGE)
                    .build();
            ContactFormComponent form = modals.contactForm().fill(data);

            assertAll(
                    () -> assertEquals(data.name(), form.valueOf("name")),
                    () -> assertEquals(data.email(), form.valueOf("email")),
                    () -> assertEquals(data.message(), form.valueOf("message"))
            );
        }
    }

    @Nested
    @Story("Negative modal scenarios")
    @Tag("negative")
    class Negative {
        @Test
        @DisplayName("N01 — Escape does not close the simple modal by configuration")
        void escape_does_not_close_simple_modal() {
            PopupComponent simple = modals.openSimpleModal();
            simple.pressEscape();
            assertTrue(simple.isVisible());
        }

        @Test
        @DisplayName("N02 — closed simple modal does not remain visible")
        void closed_simple_modal_is_not_visible() {
            PopupComponent simple = modals.openSimpleModal();
            simple.close();
            assertFalse(simple.isVisible());
        }

        @Test
        @Severity(SeverityLevel.BLOCKER)
        @DisplayName("N03 — empty required name prevents form submission")
        void empty_name_shows_validation_error() {
            modals.openFormModal();
            ContactFormComponent form = modals.contactForm();
            form.submit();
            assertFalse(form.errorText("name").isBlank());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("N04 — malformed email is rejected by the form")
        void malformed_email_shows_validation_error() {
            modals.openFormModal();
            ContactFormComponent form = modals.contactForm().fill(ContactData.builder()
                    .name(INVALID_FORM_NAME)
                    .email(INVALID_FORM_EMAIL)
                    .message(INVALID_FORM_MESSAGE)
                    .build());
            form.submit();
            assertFalse(form.errorText("email").isBlank());
        }
    }
}
