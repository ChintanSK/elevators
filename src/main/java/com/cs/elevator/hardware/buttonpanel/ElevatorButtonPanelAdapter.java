package com.cs.elevator.hardware.buttonpanel;

@FunctionalInterface
public interface ElevatorButtonPanelAdapter {
    void buttonPressed(String buttonCode);
}
