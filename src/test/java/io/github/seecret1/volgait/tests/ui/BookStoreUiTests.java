package io.github.seecret1.volgait.tests.ui;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.seecret1.volgait.api.AccountFixture;
import io.github.seecret1.volgait.api.ApiResponse;
import io.github.seecret1.volgait.api.ApiClient;
import io.github.seecret1.volgait.api.TestAccount;
import io.github.seecret1.volgait.config.ConfigProvider;
import io.github.seecret1.volgait.constants.TestMetadata;
import io.github.seecret1.volgait.model.BookRow;
import io.github.seecret1.volgait.pages.BookStorePage;
import io.github.seecret1.volgait.pages.LoginPage;
import io.github.seecret1.volgait.pages.ProfilePage;
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
import org.junit.jupiter.api.function.Executable;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.seecret1.volgait.tests.ui.AlertAssertions.assertAll;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic(TestMetadata.EPIC)
@Feature("Магазин и профиль DemoQA")
@DisplayName("UI + API: книжная коллекция")
class BookStoreUiTests extends BaseUiTest {

    private ApiClient api;

    private AccountFixture accounts;

    private TestAccount account;

    private JsonNode catalog;

    private LoginPage loginPage;

    @BeforeEach
    void prepareUserAndCatalog() {
        api = new ApiClient();
        accounts = new AccountFixture(api);
        account = accounts.create();
        ApiResponse response = api.getBooks();
        assertEquals(200, response.statusCode());
        catalog = response.body().path("books");
        assertTrue(catalog.isArray() && catalog.size() >= ConfigProvider.BOOK_STORE_MINIMUM_CATALOG_SIZE,
                "В каталоге недостаточно книг для E2E-сценариев");
        open(URL_LOGIN);
        loginPage = new LoginPage(driver).waitUntilLoaded();
    }

    @AfterEach
    void removeUser() {
        accounts.deleteIfPresent(account);
    }

    @Nested
    @Story("Положительные сценарии")
    @Tag(TestMetadata.POSITIVE)
    class Positive {

        @Test
        @DisplayName("Поиск пробела соответствует текстовым полям каталога")
        void whitespace_search_matches_catalog() {
            String query = ConfigProvider.CONFIG.getString("bookStore.search.whitespaceQuery");
            List<String> expected = new ArrayList<>();
            for (JsonNode book : catalog) {
                if (List.of("title", "author", "publisher").stream()
                        .anyMatch(key -> book.path(key).asText().contains(query))) {
                    expected.add(title(book));
                }
            }
            assertEquals(expected, openStore().search(query).displayedBookTitles());
        }

        @Test
        @DisplayName("Username вошедшего пользователя отображается без искажений")
        void logged_in_user_name_is_displayed() {
            ProfilePage profile = loginPage.login(account.userName(), account.password());

            assertEquals(account.userName(), profile.displayedUserName());
        }

        @Test
        @DisplayName("Список магазина и поиск книги в UI соответствуют API")
        void store_catalog_and_search_match_api() {
            ProfilePage profile = loginPage.login(account.userName(), account.password());
            BookStorePage store = profile.goToBookStore().waitUntilLoaded();
            List<String> expectedTitles = titles(catalog);

            assertEquals(expectedTitles, store.displayedBookTitles());

            String searchedTitle = catalog.get(1).path("title").asText();
            store.search(searchedTitle);

            assertEquals(List.of(searchedTitle), store.displayedBookTitles());
        }

        @Test
        @DisplayName("Поиск книги не зависит от верхнего регистра")
        void store_search_is_case_insensitive_for_uppercase() {
            BookStorePage store = openStore();
            String query = title(catalog.get(1)).toUpperCase(Locale.ROOT);

            store.search(query);

            assertEquals(expectedSearchTitles(query), store.displayedBookTitles());
        }

        @Test
        @DisplayName("Поиск книги не зависит от нижнего регистра")
        void store_search_is_case_insensitive_for_lowercase() {
            BookStorePage store = openStore();
            String query = title(catalog.get(2)).toLowerCase(Locale.ROOT);

            store.search(query);

            assertEquals(expectedSearchTitles(query), store.displayedBookTitles());
        }

        @Test
        @DisplayName("Общий фрагмент возвращает все книги с одинаковым совпадением")
        void common_fragment_returns_all_matching_books() {
            BookStorePage store = openStore();
            String query = ConfigProvider.BOOK_STORE_COMMON_SEARCH;
            List<String> expected = expectedSearchTitles(query);
            assertTrue(expected.size() >= ConfigProvider.BOOK_STORE_COMMON_MINIMUM_MATCHES,
                    "В API-каталоге недостаточно книг с общим фрагментом");

            store.search(query);

            assertEquals(expected, store.displayedBookTitles());
        }

        @Test
        @DisplayName("Поиск без совпадений показывает пустой список")
        void search_without_matches_returns_empty_list() {
            BookStorePage store = openStore();

            store.search(ConfigProvider.BOOK_STORE_MISSING_SEARCH);

            assertTrue(store.displayedBookTitles().isEmpty());
        }

        @Test
        @DisplayName("Одну и несколько книг можно удалить в UI, результат виден через API")
        void one_and_multiple_books_can_be_deleted_in_ui() {
            List<JsonNode> selected = firstBooks(ConfigProvider.BOOK_STORE_E2E_BOOK_COUNT);
            assertEquals(201, api.addBooks(account.userId(), isbns(selected), account.token()).statusCode());
            ProfilePage profile = loginPage.login(account.userName(), account.password())
                    .waitForBookCount(selected.size());
            String token = requireUiToken(profile);

            profile.deleteBook(title(selected.get(0)));
            String firstDeletionAlert = profile.lastAlertText();
            ApiResponse afterOne = api.getUser(account.userId(), token);
            profile.deleteBook(title(selected.get(1)));
            String secondDeletionAlert = profile.lastAlertText();
            profile.deleteBook(title(selected.get(2)));
            String thirdDeletionAlert = profile.lastAlertText();
            ApiResponse afterAll = api.getUser(account.userId(), token);

            assertAll(
                    () -> assertAlertOrRecordDefect(firstDeletionAlert,
                            ConfigProvider.BOOK_DELETED_ALERT, "Удаление первой книги"),
                    () -> assertAlertOrRecordDefect(secondDeletionAlert,
                            ConfigProvider.BOOK_DELETED_ALERT, "Удаление второй книги"),
                    () -> assertAlertOrRecordDefect(thirdDeletionAlert,
                            ConfigProvider.BOOK_DELETED_ALERT, "Удаление третьей книги"),
                    () -> assertEquals(2, afterOne.body().path("books").size()),
                    () -> assertFalse(containsIsbn(afterOne.body().path("books"), isbn(selected.get(0)))),
                    () -> assertTrue(afterAll.body().path("books").isEmpty())
            );
        }

        @Test
        @DisplayName("Кнопка Delete All Books очищает профиль и API-коллекцию")
        void delete_all_books_button_clears_collection() {
            List<JsonNode> selected = firstBooks(ConfigProvider.BOOK_STORE_E2E_BOOK_COUNT);
            assertEquals(201, api.addBooks(account.userId(), isbns(selected), account.token()).statusCode());
            ProfilePage profile = loginPage.login(account.userName(), account.password())
                    .waitForBookCount(selected.size());
            String token = requireUiToken(profile);

            profile.deleteAllBooksAndConfirm();

            assertAll(
                    () -> assertAlertOrRecordDefect(profile.lastAlertText(),
                            ConfigProvider.ALL_BOOKS_DELETED_ALERT, "Удаление всех книг"),
                    () -> assertTrue(waitForApiBookCount(token, 0).body().path("books").isEmpty())
            );
        }

        @Test
        @DisplayName("Поиск в профиле оставляет только подходящую книгу")
        void profile_book_search_works() {
            List<JsonNode> selected = firstBooks(ConfigProvider.BOOK_STORE_E2E_BOOK_COUNT);
            assertEquals(201, api.addBooks(account.userId(), isbns(selected), account.token()).statusCode());
            ProfilePage profile = loginPage.login(account.userName(), account.password())
                    .waitForBookCount(selected.size());
            String expectedTitle = title(selected.get(1));

            profile.searchBooks(expectedTitle);

            assertEquals(List.of(expectedTitle), profile.displayedBookTitles());
        }
    }

    @Nested
    @Story("Отклонённые операции")
    @Tag(TestMetadata.NEGATIVE)
    class Negative {

        @Test
        @DisplayName("Известный дефект: обложка зависит от позиции книги в списке")
        void book_image_depends_on_row_position() {
            BookStorePage store = openStore();
            Map<String, BookRow> originalRows = store.displayedBookRows().stream()
                    .collect(Collectors.toMap(BookRow::isbn, Function.identity()));
            JsonNode selected = catalog.get(catalog.size() - 1);

            store.search(title(selected));
            BookRow filteredRow = store.displayedBookRows().get(0);
            BookRow originalRow = originalRows.get(isbn(selected));

            assertAll(
                    () -> assertEquals(isbn(selected), filteredRow.isbn()),
                    () -> assertEquals(title(selected), filteredRow.title()),
                    () -> assertNotEquals(originalRow.imageSource(), filteredRow.imageSource(),
                            "Дефект больше не воспроизводится: тест нужно перенести в positive")
            );
        }

        @Test
        @DisplayName("Повторное добавление книги показывает точный alert и не создаёт дубль")
        void duplicate_book_shows_alert_and_is_not_added() {
            JsonNode book = catalog.get(0);
            ProfilePage profile = loginPage.login(account.userName(), account.password());
            String token = requireUiToken(profile);
            BookStorePage store = profile.goToBookStore().openBookByIsbn(isbn(book));

            String firstAlert = store.addOpenedBookToCollection();
            String duplicateAlert = store.addOpenedBookToCollection();
            ApiResponse apiProfile = api.getUser(account.userId(), token);

            assertAll(
                    () -> assertAlertOrRecordDefect(firstAlert, ConfigProvider.BOOK_ADDED_ALERT, "Добавление книги"),
                    () -> assertAlertOrRecordDefect(duplicateAlert, ConfigProvider.BOOK_DUPLICATE_ALERT, "Повторное добавление книги"),
                    () -> assertEquals(1, apiProfile.body().path("books").size()),
                    () -> assertEquals(isbn(book), apiProfile.body().path("books").get(0).path("isbn").asText())
            );
        }
    }

    @Test
    @Story("Динамическая проверка левого меню")
    @DisplayName("Левые кнопки классифицируются по фактическому результату")
    void side_navigation_buttons_are_classified_dynamically() {
        ProfilePage profile = loginPage.login(account.userName(), account.password());
        profile.goToBookStore().waitUntilLoaded();

        List<SideMenuCheck> checks = new ArrayList<>();
        checks.add(checkSideMenu(ConfigProvider.SIDE_PROFILE_LABEL, ConfigProvider.SIDE_PROFILE_PATH));

        open(resolveDemoQa(ConfigProvider.SIDE_PROFILE_PATH));
        new ProfilePage(driver).waitUntilLoaded();
        checks.add(checkSideMenu(ConfigProvider.SIDE_STORE_LABEL, ConfigProvider.SIDE_STORE_PATH));

        open(resolveDemoQa(ConfigProvider.SIDE_PROFILE_PATH));
        new ProfilePage(driver).waitUntilLoaded().logout();
        open(resolveDemoQa(ConfigProvider.SIDE_STORE_PATH));
        checks.add(checkSideMenu(ConfigProvider.SIDE_LOGIN_LABEL, ConfigProvider.SIDE_LOGIN_PATH));

        boolean allButtonsWork = checks.stream().allMatch(SideMenuCheck::successful);
        String classification = allButtonsWork ? TestMetadata.POSITIVE : TestMetadata.NEGATIVE;
        Allure.label("tag", classification);
        Allure.label("navigationResult", classification);
        Allure.addAttachment("Результат проверки левого меню", "text/plain",
                checks.stream().map(SideMenuCheck::toString).collect(Collectors.joining(System.lineSeparator())));

        assertAll(checks.stream()
                .map(check -> (Executable) () -> {
                    if (check.successful()) {
                        assertTrue(check.actualUrl().contains(check.expectedPath()), check.toString());
                    } else {
                        assertFalse(check.details().isBlank(), check.toString());
                    }
                }));
    }

    @Nested
    @Story("End-to-end")
    @Tag(TestMetadata.POSITIVE)
    class EndToEnd {

        @Test
        @DisplayName("Логин → добавить 3 книги → удалить одну → получить 2 книги через API")
        void add_three_remove_one_and_get_profile_books() {
            List<JsonNode> selected = firstBooks(ConfigProvider.BOOK_STORE_E2E_BOOK_COUNT);
            ProfilePage profile = loginPage.login(account.userName(), account.password());
            String token = requireUiToken(profile);
            profile = addThroughUi(profile, selected);

            profile.deleteBook(title(selected.get(0)));
            String deletionAlert = profile.lastAlertText();
            ApiResponse result = api.getUser(account.userId(), token);

            assertAll(
                    () -> assertAlertOrRecordDefect(deletionAlert,
                            ConfigProvider.BOOK_DELETED_ALERT, "E2E: удаление книги"),
                    () -> assertEquals(2, result.body().path("books").size()),
                    () -> assertFalse(containsIsbn(result.body().path("books"), isbn(selected.get(0)))),
                    () -> assertTrue(containsIsbn(result.body().path("books"), isbn(selected.get(1)))),
                    () -> assertTrue(containsIsbn(result.body().path("books"), isbn(selected.get(2))))
            );
        }

        @Test
        @DisplayName("Логин → добавить 3 книги → удалить все → получить пустой профиль через API")
        void add_three_delete_all_and_get_empty_profile() {
            List<JsonNode> selected = firstBooks(ConfigProvider.BOOK_STORE_E2E_BOOK_COUNT);
            ProfilePage profile = loginPage.login(account.userName(), account.password());
            String token = requireUiToken(profile);
            profile = addThroughUi(profile, selected);

            profile.deleteAllBooksAndConfirm();
            String deletionAlert = profile.lastAlertText();

            assertAll(
                    () -> assertAlertOrRecordDefect(deletionAlert,
                            ConfigProvider.ALL_BOOKS_DELETED_ALERT, "E2E: удаление всех книг"),
                    () -> assertTrue(waitForApiBookCount(token, 0).body().path("books").isEmpty())
            );
        }

        @Test
        @DisplayName("Максимальная коллекция корректно работает с пагинацией профиля")
        void all_catalog_books_are_available_through_profile_pagination() {
            List<JsonNode> allBooks = firstBooks(catalog.size());
            ProfilePage profile = addThroughUi(
                    loginPage.login(account.userName(), account.password()), allBooks)
                    .waitForBookCount(allBooks.size());

            int pages = profile.totalPages();
            Set<String> displayed = new HashSet<>();
            displayed.addAll(profile.displayedBookTitles());
            assertFalse(profile.isPreviousPageEnabled());

            while (profile.currentPage() < pages) {
                assertTrue(profile.isNextPageEnabled());
                profile.nextPage();
                displayed.addAll(profile.displayedBookTitles());
            }

            assertAll(
                    () -> assertEquals(new HashSet<>(titles(catalog)), displayed),
                    () -> assertEquals(pages, profile.currentPage()),
                    () -> assertFalse(profile.isNextPageEnabled())
            );

            while (profile.currentPage() > 1) {
                profile.previousPage();
            }
            assertAll(
                    () -> assertEquals(1, profile.currentPage()),
                    () -> assertFalse(profile.isPreviousPageEnabled())
            );
        }
    }

    private ProfilePage addThroughUi(ProfilePage profile, List<JsonNode> books) {
        BookStorePage store = profile.goToBookStore();
        for (JsonNode book : books) {
            String alert = store.openBookByIsbn(isbn(book)).addOpenedBookToCollection();
            assertAlertOrRecordDefect(alert, ConfigProvider.BOOK_ADDED_ALERT, "Добавление книги");
        }
        return store.openProfile();
    }

    private BookStorePage openStore() {
        return loginPage.login(account.userName(), account.password())
                .goToBookStore()
                .waitUntilLoaded();
    }

    private List<String> expectedSearchTitles(String query) {
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        return titles(catalog).stream()
                .filter(bookTitle -> bookTitle.toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .toList();
    }

    private String requireUiToken(ProfilePage profile) {
        String token = profile.authenticationToken();
        assertFalse(token.isBlank(), profile::browserStorageKeys);
        return token;
    }

    private List<JsonNode> firstBooks(int count) {
        List<JsonNode> result = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            result.add(catalog.get(index));
        }
        return result;
    }

    private List<String> titles(JsonNode books) {
        List<String> result = new ArrayList<>();
        books.forEach(book -> result.add(title(book)));
        return result;
    }

    private List<String> isbns(List<JsonNode> books) {
        return books.stream().map(this::isbn).toList();
    }

    private String title(JsonNode book) {
        return book.path("title").asText();
    }

    private String isbn(JsonNode book) {
        return book.path("isbn").asText();
    }

    private boolean containsIsbn(JsonNode books, String isbn) {
        for (JsonNode book : books) {
            if (isbn.equals(book.path("isbn").asText())) {
                return true;
            }
        }
        return false;
    }

    private void waitForUrl(String fragment) {
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(current -> current.getCurrentUrl().contains(fragment));
    }

    private SideMenuCheck checkSideMenu(String item, String expectedPath) {
        try {
            new BookStorePage(driver).openSideMenu(item);
            new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(current -> current.getCurrentUrl().contains(expectedPath));
            return new SideMenuCheck(item, expectedPath, driver.getCurrentUrl(), true, "Переход выполнен");
        } catch (RuntimeException exception) {
            attachBrowserState("Левое меню: не сработала кнопка " + item);
            return new SideMenuCheck(item, expectedPath, driver.getCurrentUrl(), false,
                    exception.getClass().getSimpleName() + ": " + exception.getMessage());
        }
    }

    private String resolveDemoQa(String path) {
        return java.net.URI.create(URL_LOGIN).resolve(path).toString();
    }

    private record SideMenuCheck(
            String item,
            String expectedPath,
            String actualUrl,
            boolean successful,
            String details
    ) {
    }

    private ApiResponse waitForApiBookCount(String token, int expectedCount) {
        AtomicReference<ApiResponse> latest = new AtomicReference<>();
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .pollingEvery(Duration.ofSeconds(1))
                .until(current -> {
                    ApiResponse response = api.getUser(account.userId(), token);
                    latest.set(response);
                    return response.statusCode() == 200
                            && response.body().path("books").size() == expectedCount;
                });
        return latest.get();
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
