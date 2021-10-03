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
import com.cs.elevator.storey.Storey;
import com.cs.elevator.util.AsyncTaskUtils;

import static com.cs.elevator.util.AsyncTaskUtils.executeAsync;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ElevatorService implements ElevatorSignalsAdapter, ElevatorDoorEventListener {
    public final Elevator elevator = new Elevator();
    public final ElevatorDoorService doorService;
    public final ElevatorButtonPanelAdapter buttonPanel = new ElevatorButtonPanel(this);
    private final ElevatorCommandsAdapter elevatorHardwareCommands;
    private final ElevatorRequests requests = new ElevatorRequests();
    private ElevatorDirection direction = ElevatorDirection.UP;

    public String currentStorey;
    private boolean stopped;

    public ElevatorService(ElevatorHardwareCommands elevatorHardwareCommands) {
        elevator.door.registerElevatorDoorEventListener(this);
        doorService = new ElevatorDoorService(elevator.door, elevatorHardwareCommands.doorCommands);
        this.elevatorHardwareCommands = elevatorHardwareCommands.elevatorCommands;
    }

    public boolean isAt(String storeyCode) {
        return (elevator.isStationary() || elevator.isServing()) && currentStorey.equals(storeyCode);
    }

    public void start() {
        startAcceptingElevatorRequests();
        startServingElevatorRequests();
    }

    public void stop() {
        stopped = true;
    }

    public void makeElevatorRequest(Storey storey) {
        requests.enqueueRequest(storey);
    }

    private void startAcceptingElevatorRequests() {
        AsyncTaskUtils.executeAsync(() -> {
            while (!stopped) {
                requests.acceptNextRequest();
            }
        }).now();
    }

    private void startServingElevatorRequests() {
        AsyncTaskUtils.executeAsync(() -> {
            while (!stopped) {
                if (hasMoreRequests() && elevator.isStationary()) {
                    serveNextElevatorRequest();
                }
            }
        }).now();
    }

    private boolean hasMoreRequests() {
        return !requests.empty();
    }

    public boolean hasNoMoreRequests() {
        return requests.empty();
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
            toggleDirection();
        }
    }

    public void toggleDirection() {
        direction = direction.toggle();
    }

    public ElevatorDirection direction() {
        return direction;
    }

    public String currentStorey() {
        return currentStorey;
    }

    public ElevatorRequestsView requests() {
        return requests;
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

    public void closeDoor() {
        doorService.close();
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
