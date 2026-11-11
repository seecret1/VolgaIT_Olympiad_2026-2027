package io.github.seecret1.volgait.tests.ui;

import org.junit.jupiter.api.Test;
import org.opentest4j.MultipleFailuresError;
import org.opentest4j.TestAbortedException;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AlertAssertionsTest {
    @Test
    void missing_alert_skips_after_remaining_checks() {
        AtomicBoolean checked = new AtomicBoolean();
        assertThrows(TestAbortedException.class, () -> AlertAssertions.assertAll(
                () -> { throw new TestAbortedException("Missing alert"); },
                () -> checked.set(true)));
        assertTrue(checked.get());
    }

    @Test
    void missing_alert_does_not_hide_other_failures() {
        assertThrows(MultipleFailuresError.class, () -> AlertAssertions.assertAll(
                () -> { throw new TestAbortedException("Missing alert"); },
                () -> fail("API check failed")));
    }

    @Test
    void incorrect_alert_text_still_fails() {
        assertThrows(MultipleFailuresError.class, () -> AlertAssertions.assertAll(
                () -> assertEquals("Expected alert", "Wrong alert")));
    }

    @Test
    void successful_checks_pass() {
        assertDoesNotThrow(() -> AlertAssertions.assertAll(
                () -> assertEquals("Expected alert", "Expected alert")));
    }
}
