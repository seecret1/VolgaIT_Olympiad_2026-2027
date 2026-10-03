package io.github.seecret1.volgait.pages;

import io.github.seecret1.volgait.components.PopupComponent;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public final class AdsPage extends BasePage {

    private final PopupComponent ad;

    @FindBy(id = "pum-1272")
    private WebElement adRoot;

    @FindBy(css = "#pum-1272 .pum-title")
    private WebElement adTitle;

    @FindBy(css = "#pum-1272 .pum-content")
    private WebElement adContent;

    @FindBy(css = "#pum-1272 button.pum-close")
    private WebElement adCloseButton;

    @FindBy(css = ".entry-content > p:first-of-type")
    private WebElement countdown;

    public AdsPage(WebDriver driver) {
        super(driver);
        ad = new PopupComponent(driver, adRoot, adTitle, adContent, adCloseButton);
    }

    public String heading() {
        return pageHeading();
    }

    public String countdownText() {
        return visible(countdown).getText().trim();
    }

    public PopupComponent ad() {
        return ad;
    }

    public PopupComponent waitForAd() {
        return ad.waitUntilVisible();
    }

    @Step("Перезагрузить страницу и дождаться повторного появления рекламы")
    public PopupComponent reloadAndWaitForAd() {
        driver.navigate().refresh();
        return waitForAd();
    }
}
