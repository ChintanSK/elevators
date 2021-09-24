package com.cs.elevator.door;

public interface ElevatorDoorEventListener {
    void onDoorStatusChange(ElevatorDoor.ElevatorDoorStateChangeEvent event);
}
