package io.github.seecret1.volgait.tests.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.seecret1.volgait.api.ApiResponse;
import io.github.seecret1.volgait.config.ConfigProvider;
import io.github.seecret1.volgait.constants.TestMetadata;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic(TestMetadata.EPIC)
@Feature("Book Store API")
@Tag("api")
@DisplayName("DemoQA Book Store API")
class BookStoreApiTests extends BaseApiTest {

    @Test
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("Коллекции двух пользователей независимы")
    void user_collections_are_isolated() {
        var other = accounts.create();
        String isbn = books.get(0).path("isbn").asText();
        response(api.addBooks(account.userId(), List.of(isbn), account.token()), 201, "collection.json");
        assertTrue(response(api.getUser(other.userId(), other.token()), 200, "user.json")
                .body().path("books").isEmpty());
        assertEquals(isbn, response(api.getUser(account.userId(), account.token()), 200, "user.json")
                .body().path("books").get(0).path("isbn").asText());
    }

    @org.junit.jupiter.params.ParameterizedTest(name = "Чужой token: {0}")
    @org.junit.jupiter.params.provider.ValueSource(strings = {"read", "add", "delete", "clear"})
    @Tag(TestMetadata.NEGATIVE)
    void another_user_cannot_access_collection(String operation) {
        var other = accounts.create();
        String isbn = books.get(0).path("isbn").asText();
        response(api.addBooks(account.userId(), List.of(isbn), account.token()), 201, "collection.json");
        ApiResponse denied = switch (operation) {
            case "read" -> api.getUser(account.userId(), other.token());
            case "add" -> api.addBooks(account.userId(), List.of(books.get(1).path("isbn").asText()), other.token());
            case "delete" -> api.deleteBook(isbn, account.userId(), other.token());
            default -> api.clearBooks(account.userId(), other.token());
        };
        ApiResponse owner = response(api.getUser(account.userId(), account.token()), 200, "user.json");
        assertAll(() -> response(denied, 401, "error.json"),
                () -> assertEquals(1, owner.body().path("books").size()),
                () -> assertEquals(isbn, owner.body().path("books").get(0).path("isbn").asText()));
    }

    @org.junit.jupiter.params.ParameterizedTest(name = "Несуществующий ISBN: {0}")
    @org.junit.jupiter.params.provider.ValueSource(strings = {"read", "add"})
    @Tag(TestMetadata.NEGATIVE)
    void unknown_isbn_is_rejected(String operation) {
        String isbn = ConfigProvider.CONFIG.getString("bookStore.api.missingIsbn");
        for (JsonNode book : books) {
            assertFalse(isbn.equals(book.path("isbn").asText()), "Тестовый ISBN не должен существовать");
        }
        ApiResponse result = operation.equals("read") ? api.getBook(isbn)
                : api.addBooks(account.userId(), List.of(isbn), account.token());
        response(result, 400, "error.json");
        assertFalse(result.body().path("message").asText().isBlank());
        assertTrue(response(api.getUser(account.userId(), account.token()), 200, "user.json")
                .body().path("books").isEmpty());
    }

    @org.junit.jupiter.params.ParameterizedTest(name = "Удаление отсутствующей книги, предварительно добавлена: {0}")
    @org.junit.jupiter.params.provider.ValueSource(booleans = {false, true})
    @Tag(TestMetadata.NEGATIVE)
    void absent_or_already_deleted_book_is_rejected(boolean previouslyAdded) {
        String isbn = books.get(0).path("isbn").asText();
        if (previouslyAdded) {
            response(api.addBooks(account.userId(), List.of(isbn), account.token()), 201, "collection.json");
            emptyResponse(api.deleteBook(isbn, account.userId(), account.token()), 204);
        }
        ApiResponse rejected = response(api.deleteBook(isbn, account.userId(), account.token()), 400, "error.json");
        assertFalse(rejected.body().path("message").asText().isBlank());
        assertTrue(response(api.getUser(account.userId(), account.token()), 200, "user.json")
                .body().path("books").isEmpty());
    }

    @Test
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("Пустую коллекцию можно очистить повторно")
    void empty_collection_can_be_cleared_repeatedly() {
        emptyResponse(api.clearBooks(account.userId(), account.token()), 204);
        emptyResponse(api.clearBooks(account.userId(), account.token()), 204);
        assertTrue(response(api.getUser(account.userId(), account.token()), 200, "user.json")
                .body().path("books").isEmpty());
    }

    private JsonNode books;

    @BeforeEach
    void prepareAccountAndCatalog() {
        account = accounts.create();
        ApiResponse catalog = response(api.getBooks(), 200, "book-list.json");
        books = catalog.body().path("books");
        assertTrue(books.isArray() && books.size() >= 2, "В каталоге должны быть минимум две книги");
    }

    @Test
    @Story("Каталог")
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("Детали книги соответствуют записи каталога")
    void book_details_match_catalog() {
        JsonNode catalogBook = books.get(0);

        ApiResponse details = response(
                api.getBook(catalogBook.path("isbn").asText()), 200, "book.json");

        assertAll(
                () -> assertEquals(200, details.statusCode()),
                () -> assertEquals(catalogBook.path("isbn").asText(), details.body().path("isbn").asText()),
                () -> assertEquals(catalogBook.path("title").asText(), details.body().path("title").asText()),
                () -> assertFalse(details.body().path("author").asText().isBlank())
        );
    }

    @Test
    @Story("Коллекция пользователя")
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("Книгу можно добавить и удалить из коллекции")
    void add_and_remove_book() {
        String isbn = books.get(0).path("isbn").asText();

        ApiResponse addition = response(
                api.addBooks(account.userId(), List.of(isbn), account.token()), 201, "collection.json");
        ApiResponse userWithBook = response(
                api.getUser(account.userId(), account.token()), 200, "user.json");
        ApiResponse deletion = emptyResponse(
                api.deleteBook(isbn, account.userId(), account.token()), 204);
        ApiResponse userWithoutBook = response(
                api.getUser(account.userId(), account.token()), 200, "user.json");

        assertAll(
                () -> assertEquals(201, addition.statusCode()),
                () -> assertEquals(isbn, userWithBook.body().path("books").get(0).path("isbn").asText()),
                () -> assertEquals(204, deletion.statusCode()),
                () -> assertTrue(userWithoutBook.body().path("books").isEmpty())
        );
    }

    @Test
    @Story("Коллекция пользователя")
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("Книгу в коллекции можно заменить другой")
    void replace_book() {
        String originalIsbn = books.get(0).path("isbn").asText();
        String replacementIsbn = books.get(1).path("isbn").asText();
        response(api.addBooks(account.userId(), List.of(originalIsbn), account.token()),
                201, "collection.json");

        ApiResponse replacement = response(api.replaceBook(
                originalIsbn, replacementIsbn, account.userId(), account.token()),
                200, "collection.json");

        assertAll(
                () -> assertEquals(200, replacement.statusCode()),
                () -> assertEquals(replacementIsbn,
                        replacement.body().path("books").get(0).path("isbn").asText())
        );
    }

    @Test
    @Story("Коллекция пользователя")
    @Tag(TestMetadata.NEGATIVE)
    @DisplayName("Одна книга не добавляется в коллекцию дважды")
    void duplicate_book_is_rejected() {
        String isbn = books.get(0).path("isbn").asText();
        response(api.addBooks(account.userId(), List.of(isbn), account.token()),
                201, "collection.json");

        ApiResponse duplicate = response(
                api.addBooks(account.userId(), List.of(isbn), account.token()), 400, "error.json");

        assertAll(
                () -> assertEquals(400, duplicate.statusCode()),
                () -> assertTrue(duplicate.body().path("message").asText()
                        .contains(ConfigProvider.BOOK_API_DUPLICATE_MESSAGE))
        );
    }

    @Test
    @Story("Авторизация коллекции")
    @Tag(TestMetadata.NEGATIVE)
    @DisplayName("Книгу нельзя добавить без Bearer token")
    void adding_book_requires_token() {
        String isbn = books.get(0).path("isbn").asText();

        ApiResponse response = response(
                api.addBooks(account.userId(), List.of(isbn), null), 401, "error.json");

        assertEquals(401, response.statusCode());
    }

    @Test
    @Story("Каталог и коллекция")
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("API возвращает список книг магазина и пустой список профиля")
    void store_and_profile_book_lists_are_available() {
        ApiResponse catalog = response(api.getBooks(), 200, "book-list.json");
        ApiResponse profile = response(
                api.getUser(account.userId(), account.token()), 200, "user.json");

        assertAll(
                () -> assertEquals(200, catalog.statusCode()),
                () -> assertTrue(catalog.body().path("books").size() >= 2),
                () -> assertEquals(200, profile.statusCode()),
                () -> assertTrue(profile.body().path("books").isArray()),
                () -> assertTrue(profile.body().path("books").isEmpty())
        );
    }

    @Test
    @Story("Коллекция пользователя")
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("Несколько книг можно добавить и удалить по одной через API")
    void add_and_remove_multiple_books() {
        List<String> isbns = List.of(
                books.get(0).path("isbn").asText(),
                books.get(1).path("isbn").asText());

        ApiResponse addition = response(
                api.addBooks(account.userId(), isbns, account.token()), 201, "collection.json");
        ApiResponse populated = response(
                api.getUser(account.userId(), account.token()), 200, "user.json");
        ApiResponse firstDeletion = emptyResponse(
                api.deleteBook(isbns.get(0), account.userId(), account.token()), 204);
        ApiResponse secondDeletion = emptyResponse(
                api.deleteBook(isbns.get(1), account.userId(), account.token()), 204);
        ApiResponse empty = response(
                api.getUser(account.userId(), account.token()), 200, "user.json");

        assertAll(
                () -> assertEquals(201, addition.statusCode()),
                () -> assertEquals(2, populated.body().path("books").size()),
                () -> assertEquals(204, firstDeletion.statusCode()),
                () -> assertEquals(204, secondDeletion.statusCode()),
                () -> assertTrue(empty.body().path("books").isEmpty())
        );
    }

    @Test
    @Story("Коллекция пользователя")
    @Tag(TestMetadata.POSITIVE)
    @DisplayName("Все книги профиля можно удалить одним API-запросом")
    void clear_all_profile_books() {
        List<String> isbns = List.of(
                books.get(0).path("isbn").asText(),
                books.get(1).path("isbn").asText());
        response(api.addBooks(account.userId(), isbns, account.token()), 201, "collection.json");

        ApiResponse clearing = emptyResponse(
                api.clearBooks(account.userId(), account.token()), 204);
        ApiResponse profile = response(
                api.getUser(account.userId(), account.token()), 200, "user.json");

        assertAll(
                () -> assertEquals(204, clearing.statusCode()),
                () -> assertTrue(profile.body().path("books").isEmpty())
        );
    }
}
