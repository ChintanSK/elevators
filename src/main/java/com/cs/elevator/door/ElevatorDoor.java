package com.cs.elevator.door;

import com.cs.elevator.util.StateChangeEvent;

import java.util.ArrayList;
import java.util.List;

import static com.cs.elevator.door.ElevatorDoor.ElevatorDoorStates.*;

public class ElevatorDoor {
    private final List<ElevatorDoorEventListener> elevatorDoorEventListeners = new ArrayList<>();
    private final ElevatorDoorState state = new ElevatorDoorState(CLOSED);

    public ElevatorDoorStates currentState() {
        return state.currentState;
    }

    public void registerElevatorDoorEventListener(ElevatorDoorEventListener elevatorDoorEventListener) {
        elevatorDoorEventListeners.add(elevatorDoorEventListener);
    }

    public void changeStateTo(ElevatorDoorStates newState) {
        ElevatorDoorStates oldState = state.currentState;
        state.changeTo(newState);
        emitStateChangeEvent(oldState, newState);
    }

    private void emitStateChangeEvent(ElevatorDoorStates oldState, ElevatorDoorStates newState) {
        ElevatorDoorStateChangeEvent stateChangeEvent = new ElevatorDoorStateChangeEvent(oldState, newState);
        elevatorDoorEventListeners.forEach(listener -> listener.onDoorStatusChange(stateChangeEvent));
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

    public static class ElevatorDoorStateChangeEvent implements StateChangeEvent<ElevatorDoorStates> {
        private final ElevatorDoorStates oldState;
        private final ElevatorDoorStates newState;

        public ElevatorDoorStateChangeEvent(ElevatorDoorStates oldState, ElevatorDoorStates newState) {
            this.oldState = oldState;
            this.newState = newState;
        }

        @Override
        public ElevatorDoorStates oldState() {
            return oldState;
        }

        @Override
        public ElevatorDoorStates newState() {
            return newState;
        }
    }
}
