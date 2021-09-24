package com.cs.elevator.hardware.buttonpanel;

import com.cs.elevator.ElevatorService;

public class ElevatorButtonPanel implements ElevatorButtonPanelAdapter {

    private final ElevatorService elevatorService;

    public ElevatorButtonPanel(ElevatorService elevatorService) {
        this.elevatorService = elevatorService;
    }

    @Override
    public void buttonPressed(String buttonCode) {
        ElevatorButtonCommand elevatorButtonCommand = ElevatorButtonCommand.getByCode(buttonCode);
        elevatorButtonCommand.executeFor(elevatorService);
    }


    public interface ElevatorButtonCommand {

        void executeFor(ElevatorService elevatorService);

        static ElevatorButtonCommand getByCode(String buttonCode) {
            switch (buttonCode) {
                case "OPEN":
                    return elevatorService -> {
                        if (elevatorService.elevator.door.isClosed() || elevatorService.elevator.door.isClosing()) {
                            elevatorService.openDoor();
                        }
                    };
                case "CLOSE":
                    return elevatorService -> {
                        if (elevatorService.elevator.door.isOpen()) {
                            elevatorService.doorService.close();
                        }
                    };
                default:
                    return elevatorService -> {
                        if (elevatorService.elevator.isStationary() && elevatorService.currentStorey().equals(buttonCode)) {
                            elevatorService.openDoor();
                        } else {
                            elevatorService.requests.add(buttonCode);
                        }
                    };
            }
        }
    }
}
