package com.cs.elevator.util;

import com.cs.elevator.storey.Storeys;

import java.util.stream.IntStream;

public interface TestSetUp {

    static void createStoreys(int numberOfStoreys) {
        Storeys.addStorey(0, "GROUND");
        IntStream.range(1, numberOfStoreys).forEach(Storeys::addStorey);
    }

    static String then(String storeyCode) {
        return storeyCode;
    }
    static String andThen(String storeyCode) {
        return then(storeyCode);
    }

    void doorOpenAction();

    void doorClosedAction();

    void stationaryElevatorAt(String storeyCode);

    void elevatorMoveUpAction();

    void elevatorMoveDownAction();

    void elevatorMovingToStoreys(String... storeyCodes);

    void elevatorStopActionAtStoreys(String... storeyCodes);
}
