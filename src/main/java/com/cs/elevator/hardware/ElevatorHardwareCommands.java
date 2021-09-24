package com.cs.elevator.hardware;

public class ElevatorHardwareCommands {
    public final ElevatorHardware.ElevatorCommandsAdapter elevatorCommands;
    public final ElevatorHardware.DoorCommandsAdapter doorCommands;

    public ElevatorHardwareCommands(ElevatorHardware.ElevatorCommandsAdapter elevatorCommands, ElevatorHardware.DoorCommandsAdapter doorCommands) {
        this.elevatorCommands = elevatorCommands;
        this.doorCommands = doorCommands;
    }
}
