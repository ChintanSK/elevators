package com.cs.elevator.door;

import com.cs.elevator.Elevator;
import com.cs.elevator.door.ElevatorDoor.ElevatorDoorStateChangeEvent;
import com.cs.elevator.hardware.ElevatorHardware;
import com.cs.elevator.hardware.ElevatorHardwareCommands;
import com.cs.elevator.ElevatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.cs.elevator.door.ElevatorDoor.ElevatorDoorStates.*;
import static com.cs.elevator.door.StateTransitionMatcher.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElevatorDoorTest {

    @Mock
    private ElevatorHardware.DoorCommandsAdapter doorCommands;
    @Mock
    private ElevatorHardware.ElevatorCommandsAdapter elevatorCommands;

    @Mock
    private ElevatorDoorEventListener elevatorDoorEventListener;

    private Elevator elevator;
    private ElevatorHardware.DoorSignalsAdapter doorHardwareSignals;
    private ElevatorService elevatorService;

    @BeforeEach
    public void initElevator() {
        elevator = new Elevator();
        elevator.door.registerElevatorDoorEventListener(elevatorDoorEventListener);
        elevatorService = new ElevatorService(elevator, new ElevatorHardwareCommands(elevatorCommands, doorCommands));
        doorHardwareSignals = elevatorService.doorService;
    }

    @Test
    @DisplayName("Elevator is initialized correctly")
    public void testElevatorInitializedWithClosedDoor() {
        assertThat(elevator.isStationary(), is(true));
        assertThat(elevator.door, is(notNullValue()));
        assertThat(elevator.door.isClosed(), is(true));
    }

    @Test
    @DisplayName("Open button, when pressed while elevator is stationary at a storey, opens the elevator door")
    public void testOpenButtonPressedWhenElevatorStationaryAtAStorey() {
        setUpDoorOpenAction();

        assertThat(elevator.isStationary(), is(true));
        elevatorService.buttonPanel.buttonPressed("OPEN");

        assertThat(elevator.door.isOpen(), is(true));
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

        assertThat(elevator.isServing(), is(true));
        elevatorService.buttonPanel.buttonPressed("CLOSE");

        assertThat(elevator.door.isClosed(), is(true));
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

        assertThat(elevator.isServing(), is(true));
        elevatorService.buttonPanel.buttonPressed("CLOSE");

        assertThat(elevator.door.isOpening(), is(true));
        verify(elevatorDoorEventListener, times(0)).onDoorStatusChange(any(ElevatorDoorStateChangeEvent.class));
    }

    @Test
    @DisplayName("Open button, when pressed, starts opening a closing elevator door")
    public void testOpenButtonPressOpensAClosingDoor() {
        setUpAClosingDoor();
        setUpDoorOpenAction();

        assertThat(elevator.isServing(), is(true));
        elevatorService.buttonPanel.buttonPressed("OPEN");

        assertThat(elevator.door.isOpen(), is(true));
        ArgumentCaptor<ElevatorDoorStateChangeEvent> stateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        verify(elevatorDoorEventListener, times(2)).onDoorStatusChange(stateChangeEvent.capture());
        List<ElevatorDoorStateChangeEvent> stateChangeEvents = stateChangeEvent.getAllValues();
        assertThat(stateChangeEvents.get(0), is(transitioning(from(CLOSING), to(OPENING))));
        assertThat(stateChangeEvents.get(1), is(transitioning(from(OPENING), to(OPEN))));
    }

    @Test
    @DisplayName("An open door closes after a delay of 5 seconds")
    public void testOpenDoorClosesAfter5Seconds() {
        setUpDoorOpenAction();
        setUpDoorClosedAction();

        assertThat(elevator.isStationary(), is(true));
        elevatorService.buttonPanel.buttonPressed("OPEN");

        assertThat(elevator.door.isOpen(), is(true));
        ArgumentCaptor<ElevatorDoorStateChangeEvent> stateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        verify(elevatorDoorEventListener, timeout(6000).times(4)).onDoorStatusChange(stateChangeEvent.capture());
        List<ElevatorDoorStateChangeEvent> stateChangeEvents = stateChangeEvent.getAllValues();
        assertThat(stateChangeEvents.get(0), is(transitioning(from(CLOSED), to(OPENING))));
        assertThat(stateChangeEvents.get(1), is(transitioning(from(OPENING), to(OPEN))));
        assertThat(stateChangeEvents.get(2), is(transitioning(from(OPEN), to(CLOSING))));
        assertThat(stateChangeEvents.get(3), is(transitioning(from(CLOSING), to(CLOSED))));
    }

    @Test
    @DisplayName("A closing door starts opening again if any obstacle is detected")
    public void testClosingDoorOpensWhenAnyObstacleDetected() {
        setUpAClosingDoor();
        setUpDoorOpenAction();

        assertThat(elevator.isServing(), is(true));
        doorHardwareSignals.obstacleDetected();

        assertThat(elevator.door.isOpen(), is(true));
        ArgumentCaptor<ElevatorDoorStateChangeEvent> stateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        verify(elevatorDoorEventListener, times(2)).onDoorStatusChange(stateChangeEvent.capture());
        List<ElevatorDoorStateChangeEvent> stateChangeEvents = stateChangeEvent.getAllValues();
        assertThat(stateChangeEvents.get(0), is(transitioning(from(CLOSING), to(OPENING))));
        assertThat(stateChangeEvents.get(1), is(transitioning(from(OPENING), to(OPEN))));
    }

    @Test
    @DisplayName("Open button, when pressed, has no impact when Elevator is moving and not stationary")
    public void testOpenButtonPressHasNoImpactWhenElevatorIsMoving() {
        elevatorService.elevatorMoving();
        assertThat(elevator.isMoving(), is(true));

        elevatorService.buttonPanel.buttonPressed("OPEN");

        assertThat(elevator.door.isClosed(), is(true));
        verify(elevatorDoorEventListener, times(0)).onDoorStatusChange(any(ElevatorDoorStateChangeEvent.class));
    }

    private void setUpDoorOpenAction() {
        doAnswer(invocation -> {
            doorHardwareSignals.doorIsOpening();
            doorHardwareSignals.doorOpened();
            return null;
        }).when(doorCommands).open();
    }

    private void setUpDoorClosedAction() {
        doAnswer(invocation -> {
            doorHardwareSignals.doorIsClosing();
            doorHardwareSignals.doorClosed();
            return null;
        }).when(doorCommands).close();
    }

    private void setUpAnOpeningDoor() {
        doorHardwareSignals.doorIsOpening();
        clearInvocations(elevatorDoorEventListener);
    }

    private void setUpAnOpenDoor() {
        setUpDoorOpenAction();
        elevatorService.buttonPanel.buttonPressed("OPEN");
        clearInvocations(doorCommands);
        clearInvocations(elevatorDoorEventListener);
    }

    private void setUpAClosingDoor() {
        doorHardwareSignals.doorIsClosing();
        clearInvocations(elevatorDoorEventListener);
    }

}
