package com.cs.elevator.hardware;

import com.cs.elevator.Elevator;
import com.cs.elevator.buttonpanel.ElevatorButtonCommand;

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


}
