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
    private static final By DATE_PICKER = By.id("ui-datepicker-div");
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
        By dayLink = By.xpath("//div[@id='ui-datepicker-div']//td[not(contains(@class,'ui-datepicker-other-month'))]/a[normalize-space()='" + day + "']");
        clickable(dayLink).click();
        return this;
    }

    @Step("Go to next calendar month")
    public CalendarPage nextMonth() {
        clickable(By.cssSelector("#ui-datepicker-div .ui-datepicker-next")).click();
        return this;
    }

    @Step("Go to previous calendar month")
    public CalendarPage previousMonth() {
        clickable(By.cssSelector("#ui-datepicker-div .ui-datepicker-prev")).click();
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
        return visible(By.cssSelector("#ui-datepicker-div .ui-datepicker-title")).getText().trim();
    }

    public List<Integer> availableDays() {
        return all(By.cssSelector("#ui-datepicker-div td:not(.ui-datepicker-other-month) a"))
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

