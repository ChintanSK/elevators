package com.cs.elevator.hardware;

public interface ElevatorHardware {
    interface ElevatorCommandsAdapter {
        void stop();

        void moveUp();

        void moveDown();
    }

    interface ElevatorSignalsAdapter {
        void elevatorStationary(String storeyCode);

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
