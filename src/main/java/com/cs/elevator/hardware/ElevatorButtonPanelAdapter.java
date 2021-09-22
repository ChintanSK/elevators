package com.cs.elevator.hardware;

@FunctionalInterface
public interface ElevatorButtonPanelAdapter {
    void buttonPressed(String buttonCode);
}
