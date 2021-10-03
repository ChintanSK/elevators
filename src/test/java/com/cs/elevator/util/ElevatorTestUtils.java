package com.cs.elevator.util;

import com.cs.elevator.ElevatorService;
import com.cs.elevator.hardware.ElevatorHardware.DoorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.DoorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorSignalsAdapter;

import java.util.Arrays;

import static com.cs.elevator.Elevator.ElevatorStates.*;
import static com.cs.elevator.ElevatorDirection.DOWN;
import static com.cs.elevator.ElevatorDirection.UP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class ElevatorTestUtils implements TestSetUp, TestAssertions {

    private final ElevatorService elevatorService;
    private DoorSignalsAdapter doorHardwareSignals;
    private DoorCommandsAdapter doorHardwareCommands;
    private ElevatorSignalsAdapter elevatorHardwareSignals;
    private ElevatorCommandsAdapter elevatorHardwareCommands;

    private ElevatorTestUtils(ElevatorService elevatorService) {
        this.elevatorService = elevatorService;
    }

    public static ElevatorTestUtils testUtilsFor(ElevatorService elevatorService) {
        return new ElevatorTestUtils(elevatorService);
    }

    public ElevatorTestUtils withDoorControls(DoorSignalsAdapter doorHardwareSignals, DoorCommandsAdapter doorHardwareCommands) {
        this.doorHardwareSignals = doorHardwareSignals;
        this.doorHardwareCommands = doorHardwareCommands;
        return this;
    }

    public ElevatorTestUtils withElevatorControls(ElevatorSignalsAdapter elevatorHardwareSignals, ElevatorCommandsAdapter elevatorHardwareCommands) {
        this.elevatorHardwareSignals = elevatorHardwareSignals;
        this.elevatorHardwareCommands = elevatorHardwareCommands;
        return this;
    }

    @Override
    public void doorOpenAction() {
        doAnswer(invocation -> {
            doorHardwareSignals.doorIsOpening();
            doorHardwareSignals.doorOpened();
            return null;
        }).when(doorHardwareCommands).open();
    }

    @Override
    public void doorClosedAction() {
        doAnswer(invocation -> {
            doorHardwareSignals.doorIsClosing();
            doorHardwareSignals.doorClosed();
            return null;
        }).when(doorHardwareCommands).close();
    }

    @Override
    public void stationaryElevatorAt(String storeyCode) {
        elevatorService.elevator.makeStationary();
        elevatorService.currentStorey = storeyCode;
    }

    @Override
    public void elevatorMoveUpAction() {
        doAnswer(invocation -> {
            elevatorHardwareSignals.elevatorMoving();
            return null;
        }).when(elevatorHardwareCommands).moveUp();
    }

    @Override
    public void elevatorMoveDownAction() {
        doAnswer(invocation -> {
            elevatorHardwareSignals.elevatorMoving();
            return null;
        }).when(elevatorHardwareCommands).moveDown();
    }

    @Override
    public void elevatorMovingToStoreys(String... storeyCodes) {
        Arrays.stream(storeyCodes).forEach(elevatorHardwareSignals::elevatorApproachingStorey);
    }

    @Override
    public void elevatorStopActionAtStoreys(String... storeyCodes) {
        doAnswer(invocation -> {
            elevatorHardwareSignals.elevatorStopped(invocation.getArgument(0));
            return null;
        }).when(elevatorHardwareCommands).stop(argThat(is(oneOf(storeyCodes))));
    }

    public void elevatorIsMoving() {
        TestAssertions.waitUntil(elevatorService.elevator::currentState, is(MOVING));
    }

    @Override
    public void elevatorDirectionIsUp() {
        assertThat(elevatorService.direction(), is(UP));
    }

    @Override
    public void elevatorIsMovingUp() {
        elevatorIsMoving();
        elevatorDirectionIsUp();
    }

    @Override
    public void elevatorDirectionIsDown() {
        assertThat(elevatorService.direction(), is(DOWN));
    }

    @Override
    public void elevatorIsMovingDown() {
        elevatorIsMoving();
        elevatorDirectionIsDown();
    }

    @Override
    public void elevatorIsServingAtStorey(String storeyCode) {
        TestAssertions.waitUntil(elevatorService.elevator::currentState, is(SERVING));
        assertThat(elevatorService.currentStorey(), is(storeyCode));
        TestAssertions.waitUntil(elevatorService.elevator.door::isClosed);
    }

    @Override
    public void elevatorIsStationary() {
        TestAssertions.waitUntil(elevatorService.elevator::currentState, is(STATIONARY));
    }

    @Override
    public void currentStoreyIs(String storeyCode) {
        TestAssertions.waitUntil(elevatorService::currentStorey, is(storeyCode));
    }

}
