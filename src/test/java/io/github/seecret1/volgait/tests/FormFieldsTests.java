package io.github.seecret1.volgait.tests;

import io.github.seecret1.volgait.constants.TestMetadata;

import io.github.seecret1.volgait.pages.FormFieldsPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic(TestMetadata.EPIC)
@Feature("Обязательный сценарий Automation Tools")
@DisplayName("Страница «Поля формы»")
class FormFieldsTests extends BaseUiTest {

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Обязательный сценарий — Selenium читает Automation Tools и записывает их через запятую в Message")
    void automation_tools_are_copied_to_message() {
        open(URL_FORM_FIELDS);
        FormFieldsPage form = new FormFieldsPage(driver);

        List<String> expectedTools = AUTOMATION_TOOLS;
        String message = form.fillMessageWithAutomationTools();

        assertAll(
                () -> assertEquals(expectedTools, form.automationTools()),
                () -> assertEquals(String.join(AUTOMATION_TOOLS_SEPARATOR, expectedTools), message),
                () -> assertEquals(message, form.messageValue())
        );
    }
}
