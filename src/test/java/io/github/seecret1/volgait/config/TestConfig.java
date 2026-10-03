package io.github.seecret1.volgait.config;

import java.time.Duration;

public final class TestConfig {
    private TestConfig() {
    }

    public static String browser() {
        return System.getProperty("browser", ConfigProvider.DEFAULT_BROWSER).trim();
    }

    public static boolean headless() {
        return Boolean.parseBoolean(System.getProperty(
                "headless", String.valueOf(ConfigProvider.DEFAULT_HEADLESS)));
    }

    public static String remoteUrl() {
        return System.getProperty("remoteUrl", ConfigProvider.CONFIG.getString("runtime.remoteUrl")).trim();
    }

    public static String browserBinary() {
        return System.getProperty("browserBinary", ConfigProvider.CONFIG.getString("runtime.browserBinary")).trim();
    }

    public static Duration timeout() {
        return Duration.ofSeconds(Long.parseLong(System.getProperty(
                "timeout", String.valueOf(ConfigProvider.DEFAULT_TIMEOUT_SECONDS))));
    }

    public static Duration pageLoadTimeout() {
        return Duration.ofSeconds(Long.parseLong(System.getProperty(
                "pageLoadTimeout", String.valueOf(ConfigProvider.DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS))));
    }
}
