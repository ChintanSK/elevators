package com.cs.elevator;

import static com.cs.elevator.ElevatorState.ElevatorStates.STATIONARY;

public class ElevatorState {

    private ElevatorStates currentState = STATIONARY;

    public void changeTo(ElevatorStates newState) {
        currentState = newState;
    }

    public enum ElevatorStates {
        STATIONARY, MOVING_UP, MOVING_DOWN, SLOWING
    }

    public ElevatorStates currentState() {
        return currentState;
    }

    public final boolean isStationary() {
        return currentState.equals(STATIONARY);
    }

    public final boolean isMoving() {
        return !currentState.equals(STATIONARY);
    }
}
