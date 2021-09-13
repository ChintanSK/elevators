package com.cs.elevators.door;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static com.cs.elevators.door.Door.DoorStatus.*;

public class Door {
    private final DoorEventListener doorEventListener;
    private DoorStatus status;
    private CompletableFuture<Void> openDoorAction;
    private CompletableFuture<Void> closeDoorAction;

    public static Door anOpenDoor(DoorEventListener doorEventListener) {
        return new Door(doorEventListener, OPEN);
    }

    public static Door aClosedDoor(DoorEventListener doorEventListener) {
        return new Door(doorEventListener, CLOSED);
    }

    private Door(DoorEventListener doorEventListener, DoorStatus doorStatus) {
        this.doorEventListener = doorEventListener;
        this.status = doorStatus;
    }

    public void open() {
        if (closeDoorAction != null && !closeDoorAction.isDone()) {
            closeDoorAction.cancel(true);
        }
        if (isClosed() || isClosing()) {
            openDoorAction = execute(new DoorOpenAction(this));
        }
    }

    public void close() {
        if ((openDoorAction == null || openDoorAction.isDone()) && isOpen()) {
            closeDoorAction = execute(new DoorCloseAction(this));
        }
    }

    public void doorOpened() {
        statusChangeTo(OPEN);
    }

    public void doorClosed() {
        statusChangeTo(CLOSED);
    }

    private CompletableFuture<Void> execute(DoorAction doorAction) {
        return CompletableFuture.runAsync(doorAction::execute);
    }

    private void statusChangeTo(DoorStatus newStatus) {
        System.out.println("Door state change from " + status.name() + " to " + newStatus.name());
        status = newStatus;
        switch (newStatus) {
            case OPENING:
                doorEventListener.onOpening();
                break;
            case OPEN:
                doorEventListener.onOpen();
                if (openDoorAction != null) openDoorAction.complete(null);
                break;
            case CLOSING:
                doorEventListener.onClosing();
                break;
            case CLOSED:
                doorEventListener.onClosed();
                if (closeDoorAction != null) closeDoorAction.complete(null);
                break;
        }
    }

    DoorStatus status() {
        return status;
    }

    public boolean isOpen() {
        return status.equals(OPEN);
    }

    public boolean isClosing() {
        return status.equals(CLOSING);
    }

    public boolean isClosed() {
        return status.equals(CLOSED);
    }

    enum DoorStatus {
        CLOSED, CLOSING, OPEN, OPENING
    }

    private abstract static class DoorAction {
        private final boolean openAction;
        protected final Door door;

        DoorAction(Door door, boolean openAction) {
            this.door = door;
            this.openAction = openAction;
        }

        void execute() {
            door.statusChangeTo(openAction ? OPENING : CLOSING);
            waitForStatus(openAction ? OPEN : CLOSED);
            if (door.isOpen()) {
                holdDoorStateFor(5);
                door.close();
            }
        }

        private void waitForStatus(DoorStatus status) {
            while (!door.status().equals(status)) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void holdDoorStateFor(int seconds) {
            IntStream.range(0, seconds)
                    .forEach(second -> {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    private static class DoorOpenAction extends DoorAction {
        DoorOpenAction(Door door) {
            super(door, true);
        }
    }

    private static class DoorCloseAction extends DoorAction {
        DoorCloseAction(Door door) {
            super(door, false);
        }
    }
}
