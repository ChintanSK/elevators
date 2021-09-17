package com.cs.elevator;

import com.cs.elevator.door.ElevatorDoor;
import com.cs.elevator.door.ElevatorDoorEventListener;
import com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStates;
import com.cs.elevator.door.hardware.ElevatorButtonPanelAdapter;
import com.cs.elevator.door.hardware.ElevatorHardwareAdapter;

import static com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStates.*;

public class Elevator implements ElevatorButtonPanelAdapter.DoorButtons, ElevatorHardwareAdapter.DoorSignals {

    private final ElevatorDoor door;

    public Elevator(ElevatorHardwareAdapter.DoorCommands doorHardwareCommands) {
        this.door = new ElevatorDoor(doorHardwareCommands);
    }

    public void registerElevatorDoorEventListener(ElevatorDoorEventListener elevatorDoorEventListener) {
        door.registerElevatorDoorEventListener(elevatorDoorEventListener);
    }

    public ElevatorDoorStates currentElevatorDoorState() {
        return door.currentState();
    }

    @Override
    public void openDoorButtonPressed() {
        if (door.isClosed() || door.isClosing()) {
            door.open();
        }
    }

    @Override
    public void closeDoorButtonPressed() {
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
