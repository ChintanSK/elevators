package com.cs.elevator.hardware.buttonpanel;

import com.cs.elevator.ElevatorService;
import com.cs.elevator.storey.Storeys;

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
                    return ElevatorService::openDoor;
                case "CLOSE":
                    return ElevatorService::closeDoor;
                default:
                    return elevatorService -> elevatorService.makeElevatorRequest(Storeys.getByCode(buttonCode));
            }
        }
    }

}
