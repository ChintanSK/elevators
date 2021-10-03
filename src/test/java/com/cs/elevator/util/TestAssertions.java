package com.cs.elevator.util;

import org.hamcrest.Matcher;

import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.Matchers.is;

public interface TestAssertions {


    static void waitUntil(Callable<Boolean> conditionSupplier) {
        waitUntil(conditionSupplier, is(true));
    }

    static <T> void waitUntil(Callable<T> conditionSupplier, Matcher<T> conditionResultMatcher) {
        with().pollInterval(1, SECONDS)
                .await().until(conditionSupplier, conditionResultMatcher);
    }

    void elevatorIsMovingUp();

    void elevatorDirectionIsUp();

    void elevatorIsMovingDown();

    void elevatorDirectionIsDown();

    void elevatorIsServingAtStorey(String storeyCode);

    void elevatorIsStationary();

    void currentStoreyIs(String storeyCode);
}
