package io.github.seecret1.volgait.tests;

import io.github.seecret1.volgait.constants.TestMetadata;

import io.github.seecret1.volgait.components.PopupComponent;
import io.github.seecret1.volgait.pages.AdsPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.NoAlertPresentException;

import static org.junit.jupiter.api.Assertions.*;

@Epic(TestMetadata.EPIC)
@Feature("Рекламное окно")
@DisplayName("Страница «Реклама»")
class AdsTests extends BaseUiTest {

    private AdsPage ads;

    @BeforeEach
    void openAdsPage() {
        open(URL_ADS);
        ads = new AdsPage(driver);
    }

    @Nested
    @Story("Позитивные сценарии рекламного окна")
    @Tag(TestMetadata.POSITIVE)
    class Positive {

        @Test
        @DisplayName("P01 — на странице отображается заголовок «Ads»")
        void page_has_expected_heading() {
            assertEquals(ADS_HEADING, ads.heading());
        }

        @Test
        @DisplayName("P02 — на странице описан обратный отсчёт до рекламы")
        void page_has_countdown_copy() {
            assertTrue(ads.countdownText().matches(ADS_COUNTDOWN_REGEX));
        }

        @Test
        @Severity(SeverityLevel.BLOCKER)
        @DisplayName("P03 — реклама автоматически появляется после задержки")
        void ad_appears_automatically() {
            assertTrue(ads.waitForAd().isVisible());
        }

        @Test
        @DisplayName("P04 — рекламное окно содержит ожидаемый заголовок")
        void ad_has_expected_title() {
            assertEquals(ADS_TITLE, ads.waitForAd().title());
        }

        @Test
        @DisplayName("P05 — рекламное окно содержит ожидаемое сообщение")
        void ad_has_expected_message() {
            assertTrue(ads.waitForAd().content().contains(ADS_MESSAGE));
        }

        @Test
        @DisplayName("P06 — рекламное окно имеет семантическую роль диалога")
        void ad_has_dialog_role() {
            assertEquals(ADS_ROLE, ads.waitForAd().role());
        }

        @Test
        @DisplayName("P07 — в рекламном окне отображается кнопка закрытия")
        void ad_has_close_control() {
            assertTrue(ads.waitForAd().closeButtonIsDisplayed());
        }

        @Test
        @Severity(SeverityLevel.CRITICAL)
        @DisplayName("P08 — кнопка закрытия скрывает рекламу")
        void close_control_hides_ad() {
            PopupComponent ad = ads.waitForAd();
            ad.close();
            assertFalse(ad.isVisible());
        }
    }

    @Nested
    @Story("Негативные сценарии рекламного окна")
    @Tag(TestMetadata.NEGATIVE)
    class Negative {
        @Test
        @DisplayName("N01 — реклама не отображается до завершения таймера")
        void ad_is_not_visible_immediately() {
            assertFalse(ads.ad().isVisible());
        }

        @Test
        @DisplayName("N02 — клавиша Escape не закрывает рекламное окно")
        void escape_does_not_dismiss_ad() {
            PopupComponent ad = ads.waitForAd();
            ad.pressEscape();
            assertTrue(ad.isVisible());
        }

        @Test
        @DisplayName("N03 — рекламное окно не является системным диалогом браузера")
        void ad_is_not_a_browser_alert() {
            ads.waitForAd();
            assertThrows(NoAlertPresentException.class, () -> driver.switchTo().alert());
        }
    }
}
