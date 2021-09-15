package com.cs.elevator;

import com.cs.elevator.door.ElevatorDoor;
import com.cs.elevator.door.ElevatorDoorEventListener;
import com.cs.elevator.door.ElevatorDoorState;
import com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStates;
import com.cs.elevator.door.hardware.ElevatorDoorControlPanel;
import com.cs.elevator.door.hardware.ElevatorDoorHardwareAdapter;

import static com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStates.*;

public class Elevator implements ElevatorDoorControlPanel, ElevatorDoorHardwareAdapter.Signals {

    private final ElevatorDoor door;

    public Elevator(ElevatorDoorHardwareAdapter.Commands hardwareCommands) {
        this.door = new ElevatorDoor(hardwareCommands);
    }

    public void registerElevatorDoorEventListener(ElevatorDoorEventListener elevatorDoorEventListener) {
        door.registerElevatorDoorEventListener(elevatorDoorEventListener);
    }

    public ElevatorDoorStates currentElevatorDoorState() {
        return door.currentState();
    }

    @Override
    public void openElevatorDoor() {
        if (door.isClosed() || door.isClosing()) {
            door.open();
        }
    }

    @Override
    public void closeElevatorDoor() {
        if (door.isOpen()) {
            door.close();
        }
    }

    @Override
    public void doorIsOpening() {
        door.changeStateTo(OPENING);
    }

    @Override
    public void doorOpened() {
        door.changeStateTo(OPEN);
    }

    @Override
    public void doorIsClosing() {
        door.changeStateTo(CLOSING);
    }

    @Override
    public void obstacleDetected() {
        door.open();
    }

    @Override
    public void doorClosed() {
        door.changeStateTo(CLOSED);
    }

}
