package com.cs.elevator.door.hardware;

public interface ElevatorHardwareAdapter {
    interface DoorCommands {
        void open();

        void close();
    }

    interface DoorSignals {
        void doorIsOpening();

        void doorOpened();

        void doorIsClosing();

        void obstacleDetected();

        void doorClosed();
    }
}
