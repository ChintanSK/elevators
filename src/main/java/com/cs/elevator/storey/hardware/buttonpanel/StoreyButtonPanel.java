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
            return elevatorService -> elevatorService.makeElevatorRequest(Storeys.getByCode(requestedFromStoreyCode), requestedDirection);
        }
    }
}
