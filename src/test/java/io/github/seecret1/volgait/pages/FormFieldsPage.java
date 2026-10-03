package io.github.seecret1.volgait.pages;

import io.github.seecret1.volgait.constants.DomAttributes;
import io.github.seecret1.volgait.config.ConfigProvider;

import io.qameta.allure.Step;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

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

    @FindBy(xpath = "//form[@id='feedbackForm']//input[@type='password']")
    private WebElement passwordInput;

    @FindBy(xpath = "//input[@name='fav_drink']")
    private List<WebElement> drinkCheckboxes;

    @FindBy(xpath = "//input[@name='fav_color']")
    private List<WebElement> colorRadioButtons;

    @FindBy(id = "automation")
    private WebElement automationSelect;

    @FindBy(id = "email")
    private WebElement emailInput;

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

    @Step("Заполнить поле Password")
    public FormFieldsPage enterPassword(String password) {
        replace(passwordInput, password);
        return this;
    }

    @Step("Заполнить поле Email")
    public FormFieldsPage enterEmail(String email) {
        replace(emailInput, email);
        return this;
    }

    @Step("Заполнить поле Message")
    public FormFieldsPage enterMessage(String value) {
        replace(message, value);
        return this;
    }

    @Step("Выбрать любимые напитки: {values}")
    public FormFieldsPage selectDrinks(List<String> values) {
        all(drinkCheckboxes).stream()
                .filter(element -> values.contains(element.getAttribute(DomAttributes.VALUE)))
                .filter(element -> !element.isSelected())
                .forEach(this::clickOption);
        return this;
    }

    @Step("Выбрать любимый цвет: {value}")
    public FormFieldsPage selectColor(String value) {
        WebElement color = all(colorRadioButtons).stream()
                .filter(element -> value.equals(element.getAttribute(DomAttributes.VALUE)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Цвет не найден: " + value));
        clickOption(color);
        return this;
    }

    @Step("Выбрать вариант автоматизации: {value}")
    public FormFieldsPage selectAutomation(String value) {
        new Select(visible(automationSelect)).selectByValue(value);
        return this;
    }

    @Step("Отправить форму обратной связи")
    public Alert submit() {
        submitWithoutWaitingForAlert();
        return wait.until(ExpectedConditions.alertIsPresent());
    }

    @Step("Нажать кнопку отправки формы")
    public void submitWithoutWaitingForAlert() {
        scrollIntoView(submitButton);
        clickable(submitButton).click();
    }

    public String messageValue() {
        return visible(message).getAttribute(DomAttributes.VALUE);
    }

    public String heading() {
        return pageHeading();
    }

    public String nameValue() {
        return visible(nameInput).getAttribute(DomAttributes.VALUE);
    }

    public String passwordValue() {
        return visible(passwordInput).getAttribute(DomAttributes.VALUE);
    }

    public String passwordType() {
        return visible(passwordInput).getAttribute(DomAttributes.TYPE);
    }

    public String emailValue() {
        return visible(emailInput).getAttribute(DomAttributes.VALUE);
    }

    public boolean nameIsRequired() {
        return visible(nameInput).getAttribute(DomAttributes.REQUIRED) != null;
    }

    public String nameValidationMessage() {
        return visible(nameInput).getAttribute("validationMessage");
    }

    public List<String> selectedDrinks() {
        return all(drinkCheckboxes).stream()
                .filter(WebElement::isSelected)
                .map(element -> element.getAttribute(DomAttributes.VALUE))
                .toList();
    }

    public List<String> automationOptions() {
        return new Select(visible(automationSelect)).getOptions().stream()
                .map(element -> element.getAttribute(DomAttributes.VALUE))
                .toList();
    }

    public String selectedAutomation() {
        return new Select(visible(automationSelect)).getOptions().stream()
                .filter(WebElement::isSelected)
                .map(element -> element.getAttribute(DomAttributes.VALUE))
                .findFirst()
                .orElse("");
    }

    public String selectedColor() {
        return all(colorRadioButtons).stream()
                .filter(WebElement::isSelected)
                .map(element -> element.getAttribute(DomAttributes.VALUE))
                .findFirst()
                .orElse("");
    }

    private void clickOption(WebElement option) {
        scrollIntoView(option);
        clickable(option).click();
    }
}
