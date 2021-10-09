package com.cs.elevator.util;

import org.awaitility.Durations;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.time.Duration;
import java.util.concurrent.Callable;

import static org.awaitility.Awaitility.with;

public class AsyncMatcher<T> extends TypeSafeMatcher<Callable<T>> {
    private final Matcher<T> expectedValueMatcher;
    private Duration duration = Durations.ONE_HUNDRED_MILLISECONDS;

    private AsyncMatcher(Matcher<T> expectedValueMatcher) {
        this.expectedValueMatcher = expectedValueMatcher;
    }

    public static <T> AsyncMatcher<T> eventually(Matcher<T> expectedValueMatcher) {
        return new AsyncMatcher<>(expectedValueMatcher);
    }

    @Override
    protected boolean matchesSafely(Callable<T> condition) {
        with().pollInterval(duration)
                .await().until(condition, expectedValueMatcher);
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendDescriptionOf(expectedValueMatcher);
    }
}
