package io.github.seecret1.volgait.pages;

import io.github.seecret1.volgait.components.ContactFormComponent;
import io.github.seecret1.volgait.components.PopupComponent;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public final class ModalsPage extends BasePage {
    private final PopupComponent simpleModal;
    private final PopupComponent formModal;

    public ModalsPage(WebDriver driver) {
        super(driver);
        simpleModal = new PopupComponent(driver, By.id("pum-1318"), io.github.seecret1.volgait.config.TestConfig.timeout());
        formModal = new PopupComponent(driver, By.id("pum-674"), io.github.seecret1.volgait.config.TestConfig.timeout());
    }

    public String heading() {
        return pageHeading();
    }

    @Step("Open simple modal")
    public PopupComponent openSimpleModal() {
        clickable(By.id("simpleModal")).click();
        return simpleModal.waitUntilVisible();
    }

    @Step("Open form modal")
    public PopupComponent openFormModal() {
        clickable(By.id("formModal")).click();
        return formModal.waitUntilVisible();
    }

    public ContactFormComponent contactForm() {
        return new ContactFormComponent(formModal);
    }
}

