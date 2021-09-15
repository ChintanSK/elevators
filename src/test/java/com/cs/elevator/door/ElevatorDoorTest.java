package com.cs.elevator.door;

import com.cs.elevator.Elevator;
import com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStateChangeEvent;
import com.cs.elevator.door.hardware.ElevatorDoorControlPanel;
import com.cs.elevator.door.hardware.ElevatorDoorHardwareAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.cs.elevator.door.ElevatorDoorState.ElevatorDoorStates.*;
import static com.cs.elevator.door.ElevatorDoorStateTransitionMatcher.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElevatorDoorTest {

    @Mock
    private ElevatorDoorHardwareAdapter.Commands elevatorDoorHardwareCommands;

    @Mock
    private ElevatorDoorEventListener elevatorDoorEventListener;

    private Elevator elevator;
    private ElevatorDoorControlPanel controlPanel;
    private ElevatorDoorHardwareAdapter.Signals hardwareSignals;

    @BeforeEach
    public void initElevator() {
        elevator = new Elevator(elevatorDoorHardwareCommands);
        elevator.registerElevatorDoorEventListener(elevatorDoorEventListener);
        controlPanel = elevator;
        hardwareSignals = elevator;
    }

    @Test
    public void testElevatorInitializedWithClosedDoor() {
        assertThat(elevator.currentElevatorDoorState(), is(CLOSED));
        assertThat(elevator, is(instanceOf(ElevatorDoorControlPanel.class)));
        assertThat(elevator, is(instanceOf(ElevatorDoorHardwareAdapter.Signals.class)));
    }

    @Test
    public void testOpenButtonPressedOnElevatorDoorControlPanel() {
        setUpDoorOpenAction();

        controlPanel.openElevatorDoor();

        assertThat(elevator.currentElevatorDoorState(), is(OPEN));
        ArgumentCaptor<ElevatorDoorStateChangeEvent> stateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        verify(elevatorDoorEventListener, times(2)).onDoorStatusChange(stateChangeEvent.capture());
        List<ElevatorDoorStateChangeEvent> stateChangeEvents = stateChangeEvent.getAllValues();
        assertThat(stateChangeEvents.get(0), is(transitioning(from(CLOSED), to(OPENING))));
        assertThat(stateChangeEvents.get(1), is(transitioning(from(OPENING), to(OPEN))));
    }

    @Test
    public void testCloseButtonPressedOnElevatorDoorControlPanel() {
        setUpAnOpenDoor();
        setUpDoorClosedAction();

        controlPanel.closeElevatorDoor();

        assertThat(elevator.currentElevatorDoorState(), is(CLOSED));
        ArgumentCaptor<ElevatorDoorStateChangeEvent> stateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        verify(elevatorDoorEventListener, times(2)).onDoorStatusChange(stateChangeEvent.capture());
        List<ElevatorDoorStateChangeEvent> stateChangeEvents = stateChangeEvent.getAllValues();
        assertThat(stateChangeEvents.get(0), is(transitioning(from(OPEN), to(CLOSING))));
        assertThat(stateChangeEvents.get(1), is(transitioning(from(CLOSING), to(CLOSED))));
    }

    @Test
    public void testCloseButtonPressHasNoImpactWhenElevatorDoorIsOpening() {
        setUpAnOpeningDoor();

        controlPanel.closeElevatorDoor();

        assertThat(elevator.currentElevatorDoorState(), is(OPENING));
        verify(elevatorDoorEventListener, times(0)).onDoorStatusChange(any(ElevatorDoorStateChangeEvent.class));
    }

    @Test
    public void testOpenButtonPressOpensAClosingDoor() {
        setUpAClosingDoor();
        setUpDoorOpenAction();

        controlPanel.openElevatorDoor();

        assertThat(elevator.currentElevatorDoorState(), is(OPEN));
        ArgumentCaptor<ElevatorDoorStateChangeEvent> stateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        verify(elevatorDoorEventListener, times(2)).onDoorStatusChange(stateChangeEvent.capture());
        List<ElevatorDoorStateChangeEvent> stateChangeEvents = stateChangeEvent.getAllValues();
        assertThat(stateChangeEvents.get(0), is(transitioning(from(CLOSING), to(OPENING))));
        assertThat(stateChangeEvents.get(1), is(transitioning(from(OPENING), to(OPEN))));
    }

    @Test
    public void testClosingDoorOpensWhenAnyObstacleDetected() {
        setUpAClosingDoor();
        setUpDoorOpenAction();

        hardwareSignals.obstacleDetected();

        assertThat(elevator.currentElevatorDoorState(), is(OPEN));
        ArgumentCaptor<ElevatorDoorStateChangeEvent> stateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        verify(elevatorDoorEventListener, times(2)).onDoorStatusChange(stateChangeEvent.capture());
        List<ElevatorDoorStateChangeEvent> stateChangeEvents = stateChangeEvent.getAllValues();
        assertThat(stateChangeEvents.get(0), is(transitioning(from(CLOSING), to(OPENING))));
        assertThat(stateChangeEvents.get(1), is(transitioning(from(OPENING), to(OPEN))));
    }

    private void setUpDoorOpenAction() {
        doAnswer(invocation -> {
            hardwareSignals.doorIsOpening();
            hardwareSignals.doorOpened();
            return null;
        }).when(elevatorDoorHardwareCommands).open();
    }

    private void setUpDoorClosedAction() {
        doAnswer(invocation -> {
            hardwareSignals.doorIsClosing();
            hardwareSignals.doorClosed();
            return null;
        }).when(elevatorDoorHardwareCommands).close();
    }

    private void setUpAnOpeningDoor() {
        hardwareSignals.doorIsOpening();
        clearInvocations(elevatorDoorEventListener);
    }

    private void setUpAnOpenDoor() {
        setUpDoorOpenAction();
        controlPanel.openElevatorDoor();
        clearInvocations(elevatorDoorHardwareCommands);
        clearInvocations(elevatorDoorEventListener);
    }

    private void setUpAClosingDoor() {
        hardwareSignals.doorIsClosing();
        clearInvocations(elevatorDoorEventListener);
    }
}
