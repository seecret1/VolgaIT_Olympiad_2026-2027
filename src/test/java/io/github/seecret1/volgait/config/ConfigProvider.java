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
    String URL_LOGIN = CONFIG.getString("urls.login");
    String URL_API_SWAGGER = CONFIG.getString("urls.swagger");
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

    String NEW_CORRECT_FIRST_NAME = CONFIG.getString("login.newUser.newCorrectUser.fistName");
    String NEW_CORRECT_LAST_NAME = CONFIG.getString("login.newUser.newCorrectUser.lastName");
    String NEW_CORRECT_USERNAME = CONFIG.getString("login.newUser.newCorrectUser.userName");
    String NEW_CORRECT_PASS = CONFIG.getString("login.passwordFormats.correctPass");

    String NEW_EMPTY_FIRST_NAME = CONFIG.getString("login.newUser.newIncorrectUser.fistName");
    String NEW_EMPTY_LAST_NAME = CONFIG.getString("login.newUser.newIncorrectUser.lastName");
    String NEW_EMPTY_USERNAME = CONFIG.getString("login.newUser.newIncorrectUser.userName");
    String NEW_EMPTY_PASS = CONFIG.getString("login.passwordFormats.emptyPass");

    String NEW_INCORRECT_PASS = CONFIG.getString("login.passwordFormats.incorrectPass");
    String GENERATED_USER_PREFIX = CONFIG.getString("login.generatedUser.prefix");
    int GENERATED_USER_SUFFIX_LENGTH = CONFIG.getInt("login.generatedUser.suffixLength");
    String GENERATED_USER_PASSWORD = CONFIG.getString("login.generatedUser.testPassword");
    String WRONG_ACCOUNT_PASS = CONFIG.getString("login.passwordFormats.wrongAccountPass");
    String WEAK_PASS = CONFIG.getString("login.passwordFormats.weakPass");

    String NEW_KIRILIC_FIRST_NAME = CONFIG.getString("login.newUser.newKirilicUser.fistName");
    String NEW_KIRILIC_LAST_NAME = CONFIG.getString("login.newUser.newKirilicUser.lastName");
    String NEW_KIRILIC_USERNAME = CONFIG.getString("login.newUser.newKirilicUser.userName");
    String NEW_KIRILIC_PASS = CONFIG.getString("login.passwordFormats.kirilicPass");

    String NEW_USER_LATIN_VALIDATION_TEXT = CONFIG.getString("login.testName.latin");
    String NEW_USER_RECAPTCHA_VALIDATION_TEXT = CONFIG.getString("login.testName.ifReCaptcha");
    String NEW_USER_SUCCESS_ALERT_TEXT = CONFIG.getString("login.testName.successAlert");
    String INVALID_LOGIN_TEXT = CONFIG.getString("login.testName.invalidLogin");
    String PASSWORD_ERROR_FRAGMENT = CONFIG.getString("login.testName.passwordErrorFragment");
    String FAILED_TOKEN_STATUS = CONFIG.getString("login.testName.failedTokenStatus");

    String REGISTRATION_DIGITS_FIRST_NAME = CONFIG.getString("login.registrationInput.digits.firstName");
    String REGISTRATION_DIGITS_LAST_NAME = CONFIG.getString("login.registrationInput.digits.lastName");
    String REGISTRATION_LOWERCASE_FIRST_NAME = CONFIG.getString("login.registrationInput.lowercase.firstName");
    String REGISTRATION_LOWERCASE_LAST_NAME = CONFIG.getString("login.registrationInput.lowercase.lastName");
    String REGISTRATION_SPACES_FIRST_NAME = CONFIG.getString("login.registrationInput.spaces.firstName");
    String REGISTRATION_SPACES_LAST_NAME = CONFIG.getString("login.registrationInput.spaces.lastName");
    String REGISTRATION_NUMERIC_FIRST_NAME = CONFIG.getString("login.registrationInput.numericUsername.firstName");
    String REGISTRATION_NUMERIC_LAST_NAME = CONFIG.getString("login.registrationInput.numericUsername.lastName");
    String REGISTRATION_NUMERIC_USERNAME_PREFIX = CONFIG.getString("login.registrationInput.numericUsername.prefix");

    int BOOK_STORE_MINIMUM_CATALOG_SIZE = CONFIG.getInt("bookStore.minimumCatalogSize");
    int BOOK_STORE_E2E_BOOK_COUNT = CONFIG.getInt("bookStore.e2eBookCount");
    String BOOK_STORE_COMMON_SEARCH = CONFIG.getString("bookStore.search.commonFragment");
    int BOOK_STORE_COMMON_MINIMUM_MATCHES = CONFIG.getInt("bookStore.search.commonMinimumMatches");
    String BOOK_STORE_MISSING_SEARCH = CONFIG.getString("bookStore.search.missingQuery");
    String BOOK_ADDED_ALERT = CONFIG.getString("bookStore.alerts.added");
    String BOOK_DUPLICATE_ALERT = CONFIG.getString("bookStore.alerts.duplicate");
    String BOOK_DELETED_ALERT = CONFIG.getString("bookStore.alerts.deleted");
    String ALL_BOOKS_DELETED_ALERT = CONFIG.getString("bookStore.alerts.allDeleted");
    String USER_DELETED_ALERT = CONFIG.getString("bookStore.alerts.userDeleted");
    String BOOK_API_DUPLICATE_MESSAGE = CONFIG.getString("bookStore.api.duplicateMessageFragment");
    String SIDE_PROFILE_LABEL = CONFIG.getString("bookStore.sideMenu.profileLabel");
    String SIDE_PROFILE_PATH = CONFIG.getString("bookStore.sideMenu.profilePath");
    String SIDE_STORE_LABEL = CONFIG.getString("bookStore.sideMenu.storeLabel");
    String SIDE_STORE_PATH = CONFIG.getString("bookStore.sideMenu.storePath");
    String SIDE_LOGIN_LABEL = CONFIG.getString("bookStore.sideMenu.loginLabel");
    String SIDE_LOGIN_PATH = CONFIG.getString("bookStore.sideMenu.loginPath");

    String NEW_NUMBER_FIRST_NAME = CONFIG.getString("login.newUser.newNumberUser.fistName");
    String NEW_NUMBER_LAST_NAME = CONFIG.getString("login.newUser.newNumberUser.lastName");
    String NEW_NUMBER_USERNAME = CONFIG.getString("login.newUser.newNumberUser.userName");
    String NEW_NUMBER_PASS = CONFIG.getString("login.passwordFormats.numberPass");

    String UPPER_CASE_PASS = CONFIG.getString("login.passwordFormats.uppercasePass");
    String SYMBOL_PASS = CONFIG.getString("login.passwordFormats.symbolPass");
    String NUMBER_PASS = CONFIG.getString("login.passwordFormats.numberPass");

}
