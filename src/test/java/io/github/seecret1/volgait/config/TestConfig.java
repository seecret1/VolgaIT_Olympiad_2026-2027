package io.github.seecret1.volgait.config;

import io.github.seecret1.volgait.constants.RuntimeKeys;

import java.time.Duration;

public final class TestConfig {
    private TestConfig() {
    }

    public static String browser() {
        return System.getProperty(RuntimeKeys.BROWSER, ConfigProvider.DEFAULT_BROWSER).trim();
    }

    public static boolean headless() {
        return Boolean.parseBoolean(System.getProperty(
                RuntimeKeys.HEADLESS, String.valueOf(ConfigProvider.DEFAULT_HEADLESS)));
    }

    public static String remoteUrl() {
        return System.getProperty(
                RuntimeKeys.REMOTE_URL, ConfigProvider.CONFIG.getString("runtime.remoteUrl")).trim();
    }

    public static String browserBinary() {
        return System.getProperty(
                RuntimeKeys.BROWSER_BINARY, ConfigProvider.CONFIG.getString("runtime.browserBinary")).trim();
    }

    public static Duration timeout() {
        return Duration.ofSeconds(Long.parseLong(System.getProperty(
                RuntimeKeys.TIMEOUT, String.valueOf(ConfigProvider.DEFAULT_TIMEOUT_SECONDS))));
    }

    public static Duration pageLoadTimeout() {
        return Duration.ofSeconds(Long.parseLong(System.getProperty(
                RuntimeKeys.PAGE_LOAD_TIMEOUT, String.valueOf(ConfigProvider.DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS))));
    }
}
