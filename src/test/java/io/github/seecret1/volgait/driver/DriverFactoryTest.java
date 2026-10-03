package io.github.seecret1.volgait.driver;

import io.github.seecret1.volgait.constants.RuntimeKeys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated("Временно изменяет системные параметры браузера")
@DisplayName("Фабрика браузеров")
class DriverFactoryTest {

    private final Map<String, String> previous = new HashMap<>();

    @BeforeEach
    void configure() {
        for (String key : new String[]{RuntimeKeys.HEADLESS, RuntimeKeys.REMOTE_URL, RuntimeKeys.BROWSER_BINARY}) {
            previous.put(key, System.getProperty(key));
            System.clearProperty(key);
        }
        System.setProperty(RuntimeKeys.HEADLESS, "false");
        System.setProperty(RuntimeKeys.REMOTE_URL, "http://localhost:4444");
    }

    @AfterEach
    void restore() {
        previous.forEach((key, value) -> {
            if (value == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, value);
            }
        });
    }

    @ParameterizedTest(name = "{index} — браузер {0} получает W3C-имя {1}")
    @DisplayName("Передаёт правильное W3C-имя выбранного браузера")
    @CsvSource({"chrome,chrome", "firefox,firefox", "edge,MicrosoftEdge", "safari,safari"})
    void suppliesCorrectW3cBrowserName(String input, String expected) {
        assertEquals(expected, DriverFactory.optionsFor(BrowserType.from(input)).getBrowserName());
    }

    @Test
    @DisplayName("Распознаёт имя браузера независимо от регистра и пробелов")
    void parsesMixedCaseAndWhitespace() {
        assertEquals(BrowserType.FIREFOX, BrowserType.from(" FireFox "));
        assertThrows(IllegalArgumentException.class, () -> BrowserType.from("unknown"));
    }

    @Test
    @DisplayName("Отклоняет запуск Safari без интерфейса")
    void rejectsSafariHeadlessBeforeStartingSession() {
        System.setProperty(RuntimeKeys.HEADLESS, "true");
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> DriverFactory.optionsFor(BrowserType.SAFARI)).getMessage().contains("headless"));
    }

    @Test
    @DisplayName("Отклоняет пользовательский бинарный файл Safari")
    void rejectsCustomSafariBinary() {
        System.setProperty(RuntimeKeys.BROWSER_BINARY, "/custom/browser");
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> DriverFactory.optionsFor(BrowserType.SAFARI)).getMessage().contains("browserBinary"));
    }
}
