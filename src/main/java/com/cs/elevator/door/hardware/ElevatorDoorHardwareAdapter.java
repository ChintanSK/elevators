package com.cs.elevator.door.hardware;

public interface ElevatorDoorHardwareAdapter {
    interface Commands {
        void open();

        void close();
    }

    interface Signals {
        void doorIsOpening();

        void doorOpened();

        void doorIsClosing();

        void obstacleDetected();

        void doorClosed();
    }
}
