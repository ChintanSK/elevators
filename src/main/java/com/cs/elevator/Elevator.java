package com.cs.elevator;

import com.cs.elevator.ElevatorState.ElevatorStates;
import com.cs.elevator.door.ElevatorDoor;
import com.cs.elevator.door.ElevatorDoorEventListener;
import com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStates;
import com.cs.elevator.hardware.buttonpanel.ElevatorButtonPanel;
import com.cs.elevator.hardware.buttonpanel.ElevatorButtonPanelAdapter;
import com.cs.elevator.hardware.ElevatorHardware;
import com.cs.elevator.hardware.ElevatorHardware.DoorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorSignalsAdapter;

import static com.cs.elevator.ElevatorState.ElevatorStates.*;
import static com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStates.*;

public class Elevator implements DoorSignalsAdapter, ElevatorSignalsAdapter {

    public final ElevatorState elevatorState = new ElevatorState();
    public final ElevatorButtonPanelAdapter buttonPanel = new ElevatorButtonPanel(this);
    public final ElevatorDoor door;
    public final ElevatorRequests requests = new ElevatorRequests();
    private String currentStorey;

    public Elevator(ElevatorHardware.DoorCommandsAdapter doorHardwareCommands) {
        this.door = new ElevatorDoor(doorHardwareCommands);
    }

    public void registerElevatorDoorEventListener(ElevatorDoorEventListener elevatorDoorEventListener) {
        door.registerElevatorDoorEventListener(elevatorDoorEventListener);
    }

    public String currentStorey() {
        return currentStorey;
    }

    public ElevatorStates currentElevatorState() {
        return elevatorState.currentState();
    }

    public ElevatorDoorStates currentDoorState() {
        return door.currentState();
    }

    public void openDoor() {
        if (!elevatorState.isMoving()) {
            door.open();
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
        openDoor();
    }

    @Override
    public void doorClosed() {
        door.changeStateTo(CLOSED);
    }

    @Override
    public void elevatorStationary(String storey) {
        currentStorey = storey;
        elevatorState.changeTo(STATIONARY);
    }

    @Override
    public void elevatorMovingUp() {
        elevatorState.changeTo(MOVING_UP);
    }

    @Override
    public void elevatorMovingDown() {
        elevatorState.changeTo(MOVING_UP);
    }

    @Override
    public void elevatorApproachingStorey(String storey) {
        // check if the coming storey needs to be serviced.
        // if so then send command to slow down.
    }

    @Override
    public void elevatorSlowingDown() {
        elevatorState.changeTo(SLOWING);
    }
}
