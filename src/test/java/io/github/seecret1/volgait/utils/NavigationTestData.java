package io.github.seecret1.volgait.utils;

import io.github.seecret1.volgait.config.ConfigProvider;

import java.util.List;
import java.util.stream.Stream;

public final class NavigationTestData implements ConfigProvider {

    public static final String EXERCISE_PAGES_SOURCE =
            "io.github.seecret1.volgait.utils.NavigationTestData#exercisePages";

    private static final String CALENDARS_NAME = "Calendars";
    private static final String MODALS_NAME = "Modals";
    private static final String ADS_NAME = "Ads";

    private static final List<ExercisePage> EXERCISE_PAGES = List.of(
            new ExercisePage(CALENDARS_NAME, URL_CALENDARS, URL_YOUTUBE_CALENDARS),
            new ExercisePage(MODALS_NAME, URL_MODALS, URL_YOUTUBE_MODALS),
            new ExercisePage(ADS_NAME, URL_ADS, URL_YOUTUBE_ADS)
    );

    private NavigationTestData() {
    }

    public static Stream<ExercisePage> exercisePages() {
        return EXERCISE_PAGES.stream();
    }

    public record ExercisePage(String name, String url, String youtubeUrl) {
        @Override
        public String toString() {
            return name;
        }
    }
}
