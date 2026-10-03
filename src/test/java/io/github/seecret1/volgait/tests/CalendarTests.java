package io.github.seecret1.volgait.tests;

import io.github.seecret1.volgait.constants.TestMetadata;

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
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

import static org.junit.jupiter.api.Assertions.*;

@Epic(TestMetadata.EPIC)
@Feature("Календарь")
@DisplayName("Страница «Календарь»")
class CalendarTests extends BaseUiTest {

    private CalendarPage calendar;

    @BeforeEach
    void openCalendarPage() {
        open(URL_CALENDARS);
        calendar = new CalendarPage(driver);
    }

    @Nested
    @Story("Позитивные сценарии календаря")
    @Tag(TestMetadata.POSITIVE)
    class Positive {

        @Test
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("На странице отображается заголовок «Calendars»")
        void page_has_expected_heading() {
            assertEquals(CALENDAR_HEADING, calendar.heading());
        }

        @Test
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("Поле отображает подсказку формата даты ISO")
        void field_has_iso_format_hint() {
            assertEquals(CALENDAR_FORMAT_HINT, calendar.formatHint());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("P03 — нажатие на поле открывает календарь")
        void click_opens_date_picker() {
            calendar.openCalendar();
            assertTrue(calendar.widgetIsVisible());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Текущий месяц содержит полный набор дней")
        void current_month_contains_valid_days() {
            calendar.openCalendar();
            List<Integer> days = calendar.availableDays();
            assertAll(
                    () -> assertTrue(days.contains(CALENDAR_FIRST_DAY)),
                    () -> assertTrue(days.contains(CALENDAR_REQUIRED_LAST_DAY)),
                    () -> assertTrue(days.size() >= CALENDAR_REQUIRED_LAST_DAY
                            && days.size() <= CALENDAR_MAX_DAYS)
            );
        }

        @Test
        @Severity(SeverityLevel.BLOCKER)
        @DisplayName("Выбор первого дня заполняет поле даты")
        void selecting_day_fills_input() {
            calendar.selectDay(CALENDAR_FIRST_DAY);
            assertEquals(CALENDAR_FIRST_DAY, calendar.selectedLocalDate().getDayOfMonth());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Кнопка перехода вперёд изменяет отображаемый месяц")
        void next_arrow_changes_month() {
            calendar.openCalendar();
            String initial = calendar.displayedMonthAndYear();
            calendar.nextMonth();
            assertNotEquals(initial, calendar.displayedMonthAndYear());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Кнопка перехода назад возвращает исходный месяц")
        void previous_arrow_returns_to_initial_month() {
            calendar.openCalendar();
            String initial = calendar.displayedMonthAndYear();
            calendar.nextMonth().previousMonth();
            assertEquals(initial, calendar.displayedMonthAndYear());
        }

        @ParameterizedTest(name = "{0} — валидную дату {1} можно ввести")
        @MethodSource("validDates")
        @Severity(SeverityLevel.BLOCKER)
        void valid_dates_can_be_entered(String scenarioId, String date) {
            calendar.enterDate(date);
            assertEquals(LocalDate.parse(date), calendar.selectedLocalDate());
        }

        static Stream<Arguments> validDates() {
            return IntStream.range(0, CALENDAR_VALID_DATES.size())
                    .mapToObj(index -> Arguments.of(
                            "P%02d".formatted(index + 8), CALENDAR_VALID_DATES.get(index)));
        }
    }

    @Nested
    @Story("Негативные сценарии календаря")
    @Tag(TestMetadata.NEGATIVE)
    class Negative {

        @ParameterizedTest(name = "{0} — невалидная дата {1} не распознаётся как дата ISO")
        @MethodSource("invalidDates")
        @Severity(SeverityLevel.CRITICAL)
        void invalid_dates_are_not_interpreted_as_iso_dates(String scenarioId, String invalidDate) {
            calendar.enterDate(invalidDate);
            assertAll(
                    () -> assertEquals(invalidDate, calendar.selectedDate()),
                    () -> assertThrows(DateTimeException.class, calendar::selectedLocalDate)
            );
        }

        static Stream<Arguments> invalidDates() {
            return IntStream.range(0, CALENDAR_INVALID_DATES.size())
                    .mapToObj(index -> Arguments.of(
                            "N%02d".formatted(index + 1), CALENDAR_INVALID_DATES.get(index)));
        }
    }
}
