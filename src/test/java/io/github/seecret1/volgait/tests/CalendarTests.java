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
        @DisplayName("Нажатие на поле открывает календарь")
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

        @ParameterizedTest(name = "Валидную дату {0} можно ввести")
        @MethodSource("validDates")
        @Severity(SeverityLevel.BLOCKER)
        void valid_dates_can_be_entered(String date) {
            calendar.enterDate(date);
            assertEquals(LocalDate.parse(date), calendar.selectedLocalDate());
        }

        static Stream<String> validDates() {
            return CALENDAR_VALID_DATES.stream();
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Очистка удаляет ранее введённую дату")
        void date_can_be_cleared() {
            calendar.enterDate(CALENDAR_VALID_DATES.get(0)).clearDate();
            assertEquals("", calendar.selectedDate());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("Можно выбрать последний доступный день текущего месяца")
        void last_available_day_can_be_selected() {
            int lastDay = calendar.selectLastAvailableDay();
            assertEquals(lastDay, calendar.selectedLocalDate().getDayOfMonth());
        }

        @Test
        @Severity(SeverityLevel.NORMAL)
        @DisplayName("Переход на два месяца вперёд и назад возвращает исходный месяц")
        void multiple_month_navigation_returns_to_initial_month() {
            calendar.openCalendar();
            String initial = calendar.displayedMonthAndYear();
            IntStream.range(0, CALENDAR_NAVIGATION_STEPS).forEach(index -> calendar.nextMonth());
            IntStream.range(0, CALENDAR_NAVIGATION_STEPS).forEach(index -> calendar.previousMonth());
            assertEquals(initial, calendar.displayedMonthAndYear());
        }
    }

    @Nested
    @Story("Негативные сценарии календаря")
    @Tag(TestMetadata.NEGATIVE)
    class Negative {

        @ParameterizedTest(name = "Невалидная дата {0} не распознаётся как дата ISO")
        @MethodSource("invalidDates")
        @Severity(SeverityLevel.CRITICAL)
        void invalid_dates_are_not_interpreted_as_iso_dates(String invalidDate) {
            calendar.enterDate(invalidDate);
            assertAll(
                    () -> assertEquals(invalidDate, calendar.selectedDate()),
                    () -> assertThrows(DateTimeException.class, calendar::selectedLocalDate)
            );
        }

        static Stream<String> invalidDates() {
            return CALENDAR_INVALID_DATES.stream();
        }
    }
}
