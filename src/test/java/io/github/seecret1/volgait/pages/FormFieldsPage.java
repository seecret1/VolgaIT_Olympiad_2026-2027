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

    @Step("Read Automation Tools with Selenium")
    public List<String> automationTools() {
        return all(automationTools).stream().map(WebElement::getText).map(String::trim).toList();
    }

    @Step("Fill Message with comma-separated Automation Tools")
    public String fillMessageWithAutomationTools() {
        String message = String.join(ConfigProvider.AUTOMATION_TOOLS_SEPARATOR, automationTools());
        replace(this.message, message);
        return message;
    }

    @Step("Fill required name")
    public FormFieldsPage enterName(String name) {
        replace(nameInput, name);
        return this;
    }

    @Step("Submit feedback form")
    public Alert submit() {
        scrollIntoView(submitButton);
        clickable(submitButton).click();
        return wait.until(ExpectedConditions.alertIsPresent());
    }

    public String messageValue() {
        return visible(message).getAttribute(DomAttributes.VALUE);
    }
}
