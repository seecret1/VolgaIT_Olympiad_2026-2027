package io.github.seecret1.volgait.tests.api;

import io.github.seecret1.volgait.api.ApiResponse;
import io.github.seecret1.volgait.api.AccountFixture;
import io.github.seecret1.volgait.config.ConfigProvider;
import io.github.seecret1.volgait.constants.TestMetadata;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import io.github.seecret1.volgait.api.TestAccount;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic(TestMetadata.EPIC)
@Feature("Account API")
@Tag("api")
@DisplayName("DemoQA Account API")
class AccountApiTests extends BaseApiTest {

    @ParameterizedTest(name = "Пустое поле регистрации: {0}")
    @org.junit.jupiter.params.provider.ValueSource(strings = {"username", "password", "both"})
    @Tag(TestMetadata.NEGATIVE)
    void empty_registration_credentials_are_rejected(String missing) {
        String username = missing.equals("password") ? AccountFixture.uniqueUserName() : "";
        String password = missing.equals("username") ? ConfigProvider.GENERATED_USER_PASSWORD : "";
        ApiResponse result = api.createUser(username, password);
        try {
            response(result, 400, "error.json");
            assertFalse(result.body().path("message").asText().isBlank());
        } finally {
            if (result.statusCode() == 201) {
                accounts.deleteIfPresent(new TestAccount(result.body().path("userID").asText(),
                        username, password, ""));
            }
        }
    }

    @Test
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("Пароль из восьми символов позволяет зарегистрироваться и войти")
    void minimum_length_password_is_accepted() {
        account = accounts.create(AccountFixture.uniqueUserName(),
                ConfigProvider.CONFIG.getString("login.passwordFormats.minimumLengthPass"));
        ApiResponse user = response(api.getUser(account.userId(), account.token()), 200, "user.json");
        assertEquals(account.userName(), user.body().path("username").asText());
    }

    @Test
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("После удаления username доступен для повторной регистрации")
    void deleted_username_can_register_again() {
        account = accounts.create();
        accounts.deleteIfPresent(account);
        TestAccount recreated = accounts.create(account.userName(), account.password());
        ApiResponse user = response(api.getUser(recreated.userId(), recreated.token()), 200, "user.json");
        assertEquals(account.userName(), user.body().path("username").asText());
        assertTrue(user.body().path("books").isEmpty());
    }

    @Test
    @Tag(TestMetadata.NEGATIVE)
    @DisplayName("Несуществующий пользователь не получает токен")
    void unknown_user_cannot_login() {
        ApiResponse token = response(api.generateToken(AccountFixture.uniqueUserName(),
                ConfigProvider.GENERATED_USER_PASSWORD), 200, "token.json");
        assertEquals(ConfigProvider.FAILED_TOKEN_STATUS, token.body().path("status").asText());
        assertTrue(token.body().path("token").isNull());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidRegistrationPasswords")
    @Tag(TestMetadata.NEGATIVE)
    @Story("Валидация регистрации")
    @DisplayName("Регистрация отклоняет нарушение каждого требования к паролю")
    void registration_rejects_invalid_password_format(String caseName, String password) {
        String userName = AccountFixture.uniqueUserName();
        ApiResponse rejected = api.createUser(userName, password);
        try {
            response(rejected, 400, "error.json");
            assertTrue(rejected.body().path("message").asText()
                    .contains(ConfigProvider.PASSWORD_ERROR_FRAGMENT));
            response(api.authorized(userName, password), 404, "error.json");
        } finally {
            if (rejected.statusCode() == 201) {
                accounts.deleteIfPresent(new TestAccount(
                        rejected.body().path("userID").asText(), userName, password, ""));
            }
        }
    }

    static Stream<Arguments> invalidRegistrationPasswords() {
        return ConfigProvider.CONFIG.getConfigList("login.registrationInput.invalidPasswords").stream()
                .map(value -> Arguments.of(
                        value.getString("name"), value.getString("value")));
    }

    @Test
    @Story("Жизненный цикл пользователя")
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("Пользователя можно создать, авторизовать, прочитать и удалить")
    void user_lifecycle() {
        account = accounts.create();

        ApiResponse authorization = response(
                api.authorized(account.userName(), account.password()), 200, "authorization.json");
        ApiResponse user = response(
                api.getUser(account.userId(), account.token()), 200, "user.json");

        assertAll(
                () -> assertEquals(200, authorization.statusCode()),
                () -> assertTrue(authorization.body().asBoolean()),
                () -> assertEquals(200, user.statusCode()),
                () -> assertEquals(account.userId(), user.body().path("userId").asText()),
                () -> assertEquals(account.userName(), user.body().path("username").asText()),
                () -> assertTrue(user.body().path("books").isArray()),
                () -> assertTrue(user.body().path("books").isEmpty())
        );

        ApiResponse deletion = emptyResponse(
                api.deleteUser(account.userId(), account.token()), 204);
        ApiResponse authorizationAfterDeletion = response(
                api.authorized(account.userName(), account.password()), 404, "error.json");
        assertAll(
                () -> assertEquals(204, deletion.statusCode()),
                () -> assertEquals(404, authorizationAfterDeletion.statusCode())
        );
    }

    @Test
    @Story("Валидация регистрации")
    @Tag(TestMetadata.NEGATIVE)
    @DisplayName("Повторная регистрация username отклоняется")
    void duplicate_username_is_rejected() {
        account = accounts.create();

        ApiResponse duplicate = response(
                api.createUser(account.userName(), account.password()), 406, "error.json");

        assertAll(
                () -> assertEquals(406, duplicate.statusCode()),
                () -> assertFalse(duplicate.body().path("message").asText().isBlank())
        );
    }

    @Test
    @Story("Валидация регистрации")
    @Tag(TestMetadata.NEGATIVE)
    @DisplayName("Слабый пароль отклоняется")
    void weak_password_is_rejected() {
        ApiResponse response = response(api.createUser(
                AccountFixture.uniqueUserName(), ConfigProvider.WEAK_PASS),
                400, "error.json");

        assertAll(
                () -> assertEquals(400, response.statusCode()),
                () -> assertTrue(response.body().path("message").asText()
                        .contains(ConfigProvider.PASSWORD_ERROR_FRAGMENT))
        );
    }

    @Test
    @Story("Авторизация")
    @Tag(TestMetadata.NEGATIVE)
    @DisplayName("Неверный пароль не выдаёт токен")
    void invalid_password_does_not_issue_token() {
        account = accounts.create();

        ApiResponse token = response(
                api.generateToken(account.userName(), ConfigProvider.WRONG_ACCOUNT_PASS), 200, "token.json");
        ApiResponse authorization = response(
                api.authorized(account.userName(), ConfigProvider.WRONG_ACCOUNT_PASS), 404, "error.json");

        assertAll(
                () -> assertEquals(200, token.statusCode()),
                () -> assertEquals(ConfigProvider.FAILED_TOKEN_STATUS,
                        token.body().path("status").asText()),
                () -> assertTrue(token.body().path("token").isNull()),
                () -> assertEquals(404, authorization.statusCode())
        );
    }

    @Test
    @Story("Авторизация")
    @Tag(TestMetadata.NEGATIVE)
    @DisplayName("Чтение пользователя без Bearer token запрещено")
    void user_requires_bearer_token() {
        account = accounts.create();

        ApiResponse response = response(
                api.getUser(account.userId(), null), 401, "error.json");

        assertEquals(401, response.statusCode());
    }
}
