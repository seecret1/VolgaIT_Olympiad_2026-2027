package io.github.seecret1.volgait.components;

import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Optional;

public final class AlertComponent {

    private final WebDriver driver;

    public AlertComponent(WebDriver driver) {
        this.driver = driver;
    }

    public String waitForTextAndAccept(Duration timeout) {
        Alert alert = new WebDriverWait(driver, timeout)
                .until(ExpectedConditions.alertIsPresent());
        String text = alert.getText();
        alert.accept();
        return text;
    }

    public Optional<String> acceptIfPresent(Duration timeout) {
        try {
            return Optional.of(waitForTextAndAccept(timeout));
        } catch (TimeoutException exception) {
            return Optional.empty();
        }
    }

    public boolean isPresentNow() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException exception) {
            return false;
        }
    }
}
