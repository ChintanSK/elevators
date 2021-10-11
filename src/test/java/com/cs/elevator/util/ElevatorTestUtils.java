package com.cs.elevator.util;

import com.cs.elevator.ElevatorService;
import com.cs.elevator.hardware.ElevatorHardware.DoorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.DoorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorSignalsAdapter;

import java.util.Arrays;

import static com.cs.elevator.Elevator.ElevatorStates.MOVING;
import static com.cs.elevator.Elevator.ElevatorStates.SERVING;
import static com.cs.elevator.ElevatorDirection.DOWN;
import static com.cs.elevator.ElevatorDirection.UP;
import static com.cs.elevator.door.ElevatorDoor.ElevatorDoorStates.CLOSED;
import static com.cs.elevator.util.AsyncMatcher.eventually;
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

    @Override
    public void elevatorIsMovingUp() {
        assertThat(elevatorService::currentElevatorState, eventually(is(MOVING)));
        assertThat(elevatorService.direction(), is(UP));
    }

    @Override
    public void elevatorIsMovingDown() {
        assertThat(elevatorService::currentElevatorState, eventually(is(MOVING)));
        assertThat(elevatorService.direction(), is(DOWN));
    }

    @Override
    public void elevatorIsServingAtStorey(String storeyCode) {
        assertThat(elevatorService::currentElevatorState, eventually(is(SERVING)));
        assertThat(elevatorService.currentStorey(), is(storeyCode));
        assertThat(elevatorService::currentElevatorDoorState, eventually(is(CLOSED)));
    }

}
