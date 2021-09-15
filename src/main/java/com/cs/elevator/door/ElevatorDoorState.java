package com.cs.elevator.door;

import static com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStates.*;

public class ElevatorDoorState {

    private ElevatorDoorStates currentState = CLOSED;

    public ElevatorDoorStates currentState() {
        return currentState;
    }

    public ElevatorDoorStateChangeEvent changeTo(ElevatorDoorStates newState) {
        ElevatorDoorStateChangeEvent elevatorDoorStateChangeEvent = new ElevatorDoorStateChangeEvent(currentState, newState);
        currentState = newState;
        return elevatorDoorStateChangeEvent;
    }

    public boolean isOpen() {
        return currentState.equals(OPEN);
    }

    public boolean isClosing() {
        return currentState.equals(CLOSING);
    }

    public boolean isClosed() {
        return currentState.equals(CLOSED);
    }

    public enum ElevatorDoorStates {
        OPEN, OPENING, CLOSED, CLOSING
    }

    public static class ElevatorDoorStateChangeEvent {
        public final ElevatorDoorStates oldState;
        public final ElevatorDoorStates newState;

        public ElevatorDoorStateChangeEvent(ElevatorDoorStates oldState, ElevatorDoorStates newState) {
            this.oldState = oldState;
            this.newState = newState;
        }

        @Override
        public String toString() {
            return "State Change" + " from " + oldState + " to " + newState;
        }
    }
}
