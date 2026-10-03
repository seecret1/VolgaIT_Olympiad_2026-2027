package io.github.seecret1.volgait.driver;

public enum BrowserType {
    CHROME,
    FIREFOX;

    public static BrowserType from(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unsupported browser '%s'. Use chrome or firefox.".formatted(value), exception);
        }
    }
}

