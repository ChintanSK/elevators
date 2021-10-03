package com.cs.elevator.storey.hardware.buttonpanel;

import com.cs.elevator.ElevatorDirection;
import com.cs.elevator.ElevatorService;
import com.cs.elevator.storey.Storeys;

public class StoreyButtonPanel implements StoreyButtonPanelAdapter {
    private final ElevatorService elevatorService;

    public StoreyButtonPanel(ElevatorService elevatorService) {
        this.elevatorService = elevatorService;
    }

    @Override
    public void upButtonPressed(String storeyCode) {
        StoreyButtonCommand command = StoreyButtonCommand.getByCode(storeyCode, ElevatorDirection.UP);
        command.executeFor(elevatorService);
    }

    @Override
    public void downButtonPressed(String storeyCode) {
        StoreyButtonCommand command = StoreyButtonCommand.getByCode(storeyCode, ElevatorDirection.DOWN);
        command.executeFor(elevatorService);
    }

    public interface StoreyButtonCommand {
        void executeFor(ElevatorService elevatorService);

        static StoreyButtonCommand getByCode(String requestedFromStoreyCode, ElevatorDirection requestedDirection) {
            return elevatorService -> {
                boolean directionMatched = requestedDirection.equals(elevatorService.direction());
                if ((directionMatched || elevatorService.hasNoMoreRequests()) && elevatorService.isAt(requestedFromStoreyCode)) {
                    if (!directionMatched && elevatorService.hasNoMoreRequests()) {
                        elevatorService.toggleDirection();
                    }
                    elevatorService.openDoor();
                } else {
                    elevatorService.makeElevatorRequest(Storeys.getByCode(requestedFromStoreyCode));
                }
            };
        }
    }
}
