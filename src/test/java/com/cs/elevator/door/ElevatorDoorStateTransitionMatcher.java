package com.cs.elevator.door;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsEqual;

public final class ElevatorDoorStateTransitionMatcher extends TypeSafeMatcher<ElevatorDoorState.ElevatorDoorStateChangeEvent> {

    private final Matcher<ElevatorDoorState.ElevatorDoorStates> oldStateMatcher;
    private final Matcher<ElevatorDoorState.ElevatorDoorStates> newStateMatcher;

    public ElevatorDoorStateTransitionMatcher(Matcher<ElevatorDoorState.ElevatorDoorStates> oldStateMatcher, Matcher<ElevatorDoorState.ElevatorDoorStates> newStateMatcher) {
        this.oldStateMatcher = oldStateMatcher;
        this.newStateMatcher = newStateMatcher;
    }

    public static ElevatorDoorStateTransitionMatcher transitioning(Matcher<ElevatorDoorState.ElevatorDoorStates> oldStateMatcher, Matcher<ElevatorDoorState.ElevatorDoorStates> newStateMatcher) {
        return new ElevatorDoorStateTransitionMatcher(oldStateMatcher, newStateMatcher);
    }

    public static Matcher<ElevatorDoorState.ElevatorDoorStates> from(ElevatorDoorState.ElevatorDoorStates oldState) {
        return new IsEqual<>(oldState);
    }

    public static Matcher<ElevatorDoorState.ElevatorDoorStates> to(ElevatorDoorState.ElevatorDoorStates newState) {
        return new IsEqual<>(newState);
    }

    @Override
    protected boolean matchesSafely(ElevatorDoorState.ElevatorDoorStateChangeEvent actualEvent) {
        return oldStateMatcher.matches(actualEvent.oldState) && newStateMatcher.matches(actualEvent.newState);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("State Change from ");
        oldStateMatcher.describeTo(description);
        description.appendText(" to ");
        newStateMatcher.describeTo(description);
    }
}
