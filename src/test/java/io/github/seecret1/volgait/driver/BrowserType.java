package io.github.seecret1.volgait.driver;

import java.util.Locale;

public enum BrowserType {
    CHROME,
    FIREFOX,
    EDGE,
    SAFARI;

    public static BrowserType from(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Браузер '%s' не поддерживается. Используйте chrome, firefox, edge или safari."
                            .formatted(value), exception);
        }
    }
}
