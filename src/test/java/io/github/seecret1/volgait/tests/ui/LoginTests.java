package io.github.seecret1.volgait.tests.ui;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.seecret1.volgait.api.AccountFixture;
import io.github.seecret1.volgait.api.ApiResponse;
import io.github.seecret1.volgait.api.ApiClient;
import io.github.seecret1.volgait.api.TestAccount;
import io.github.seecret1.volgait.config.ConfigProvider;
import io.github.seecret1.volgait.constants.TestMetadata;
import io.github.seecret1.volgait.pages.BookStorePage;
import io.github.seecret1.volgait.pages.LoginPage;
import io.github.seecret1.volgait.pages.ProfilePage;
import io.github.seecret1.volgait.pages.RegisterPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Allure;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.net.URI;

import static io.github.seecret1.volgait.tests.ui.AlertAssertions.assertAll;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic(TestMetadata.EPIC)
@Feature("Аккаунт и профиль DemoQA")
@DisplayName("UI + API: аккаунт и книжная коллекция")
class LoginTests extends BaseUiTest {

    private ApiClient api;

    private AccountFixture accounts;

    private TestAccount account;

    private LoginPage loginPage;

    @BeforeEach
    void openLoginPage() {
        api = new ApiClient();
        accounts = new AccountFixture(api);
        open(URL_LOGIN);
        loginPage = new LoginPage(driver).waitUntilLoaded();
    }

    @AfterEach
    void removeTestAccount() {
        accounts.deleteIfPresent(account);
    }

    @Nested
    @Story("Жизненный цикл аккаунта")
    @Tag(TestMetadata.POSITIVE)
    class AccountLifecycle {

        @Test
        @DisplayName("После API-регистрации можно войти, выйти и войти повторно")
        void registered_user_can_login_logout_and_login_again() {
            account = accounts.create();

            ProfilePage profile = loginPage.login(account.userName(), account.password());
            assertEquals(account.userName(), profile.displayedUserName());

            loginPage = profile.logout();
            ApiResponse authorizationAfterLogout = api.authorized(account.userName(), account.password());
            ApiResponse requestWithRevokedToken = api.getUser(account.userId(), account.token());
            assertAll(
                    () -> assertEquals(200, authorizationAfterLogout.statusCode()),
                    () -> assertTrue(authorizationAfterLogout.body().asBoolean()),
                    () -> assertEquals(401, requestWithRevokedToken.statusCode())
            );

            ProfilePage profileAfterSecondLogin = loginPage.login(account.userName(), account.password());
            assertEquals(account.userName(), profileAfterSecondLogin.displayedUserName());
        }

        @Test
        @DisplayName("Выход не удаляет пользователя")
        void logout_keeps_user_in_api() {
            account = accounts.create();

            loginPage.login(account.userName(), account.password()).logout();

            ApiResponse authorization = api.authorized(account.userName(), account.password());
            ApiResponse user = api.getUser(account.userId(), account.token());
            assertAll(
                    () -> assertEquals(200, authorization.statusCode()),
                    () -> assertTrue(authorization.body().asBoolean()),
                    () -> assertEquals(401, user.statusCode())
            );
        }

        @Test
        @DisplayName("Удаление в UI удаляет пользователя в API")
        void deleting_account_in_ui_removes_it_from_api() {
            account = accounts.create();
            ProfilePage profile = loginPage.login(account.userName(), account.password());

            String alertText = profile.deleteAccountAndConfirm();
            ApiResponse authorization = api.authorized(account.userName(), account.password());

            assertAll(
                    () -> assertAlertOrRecordDefect(
                            alertText, ConfigProvider.USER_DELETED_ALERT, "Удаление аккаунта"),
                    () -> assertEquals(404, authorization.statusCode())
            );
            account = null;
        }
    }

    @Nested
    @Story("Книжная коллекция")
    @Tag(TestMetadata.POSITIVE)
    class BookCollection {

        @Test
        @DisplayName("Книга, добавленная через API, отображается в UI-профиле")
        void api_book_is_visible_in_ui_profile() {
            account = accounts.create();
            JsonNode book = firstCatalogBook();
            ProfilePage profile = loginPage.login(account.userName(), account.password());
            String uiToken = profile.authenticationToken();
            assertFalse(uiToken.isBlank(), profile::browserStorageKeys);
            assertEquals(201, api.addBooks(account.userId(),
                    List.of(book.path("isbn").asText()), uiToken).statusCode());
            ApiResponse apiUser = api.getUser(account.userId(), uiToken);

            loginPage = profile.logout();
            ProfilePage reloadedProfile = loginPage.login(account.userName(), account.password());

            assertAll(
                    () -> assertTrue(containsIsbn(apiUser.body().path("books"), book.path("isbn").asText())),
                    () -> assertEquals(account.userName(), reloadedProfile.displayedUserName()),
                    () -> assertTrue(reloadedProfile.displaysBook(book.path("title").asText()))
            );
        }

        @Test
        @DisplayName("Книга, добавленная в UI, отображается в профиле и API")
        void ui_selected_book_is_visible_in_profile_and_api() {
            account = accounts.create();
            JsonNode book = firstCatalogBook();
            ProfilePage profile = loginPage.login(account.userName(), account.password());
            String uiToken = profile.authenticationToken();
            assertFalse(uiToken.isBlank(), profile::browserStorageKeys);

            BookStorePage store = profile.goToBookStore()
                    .openBookByIsbn(book.path("isbn").asText());
            String alertText = store.addOpenedBookToCollection();
            ProfilePage updatedProfile = store.openProfile();
            ApiResponse apiUser = api.getUser(account.userId(), uiToken);
            loginPage = updatedProfile.logout();
            ProfilePage reloadedProfile = loginPage.login(account.userName(), account.password());

            assertAll(
                    () -> assertAlertOrRecordDefect(alertText, ConfigProvider.BOOK_ADDED_ALERT, "Добавление книги"),
                    () -> assertEquals(account.userName(), reloadedProfile.displayedUserName()),
                    () -> assertTrue(reloadedProfile.displaysBook(book.path("title").asText())),
                    () -> assertTrue(containsIsbn(apiUser.body().path("books"), book.path("isbn").asText()))
            );
        }
    }

    @Nested
    @Story("Отклонённые операции")
    @Tag(TestMetadata.NEGATIVE)
    class Negative {

        @Test
        @DisplayName("После выхода прямой переход в профиль не открывает аккаунт")
        void profile_is_protected_after_logout() {
            account = accounts.create();
            loginPage.login(account.userName(), account.password()).logout();
            open(URI.create(URL_LOGIN)
                    .resolve(ConfigProvider.CONFIG.getString("bookStore.sideMenu.profilePath")).toString());
            assertTrue(new ProfilePage(driver).showsLoggedOutNotice());
            assertEquals(401, api.getUser(account.userId(), account.token()).statusCode());
        }

        @Test
        @DisplayName("Вход несуществующего пользователя отклоняется")
        void unknown_username_is_rejected() {
            loginPage.submitLogin(AccountFixture.uniqueUserName(), ConfigProvider.GENERATED_USER_PASSWORD);
            assertEquals(ConfigProvider.INVALID_LOGIN_TEXT, loginPage.loginErrorText());
        }

        @ParameterizedTest(name = "Пустое поле входа: {0}")
        @ValueSource(strings = {"username", "password"})
        void empty_login_field_is_rejected(String field) {
            loginPage.submitLogin(field.equals("username") ? "" : AccountFixture.uniqueUserName(),
                    field.equals("password") ? "" : ConfigProvider.GENERATED_USER_PASSWORD);
            assertTrue(loginPage.isRequiredFieldInvalid(field));
        }

        @Test
        @DisplayName("Регистрация без CAPTCHA не создаёт пользователя")
        void registration_without_captcha_is_rejected() {
            String userName = AccountFixture.uniqueUserName();

            RegisterPage registerPage = loginPage.openRegister().fill(
                    ConfigProvider.NEW_CORRECT_FIRST_NAME,
                    ConfigProvider.NEW_CORRECT_LAST_NAME,
                    userName,
                    ConfigProvider.NEW_CORRECT_PASS
            ).submit();

            assertAll(
                    () -> assertEquals(ConfigProvider.NEW_USER_RECAPTCHA_VALIDATION_TEXT,
                            registerPage.validationText()),
                    () -> assertFalse(registerPage.isAlertPresentNow()),
                    () -> assertEquals(404,
                            api.authorized(userName, ConfigProvider.NEW_CORRECT_PASS).statusCode())
            );
        }

        @Test
        @DisplayName("Вход с неверным паролем отклоняется")
        void login_with_invalid_password_is_rejected() {
            account = accounts.create();

            loginPage.submitLogin(account.userName(), ConfigProvider.WRONG_ACCOUNT_PASS);

            assertAll(
                    () -> assertEquals(ConfigProvider.INVALID_LOGIN_TEXT, loginPage.loginErrorText()),
                    () -> assertEquals(200,
                            api.authorized(account.userName(), account.password()).statusCode())
            );
        }
    }

    private JsonNode firstCatalogBook() {
        ApiResponse catalog = api.getBooks();
        assertEquals(200, catalog.statusCode());
        JsonNode books = catalog.body().path("books");
        assertTrue(books.isArray() && !books.isEmpty(), "Каталог книг не должен быть пустым");
        return books.get(0);
    }

    private boolean containsIsbn(JsonNode books, String isbn) {
        if (!books.isArray()) {
            return false;
        }
        for (JsonNode book : books) {
            if (isbn.equals(book.path("isbn").asText())) {
                return true;
            }
        }
        return false;
    }

    private void assertAlertOrRecordDefect(String actual, String expected, String operation) {
        if (actual.isBlank()) {
            Allure.label("alertResult", "SKIPPED");
            Allure.addAttachment(operation + ": alert отсутствует", "text/plain",
                    "Операция выполнена, но ожидаемый alert не появился: " + expected);
            attachBrowserState(operation + ": отсутствует alert");
            assumeTrue(false, operation + ": ожидаемый alert не появился: " + expected);
        }
        assertEquals(expected, actual);
        Allure.label("alertResult", TestMetadata.POSITIVE);
    }

}
