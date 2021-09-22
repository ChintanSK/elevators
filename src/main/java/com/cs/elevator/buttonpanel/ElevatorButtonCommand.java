package com.cs.elevator.buttonpanel;

import com.cs.elevator.Elevator;

public interface ElevatorButtonCommand {

    void executeFor(Elevator elevator);

    static ElevatorButtonCommand getByCode(String buttonCode) {
        switch (buttonCode) {
            case "OPEN":
                return elevator -> {
                    if (elevator.door.isClosed() || elevator.door.isClosing()) {
                        elevator.openDoor();
                    }
                };
            case "CLOSE":
                return elevator -> {
                    if (elevator.door.isOpen()) {
                        elevator.door.close();
                    }
                };
            default:
                return elevator -> {
                    if (elevator.elevatorState.isStationary() && elevator.currentStorey().equals(buttonCode)) {
                        elevator.openDoor();
                    } else {
                        elevator.requests.add(buttonCode);
                    }
                };
        }
    }
}
