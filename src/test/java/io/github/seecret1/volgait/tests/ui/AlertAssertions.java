package io.github.seecret1.volgait.tests.ui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.opentest4j.TestAbortedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/** Runs all checks before reporting missing alerts as skipped, without hiding real failures. */
final class AlertAssertions {
    private AlertAssertions() { }

    static void assertAll(Executable... checks) {
        assertAll(Arrays.stream(checks));
    }

    static void assertAll(Stream<Executable> checks) {
        List<TestAbortedException> skipped = new ArrayList<>();
        Assertions.assertAll(checks.map(check -> (Executable) () -> {
            try {
                check.execute();
            } catch (TestAbortedException exception) {
                skipped.add(exception);
            }
        }));
        if (!skipped.isEmpty()) {
            throw new TestAbortedException(skipped.stream()
                    .map(Throwable::getMessage).collect(java.util.stream.Collectors.joining("; ")));
        }
    }
}
