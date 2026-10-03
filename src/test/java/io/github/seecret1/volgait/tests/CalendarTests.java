package io.github.seecret1.volgait.tests;

import io.github.seecret1.volgait.pages.CalendarPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Epic("VolgaIT semifinal")
@Feature("Calendar")
@DisplayName("Calendar page")
class CalendarTests extends BaseUiTest {
    private CalendarPage calendar;

    @BeforeEach
    void openCalendarPage() {
        open("/calendars/");
        calendar = new CalendarPage(driver);
    }

    @Nested
    @Story("Positive calendar scenarios")
    @Tag("positive")
    class Positive {
        @Test
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("P01 — page has the Calendar heading")
        void page_has_expected_heading() {
            assertEquals("Calendars", calendar.heading());
        }

        @Test
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("P02 — field displays the ISO date format hint")
        void field_has_iso_format_hint() {
            assertEquals("YYYY-MM-DD", calendar.formatHint());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("P03 — click on the field opens the date picker")
        void click_opens_date_picker() {
            calendar.openCalendar();
            assertTrue(calendar.widgetIsVisible());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("P04 — current month contains a complete set of days")
        void current_month_contains_valid_days() {
            calendar.openCalendar();
            List<Integer> days = calendar.availableDays();
            assertAll(
                    () -> assertTrue(days.contains(1)),
                    () -> assertTrue(days.contains(28)),
                    () -> assertTrue(days.size() >= 28 && days.size() <= 31)
            );
        }

        @Test
        @Severity(SeverityLevel.BLOCKER)
        @DisplayName("P05 — selecting day 1 fills the input")
        void selecting_day_fills_input() {
            calendar.selectDay(1);
            assertEquals(1, calendar.selectedLocalDate().getDayOfMonth());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("P06 — next arrow changes the displayed month")
        void next_arrow_changes_month() {
            calendar.openCalendar();
            String initial = calendar.displayedMonthAndYear();
            calendar.nextMonth();
            assertNotEquals(initial, calendar.displayedMonthAndYear());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("P07 — previous arrow returns to the initial month")
        void previous_arrow_returns_to_initial_month() {
            calendar.openCalendar();
            String initial = calendar.displayedMonthAndYear();
            calendar.nextMonth().previousMonth();
            assertEquals(initial, calendar.displayedMonthAndYear());
        }

        @ParameterizedTest(name = "P{index} — valid date {0} can be entered")
        @ValueSource(strings = {"2024-02-29", "2026-01-01", "2099-12-31"})
        @Severity(SeverityLevel.BLOCKER)
        void valid_dates_can_be_entered(String date) {
            calendar.enterDate(date);
            assertEquals(LocalDate.parse(date), calendar.selectedLocalDate());
        }
    }

    @Nested
    @Story("Negative calendar scenarios")
    @Tag("negative")
    class Negative {
        @ParameterizedTest(name = "N{index} — invalid date {0} is not interpreted as ISO date")
        @ValueSource(strings = {"31/12/2026", "2025-02-30", "not-a-date", "2026-13-01"})
        @Severity(SeverityLevel.CRITICAL)
        void invalid_dates_are_not_interpreted_as_iso_dates(String invalidDate) {
            calendar.enterDate(invalidDate);
            assertAll(
                    () -> assertEquals(invalidDate, calendar.selectedDate()),
                    () -> assertThrows(DateTimeException.class, calendar::selectedLocalDate)
            );
        }
    }
}

