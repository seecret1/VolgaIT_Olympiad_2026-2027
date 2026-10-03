package io.github.seecret1.volgait.pages;

import io.github.seecret1.volgait.components.PopupComponent;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public final class AdsPage extends BasePage {
    private final PopupComponent ad;

    @FindBy(xpath = "//*[@id='pum-1272']")
    private WebElement adRoot;

    @FindBy(xpath = "//*[@id='pum-1272']//*[contains(concat(' ', normalize-space(@class), ' '), ' pum-title ')]")
    private WebElement adTitle;

    @FindBy(xpath = "//*[@id='pum-1272']//*[contains(concat(' ', normalize-space(@class), ' '), ' pum-content ')]")
    private WebElement adContent;

    @FindBy(xpath = "//*[@id='pum-1272']//button[contains(concat(' ', normalize-space(@class), ' '), ' pum-close ')]")
    private WebElement adCloseButton;

    @FindBy(xpath = "//*[contains(concat(' ', normalize-space(@class), ' '), ' entry-content ')]/p[1]")
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
}
