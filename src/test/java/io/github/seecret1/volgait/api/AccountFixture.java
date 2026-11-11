package io.github.seecret1.volgait.api;

import io.github.seecret1.volgait.config.ConfigProvider;

import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class AccountFixture {

    private final ApiClient api;
    private final Set<TestAccount> createdAccounts = ConcurrentHashMap.newKeySet();

    public AccountFixture(ApiClient api) {
        this.api = api;
    }

    public TestAccount create() {
        return create(uniqueUserName(), ConfigProvider.GENERATED_USER_PASSWORD);
    }

    public TestAccount create(String userName, String password) {
        ApiResponse createResponse = api.createUser(userName, password);
        String userId = createResponse.body().path("userID").asText(
                createResponse.body().path("userId").asText());
        TestAccount provisionalAccount = new TestAccount(
                userId, userName, password, "");
        if (createResponse.statusCode() == 201 && !userId.isBlank()) {
            createdAccounts.add(provisionalAccount);
        }
        ApiResponseValidator.verify(createResponse, 201, "created-user.json");

        ApiResponse tokenResponse = ApiResponseValidator.verify(
                api.generateToken(userName, password), 200, "token.json");
        String token = tokenResponse.body().path("token").asText();
        if (tokenResponse.statusCode() != 200 || token.isBlank()) {
            throw new IllegalStateException("Cannot generate test token: " + tokenResponse.rawBody());
        }

        TestAccount account = new TestAccount(
                userId, userName, password, token);
        createdAccounts.remove(provisionalAccount);
        createdAccounts.add(account);
        return account;
    }

    public void deleteIfPresent(TestAccount account) {
        if (account == null || account.userId().isBlank()) {
            return;
        }
        ApiResponse tokenResponse = ApiResponseValidator.verify(
                api.generateToken(account.userName(), account.password()), 200, "token.json");
        String cleanupToken = tokenResponse.body().path("token").asText();
        if (tokenResponse.statusCode() == 200 && !cleanupToken.isBlank()) {
            ApiResponse deletion = api.deleteUser(account.userId(), cleanupToken);
            if (deletion.statusCode() == 204) {
                ApiResponseValidator.verifyEmpty(deletion, 204);
                createdAccounts.remove(account);
                return;
            }
            ApiResponseValidator.verify(deletion, 200, "error.json");
        }

        ApiResponseValidator.verify(
                api.authorized(account.userName(), account.password()), 404, "error.json");
        createdAccounts.remove(account);
    }

    public void deleteAll() {
        for (TestAccount account : createdAccounts.toArray(TestAccount[]::new)) {
            deleteIfPresent(account);
        }
    }

    public static String uniqueUserName() {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        return ConfigProvider.GENERATED_USER_PREFIX
                + suffix.substring(0, ConfigProvider.GENERATED_USER_SUFFIX_LENGTH);
    }
}
