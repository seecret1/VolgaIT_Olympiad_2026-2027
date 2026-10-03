package io.github.seecret1.volgait.components;

import io.github.seecret1.volgait.model.ContactData;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public final class ContactFormComponent {
    private final PopupComponent popup;

    public ContactFormComponent(PopupComponent popup) {
        this.popup = popup;
    }

    @Step("Fill contact form")
    public ContactFormComponent fill(ContactData data) {
        popup.replaceValue(By.cssSelector("input.name"), data.name());
        popup.replaceValue(By.cssSelector("input.email"), data.email());
        popup.replaceValue(By.cssSelector("textarea.textarea"), data.message());
        return this;
    }

    @Step("Submit contact form")
    public void submit() {
        root().findElement(By.cssSelector("button[type='submit']")).click();
    }

    public String valueOf(String field) {
        By locator = switch (field) {
            case "name" -> By.cssSelector("input.name");
            case "email" -> By.cssSelector("input.email");
            case "message" -> By.cssSelector("textarea.textarea");
            default -> throw new IllegalArgumentException("Unknown field: " + field);
        };
        return root().findElement(locator).getAttribute("value");
    }

    public boolean requiredNameIsMarked() {
        WebElement input = root().findElement(By.cssSelector("input.name"));
        return Boolean.parseBoolean(input.getAttribute("required"))
                && "true".equals(input.getAttribute("aria-required"));
    }

    public boolean validationErrorIsVisible(String field) {
        return root().findElements(By.cssSelector("[id*='-%s-'][id$='-error']".formatted(field)))
                .stream()
                .anyMatch(element -> element.isDisplayed() && !element.getText().isBlank());
    }

    public String errorText(String field) {
        return popup.waitForNonBlankText(
                By.cssSelector("[id*='-%s-'][id$='-error']".formatted(field)));
    }

    private WebElement root() {
        return popup.container();
    }
}
