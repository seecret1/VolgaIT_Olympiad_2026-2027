package io.github.seecret1.volgait.pages;

import io.github.seecret1.volgait.constants.DomAttributes;
import io.github.seecret1.volgait.config.ConfigProvider;

import io.qameta.allure.Step;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public final class FormFieldsPage extends BasePage {

    @FindBy(id = "message")
    private WebElement message;

    @FindBy(xpath = "//label[normalize-space()='Automation tools']/following-sibling::ul[1]/li")
    private List<WebElement> automationTools;

    @FindBy(xpath = "//button[@id='submit-btn']")
    private WebElement submitButton;

    @FindBy(xpath = "//input[@id='name-input']")
    private WebElement nameInput;

    public FormFieldsPage(WebDriver driver) {
        super(driver);
    }

    @Step("Прочитать список Automation Tools с помощью Selenium")
    public List<String> automationTools() {
        return all(automationTools).stream().map(WebElement::getText).map(String::trim).toList();
    }

    @Step("Заполнить Message списком Automation Tools через запятую")
    public String fillMessageWithAutomationTools() {
        String message = String.join(ConfigProvider.AUTOMATION_TOOLS_SEPARATOR, automationTools());
        replace(this.message, message);
        return message;
    }

    @Step("Заполнить обязательное поле имени")
    public FormFieldsPage enterName(String name) {
        replace(nameInput, name);
        return this;
    }

    @Step("Отправить форму обратной связи")
    public Alert submit() {
        scrollIntoView(submitButton);
        clickable(submitButton).click();
        return wait.until(ExpectedConditions.alertIsPresent());
    }

    public String messageValue() {
        return visible(message).getAttribute(DomAttributes.VALUE);
    }
}
