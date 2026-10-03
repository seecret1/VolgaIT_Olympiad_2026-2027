package io.github.seecret1.volgait.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.List;

public interface ConfigProvider {
    static Config readConfig() {
        return ConfigFactory.load("application.conf");
    }

    Config CONFIG = readConfig();

    String URL_CALENDARS = CONFIG.getString("urls.calendars");
    String URL_MODALS = CONFIG.getString("urls.modals");
    String URL_ADS = CONFIG.getString("urls.ads");
    String URL_FORM_FIELDS = CONFIG.getString("urls.formFields");
    String URL_HOME = CONFIG.getString("urls.home");
    String URL_BLOG = CONFIG.getString("urls.blog");
    String URL_YOUTUBE_CALENDARS = CONFIG.getString("urls.youtube.calendars");
    String URL_YOUTUBE_MODALS = CONFIG.getString("urls.youtube.modals");
    String URL_YOUTUBE_ADS = CONFIG.getString("urls.youtube.ads");

    String DEFAULT_BROWSER = CONFIG.getString("runtime.browser");
    boolean DEFAULT_HEADLESS = CONFIG.getBoolean("runtime.headless");
    long DEFAULT_TIMEOUT_SECONDS = CONFIG.getLong("runtime.timeoutSeconds");
    long DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS = CONFIG.getLong("runtime.pageLoadTimeoutSeconds");

    String CALENDAR_HEADING = CONFIG.getString("calendar.heading");
    String CALENDAR_FORMAT_HINT = CONFIG.getString("calendar.formatHint");
    int CALENDAR_FIRST_DAY = CONFIG.getInt("calendar.firstDay");
    int CALENDAR_REQUIRED_LAST_DAY = CONFIG.getInt("calendar.requiredLastDay");
    int CALENDAR_MAX_DAYS = CONFIG.getInt("calendar.maxDays");
    int CALENDAR_NAVIGATION_STEPS = CONFIG.getInt("calendar.navigationSteps");
    List<String> CALENDAR_VALID_DATES = CONFIG.getStringList("calendar.validDates");
    List<String> CALENDAR_INVALID_DATES = CONFIG.getStringList("calendar.invalidDates");

    String MODALS_HEADING = CONFIG.getString("modals.heading");
    String MODALS_ROLE = CONFIG.getString("modals.role");
    String SIMPLE_MODAL_TITLE = CONFIG.getString("modals.simple.title");
    String SIMPLE_MODAL_TEXT = CONFIG.getString("modals.simple.text");
    String FORM_MODAL_TITLE = CONFIG.getString("modals.form.title");
    String FORM_NAME = CONFIG.getString("modals.form.valid.name");
    String FORM_EMAIL = CONFIG.getString("modals.form.valid.email");
    String FORM_MESSAGE = CONFIG.getString("modals.form.valid.message");
    String INVALID_FORM_NAME = CONFIG.getString("modals.form.invalid.name");
    String INVALID_FORM_EMAIL = CONFIG.getString("modals.form.invalid.email");
    String INVALID_FORM_MESSAGE = CONFIG.getString("modals.form.invalid.message");

    String ADS_HEADING = CONFIG.getString("ads.heading");
    String ADS_COUNTDOWN_REGEX = CONFIG.getString("ads.countdownRegex");
    String ADS_TITLE = CONFIG.getString("ads.title");
    String ADS_MESSAGE = CONFIG.getString("ads.message");
    String ADS_ROLE = CONFIG.getString("ads.role");
    String ADS_ACTIVE_CLASS = CONFIG.getString("ads.activeClass");

    List<String> AUTOMATION_TOOLS = CONFIG.getStringList("formFields.automationTools");
    String AUTOMATION_TOOLS_SEPARATOR = CONFIG.getString("formFields.separator");
    String FORM_FIELDS_HEADING = CONFIG.getString("formFields.heading");
    String FORM_FIELDS_NUMBERS = CONFIG.getString("formFields.inputValues.numbers");
    String FORM_FIELDS_DOUBLE_SPACE = CONFIG.getString("formFields.inputValues.doubleSpace");
    String FORM_FIELDS_TRIPLE_SPACE = CONFIG.getString("formFields.inputValues.tripleSpace");
    String FORM_FIELDS_PASSWORD = CONFIG.getString("formFields.password");
    String FORM_FIELDS_EMAIL = CONFIG.getString("formFields.email");
    List<String> FORM_FIELDS_DRINKS = CONFIG.getStringList("formFields.drinks");
    List<String> FORM_FIELDS_COLORS = CONFIG.getStringList("formFields.colors");
    List<String> FORM_FIELDS_AUTOMATION_OPTIONS = CONFIG.getStringList("formFields.automationOptions");
    String FORM_FIELDS_AUTOMATION_SELECTION = CONFIG.getString("formFields.automationSelection");
    String FORM_FIELDS_ALERT_TEXT = CONFIG.getString("formFields.alertText");
}
