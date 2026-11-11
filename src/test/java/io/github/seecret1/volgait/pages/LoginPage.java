package io.github.seecret1.volgait.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public final class LoginPage extends BasePage {

    @FindBy(id = "newUser")
    private WebElement newUserButton;

    @FindBy(id = "userName")
    private WebElement userName;

    @FindBy(id = "password")
    private WebElement password;

    @FindBy(id = "login")
    private WebElement loginButton;

    @FindBy(id = "name")
    private WebElement errorMessage;

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    @Step("Войти как {newUserName}")
    public ProfilePage login(String newUserName, String newPassword) {
        replace(userName, newUserName);
        replace(password, newPassword);
        clickable(loginButton).click();
        return new ProfilePage(driver).waitUntilLoaded();
    }

    @Step("Дождаться формы входа")
    public LoginPage waitUntilLoaded() {
        WebDriverWait pageWait = new WebDriverWait(driver, Duration.ofSeconds(30));
        pageWait.until(ExpectedConditions.visibilityOf(userName));
        pageWait.until(ExpectedConditions.elementToBeClickable(loginButton));
        return this;
    }

    @Step("Выполнить вход в профиль")
    public LoginPage submitLogin(String newUserName, String newPassword) {
        replace(userName, newUserName);
        replace(password, newPassword);
        clickable(loginButton).click();
        return this;
    }

    @Step("Открыть страницу регистрации")
    public RegisterPage openRegister() {
        clickable(newUserButton).click();
        return new RegisterPage(driver).waitUntilLoaded();
    }

    public String loginErrorText() {
        return visible(errorMessage).getText().trim();
    }

    public boolean isRequiredFieldInvalid(String field) {
        WebElement input = switch (field) {
            case "username" -> userName;
            case "password" -> password;
            default -> throw new IllegalArgumentException("Unknown login field: " + field);
        };
        return new WebDriverWait(driver, Duration.ofSeconds(5)).until(current ->
                input.getAttribute("class").contains("is-invalid"));
    }
}
