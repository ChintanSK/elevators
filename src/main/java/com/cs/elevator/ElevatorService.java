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
import com.cs.elevator.util.AsyncTaskUtils;

import static com.cs.elevator.util.AsyncTaskUtils.executeAsync;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ElevatorService implements ElevatorSignalsAdapter, ElevatorDoorEventListener {
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
        startAcceptingElevatorRequests();
        startServingElevatorRequests();
    }

    public void stop() {
        stopped = true;
    }

    private void startAcceptingElevatorRequests() {
        AsyncTaskUtils.executeAsync(() -> {
            while (!stopped) {
                requests.serveNext();
            }
        }).now();
    }

    private void startServingElevatorRequests() {
        AsyncTaskUtils.executeAsync(() -> {
            while (!stopped) {
                if (requests.hasMore() && elevator.isStationary()) {
                    serveNextElevatorRequest();
                }
            }
        }).now();
    }

    private void serveNextElevatorRequest() {
        toggleDirectionIfNeeded();
        switch (direction) {
            case UP:
                elevatorHardwareCommands.moveUp();
                break;
            case DOWN:
                elevatorHardwareCommands.moveDown();
                break;
        }
    }

    private void toggleDirectionIfNeeded() {
        if (requests.next(currentStorey, direction) == null) {
            direction = direction.toggle();
        }
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
            if (isDoorClosedLongEnough()) {
                elevator.makeStationary();
            }
        } else {
            elevator.makeServing();
        }
    }

    private boolean isDoorClosedLongEnough() {
        boolean closedASecondAgo = elevator.door.isClosed();
        Boolean stillClosed = executeAsync(elevator.door::isClosed).withDelayOf(2, SECONDS).andGetResult();
        return closedASecondAgo && stillClosed;
    }
}
