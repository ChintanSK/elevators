package com.cs.elevator.door;

import com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStateChangeEvent;
import com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStates;
import com.cs.elevator.door.hardware.ElevatorDoorHardwareAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ElevatorDoor {
    private final List<ElevatorDoorEventListener> elevatorDoorEventListeners = new ArrayList<>();
    private final ElevatorDoorState currentState = new ElevatorDoorState();
    private final ElevatorDoorHardwareAdapter.Commands elevatorDoorHardwareCommands;

    public ElevatorDoor(ElevatorDoorHardwareAdapter.Commands elevatorDoorHardwareCommands) {
        this.elevatorDoorHardwareCommands = Objects.requireNonNull(elevatorDoorHardwareCommands, "The hardware adapter taking commands from ElevatorDoor cannot be null");
    }

    public final boolean registerElevatorDoorEventListener(ElevatorDoorEventListener elevatorDoorEventListener) {
        return elevatorDoorEventListeners.add(elevatorDoorEventListener);
    }

    public void changeStateTo(ElevatorDoorStates newState) {
        emitStateChangeEvent(currentState.changeTo(newState));
    }

    private void emitStateChangeEvent(ElevatorDoorStateChangeEvent stateChangeEvent) {
        elevatorDoorEventListeners.forEach(listener -> listener.onDoorStatusChange(stateChangeEvent));
    }

    public boolean isOpen() {
        return currentState.isOpen();
    }

    public boolean isClosing() {
        return currentState.isClosing();
    }

    public boolean isClosed() {
        return currentState.isClosed();
    }

    public ElevatorDoorStates currentState() {
        return currentState.currentState();
    }

    public void close() {
        elevatorDoorHardwareCommands.close();
    }

    public void open() {
        elevatorDoorHardwareCommands.open();
    }

}
