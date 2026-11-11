package io.github.seecret1.volgait.pages;

import io.github.seecret1.volgait.components.AlertComponent;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public final class RegisterPage extends BasePage {

    @FindBy(id = "firstname")
    private WebElement firstName;

    @FindBy(id = "lastname")
    private WebElement lastName;

    @FindBy(id = "userName")
    private WebElement userName;

    @FindBy(id = "password")
    private WebElement password;

    @FindBy(id = "register")
    private WebElement registerButton;

    @FindBy(id = "gotologin")
    private WebElement backToLoginButton;

    @FindBy(id = "name")
    private WebElement validationMessage;

    private final AlertComponent alerts;

    public RegisterPage(WebDriver driver) {
        super(driver);
        alerts = new AlertComponent(driver);
    }

    public RegisterPage waitUntilLoaded() {
        visible(firstName);
        clickable(registerButton);
        return this;
    }

    @Step("Заполнить форму регистрации пользователя {newUserName}")
    public RegisterPage fill(String newFirstName, String newLastName,
                             String newUserName, String newPassword) {
        TimeoutException lastError = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                replaceAndWaitForReact(firstName, newFirstName);
                replaceAndWaitForReact(lastName, newLastName);
                replaceAndWaitForReact(userName, newUserName);
                replaceAndWaitForReact(password, newPassword);
                waitForStableForm(newFirstName, newLastName, newUserName, newPassword);
                return this;
            } catch (TimeoutException exception) {
                lastError = exception;
            }
        }
        throw lastError;
    }

    @Step("Отправить форму регистрации")
    public RegisterPage submit() {
        clickable(registerButton).click();
        return this;
    }

    public String submitAndAcceptAlert() {
        submit();
        return alerts.waitForTextAndAccept(Duration.ofSeconds(30));
    }

    public Optional<String> acceptAlertIfPresent(Duration timeout) {
        return alerts.acceptIfPresent(timeout);
    }

    public boolean isAlertPresentNow() {
        return alerts.isPresentNow();
    }

    public String validationText() {
        return visible(validationMessage).getText().trim();
    }

    public String firstNameValue() {
        return firstName.getAttribute("value");
    }

    public String lastNameValue() {
        return lastName.getAttribute("value");
    }

    public String userNameValue() {
        return userName.getAttribute("value");
    }

    public boolean isFieldInvalid(String fieldId) {
        return new WebDriverWait(driver, Duration.ofSeconds(5)).until(current ->
                current.findElement(By.id(fieldId)).getAttribute("class").contains("is-invalid"));
    }

    public boolean hasRecaptchaWidget() {
        return !driver.findElements(By.cssSelector(
                "iframe[src*='recaptcha'], iframe[title*='reCAPTCHA']")).isEmpty();
    }

    public boolean hasValidationTextNow(String expectedText) {
        try {
            return driver.getPageSource().contains(expectedText);
        } catch (UnhandledAlertException exception) {
            return false;
        }
    }

    public LoginPage backToLogin() {
        clickable(backToLoginButton).click();
        return new LoginPage(driver).waitUntilLoaded();
    }

    private void replaceAndWaitForReact(WebElement element, String value) {
        WebElement input = visible(element);
        ((JavascriptExecutor) driver).executeScript("""
                const element = arguments[0];
                const value = arguments[1];
                const setter = Object.getOwnPropertyDescriptor(
                        HTMLInputElement.prototype, 'value').set;
                setter.call(element, value);
                element.dispatchEvent(new Event('input', { bubbles: true }));
                element.dispatchEvent(new Event('change', { bubbles: true }));
                """, input, value);
        AtomicInteger stableReads = new AtomicInteger();
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .pollingEvery(Duration.ofMillis(100))
                .until(current -> {
                    if (value.equals(input.getAttribute("value"))) {
                        return stableReads.incrementAndGet() >= 3;
                    }
                    stableReads.set(0);
                    return false;
                });
    }

    private void waitForStableForm(String expectedFirstName, String expectedLastName,
                                   String expectedUserName, String expectedPassword) {
        AtomicInteger stableReads = new AtomicInteger();
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .pollingEvery(Duration.ofMillis(100))
                .until(current -> {
                    boolean allValuesMatch = expectedFirstName.equals(firstName.getAttribute("value"))
                            && expectedLastName.equals(lastName.getAttribute("value"))
                            && expectedUserName.equals(userName.getAttribute("value"))
                            && expectedPassword.equals(password.getAttribute("value"));
                    if (allValuesMatch) {
                        return stableReads.incrementAndGet() >= 5;
                    }
                    stableReads.set(0);
                    return false;
                });
    }
}
