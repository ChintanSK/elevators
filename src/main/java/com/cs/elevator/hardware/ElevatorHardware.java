package com.cs.elevator.hardware;

public interface ElevatorHardware {
    interface ElevatorSignalsAdapter {
        void elevatorStationary(String storey);

        void elevatorMovingUp();

        void elevatorMovingDown();

        void elevatorApproachingStorey(String storey);

        void elevatorSlowingDown();
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
