package io.github.seecret1.volgait.config;

import java.time.Duration;

public final class TestConfig {
    private static final String DEFAULT_BASE_URL = "https://practice-automation.com";

    private TestConfig() {
    }

    public static String baseUrl() {
        return System.getProperty("baseUrl", DEFAULT_BASE_URL).replaceAll("/+$", "");
    }

    public static String browser() {
        return System.getProperty("browser", "chrome").trim().toLowerCase();
    }

    public static boolean headless() {
        return Boolean.parseBoolean(System.getProperty("headless", "true"));
    }

    public static Duration timeout() {
        return Duration.ofSeconds(Long.parseLong(System.getProperty("timeout", "15")));
    }

    public static Duration pageLoadTimeout() {
        return Duration.ofSeconds(Long.parseLong(System.getProperty("pageLoadTimeout", "40")));
    }
}

