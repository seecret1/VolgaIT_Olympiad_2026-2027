package io.github.seecret1.volgait.tests;

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

@Epic("VolgaIT semifinal")
@Feature("Mandatory Automation Tools scenario")
@DisplayName("Form Fields page")
class FormFieldsTests extends BaseUiTest {
    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Selenium reads Automation Tools and writes them to Message separated by commas")
    void automation_tools_are_copied_to_message() {
        open("/form-fields/");
        FormFieldsPage form = new FormFieldsPage(driver);

        List<String> expectedTools = List.of(
                "Selenium", "Playwright", "Cypress", "Appium", "Katalon Studio");
        String message = form.fillMessageWithAutomationTools();

        assertAll(
                () -> assertEquals(expectedTools, form.automationTools()),
                () -> assertEquals(String.join(", ", expectedTools), message),
                () -> assertEquals(message, form.messageValue())
        );

    }
}
