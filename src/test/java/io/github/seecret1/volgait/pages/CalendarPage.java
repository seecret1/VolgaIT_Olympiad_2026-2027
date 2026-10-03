package io.github.seecret1.volgait.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class CalendarPage extends BasePage {
    private static final By DATE_INPUT = By.cssSelector("input.jp-contact-form-date");
    private static final By FORMAT_HINT = By.cssSelector(".contact-form__field-format");
    private static final By DATE_PICKER = By.cssSelector(".dp-below, .dp-above");
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    public CalendarPage(WebDriver driver) {
        super(driver);
    }

    public String heading() {
        return pageHeading();
    }

    @Step("Open calendar widget")
    public CalendarPage openCalendar() {
        clickable(DATE_INPUT).click();
        visible(DATE_PICKER);
        return this;
    }

    @Step("Enter date: {date}")
    public CalendarPage enterDate(String date) {
        replace(DATE_INPUT, date);
        visible(DATE_INPUT).sendKeys(Keys.TAB);
        return this;
    }

    @Step("Select day {day} in the current calendar month")
    public CalendarPage selectDay(int day) {
        openCalendar();
        By dayLink = By.xpath("//*[contains(@class,'dp-below') or contains(@class,'dp-above')]//button[contains(@class,'dp-day') and not(contains(@class,'dp-edge-day')) and normalize-space()='" + day + "']");
        clickable(dayLink).click();
        return this;
    }

    @Step("Go to next calendar month")
    public CalendarPage nextMonth() {
        clickable(By.cssSelector(".dp-next")).click();
        return this;
    }

    @Step("Go to previous calendar month")
    public CalendarPage previousMonth() {
        clickable(By.cssSelector(".dp-prev")).click();
        return this;
    }

    public boolean widgetIsVisible() {
        return isDisplayed(DATE_PICKER);
    }

    public String selectedDate() {
        return visible(DATE_INPUT).getAttribute("value");
    }

    public String formatHint() {
        return visible(FORMAT_HINT).getText().trim();
    }

    public String displayedMonthAndYear() {
        String month = visible(By.cssSelector(".dp-cal-month")).getText().trim();
        String year = visible(By.cssSelector(".dp-cal-year")).getText().trim();
        return month + " " + year;
    }

    public List<Integer> availableDays() {
        return all(By.cssSelector(".dp-day:not(.dp-edge-day):not(.dp-day-disabled)"))
                .stream().map(WebElement::getText).map(Integer::parseInt).toList();
    }

    public boolean hasValidationError() {
        return driver.findElements(By.cssSelector(".jp-contact-form-date-wrap .contact-form__input-error"))
                .stream().anyMatch(element -> element.isDisplayed() && !element.getText().isBlank());
    }

    public LocalDate selectedLocalDate() {
        return LocalDate.parse(selectedDate(), ISO_DATE);
    }
}
