package com.cs.elevator;

import com.cs.elevator.door.ElevatorDoor.ElevatorDoorStateChangeEvent;
import com.cs.elevator.door.ElevatorDoorEventListener;
import com.cs.elevator.hardware.ElevatorHardware.DoorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.DoorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardwareCommands;
import com.cs.elevator.service.ElevatorService;
import com.cs.elevator.storey.Storey;
import com.cs.elevator.storey.Storeys;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.cs.elevator.door.ElevatorDoor.ElevatorDoorStates.*;
import static com.cs.elevator.door.ElevatorDoorStateTransitionMatcher.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElevatorTest {

    @Mock
    private ElevatorCommandsAdapter elevatorHardwareCommands;
    @Mock
    private DoorCommandsAdapter doorHardwareCommands;

    @Mock
    private ElevatorDoorEventListener elevatorDoorEventListener;

    private Elevator elevator;
    private ElevatorService elevatorService;
    private DoorSignalsAdapter doorHardwareSignals;
    private ElevatorSignalsAdapter elevatorHardwareSignals;
    private ArgumentCaptor<ElevatorDoorStateChangeEvent> stateChangeEvent;

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
        elevatorService = new ElevatorService(elevator, new ElevatorHardwareCommands(elevatorHardwareCommands, doorHardwareCommands));
        elevator.door.registerElevatorDoorEventListener(elevatorDoorEventListener);
        stateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        doorHardwareSignals = elevatorService.doorService;
        elevatorHardwareSignals = elevatorService;
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
        setUpStationaryElevatorAt("GROUND");

        elevatorService.buttonPanel.buttonPressed("GROUND");

        assertThat(elevator.isStationary(), is(true));
        assertThat(elevatorService.currentStorey(), is("GROUND"));
        assertThat(elevator.door.isOpen(), is(true));
        verify(elevatorDoorEventListener, times(2)).onDoorStatusChange(stateChangeEvent.capture());
        List<ElevatorDoorStateChangeEvent> stateChangeEvents = stateChangeEvent.getAllValues();
        assertThat(stateChangeEvents.get(0), is(transitioning(from(CLOSED), to(OPENING))));
        assertThat(stateChangeEvents.get(1), is(transitioning(from(OPENING), to(OPEN))));
    }

    @Test
    @DisplayName("Elevator records all the requests made from within the Elevator by button presses")
    public void testElevatorRecordsElevatorRequests() {
        elevatorHardwareSignals.elevatorStationary("GROUND");

        elevatorService.buttonPanel.buttonPressed("1");
        elevatorService.buttonPanel.buttonPressed("2");
        elevatorService.buttonPanel.buttonPressed("3");

        assertThat(elevatorService.requests.getAll(), hasItems(new Storey(1), new Storey(2), new Storey(3)));
    }

    @Test
    @DisplayName("Elevator moves up when storey button pressed")
    public void testElevatorMovesUpWhenStoreyButtonPressed() {
        setUpStationaryElevatorAt("GROUND");
        setUpElevatorMoveUpAction();

        elevatorService.buttonPanel.buttonPressed("3");

        verify(elevatorHardwareCommands, timeout(7000L).times(1)).moveUp();
        assertThat(elevator.isMoving(), is(true));
    }

    @Test
    @DisplayName("Elevator moves down when storey button pressed")
    public void testElevatorMovesDownWhenStoreyButtonPressed() {
        setUpStationaryElevatorAt("3");
        setUpElevatorMoveDownAction();

        elevatorService.buttonPanel.buttonPressed("1");

        verify(elevatorHardwareCommands, timeout(7000L).times(1)).moveDown();
        assertThat(elevator.isMoving(), is(true));
    }

    private void setUpDoorOpenAction() {
        doAnswer(invocation -> {
            doorHardwareSignals.doorIsOpening();
            doorHardwareSignals.doorOpened();
            return null;
        }).when(doorHardwareCommands).open();
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

    @AfterEach
    public void stopElevatorService() {
        elevatorService.stop();
    }
}