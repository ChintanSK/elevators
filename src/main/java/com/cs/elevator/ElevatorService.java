package com.cs.elevator;

import com.cs.elevator.door.ElevatorDoor;
import com.cs.elevator.door.ElevatorDoor.ElevatorDoorStates;
import com.cs.elevator.door.ElevatorDoorEventListener;
import com.cs.elevator.door.ElevatorDoorService;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardwareCommands;
import com.cs.elevator.hardware.buttonpanel.ElevatorButtonPanel;
import com.cs.elevator.hardware.buttonpanel.ElevatorButtonPanelAdapter;

import java.util.concurrent.CompletableFuture;

public class ElevatorService implements Runnable, ElevatorSignalsAdapter, ElevatorDoorEventListener {
    public final Elevator elevator;
    public final ElevatorDoorService doorService;
    private final ElevatorCommandsAdapter elevatorHardwareCommands;
    public final ElevatorButtonPanelAdapter buttonPanel = new ElevatorButtonPanel(this);
    public final ElevatorRequests requests = new ElevatorRequests();
    public ElevatorDirection direction = ElevatorDirection.UP;

    public String currentStorey;
    private boolean stopped;

    public ElevatorService(Elevator elevator, ElevatorHardwareCommands elevatorHardwareCommands) {
        this.elevator = elevator;
        elevator.door.registerElevatorDoorEventListener(this);
        doorService = new ElevatorDoorService(elevator.door, elevatorHardwareCommands.doorCommands);
        this.elevatorHardwareCommands = elevatorHardwareCommands.elevatorCommands;
    }

    public void start() {
        new Thread(this).start();
    }

    public void stop() {
        stopped = true;
    }

    @Override
    public void run() {
        startAcceptingElevatorRequests();
        while (!stopped) {
            if (requests.hasMore() && elevator.isStationary()) {
                if (requests.next(currentStorey, direction) == null) {
                    direction = direction.toggle();
                }
                switch (direction) {
                    case UP:
                        elevatorHardwareCommands.moveUp();
                        break;
                    case DOWN:
                        elevatorHardwareCommands.moveDown();
                        break;
                }
            }
        }
    }

    private void startAcceptingElevatorRequests() {
        new Thread(() -> {
            while (!stopped) {
                requests.serveNext();
            }
        }).start();
    }

    public String currentStorey() {
        return currentStorey;
    }

    @Override
    public void elevatorStopped(String storeyCode) {
        currentStorey = storeyCode;
        elevator.makeServing();
        openDoor();
        requests.markAsServed(storeyCode);
    }

    @Override
    public void elevatorMoving() {
        elevator.makeMoving();
    }

    @Override
    public void elevatorApproachingStorey(String storeyCode) {
        if (requests.contains(storeyCode)) {
            elevatorHardwareCommands.stop(storeyCode);
        }
    }

    public void openDoor() {
        if (!elevator.isMoving()) {
            doorService.open();
        }
    }

    @Override
    public void onDoorStatusChange(ElevatorDoor.ElevatorDoorStateChangeEvent event) {
        if (ElevatorDoorStates.CLOSED.equals(event.newState())) {
            try {
                if (isDoorClosedLongEnough()) {
                    elevator.makeStationary();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            elevator.makeServing();
        }
    }

    private boolean isDoorClosedLongEnough() throws InterruptedException {
        boolean closedASecondAgo = elevator.door.isClosed();
        Thread.sleep(2000L);
        boolean stillClosed = elevator.door.isClosed();
        return closedASecondAgo && stillClosed;
    }
}
