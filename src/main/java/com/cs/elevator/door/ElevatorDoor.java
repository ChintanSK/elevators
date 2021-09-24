package com.cs.elevator.door;

import java.util.ArrayList;
import java.util.List;

import static com.cs.elevator.door.ElevatorDoor.ElevatorDoorStates.*;

public class ElevatorDoor {
    private final List<ElevatorDoorEventListener> elevatorDoorEventListeners = new ArrayList<>();
    private final ElevatorDoorState state = new ElevatorDoorState(CLOSED);

    public void registerElevatorDoorEventListener(ElevatorDoorEventListener elevatorDoorEventListener) {
        elevatorDoorEventListeners.add(elevatorDoorEventListener);
    }

    public void changeStateTo(ElevatorDoorStates newState) {
        emitStateChangeEvent(new ElevatorDoorStateChangeEvent(state.currentState, newState));
        state.changeTo(newState);
    }

    private void emitStateChangeEvent(ElevatorDoorStateChangeEvent stateChangeEvent) {
        elevatorDoorEventListeners.forEach(listener -> listener.onDoorStatusChange(stateChangeEvent));
    }

    public boolean isOpening() {
        return state.currentState.equals(OPENING);
    }

    public boolean isOpen() {
        return state.currentState.equals(OPEN);
    }

    public boolean isClosing() {
        return state.currentState.equals(CLOSING);
    }

    public boolean isClosed() {
        return state.currentState.equals(CLOSED);
    }

    public enum ElevatorDoorStates {
        OPEN, OPENING, CLOSED, CLOSING
    }

    public static class ElevatorDoorState {
        private ElevatorDoorStates currentState;

        public ElevatorDoorState(ElevatorDoorStates initialState) {
            currentState = initialState;
        }

        public void changeTo(ElevatorDoorStates newState) {
            currentState = newState;
        }
    }

    public static class ElevatorDoorStateChangeEvent {
        public final ElevatorDoorStates oldState;
        public final ElevatorDoorStates newState;

        public ElevatorDoorStateChangeEvent(ElevatorDoorStates oldState, ElevatorDoorStates newState) {
            this.oldState = oldState;
            this.newState = newState;
        }
    }
}
