package com.cs.elevator.door;

import com.cs.elevator.ElevatorService;
import com.cs.elevator.door.ElevatorDoor.ElevatorDoorStateChangeEvent;
import com.cs.elevator.hardware.ElevatorHardware;
import com.cs.elevator.hardware.ElevatorHardware.DoorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardwareCommands;
import com.cs.elevator.util.ElevatorTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.cs.elevator.Elevator.ElevatorStates.SERVING;
import static com.cs.elevator.Elevator.ElevatorStates.STATIONARY;
import static com.cs.elevator.door.ElevatorDoor.ElevatorDoorStates.*;
import static com.cs.elevator.util.AsyncMatcher.eventually;
import static com.cs.elevator.util.ElevatorTestUtils.testUtilsFor;
import static com.cs.elevator.util.StateTransitionMatcher.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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

    private DoorSignalsAdapter doorHardwareSignals;
    private ElevatorService elevatorService;
    private ElevatorTestUtils setUp;

    @BeforeEach
    public void initElevator() {
        elevatorService = new ElevatorService(new ElevatorHardwareCommands(elevatorCommands, doorCommands));
        elevatorService.registerElevatorDoorEventListener(elevatorDoorEventListener);
        doorHardwareSignals = elevatorService.doorHardwareSignals();
        setUp = testUtilsFor(elevatorService).withDoorControls(doorHardwareSignals, doorCommands);
    }

    @Test
    @DisplayName("Elevator is initialized correctly")
    public void testElevatorInitializedWithClosedDoor() {
        assertThat(elevatorService.currentElevatorState(), is(STATIONARY));
        assertThat(elevatorService.currentElevatorDoorState(), is(CLOSED));
    }

    @Test
    @DisplayName("Open button, when pressed while elevator is stationary at a storey, opens the elevator door")
    public void testOpenButtonPressedWhenElevatorStationaryAtAStorey() {
        setUp.doorOpenAction();

        assertThat(elevatorService.currentElevatorState(), is(STATIONARY));
        elevatorService.buttonPressed("OPEN");

        assertThat(elevatorService::currentElevatorDoorState, eventually(is(OPEN)));
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
        setUp.doorClosedAction();

        assertThat(elevatorService.currentElevatorState(), is(SERVING));
        elevatorService.buttonPressed("CLOSE");

        assertThat(elevatorService::currentElevatorDoorState, eventually(is(CLOSED)));
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

        elevatorService.buttonPressed("CLOSE");

        assertThat(elevatorService.currentElevatorDoorState(), is(OPENING));
        verify(elevatorDoorEventListener, times(0)).onDoorStatusChange(any(ElevatorDoorStateChangeEvent.class));
    }

    @Test
    @DisplayName("Open button, when pressed, starts opening a closing elevator door")
    public void testOpenButtonPressOpensAClosingDoor() {
        setUpAClosingDoor();
        setUp.doorOpenAction();

        assertThat(elevatorService.currentElevatorState(), is(SERVING));
        elevatorService.buttonPressed("OPEN");

        assertThat(elevatorService::currentElevatorDoorState, eventually(is(OPEN)));
        ArgumentCaptor<ElevatorDoorStateChangeEvent> stateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        verify(elevatorDoorEventListener, times(2)).onDoorStatusChange(stateChangeEvent.capture());
        List<ElevatorDoorStateChangeEvent> stateChangeEvents = stateChangeEvent.getAllValues();
        assertThat(stateChangeEvents.get(0), is(transitioning(from(CLOSING), to(OPENING))));
        assertThat(stateChangeEvents.get(1), is(transitioning(from(OPENING), to(OPEN))));
    }

    @Test
    @DisplayName("An open door closes after a delay of 5 seconds")
    public void testOpenDoorClosesAfter5Seconds() {
        setUp.doorOpenAction();
        setUp.doorClosedAction();

        assertThat(elevatorService.currentElevatorState(), is(STATIONARY));
        elevatorService.buttonPressed("OPEN");

        assertThat(elevatorService::currentElevatorDoorState, eventually(is(OPEN)));
        assertThat(elevatorService::currentElevatorDoorState, eventually(is(CLOSED)));
        ArgumentCaptor<ElevatorDoorStateChangeEvent> stateChangeEvent = ArgumentCaptor.forClass(ElevatorDoorStateChangeEvent.class);
        verify(elevatorDoorEventListener, timeout(3000).times(4)).onDoorStatusChange(stateChangeEvent.capture());
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
        setUp.doorOpenAction();

        assertThat(elevatorService.currentElevatorState(), is(SERVING));
        doorHardwareSignals.obstacleDetected();

        assertThat(elevatorService::currentElevatorDoorState, eventually(is(OPEN)));
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

        elevatorService.buttonPressed("OPEN");

        assertThat(elevatorService.currentElevatorDoorState(), is(CLOSED));
        verify(elevatorDoorEventListener, times(0)).onDoorStatusChange(any(ElevatorDoorStateChangeEvent.class));
    }

    private void setUpAnOpeningDoor() {
        doorHardwareSignals.doorIsOpening();
        clearInvocations(elevatorDoorEventListener);
    }

    private void setUpAnOpenDoor() {
        setUp.doorOpenAction();
        elevatorService.buttonPressed("OPEN");
        clearInvocations(doorCommands);
        clearInvocations(elevatorDoorEventListener);
    }

    private void setUpAClosingDoor() {
        doorHardwareSignals.doorIsClosing();
        clearInvocations(elevatorDoorEventListener);
    }

}
