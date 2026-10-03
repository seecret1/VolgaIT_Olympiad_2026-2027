package io.github.seecret1.volgait.components;

import io.github.seecret1.volgait.constants.DomAttributes;
import io.github.seecret1.volgait.model.ContactData;
import io.github.seecret1.volgait.model.ContactField;
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
        popup.click(submitButton);
    }

    public String valueOf(ContactField field) {
        WebElement element = switch (field) {
            case NAME -> name;
            case EMAIL -> email;
            case MESSAGE -> message;
        };
        return element.getAttribute(DomAttributes.VALUE);
    }

    public boolean requiredNameIsMarked() {
        return Boolean.parseBoolean(name.getAttribute(DomAttributes.REQUIRED))
                && "true".equals(name.getAttribute(DomAttributes.ARIA_REQUIRED));
    }

    public String errorText(ContactField field) {
        return popup.waitForNonBlankText(errorsFor(field));
    }

    private List<WebElement> errorsFor(ContactField field) {
        return switch (field) {
            case NAME -> nameErrors;
            case EMAIL -> emailErrors;
            case MESSAGE -> messageErrors;
        };
    }
}
