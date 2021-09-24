package com.cs.elevator;

import com.cs.elevator.door.ElevatorDoor;

import static com.cs.elevator.Elevator.ElevatorStates.MOVING;
import static com.cs.elevator.Elevator.ElevatorStates.STATIONARY;

public class Elevator {
    public final ElevatorState state = new ElevatorState(STATIONARY);
    public final ElevatorDoor door;

    public Elevator() {
        this.door = new ElevatorDoor();
    }

    public void makeStationary() {
        state.changeTo(STATIONARY);
    }

    public void makeMoving() {
        state.changeTo(MOVING);
    }

    public final boolean isStationary() {
        return state.currentState.equals(STATIONARY);
    }

    public final boolean isMoving() {
        return state.currentState.equals(MOVING);
    }

    public enum ElevatorStates {
        STATIONARY, MOVING
    }

    private static class ElevatorState {

        private ElevatorStates currentState;

        public ElevatorState(ElevatorStates initialState) {
            currentState = initialState;
        }

        public void changeTo(ElevatorStates newState) {
            currentState = newState;
        }
    }
}
