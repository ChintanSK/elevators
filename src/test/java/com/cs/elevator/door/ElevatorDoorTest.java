package com.cs.elevator.door;

import com.cs.elevator.Elevator;
import com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStateChangeEvent;
import com.cs.elevator.hardware.ElevatorHardware;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElevatorDoorTest {

    @Mock
    private ElevatorHardware.DoorCommandsAdapter doorHardwareCommands;

    @Mock
    private ElevatorDoorEventListener elevatorDoorEventListener;

    private Elevator elevator;
    private ElevatorHardware.DoorSignalsAdapter doorHardwareSignals;

    @BeforeEach
    public void initElevator() {
        elevator = new Elevator(doorHardwareCommands);
        elevator.registerElevatorDoorEventListener(elevatorDoorEventListener);
        doorHardwareSignals = elevator;
    }

    @Test
    @DisplayName("Elevator is initialized correctly")
    public void testElevatorInitializedWithClosedDoor() {
        assertThat(elevator.currentElevatorState(), is(STATIONARY));
        assertThat(elevator.door, is(notNullValue()));
        assertThat(elevator.currentDoorState(), is(CLOSED));
        assertThat(elevator.buttonPanel, is(notNullValue()));
        assertThat(elevator, is(instanceOf(ElevatorHardware.DoorSignalsAdapter.class)));
    }

    @Test
    @DisplayName("Open button, when pressed while elevator is stationary at a storey, opens the elevator door")
    public void testOpenButtonPressedWhenElevatorStationaryAtAStorey() {
        setUpDoorOpenAction();

        assertThat(elevator.currentElevatorState(), is(STATIONARY));
        elevator.buttonPanel.buttonPressed("OPEN");

        assertThat(elevator.currentDoorState(), is(OPEN));
        ArgumentCaptor<ElevatorDoorStateChangeEvent> stateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        verify(elevatorDoorEventListener, times(2)).onDoorStatusChange(stateChangeEvent.capture());
        List<ElevatorDoorStateChangeEvent> stateChangeEvents = stateChangeEvent.getAllValues();
        assertThat(stateChangeEvents.get(0), is(transitioning(from(CLOSED), to(OPENING))));
        assertThat(stateChangeEvents.get(1), is(transitioning(from(OPENING), to(OPEN))));
    }

    @Test
    @DisplayName("Close button, when pressed while elevator is stationary at a storey with door opened, closes the elevator door")
    public void testCloseButtonPressedWhenElevatorStationaryAtAStoreyWithDoorOpen() {
        setUpAnOpenDoor();
        setUpDoorClosedAction();

        assertThat(elevator.currentElevatorState(), is(STATIONARY));
        elevator.buttonPanel.buttonPressed("CLOSE");

        assertThat(elevator.currentDoorState(), is(CLOSED));
        ArgumentCaptor<ElevatorDoorStateChangeEvent> stateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        verify(elevatorDoorEventListener, times(2)).onDoorStatusChange(stateChangeEvent.capture());
        List<ElevatorDoorStateChangeEvent> stateChangeEvents = stateChangeEvent.getAllValues();
        assertThat(stateChangeEvents.get(0), is(transitioning(from(OPEN), to(CLOSING))));
        assertThat(stateChangeEvents.get(1), is(transitioning(from(CLOSING), to(CLOSED))));
    }

    @Test
    @DisplayName("Close button has no impact when the elevator door is opening")
    public void testCloseButtonPressHasNoImpactWhenElevatorDoorIsOpening() {
        setUpAnOpeningDoor();

        assertThat(elevator.currentElevatorState(), is(STATIONARY));
        elevator.buttonPanel.buttonPressed("CLOSE");

        assertThat(elevator.currentDoorState(), is(OPENING));
        verify(elevatorDoorEventListener, times(0)).onDoorStatusChange(any(ElevatorDoorStateChangeEvent.class));
    }

    @Test
    @DisplayName("Open button, when pressed, starts opening a closing elevator door")
    public void testOpenButtonPressOpensAClosingDoor() {
        setUpAClosingDoor();
        setUpDoorOpenAction();

        assertThat(elevator.currentElevatorState(), is(STATIONARY));
        elevator.buttonPanel.buttonPressed("OPEN");

        assertThat(elevator.currentDoorState(), is(OPEN));
        ArgumentCaptor<ElevatorDoorStateChangeEvent> stateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        verify(elevatorDoorEventListener, times(2)).onDoorStatusChange(stateChangeEvent.capture());
        List<ElevatorDoorStateChangeEvent> stateChangeEvents = stateChangeEvent.getAllValues();
        assertThat(stateChangeEvents.get(0), is(transitioning(from(CLOSING), to(OPENING))));
        assertThat(stateChangeEvents.get(1), is(transitioning(from(OPENING), to(OPEN))));
    }

    @Test
    @DisplayName("A closing door starts opening again if any obstacle is detected")
    public void testClosingDoorOpensWhenAnyObstacleDetected() {
        setUpAClosingDoor();
        setUpDoorOpenAction();

        assertThat(elevator.currentElevatorState(), is(STATIONARY));
        doorHardwareSignals.obstacleDetected();

        assertThat(elevator.currentDoorState(), is(OPEN));
        ArgumentCaptor<ElevatorDoorStateChangeEvent> stateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        verify(elevatorDoorEventListener, times(2)).onDoorStatusChange(stateChangeEvent.capture());
        List<ElevatorDoorStateChangeEvent> stateChangeEvents = stateChangeEvent.getAllValues();
        assertThat(stateChangeEvents.get(0), is(transitioning(from(CLOSING), to(OPENING))));
        assertThat(stateChangeEvents.get(1), is(transitioning(from(OPENING), to(OPEN))));
    }

    @Test
    @DisplayName("Open button, when pressed, has no impact when Elevator is moving and not stationary")
    public void testOpenButtonPressHasNoImpactWhenElevatorIsMoving() {
        elevator.elevatorMovingUp();
        assertThat(elevator.currentElevatorState(), is(MOVING_UP));

        elevator.buttonPanel.buttonPressed("OPEN");

        assertThat(elevator.currentDoorState(), is(CLOSED));
        verify(elevatorDoorEventListener, times(0)).onDoorStatusChange(any(ElevatorDoorStateChangeEvent.class));
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

    private void setUpAnOpeningDoor() {
        doorHardwareSignals.doorIsOpening();
        clearInvocations(elevatorDoorEventListener);
    }

    private void setUpAnOpenDoor() {
        setUpDoorOpenAction();
        elevator.buttonPanel.buttonPressed("OPEN");
        clearInvocations(doorHardwareCommands);
        clearInvocations(elevatorDoorEventListener);
    }

    private void setUpAClosingDoor() {
        doorHardwareSignals.doorIsClosing();
        clearInvocations(elevatorDoorEventListener);
    }
}
