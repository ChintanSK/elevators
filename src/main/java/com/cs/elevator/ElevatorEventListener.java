package com.cs.elevator;

public interface ElevatorEventListener {
    void onElevatorStatusChange(Elevator.ElevatorStateChangeEvent event);
}
