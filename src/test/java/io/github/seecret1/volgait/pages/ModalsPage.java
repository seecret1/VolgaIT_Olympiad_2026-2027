package io.github.seecret1.volgait.pages;

import io.github.seecret1.volgait.components.ContactFormComponent;
import io.github.seecret1.volgait.components.PopupComponent;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public final class ModalsPage extends BasePage {
    private final PopupComponent simpleModal;
    private final PopupComponent formModal;

    @FindBy(xpath = "//*[@id='simpleModal']")
    private WebElement simpleModalButton;

    @FindBy(xpath = "//*[@id='formModal']")
    private WebElement formModalButton;

    @FindBy(xpath = "//*[@id='pum-1318']")
    private WebElement simpleModalRoot;

    @FindBy(xpath = "//*[@id='pum-1318']//*[contains(concat(' ', normalize-space(@class), ' '), ' pum-title ')]")
    private WebElement simpleModalTitle;

    @FindBy(xpath = "//*[@id='pum-1318']//*[contains(concat(' ', normalize-space(@class), ' '), ' pum-content ')]")
    private WebElement simpleModalContent;

    @FindBy(xpath = "//*[@id='pum-1318']//button[contains(concat(' ', normalize-space(@class), ' '), ' pum-close ')]")
    private WebElement simpleModalCloseButton;

    @FindBy(xpath = "//*[@id='pum-674']")
    private WebElement formModalRoot;

    @FindBy(xpath = "//*[@id='pum-674']//*[contains(concat(' ', normalize-space(@class), ' '), ' pum-title ')]")
    private WebElement formModalTitle;

    @FindBy(xpath = "//*[@id='pum-674']//*[contains(concat(' ', normalize-space(@class), ' '), ' pum-content ')]")
    private WebElement formModalContent;

    @FindBy(xpath = "//*[@id='pum-674']//button[contains(concat(' ', normalize-space(@class), ' '), ' pum-close ')]")
    private WebElement formModalCloseButton;

    @FindBy(xpath = "//*[@id='pum-674']//input[contains(concat(' ', normalize-space(@class), ' '), ' name ')]")
    private WebElement formName;

    @FindBy(xpath = "//*[@id='pum-674']//input[contains(concat(' ', normalize-space(@class), ' '), ' email ')]")
    private WebElement formEmail;

    @FindBy(xpath = "//*[@id='pum-674']//textarea[contains(concat(' ', normalize-space(@class), ' '), ' textarea ')]")
    private WebElement formMessage;

    @FindBy(xpath = "//*[@id='pum-674']//button[@type='submit']")
    private WebElement formSubmitButton;

    @FindBy(xpath = "//*[@id='pum-674']//*[contains(@id, '-name-') and substring(@id, string-length(@id) - 5) = '-error']")
    private List<WebElement> nameErrors;

    @FindBy(xpath = "//*[@id='pum-674']//*[contains(@id, '-email-') and substring(@id, string-length(@id) - 5) = '-error']")
    private List<WebElement> emailErrors;

    @FindBy(xpath = "//*[@id='pum-674']//*[contains(@id, '-message-') and substring(@id, string-length(@id) - 5) = '-error']")
    private List<WebElement> messageErrors;

    public ModalsPage(WebDriver driver) {
        super(driver);
        simpleModal = new PopupComponent(driver, simpleModalRoot, simpleModalTitle,
                simpleModalContent, simpleModalCloseButton);
        formModal = new PopupComponent(driver, formModalRoot, formModalTitle,
                formModalContent, formModalCloseButton);
    }

    public String heading() {
        return pageHeading();
    }

    @Step("Open simple modal")
    public PopupComponent openSimpleModal() {
        clickable(simpleModalButton).click();
        return simpleModal.waitUntilVisible();
    }

    @Step("Open form modal")
    public PopupComponent openFormModal() {
        clickable(formModalButton).click();
        return formModal.waitUntilVisible();
    }

    public ContactFormComponent contactForm() {
        return new ContactFormComponent(formModal, formName, formEmail, formMessage,
                formSubmitButton, nameErrors, emailErrors, messageErrors);
    }
}
