package com.cs.elevator.hardware;

public interface ElevatorHardware {
    interface ElevatorCommandsAdapter {
        void stop(String storeyCode);

        void moveUp();

        void moveDown();
    }

    interface ElevatorSignalsAdapter {
        void elevatorStopped(String storeyCode);

        void elevatorMoving();

        void elevatorApproachingStorey(String storeyCode);
    }

    interface DoorCommandsAdapter {
        void open();

        void close();
    }

    interface DoorSignalsAdapter {
        void doorIsOpening();

        void doorOpened();

        void doorIsClosing();

        void obstacleDetected();

        void doorClosed();
    }
}
