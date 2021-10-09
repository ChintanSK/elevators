package com.cs.elevator;

import com.cs.elevator.Elevator.ElevatorStateChangeEvent;
import com.cs.elevator.door.ElevatorDoor.ElevatorDoorStateChangeEvent;
import com.cs.elevator.door.ElevatorDoorEventListener;
import com.cs.elevator.hardware.ElevatorHardware.DoorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.DoorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardwareCommands;
import com.cs.elevator.util.ElevatorTestUtils;
import com.cs.elevator.util.TestAssertions;
import com.cs.elevator.util.TestSetUp;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.cs.elevator.Elevator.ElevatorStates.*;
import static com.cs.elevator.door.ElevatorDoor.ElevatorDoorStates.*;
import static com.cs.elevator.util.AsyncMatcher.eventually;
import static com.cs.elevator.util.ElevatorTestUtils.testUtilsFor;
import static com.cs.elevator.util.StateTransitionMatcher.*;
import static com.cs.elevator.util.TestSetUp.andThen;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ElevatorTest {

    @Mock
    private ElevatorCommandsAdapter elevatorHardwareCommands;
    @Mock
    private DoorCommandsAdapter doorHardwareCommands;

    @Mock
    private ElevatorDoorEventListener elevatorDoorEventListener;
    @Mock
    private ElevatorEventListener elevatorEventListener;

    private Elevator elevator;
    private ElevatorService elevatorService;
    private ArgumentCaptor<ElevatorDoorStateChangeEvent> elevatorDoorStateChangeEvent;
    private ArgumentCaptor<ElevatorStateChangeEvent> elevatorStateChangeEvent;
    private TestSetUp setUp;
    private TestAssertions assertThat;

    @BeforeAll
    public static void initStoreys() {
        TestSetUp.createStoreys(4);
    }

    @BeforeEach
    public void initElevator() {
        elevatorService = new ElevatorService(new ElevatorHardwareCommands(elevatorHardwareCommands, doorHardwareCommands));
        elevator = elevatorService.elevator;
        elevator.registerElevatorStateChangeListener(elevatorEventListener);
        elevator.door.registerElevatorDoorEventListener(elevatorDoorEventListener);
        elevatorStateChangeEvent = ArgumentCaptor.forClass(ElevatorStateChangeEvent.class);
        elevatorDoorStateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        ElevatorSignalsAdapter elevatorHardwareSignals = elevatorService;
        DoorSignalsAdapter doorHardwareSignals = elevatorService.doorService;
        ElevatorTestUtils elevatorTestUtils = testUtilsFor(elevatorService)
                .withDoorControls(doorHardwareSignals, doorHardwareCommands)
                .withElevatorControls(elevatorHardwareSignals, elevatorHardwareCommands);
        setUp = elevatorTestUtils;
        assertThat = elevatorTestUtils;
        elevatorService.start();
    }

    @Test
    @DisplayName("New Elevator is always stationary")
    public void testNewElevatorIsStationary() {
        assertThat(elevator.isStationary(), is(true));
    }

    @Test
    @DisplayName("When button pressed for ground floor while the elevator is stationed at ground floor, then the door opens")
    public void testGroundButtonPressedWhileElevatorStationedAtGroundLevel() {
        setUp.doorOpenAction();
        setUp.doorClosedAction();
        setUp.stationaryElevatorAt("GROUND");

        elevatorService.buttonPanel.buttonPressed("GROUND");

        assertThat(elevatorService.currentStorey(), is("GROUND"));
        assertThat(elevator.isServing(), is(true));
        assertThat(elevator.door.isOpen(), is(true));
        assertThat(elevatorService.elevator::currentState, eventually(is(STATIONARY)));
        verify(elevatorDoorEventListener, times(4)).onDoorStatusChange(elevatorDoorStateChangeEvent.capture());
        List<ElevatorDoorStateChangeEvent> elevatorDoorStateChangeEvents = elevatorDoorStateChangeEvent.getAllValues();
        assertThat(elevatorDoorStateChangeEvents.get(0), is(transitioning(from(CLOSED), to(OPENING))));
        assertThat(elevatorDoorStateChangeEvents.get(1), is(transitioning(from(OPENING), to(OPEN))));
        assertThat(elevatorDoorStateChangeEvents.get(2), is(transitioning(from(OPEN), to(CLOSING))));
        assertThat(elevatorDoorStateChangeEvents.get(3), is(transitioning(from(CLOSING), to(CLOSED))));
        verify(elevatorEventListener, times(2)).onElevatorStatusChange(elevatorStateChangeEvent.capture());
        List<ElevatorStateChangeEvent> elevatorStateChangeEvents = elevatorStateChangeEvent.getAllValues();
        assertThat(elevatorStateChangeEvents.get(0), is(transitioning(from(STATIONARY), to(SERVING))));
        assertThat(elevatorStateChangeEvents.get(1), is(transitioning(from(SERVING), to(STATIONARY))));
    }

    @Test
    @DisplayName("Elevator records all the requests made from within the Elevator by button presses")
    public void testElevatorRecordsElevatorRequests() {
        setUp.stationaryElevatorAt("GROUND");

        elevatorService.buttonPanel.buttonPressed("1");
        elevatorService.buttonPanel.buttonPressed("2");
        elevatorService.buttonPanel.buttonPressed("3");

        assertThat(elevatorService.requests()::size, eventually(is(3)));
        assertThat(elevatorService.requests().contains("1"), is(true));
        assertThat(elevatorService.requests().contains("2"), is(true));
        assertThat(elevatorService.requests().contains("3"), is(true));
    }

    @Test
    @DisplayName("Elevator moves down when storey button pressed")
    public void testElevatorMovesDownWhenStoreyButtonPressed() {
        setUp.stationaryElevatorAt("3");
        setUp.elevatorMoveDownAction();
        setUp.elevatorStopActionAtStoreys("1");

        elevatorService.buttonPanel.buttonPressed("1");

        assertThat.elevatorIsMovingDown();
        verify(elevatorHardwareCommands, times(1)).moveDown();
        setUp.elevatorMovingToStoreys("2", andThen("1"));

        assertThat(elevatorService::currentStorey, eventually(is("1")));
        verify(elevatorHardwareCommands, times(1)).stop("1");
        verify(elevatorEventListener, times(2)).onElevatorStatusChange(elevatorStateChangeEvent.capture());
        List<ElevatorStateChangeEvent> elevatorStateChangeEvents = elevatorStateChangeEvent.getAllValues();
        assertThat(elevatorStateChangeEvents.get(0), is(transitioning(from(STATIONARY), to(MOVING))));
        assertThat(elevatorStateChangeEvents.get(1), is(transitioning(from(MOVING), to(SERVING))));
    }

    @Test
    @DisplayName("An up going Elevator serves upper storeys and then comes back for lower storeys")
    public void testUpGoingElevatorServesUpperStoreysAndThenLowerStoreys() {
        setUp.doorOpenAction();
        setUp.doorClosedAction();
        setUp.stationaryElevatorAt("GROUND");
        setUp.elevatorMoveUpAction();
        setUp.elevatorMoveDownAction();
        setUp.elevatorStopActionAtStoreys("3", "1");

        elevatorService.buttonPanel.buttonPressed("3");
        assertThat.elevatorIsMovingUp();
        setUp.elevatorMovingToStoreys("1", andThen("2"));
        elevatorService.buttonPanel.buttonPressed("1");
        setUp.elevatorMovingToStoreys("3");
        assertThat.elevatorIsServingAtStorey("3");

        assertThat.elevatorIsMovingDown();
        setUp.elevatorMovingToStoreys("2", andThen("1"));

        assertThat.elevatorIsServingAtStorey("1");

        assertThat(elevatorService.elevator::currentState, eventually(is(STATIONARY)));
        verify(elevatorHardwareCommands, times(1)).moveUp();
        verify(elevatorHardwareCommands, times(1)).moveDown();
    }

    @Test
    @DisplayName("A down going Elevator serves lower storeys and then comes back for upper storeys")
    public void testDownGoingElevatorServesLowerStoreysAndThenUpperStoreys() {
        setUp.doorOpenAction();
        setUp.doorClosedAction();
        setUp.stationaryElevatorAt("3");
        setUp.elevatorMoveUpAction();
        setUp.elevatorMoveDownAction();
        setUp.elevatorStopActionAtStoreys("GROUND", "2");

        elevatorService.buttonPanel.buttonPressed("GROUND");
        assertThat.elevatorIsMovingDown();
        setUp.elevatorMovingToStoreys("2", andThen("1"));
        elevatorService.buttonPanel.buttonPressed("2");
        setUp.elevatorMovingToStoreys("GROUND");
        assertThat.elevatorIsServingAtStorey("GROUND");

        assertThat.elevatorIsMovingUp();
        setUp.elevatorMovingToStoreys("1", andThen("2"));

        assertThat.elevatorIsServingAtStorey("2");

        assertThat(elevatorService.elevator::currentState, eventually(is(STATIONARY)));
        verify(elevatorHardwareCommands, times(1)).moveDown();
        verify(elevatorHardwareCommands, times(1)).moveUp();
    }

    @AfterEach
    public void stopElevatorService() {
        elevatorService.stop();
    }
}