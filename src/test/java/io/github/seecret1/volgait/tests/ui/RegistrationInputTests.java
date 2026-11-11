package io.github.seecret1.volgait.tests.ui;

import io.github.seecret1.volgait.api.AccountFixture;
import io.github.seecret1.volgait.config.ConfigProvider;
import io.github.seecret1.volgait.constants.TestMetadata;
import io.github.seecret1.volgait.pages.LoginPage;
import io.github.seecret1.volgait.pages.RegisterPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Epic(TestMetadata.EPIC)
@Feature("Регистрация DemoQA")
@Story("Допустимые форматы полей")
@DisplayName("UI: форматы ФИО и username")
class RegistrationInputTests extends BaseUiTest {

    @ParameterizedTest(name = "{0}")
    @Tag(TestMetadata.POSITIVE)
    @MethodSource("registrationValues")
    @DisplayName("Форма сохраняет введённые значения")
    void registration_form_preserves_entered_values(
            String caseName,
            String firstName,
            String lastName,
            String userName
    ) {
        open(URL_LOGIN);
        RegisterPage page = new LoginPage(driver).waitUntilLoaded()
                .openRegister()
                .fill(firstName, lastName, userName, ConfigProvider.NEW_CORRECT_PASS);

        assertAll(
                () -> assertEquals(firstName, page.firstNameValue()),
                () -> assertEquals(lastName, page.lastNameValue()),
                () -> assertEquals(userName, page.userNameValue())
        );
    }

    private static Stream<Arguments> registrationValues() {
        return Stream.concat(Stream.of(
                Arguments.of("ФИО содержит цифры", ConfigProvider.REGISTRATION_DIGITS_FIRST_NAME,
                        ConfigProvider.REGISTRATION_DIGITS_LAST_NAME,
                        AccountFixture.uniqueUserName()),
                Arguments.of("ФИО начинается с маленькой буквы", ConfigProvider.REGISTRATION_LOWERCASE_FIRST_NAME,
                        ConfigProvider.REGISTRATION_LOWERCASE_LAST_NAME,
                        AccountFixture.uniqueUserName()),
                Arguments.of("ФИО содержит пробелы", ConfigProvider.REGISTRATION_SPACES_FIRST_NAME,
                        ConfigProvider.REGISTRATION_SPACES_LAST_NAME,
                        AccountFixture.uniqueUserName()),
                Arguments.of("username содержит цифры", ConfigProvider.REGISTRATION_NUMERIC_FIRST_NAME,
                        ConfigProvider.REGISTRATION_NUMERIC_LAST_NAME,
                        ConfigProvider.REGISTRATION_NUMERIC_USERNAME_PREFIX + AccountFixture.uniqueUserName())
        ), ConfigProvider.CONFIG.getConfigList("login.registrationInput.additionalNames").stream()
                .map(value -> Arguments.of(value.getString("name"), value.getString("firstName"),
                        value.getString("lastName"), AccountFixture.uniqueUserName())));
    }

    @ParameterizedTest(name = "Обязательное поле: {0}")
    @ValueSource(strings = {"firstname", "lastname", "userName", "password"})
    @Tag(TestMetadata.NEGATIVE)
    @DisplayName("Пустое обязательное поле блокирует регистрацию")
    void required_field_cannot_be_empty(String fieldId) {
        open(URL_LOGIN);
        RegisterPage page = new LoginPage(driver).waitUntilLoaded().openRegister().fill(
                fieldId.equals("firstname") ? "" : ConfigProvider.NEW_CORRECT_FIRST_NAME,
                fieldId.equals("lastname") ? "" : ConfigProvider.NEW_CORRECT_LAST_NAME,
                fieldId.equals("userName") ? "" : AccountFixture.uniqueUserName(),
                fieldId.equals("password") ? "" : ConfigProvider.NEW_CORRECT_PASS).submit();
        assertTrue(page.isFieldInvalid(fieldId));
        assertFalse(page.isAlertPresentNow());
    }
}
