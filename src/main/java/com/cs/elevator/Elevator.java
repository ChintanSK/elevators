package com.cs.elevator;

import com.cs.elevator.door.ElevatorDoor;
import com.cs.util.StateChangeEvent;

import java.util.ArrayList;
import java.util.List;

import static com.cs.elevator.Elevator.ElevatorStates.*;

public class Elevator {
    private final List<ElevatorEventListener> elevatorEventListeners = new ArrayList<>();
    public final ElevatorState state = new ElevatorState(STATIONARY);
    public final ElevatorDoor door;

    public Elevator() {
        this.door = new ElevatorDoor();
    }

    public void registerElevatorStateChangeListener(ElevatorEventListener listener) {
        elevatorEventListeners.add(listener);
    }

    public void makeStationary() {
        changeStateTo(STATIONARY);
    }

    public void makeMoving() {
        changeStateTo(MOVING);
    }

    public void makeServing() {
        changeStateTo(SERVING);
    }

    private void changeStateTo(ElevatorStates newState) {
        ElevatorStates oldState = state.currentState;
        state.changeTo(newState);
        emitStateChangeEvent(oldState, newState);
    }

    private void emitStateChangeEvent(ElevatorStates oldState, ElevatorStates newState) {
        if (oldState != newState) {
            ElevatorStateChangeEvent stateChangeEvent = new ElevatorStateChangeEvent(oldState, newState);
            elevatorEventListeners.forEach(listener -> listener.onElevatorStatusChange(stateChangeEvent));
        }
    }

    public final boolean isStationary() {
        return state.currentState.equals(STATIONARY);
    }

    public final boolean isMoving() {
        return state.currentState.equals(MOVING);
    }

    public final boolean isServing() {
        return state.currentState.equals(SERVING);
    }

    public ElevatorStates currentState() {
        return state.currentState;
    }

    public enum ElevatorStates {
        STATIONARY, MOVING, SERVING
    }

    public static class ElevatorState {

        private ElevatorStates currentState;

        public ElevatorState(ElevatorStates initialState) {
            currentState = initialState;
        }

        public void changeTo(ElevatorStates newState) {
            currentState = newState;
        }
    }

    public static class ElevatorStateChangeEvent implements StateChangeEvent<ElevatorStates> {
        private final ElevatorStates oldState;
        private final ElevatorStates newState;

        public ElevatorStateChangeEvent(ElevatorStates oldState, ElevatorStates newState) {
            this.oldState = oldState;
            this.newState = newState;
        }

        @Override
        public ElevatorStates oldState() {
            return oldState;
        }

        @Override
        public ElevatorStates newState() {
            return newState;
        }
    }
}
