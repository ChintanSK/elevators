package com.cs.elevator.door;

import com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStateChangeEvent;
import com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStates;
import com.cs.elevator.door.hardware.ElevatorHardwareAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ElevatorDoor {
    private final List<ElevatorDoorEventListener> elevatorDoorEventListeners = new ArrayList<>();
    private final ElevatorDoorState currentState = new ElevatorDoorState();
    private final ElevatorHardwareAdapter.DoorCommands doorHardwareCommands;

    public ElevatorDoor(ElevatorHardwareAdapter.DoorCommands doorHardwareCommands) {
        this.doorHardwareCommands = Objects.requireNonNull(doorHardwareCommands, "The hardware adapter taking commands from ElevatorDoor cannot be null");
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
        doorHardwareCommands.close();
    }

    public void open() {
        doorHardwareCommands.open();
    }

}
