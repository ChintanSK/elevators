package com.cs.elevator;

import com.cs.elevator.door.ElevatorDoorService;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardwareCommands;
import com.cs.elevator.hardware.buttonpanel.ElevatorButtonPanel;
import com.cs.elevator.hardware.buttonpanel.ElevatorButtonPanelAdapter;
import com.cs.elevator.storey.Storey;

import java.util.Set;
import java.util.TreeSet;

public class ElevatorService implements Runnable, ElevatorSignalsAdapter {
    public final Elevator elevator;
    public final ElevatorDoorService doorService;
    private final ElevatorCommandsAdapter elevatorHardwareCommands;
    public final ElevatorButtonPanelAdapter buttonPanel = new ElevatorButtonPanel(this);
    public final ElevatorRequests requests = new ElevatorRequests();
    public ElevatorDirection direction = ElevatorDirection.UP;

    public final Set<String> storeyStops = new TreeSet<>();
    public String currentStorey;
    private boolean stopped;

    public ElevatorService(Elevator elevator, ElevatorHardwareCommands elevatorHardwareCommands) {
        this.elevator = elevator;
        doorService = new ElevatorDoorService(elevator.door, elevatorHardwareCommands.doorCommands);
        this.elevatorHardwareCommands = elevatorHardwareCommands.elevatorCommands;
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                while (requests.hasNext()) {
                    if (elevator.isStationary() && isDoorClosedLongEnough()) {
                        Storey next = nextRequest();
                        move(next);
                        requests.remove(next.name);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isDoorClosedLongEnough() throws InterruptedException {
        boolean closedASecondAgo = elevator.door.isClosed();
        Thread.sleep(1000L);
        boolean stillClosed = elevator.door.isClosed();
        return closedASecondAgo && stillClosed;
    }

    public void move(Storey storeyStop) {
        storeyStops.add(storeyStop.name);
        switch (direction) {
            case UP:
                elevatorHardwareCommands.moveUp();
                break;
            case DOWN:
                elevatorHardwareCommands.moveDown();
                break;
        }
    }

    public void start() {
        new Thread(this).start();
    }

    public void stop() {
        stopped = true;
    }

    public Storey nextRequest() {
        Storey next = requests.next(currentStorey, direction);
        if (next == null) {
            direction = direction.toggle();
            next = requests.next(currentStorey, direction);
        }
        return next;
    }

    public String currentStorey() {
        return currentStorey;
    }

    @Override
    public void elevatorStationary(String storeyCode) {
        currentStorey = storeyCode;
        elevator.makeStationary();
        openDoor();
        storeyStops.remove(storeyCode);
    }

    @Override
    public void elevatorMoving() {
        elevator.makeMoving();
    }

    @Override
    public void elevatorApproachingStorey(String storeyCode) {
        if (storeyStops.contains(storeyCode)) {
            elevatorHardwareCommands.stop();
        }
    }

    public void openDoor() {
        if (!elevator.isMoving()) {
            doorService.open();
        }
    }
}
