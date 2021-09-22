package com.cs.elevator;

import com.cs.elevator.door.ElevatorDoorEventListener;
import com.cs.elevator.door.ElevatorDoorState;
import com.cs.elevator.hardware.ElevatorHardware;
import com.cs.elevator.hardware.ElevatorHardware.DoorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorSignalsAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.cs.elevator.ElevatorState.ElevatorStates.MOVING_UP;
import static com.cs.elevator.ElevatorState.ElevatorStates.STATIONARY;
import static com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStates.*;
import static com.cs.elevator.door.ElevatorDoorStateTransitionMatcher.*;
import static com.cs.elevator.door.ElevatorDoorStateTransitionMatcher.to;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElevatorTest {

    @Mock
    private ElevatorHardware.DoorCommandsAdapter doorHardwareCommands;

    @Mock
    private ElevatorDoorEventListener elevatorDoorEventListener;

    private Elevator elevator;
    private DoorSignalsAdapter doorHardwareSignals;
    private ElevatorSignalsAdapter elevatorHardwareSignals;

    @BeforeEach
    public void initElevator() {
        elevator = new Elevator(doorHardwareCommands);
        elevator.registerElevatorDoorEventListener(elevatorDoorEventListener);
        doorHardwareSignals = elevator;
        elevatorHardwareSignals = elevator;
    }

    @Test
    @DisplayName("New Elevator is always stationary")
    public void testNewElevatorIsStationary() {
        assertThat(elevator.currentElevatorState(), is(STATIONARY));
    }

    @Test
    @DisplayName("When button pressed for ground floor while the elevator is stationed at ground floor, then the door opens")
    public void testGroundButtonPressedWhileElevatorStationedAtGroundLevel() {
        setUpDoorOpenAction();
        elevator.elevatorStationary("GROUND");

        elevator.buttonPanel.buttonPressed("GROUND");

        assertThat(elevator.currentElevatorState(), is(STATIONARY));
        assertThat(elevator.currentStorey(), is("GROUND"));
        assertThat(elevator.currentDoorState(), is(OPEN));
        ArgumentCaptor<ElevatorDoorState.ElevatorDoorStateChangeEvent> stateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorState.ElevatorDoorStateChangeEvent.class);
        verify(elevatorDoorEventListener, times(2)).onDoorStatusChange(stateChangeEvent.capture());
        List<ElevatorDoorState.ElevatorDoorStateChangeEvent> stateChangeEvents = stateChangeEvent.getAllValues();
        assertThat(stateChangeEvents.get(0), is(transitioning(from(CLOSED), to(OPENING))));
        assertThat(stateChangeEvents.get(1), is(transitioning(from(OPENING), to(OPEN))));
    }

    @Test
    @DisplayName("Elevator records all the requests made from within the Elevator by button presses")
    public void testElevatorRecordsElevatorRequests() {
        elevator.elevatorStationary("GROUND");

        elevator.buttonPanel.buttonPressed("1");
        elevator.buttonPanel.buttonPressed("2");
        elevator.buttonPanel.buttonPressed("3");

        assertThat(elevator.requests.getAll(), hasItems("1", "2", "3"));
    }

    @Test
    @DisplayName("Elevator moves up when storey button pressed")
    public void testElevatorMovesUpWhenStoreyButtonPressed() {

//        assertThat(elevator.currentElevatorState(), is(MOVING_UP));
    }

    @Test
    @DisplayName("Elevator moves down when storey button pressed")
    public void testElevatorMovesDownWhenStoreyButtonPressed() {

//        assertThat(elevator.currentElevatorState(), is(MOVING_DOWN));
    }

    private void setUpDoorOpenAction() {
        doAnswer(invocation -> {
            doorHardwareSignals.doorIsOpening();
            doorHardwareSignals.doorOpened();
            return null;
        }).when(doorHardwareCommands).open();
    }
}