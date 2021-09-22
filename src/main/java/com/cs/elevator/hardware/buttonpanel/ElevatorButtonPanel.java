package com.cs.elevator.hardware.buttonpanel;

import com.cs.elevator.Elevator;

public class ElevatorButtonPanel implements ElevatorButtonPanelAdapter {

    private final Elevator elevator;

    public ElevatorButtonPanel(Elevator elevator) {
        this.elevator = elevator;
    }

    @Override
    public void buttonPressed(String buttonCode) {
        ElevatorButtonCommand elevatorButtonCommand = ElevatorButtonCommand.getByCode(buttonCode);
        elevatorButtonCommand.executeFor(elevator);
    }


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
}
