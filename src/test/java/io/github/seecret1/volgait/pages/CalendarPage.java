package io.github.seecret1.volgait.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class CalendarPage extends BasePage {
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    @FindBy(css = "input.jp-contact-form-date")
    private WebElement dateInput;

    @FindBy(css = ".contact-form__field-format")
    private WebElement formatHint;

    @FindBy(css = ".dp-below, .dp-above")
    private WebElement datePicker;

    @FindBy(css = ".dp-day:not(.dp-edge-day):not(.dp-day-disabled)")
    private List<WebElement> availableDays;

    @FindBy(css = ".dp-next")
    private WebElement nextMonth;

    @FindBy(css = ".dp-prev")
    private WebElement previousMonth;

    @FindBy(css = ".dp-cal-month")
    private WebElement displayedMonth;

    @FindBy(css = ".dp-cal-year")
    private WebElement displayedYear;

    @FindBy(css = ".jp-contact-form-date-wrap .contact-form__input-error")
    private List<WebElement> validationErrors;

    public CalendarPage(WebDriver driver) {
        super(driver);
    }

    public String heading() {
        return pageHeading();
    }

    @Step("Open calendar widget")
    public CalendarPage openCalendar() {
        clickable(dateInput).click();
        visible(datePicker);
        return this;
    }

    @Step("Enter date: {date}")
    public CalendarPage enterDate(String date) {
        replace(dateInput, date);
        visible(dateInput).sendKeys(Keys.TAB);
        return this;
    }

    @Step("Select day {day} in the current calendar month")
    public CalendarPage selectDay(int day) {
        openCalendar();
        WebElement dayButton = all(availableDays).stream()
                .filter(element -> String.valueOf(day).equals(element.getText().trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Day is unavailable: " + day));
        clickable(dayButton).click();
        return this;
    }

    @Step("Go to next calendar month")
    public CalendarPage nextMonth() {
        clickable(nextMonth).click();
        return this;
    }

    @Step("Go to previous calendar month")
    public CalendarPage previousMonth() {
        clickable(previousMonth).click();
        return this;
    }

    public boolean widgetIsVisible() {
        return datePicker.isDisplayed();
    }

    public String selectedDate() {
        return visible(dateInput).getAttribute("value");
    }

    public String formatHint() {
        return visible(formatHint).getText().trim();
    }

    public String displayedMonthAndYear() {
        String month = visible(displayedMonth).getText().trim();
        String year = visible(displayedYear).getText().trim();
        return month + " " + year;
    }

    public List<Integer> availableDays() {
        return all(availableDays)
                .stream().map(WebElement::getText).map(Integer::parseInt).toList();
    }

    public boolean hasValidationError() {
        return validationErrors.stream()
                .anyMatch(element -> element.isDisplayed() && !element.getText().isBlank());
    }

    public LocalDate selectedLocalDate() {
        return LocalDate.parse(selectedDate(), ISO_DATE);
    }
}
