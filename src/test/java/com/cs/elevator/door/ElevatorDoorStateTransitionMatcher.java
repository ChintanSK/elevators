package com.cs.elevator.door;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsEqual;

public final class ElevatorDoorStateTransitionMatcher extends TypeSafeMatcher<ElevatorDoor.ElevatorDoorStateChangeEvent> {

    private final Matcher<ElevatorDoor.ElevatorDoorStates> oldStateMatcher;
    private final Matcher<ElevatorDoor.ElevatorDoorStates> newStateMatcher;

    public ElevatorDoorStateTransitionMatcher(Matcher<ElevatorDoor.ElevatorDoorStates> oldStateMatcher, Matcher<ElevatorDoor.ElevatorDoorStates> newStateMatcher) {
        this.oldStateMatcher = oldStateMatcher;
        this.newStateMatcher = newStateMatcher;
    }

    public static ElevatorDoorStateTransitionMatcher transitioning(Matcher<ElevatorDoor.ElevatorDoorStates> oldStateMatcher, Matcher<ElevatorDoor.ElevatorDoorStates> newStateMatcher) {
        return new ElevatorDoorStateTransitionMatcher(oldStateMatcher, newStateMatcher);
    }

    public static Matcher<ElevatorDoor.ElevatorDoorStates> from(ElevatorDoor.ElevatorDoorStates oldState) {
        return new IsEqual<>(oldState);
    }

    public static Matcher<ElevatorDoor.ElevatorDoorStates> to(ElevatorDoor.ElevatorDoorStates newState) {
        return new IsEqual<>(newState);
    }

    @Override
    protected boolean matchesSafely(ElevatorDoor.ElevatorDoorStateChangeEvent actualEvent) {
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
