package io.github.seecret1.volgait.pages;

import io.github.seecret1.volgait.components.PopupComponent;
import io.github.seecret1.volgait.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public final class AdsPage extends BasePage {
    private final PopupComponent ad;

    public AdsPage(WebDriver driver) {
        super(driver);
        ad = new PopupComponent(driver, By.id("pum-1272"), TestConfig.timeout());
    }

    public String heading() {
        return pageHeading();
    }

    public String countdownText() {
        return visible(By.cssSelector(".entry-content > p:first-of-type")).getText().trim();
    }

    public PopupComponent ad() {
        return ad;
    }

    public PopupComponent waitForAd() {
        return ad.waitUntilVisible();
    }
}

