package io.github.seecret1.volgait.components;

import io.github.seecret1.volgait.model.ContactData;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;

import java.util.List;

public final class ContactFormComponent {
    private final PopupComponent popup;
    private final WebElement name;
    private final WebElement email;
    private final WebElement message;
    private final WebElement submitButton;
    private final List<WebElement> nameErrors;
    private final List<WebElement> emailErrors;
    private final List<WebElement> messageErrors;

    public ContactFormComponent(PopupComponent popup, WebElement name, WebElement email,
                                WebElement message, WebElement submitButton,
                                List<WebElement> nameErrors, List<WebElement> emailErrors,
                                List<WebElement> messageErrors) {
        this.popup = popup;
        this.name = name;
        this.email = email;
        this.message = message;
        this.submitButton = submitButton;
        this.nameErrors = nameErrors;
        this.emailErrors = emailErrors;
        this.messageErrors = messageErrors;
    }

    @Step("Fill contact form")
    public ContactFormComponent fill(ContactData data) {
        popup.replaceValue(name, data.name());
        popup.replaceValue(email, data.email());
        popup.replaceValue(message, data.message());
        return this;
    }

    @Step("Submit contact form")
    public void submit() {
        submitButton.click();
    }

    public String valueOf(String field) {
        WebElement element = switch (field) {
            case "name" -> name;
            case "email" -> email;
            case "message" -> message;
            default -> throw new IllegalArgumentException("Unknown field: " + field);
        };
        return element.getAttribute("value");
    }

    public boolean requiredNameIsMarked() {
        return Boolean.parseBoolean(name.getAttribute("required"))
                && "true".equals(name.getAttribute("aria-required"));
    }

    public boolean validationErrorIsVisible(String field) {
        return errorsFor(field).stream()
                .anyMatch(element -> element.isDisplayed() && !element.getText().isBlank());
    }

    public String errorText(String field) {
        return popup.waitForNonBlankText(errorsFor(field));
    }

    private List<WebElement> errorsFor(String field) {
        return switch (field) {
            case "name" -> nameErrors;
            case "email" -> emailErrors;
            case "message" -> messageErrors;
            default -> throw new IllegalArgumentException("Unknown field: " + field);
        };
    }
}
