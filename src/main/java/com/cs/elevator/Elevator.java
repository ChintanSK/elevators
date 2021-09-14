package com.cs.elevator;

import com.cs.elevator.door.ElevatorDoor;
import com.cs.elevator.door.hardware.ElevatorDoorControlPanel;
import com.cs.elevator.door.hardware.ElevatorDoorHardwareAdapter;

import static com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStates.*;

public class Elevator implements ElevatorDoorControlPanel, ElevatorDoorHardwareAdapter.Signals {

    private final ElevatorDoor door;

    public Elevator(ElevatorDoor door) {
        this.door = door;
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
