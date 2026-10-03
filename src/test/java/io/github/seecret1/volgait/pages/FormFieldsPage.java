package io.github.seecret1.volgait.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public final class FormFieldsPage extends BasePage {
    private static final By MESSAGE = By.id("message");
    private static final By TOOLS = By.xpath("//label[normalize-space()='Automation tools']/following-sibling::ul[1]/li");

    public FormFieldsPage(WebDriver driver) {
        super(driver);
    }

    @Step("Read Automation Tools with Selenium")
    public List<String> automationTools() {
        return all(TOOLS).stream().map(WebElement::getText).map(String::trim).toList();
    }

    @Step("Fill Message with comma-separated Automation Tools")
    public String fillMessageWithAutomationTools() {
        String message = String.join(", ", automationTools());
        replace(MESSAGE, message);
        return message;
    }

    @Step("Fill required name")
    public FormFieldsPage enterName(String name) {
        replace(By.id("name-input"), name);
        return this;
    }

    @Step("Submit feedback form")
    public Alert submit() {
        clickable(By.id("submit-btn")).click();
        return wait.until(ExpectedConditions.alertIsPresent());
    }

    public String messageValue() {
        return visible(MESSAGE).getAttribute("value");
    }
}
