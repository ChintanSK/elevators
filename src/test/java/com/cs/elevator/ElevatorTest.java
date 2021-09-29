package com.cs.elevator;

import com.cs.elevator.Elevator.ElevatorStateChangeEvent;
import com.cs.elevator.door.ElevatorDoor.ElevatorDoorStateChangeEvent;
import com.cs.elevator.door.ElevatorDoorEventListener;
import com.cs.elevator.hardware.ElevatorHardware.DoorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.DoorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardwareCommands;
import com.cs.elevator.storey.Storeys;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static com.cs.elevator.Elevator.ElevatorStates.*;
import static com.cs.elevator.door.ElevatorDoor.ElevatorDoorStates.*;
import static com.cs.elevator.door.StateTransitionMatcher.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;
import static org.mockito.Mockito.*;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

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
    private DoorSignalsAdapter doorHardwareSignals;
    private ElevatorSignalsAdapter elevatorHardwareSignals;
    private ArgumentCaptor<ElevatorDoorStateChangeEvent> elevatorDoorStateChangeEvent;
    private ArgumentCaptor<ElevatorStateChangeEvent> elevatorStateChangeEvent;

    @BeforeAll
    public static void initStoreys() {
        Storeys.addStorey(0, "GROUND");
        Storeys.addStorey(1);
        Storeys.addStorey(2);
        Storeys.addStorey(3);
    }

    @BeforeEach
    public void initElevator() {
        elevator = new Elevator();
        elevator.registerElevatorStateChangeListener(elevatorEventListener);
        elevator.door.registerElevatorDoorEventListener(elevatorDoorEventListener);
        elevatorService = new ElevatorService(elevator, new ElevatorHardwareCommands(elevatorHardwareCommands, doorHardwareCommands));
        elevatorStateChangeEvent = ArgumentCaptor.forClass(ElevatorStateChangeEvent.class);
        elevatorDoorStateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        elevatorHardwareSignals = elevatorService;
        doorHardwareSignals = elevatorService.doorService;
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
        setUpDoorOpenAction();
        setUpDoorClosedAction();
        setUpStationaryElevatorAt("GROUND");

        elevatorService.buttonPanel.buttonPressed("GROUND");

        assertThat(elevatorService.currentStorey(), is("GROUND"));
        assertThat(elevator.isServing(), is(true));
        assertThat(elevator.door.isOpen(), is(true));
        await().until(() -> elevator.isStationary());
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
        setUpStationaryElevatorAt("GROUND");

        elevatorService.buttonPanel.buttonPressed("1");
        elevatorService.buttonPanel.buttonPressed("2");
        elevatorService.buttonPanel.buttonPressed("3");

        with().pollInterval(1, SECONDS)
                .await().until(() -> elevatorService.requests.size(), is(3));
        assertThat(elevatorService.requests.contains("1"), is(true));
        assertThat(elevatorService.requests.contains("2"), is(true));
        assertThat(elevatorService.requests.contains("3"), is(true));
    }

    @Test
    @DisplayName("Elevator moves up when storey button pressed")
    public void testElevatorMovesUpWhenStoreyButtonPressed() {
        setUpStationaryElevatorAt("GROUND");
        setUpElevatorMoveUpAction();
        setUpElevatorStopAction("3");

        elevatorService.buttonPanel.buttonPressed("3");

        with().pollInterval(1, SECONDS)
                .await().until(() -> elevator.currentState(), is(MOVING));
        verify(elevatorHardwareCommands, times(1)).moveUp();
        elevatorApproaching("1", "2", "3");

        await().until(() -> elevatorService.currentStorey(), is("3"));
        verify(elevatorHardwareCommands, times(1)).stop("3");
        verify(elevatorEventListener, times(2)).onElevatorStatusChange(elevatorStateChangeEvent.capture());
        List<ElevatorStateChangeEvent> elevatorStateChangeEvents = elevatorStateChangeEvent.getAllValues();
        assertThat(elevatorStateChangeEvents.get(0), is(transitioning(from(STATIONARY), to(MOVING))));
        assertThat(elevatorStateChangeEvents.get(1), is(transitioning(from(MOVING), to(SERVING))));
    }

    @Test
    @DisplayName("Elevator moves down when storey button pressed")
    public void testElevatorMovesDownWhenStoreyButtonPressed() {
        setUpStationaryElevatorAt("3");
        setUpElevatorMoveDownAction();
        setUpElevatorStopAction("1");

        elevatorService.buttonPanel.buttonPressed("1");

        with().pollInterval(1, SECONDS)
                .await().until(() -> elevator.currentState(), is(MOVING));
        verify(elevatorHardwareCommands, times(1)).moveDown();
        elevatorApproaching("2", "1");

        await().until(() -> elevatorService.currentStorey(), is("1"));
        verify(elevatorHardwareCommands, times(1)).stop("1");
        verify(elevatorEventListener, times(2)).onElevatorStatusChange(elevatorStateChangeEvent.capture());
        List<ElevatorStateChangeEvent> elevatorStateChangeEvents = elevatorStateChangeEvent.getAllValues();
        assertThat(elevatorStateChangeEvents.get(0), is(transitioning(from(STATIONARY), to(MOVING))));
        assertThat(elevatorStateChangeEvents.get(1), is(transitioning(from(MOVING), to(SERVING))));
    }

    @Test
    @DisplayName("Elevator serves lower storeys after all requested upper storeys are served")
    public void testElevatorServesLowerStoreysAfterUpperStoreysAreServed() {
        setUpDoorOpenAction();
        setUpDoorClosedAction();
        setUpStationaryElevatorAt("GROUND");
        setUpElevatorMoveUpAction();
        setUpElevatorMoveDownAction();
        setUpElevatorStopAction("2", "3", "1");

        assertThat(elevator.currentState(), is(STATIONARY));
        assertThat(elevatorService.currentStorey(), is("GROUND"));

        elevatorService.buttonPanel.buttonPressed("2");
        elevatorService.buttonPanel.buttonPressed("3");
        with().pollInterval(1, SECONDS)
                .await().until(() -> elevator.currentState(), is(MOVING));
        elevatorService.buttonPanel.buttonPressed("1");

        elevatorApproaching("2");
        with().pollInterval(1, SECONDS)
                .await().until(() -> elevator.currentState(), is(SERVING));
        with().pollInterval(1, SECONDS)
                .await().until(() -> elevator.door.isClosed());
        with().pollInterval(1, SECONDS)
                .await().until(() -> elevator.currentState(), is(MOVING));
        verify(elevatorHardwareCommands, times(2)).moveUp();

        elevatorApproaching("3");
        with().pollInterval(1, SECONDS)
                .await().until(() -> elevator.currentState(), is(SERVING));
        with().pollInterval(1, SECONDS)
                .await().until(() -> elevator.door.isClosed());
        with().pollInterval(1, SECONDS)
                .await().until(() -> elevator.currentState(), is(MOVING));
        verify(elevatorHardwareCommands, times(1)).moveDown();

        elevatorApproaching("2", "1");
        with().pollInterval(1, SECONDS)
                .await().until(() -> elevator.currentState(), is(STATIONARY));

        assertThat(elevatorService.currentStorey(), is("1"));
    }

    private void setUpDoorOpenAction() {
        doAnswer(invocation -> {
            doorHardwareSignals.doorIsOpening();
            doorHardwareSignals.doorOpened();
            return null;
        }).when(doorHardwareCommands).open();
    }


    private void setUpDoorClosedAction() {
        doAnswer(invocation -> {
            doorHardwareSignals.doorIsClosing();
            doorHardwareSignals.doorClosed();
            return null;
        }).when(doorHardwareCommands).close();
    }

    private void setUpStationaryElevatorAt(String storeyCode) {
        elevator.makeStationary();
        elevatorService.currentStorey = storeyCode;
    }

    private void setUpElevatorMoveUpAction() {
        doAnswer(invocation -> {
            elevatorHardwareSignals.elevatorMoving();
            return null;
        }).when(elevatorHardwareCommands).moveUp();
    }

    private void setUpElevatorMoveDownAction() {
        doAnswer(invocation -> {
            elevatorHardwareSignals.elevatorMoving();
            return null;
        }).when(elevatorHardwareCommands).moveDown();
    }

    private void setUpElevatorStopAction(String... storeyCodes) {
        doAnswer(invocation -> {
            elevatorHardwareSignals.elevatorStopped(invocation.getArgument(0));
            return null;
        }).when(elevatorHardwareCommands).stop(argThat(is(oneOf(storeyCodes))));
    }

    private void elevatorApproaching(String... storeyCodes) {
        Arrays.stream(storeyCodes).forEach(elevatorHardwareSignals::elevatorApproachingStorey);
    }

    @AfterEach
    public void stopElevatorService() {
        elevatorService.stop();
    }
}