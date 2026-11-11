package io.github.seecret1.volgait.api;

import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import io.qameta.allure.Allure;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ApiResponseValidator {

    private static final String SCHEMA_ROOT = "classpath:schemas/api/";

    private static final SchemaRegistry SCHEMA_REGISTRY = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);

    private static final Map<String, Schema> SCHEMAS = new ConcurrentHashMap<>();

    private ApiResponseValidator() { }

    public static ApiResponse verify(ApiResponse response, int expectedStatus, String schemaFile) {
        Allure.addAttachment("API response: HTTP " + response.statusCode(), "application/json",
                response.rawBody().isBlank() ? "<empty body>" : response.rawBody(), ".json");

        assertAll(
                () -> assertEquals(expectedStatus, response.statusCode(), response.rawBody()),
                () -> validateBody(response, schemaFile)
        );
        return response;
    }

    public static ApiResponse verifyEmpty(ApiResponse response, int expectedStatus) {
        Allure.addAttachment("API response: HTTP " + response.statusCode(), "text/plain",
                response.rawBody().isBlank() ? "<empty body>" : response.rawBody(), ".txt");

        assertAll(
                () -> assertEquals(expectedStatus, response.statusCode(), response.rawBody()),
                () -> assertTrue(response.rawBody().isBlank(),
                        () -> "Ожидалось пустое тело ответа, получено: " + response.rawBody())
        );
        return response;
    }

    private static void validateBody(ApiResponse response, String schemaFile) {
        assertTrue(!response.rawBody().isBlank(), "JSON-тело ответа не должно быть пустым");
        Schema schema = SCHEMAS.computeIfAbsent(schemaFile, ApiResponseValidator::loadSchema);
        List<com.networknt.schema.Error> errors = schema.validate(response.rawBody(), InputFormat.JSON);
        assertTrue(errors.isEmpty(),
                () -> "Ответ не соответствует JSON Schema " + schemaFile + ": " + errors);
    }

    private static Schema loadSchema(String schemaFile) {
        Schema schema = SCHEMA_REGISTRY.getSchema(SchemaLocation.of(SCHEMA_ROOT + schemaFile));
        schema.initializeValidators();
        return schema;
    }
}
