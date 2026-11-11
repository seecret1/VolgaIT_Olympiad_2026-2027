package io.github.seecret1.volgait.tests.api;

import io.github.seecret1.volgait.api.AccountFixture;
import io.github.seecret1.volgait.api.ApiClient;
import io.github.seecret1.volgait.api.ApiResponse;
import io.github.seecret1.volgait.api.TestAccount;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static io.github.seecret1.volgait.api.ApiResponseValidator.verify;
import static io.github.seecret1.volgait.api.ApiResponseValidator.verifyEmpty;

abstract class BaseApiTest {

    protected ApiClient api;

    protected AccountFixture accounts;

    protected TestAccount account;

    @BeforeEach
    void initializeApiClient() {
        api = new ApiClient();
        accounts = new AccountFixture(api);
    }

    @AfterEach
    void cleanUpAccount() {
        accounts.deleteAll();
        account = null;
    }

    protected ApiResponse response(ApiResponse response, int status, String schemaFile) {
        return verify(response, status, schemaFile);
    }

    protected ApiResponse emptyResponse(ApiResponse response, int status) {
        return verifyEmpty(response, status);
    }
}
