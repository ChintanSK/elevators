package com.cs.elevator.door;

public interface ElevatorDoorEventListener {
    void onDoorStatusChange(ElevatorDoorState.ElevatorDoorStateChangeEvent event);
}
