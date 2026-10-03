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

    @FindBy(id = "simpleModal")
    private WebElement simpleModalButton;

    @FindBy(id = "formModal")
    private WebElement formModalButton;

    @FindBy(id = "pum-1318")
    private WebElement simpleModalRoot;

    @FindBy(css = "#pum-1318 .pum-title")
    private WebElement simpleModalTitle;

    @FindBy(css = "#pum-1318 .pum-content")
    private WebElement simpleModalContent;

    @FindBy(css = "#pum-1318 button.pum-close")
    private WebElement simpleModalCloseButton;

    @FindBy(id = "pum-674")
    private WebElement formModalRoot;

    @FindBy(css = "#pum-674 .pum-title")
    private WebElement formModalTitle;

    @FindBy(css = "#pum-674 .pum-content")
    private WebElement formModalContent;

    @FindBy(css = "#pum-674 button.pum-close")
    private WebElement formModalCloseButton;

    @FindBy(css = "#pum-674 input.name")
    private WebElement formName;

    @FindBy(css = "#pum-674 input.email")
    private WebElement formEmail;

    @FindBy(css = "#pum-674 textarea.textarea")
    private WebElement formMessage;

    @FindBy(css = "#pum-674 button[type='submit']")
    private WebElement formSubmitButton;

    @FindBy(css = "#pum-674 [id*='-name-'][id$='-error']")
    private List<WebElement> nameErrors;

    @FindBy(css = "#pum-674 [id*='-email-'][id$='-error']")
    private List<WebElement> emailErrors;

    @FindBy(css = "#pum-674 [id*='-message-'][id$='-error']")
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
