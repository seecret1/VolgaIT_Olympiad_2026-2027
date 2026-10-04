package io.github.seecret1.volgait.tests;

import io.github.seecret1.volgait.components.NavigationComponent;
import io.github.seecret1.volgait.constants.TestMetadata;
import io.github.seecret1.volgait.utils.NavigationTestData;
import io.github.seecret1.volgait.utils.NavigationTestData.ExercisePage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.WebDriverException;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic(TestMetadata.EPIC)
@Feature("Общая навигация")
@DisplayName("Навигация страниц упражнений")
@Tag(TestMetadata.POSITIVE)
class NavigationTests extends BaseUiTest {

    private static final String NEW_TAB_TARGET = "_blank";

    @ParameterizedTest(name = "{0} — кнопка Blog открывает сайт automateNow")
    @MethodSource(NavigationTestData.EXERCISE_PAGES_SOURCE)
    @Story("Переход в Blog")
    @Severity(SeverityLevel.NORMAL)
    void blog_link_opens_automatenow(ExercisePage page) {
        open(page.url());
        NavigationComponent navigation = new NavigationComponent(driver);

        assertEquals(URL_BLOG, navigation.blogHref());
        assertEquals(hostOf(URL_BLOG), hostOf(navigation.openBlog()));
    }

    @ParameterizedTest(name = "{0} — ссылка Home открывает главную страницу")
    @MethodSource(NavigationTestData.EXERCISE_PAGES_SOURCE)
    @Story("Переход на главную страницу")
    @Severity(SeverityLevel.CRITICAL)
    void home_link_opens_main_page(ExercisePage page) {
        open(page.url());
        NavigationComponent navigation = new NavigationComponent(driver);

        assertEquals(URL_HOME, navigation.homeHref());
        assertEquals(URL_HOME, navigation.openHome());
    }

    @ParameterizedTest(name = "{0} — ссылка открывает указанный YouTube-ролик")
    @MethodSource(NavigationTestData.EXERCISE_PAGES_SOURCE)
    @Story("Переход к обучающему ролику")
    @Severity(SeverityLevel.NORMAL)
    void youtube_link_opens_video(ExercisePage page) {
        open(page.url());
        NavigationComponent navigation = new NavigationComponent(driver);

        assertAll(
                () -> assertEquals(page.youtubeUrl(), navigation.youtubeHref()),
                () -> assertEquals(NEW_TAB_TARGET, navigation.youtubeTarget())
        );
        assertEquals(hostOf(page.youtubeUrl()), hostOf(navigation.openYoutube()));

        try {
            navigation.waitForYoutubeVideo();
        } catch (WebDriverException exception) {
            Assumptions.assumeTrue(false,
                    () -> "YouTube-ролик недоступен во внешнем сервисе: " + exception.getMessage());
        }
    }

    private static String hostOf(String url) {
        return URI.create(url).getHost();
    }
}
