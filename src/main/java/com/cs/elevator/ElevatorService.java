package com.cs.elevator;

import com.cs.elevator.Elevator.ElevatorStates;
import com.cs.elevator.door.ElevatorDoor;
import com.cs.elevator.door.ElevatorDoor.ElevatorDoorStates;
import com.cs.elevator.door.ElevatorDoorEventListener;
import com.cs.elevator.door.ElevatorDoorService;
import com.cs.elevator.hardware.ElevatorHardware.DoorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardwareCommands;
import com.cs.elevator.hardware.buttonpanel.ElevatorButtonPanel;
import com.cs.elevator.hardware.buttonpanel.ElevatorButtonPanelAdapter;
import com.cs.elevator.storey.Storey;
import com.cs.elevator.util.AsyncTaskUtils;

import java.util.Set;

import static com.cs.elevator.util.AsyncTaskUtils.executeAsync;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ElevatorService implements ElevatorSignalsAdapter, ElevatorDoorEventListener {
    private final Elevator elevator = new Elevator();
    private final ElevatorDoorService doorService;
    private final ElevatorButtonPanelAdapter buttonPanel = new ElevatorButtonPanel(this);
    private final ElevatorCommandsAdapter elevatorHardwareCommands;
    private final ElevatorRequests requests = new ElevatorRequests();
    private ElevatorDirection direction = ElevatorDirection.UP;

    public String currentStorey;
    private boolean stopped;

    public ElevatorService(ElevatorHardwareCommands elevatorHardwareCommands) {
        registerElevatorDoorEventListener(this);
        doorService = new ElevatorDoorService(elevator.door(), elevatorHardwareCommands.doorCommands);
        this.elevatorHardwareCommands = elevatorHardwareCommands.elevatorCommands;
    }

    public void registerElevatorEventListener(ElevatorEventListener elevatorEventListener) {
        elevator.registerElevatorEventListener(elevatorEventListener);
    }

    public void registerElevatorDoorEventListener(ElevatorDoorEventListener elevatorDoorEventListener) {
        elevator.door().registerElevatorDoorEventListener(elevatorDoorEventListener);
    }

    public boolean isAt(String storeyCode) {
        return (elevator.isStationary() || elevator.isServing()) && currentStorey.equals(storeyCode);
    }

    public ElevatorStates currentElevatorState() {
        return elevator.currentState();
    }

    public ElevatorDoorStates currentElevatorDoorState() {
        return elevator.door().currentState();
    }

    public ElevatorSignalsAdapter elevatorHardwareSignals() {
        return this;
    }

    public DoorSignalsAdapter doorHardwareSignals() {
        return doorService;
    }

    public void start() {
        startAcceptingElevatorRequests();
        startServingElevatorRequests();
    }

    public void stop() {
        stopped = true;
    }

    public void buttonPressed(String buttonCode) {
        buttonPanel.buttonPressed(buttonCode);
    }

    public void makeElevatorRequest(Storey storey) {
        makeElevatorRequest(storey, null);
    }

    public void makeElevatorRequest(Storey storey, ElevatorDirection requestedDirection) {
        boolean directionMatched = isNull(requestedDirection) || requestedDirection.equals(direction);
        if ((directionMatched || hasNoMoreRequests()) && isAt(storey.name)) {
            if (!directionMatched && hasNoMoreRequests()) {
                toggleDirection();
            }
            openDoor();
        } else {
            requests.enqueueRequest(storey);
        }
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

    private void toggleDirection() {
        direction = direction.toggle();
    }

    public ElevatorDirection direction() {
        return direction;
    }

    public String currentStorey() {
        return currentStorey;
    }

    public Set<String> requests() {
        return requests.view();
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
        boolean closedASecondAgo = elevator.door().isClosed();
        Boolean stillClosed = executeAsync(elevator.door()::isClosed).withDelayOf(2, SECONDS).andGetResult();
        return closedASecondAgo && stillClosed;
    }
}
