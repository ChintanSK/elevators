package com.cs.elevator.service;

import com.cs.elevator.door.ElevatorDoor;
import com.cs.elevator.hardware.ElevatorHardware.DoorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.DoorSignalsAdapter;

import java.util.concurrent.CompletableFuture;

import static com.cs.elevator.door.ElevatorDoor.ElevatorDoorStates.*;
import static com.cs.util.ThreadUtils.setTimeout;

public class ElevatorDoorService implements DoorSignalsAdapter {
    private final ElevatorDoor door;
    private final DoorCommandsAdapter doorCommands;
    private CompletableFuture<Void> delayedCloseDoorTask;

    public ElevatorDoorService(ElevatorDoor door, DoorCommandsAdapter doorCommands) {
        this.door = door;
        this.doorCommands = doorCommands;
    }

    public void open() {
        doorCommands.open();
        if (delayedCloseDoorTask != null) {
            delayedCloseDoorTask.cancel(true);
        }
        delayedCloseDoorTask = setTimeout(() -> {
            close();
            return null;
        }, 5000);
    }

    public void close() {
        doorCommands.close();
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
        open();
    }

    @Override
    public void doorClosed() {
        door.changeStateTo(CLOSED);
    }
}
