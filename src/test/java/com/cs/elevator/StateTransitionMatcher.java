package com.cs.elevator;

import com.cs.util.StateChangeEvent;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsEqual;

public final class StateTransitionMatcher<T> extends TypeSafeMatcher<StateChangeEvent<T>> {

    private final Matcher<T> oldStateMatcher;
    private final Matcher<T> newStateMatcher;

    public StateTransitionMatcher(Matcher<T> oldStateMatcher, Matcher<T> newStateMatcher) {
        this.oldStateMatcher = oldStateMatcher;
        this.newStateMatcher = newStateMatcher;
    }

    public static <T> StateTransitionMatcher<T> transitioning(Matcher<T> oldStateMatcher, Matcher<T> newStateMatcher) {
        return new StateTransitionMatcher<>(oldStateMatcher, newStateMatcher);
    }

    public static <T> Matcher<T> from(T oldState) {
        return new IsEqual<>(oldState);
    }

    public static <T> Matcher<T> to(T newState) {
        return new IsEqual<>(newState);
    }

    @Override
    protected boolean matchesSafely(StateChangeEvent<T> actualEvent) {
        return oldStateMatcher.matches(actualEvent.oldState()) && newStateMatcher.matches(actualEvent.newState());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("State Change from ");
        oldStateMatcher.describeTo(description);
        description.appendText(" to ");
        newStateMatcher.describeTo(description);
    }
}
