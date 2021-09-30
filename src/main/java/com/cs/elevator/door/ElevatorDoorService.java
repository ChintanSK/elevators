package com.cs.elevator.door;

import com.cs.elevator.hardware.ElevatorHardware.DoorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.DoorSignalsAdapter;
import com.cs.util.ScheduledTask;

import java.util.concurrent.Callable;

import static com.cs.elevator.door.ElevatorDoor.ElevatorDoorStates.*;
import static com.cs.util.ScheduledTask.execute;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ElevatorDoorService implements DoorSignalsAdapter {
    private final ElevatorDoor door;
    private final DoorCommandsAdapter doorCommands;
    private ScheduledTask<Void> delayedCloseDoorTask;

    public ElevatorDoorService(ElevatorDoor door, DoorCommandsAdapter doorCommands) {
        this.door = door;
        this.doorCommands = doorCommands;
    }

    public void open() {
        doorCommands.open();
        if (delayedCloseDoorTask != null) {
            delayedCloseDoorTask.cancel();
        }
        delayedCloseDoorTask = execute((Callable<Void>) () -> {
            close();
            return null;
        }).withDelayOf(5, SECONDS);
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
