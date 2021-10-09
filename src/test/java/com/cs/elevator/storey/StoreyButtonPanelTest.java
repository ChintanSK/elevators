package com.cs.elevator.storey;

import com.cs.elevator.ElevatorService;
import com.cs.elevator.hardware.ElevatorHardware.DoorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.DoorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorCommandsAdapter;
import com.cs.elevator.hardware.ElevatorHardware.ElevatorSignalsAdapter;
import com.cs.elevator.hardware.ElevatorHardwareCommands;
import com.cs.elevator.storey.hardware.buttonpanel.StoreyButtonPanel;
import com.cs.elevator.storey.hardware.buttonpanel.StoreyButtonPanelAdapter;
import com.cs.elevator.util.ElevatorTestUtils;
import com.cs.elevator.util.TestAssertions;
import com.cs.elevator.util.TestSetUp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.cs.elevator.ElevatorDirection.DOWN;
import static com.cs.elevator.ElevatorDirection.UP;
import static com.cs.elevator.util.ElevatorTestUtils.testUtilsFor;
import static com.cs.elevator.util.TestSetUp.andThen;
import static com.cs.elevator.util.TestSetUp.createStoreys;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
public class StoreyButtonPanelTest {
    private ElevatorService elevatorService;
    @Mock
    private ElevatorCommandsAdapter elevatorHardwareCommands;
    @Mock
    private DoorCommandsAdapter doorHardwareCommands;
    private TestSetUp setUp;
    private TestAssertions assertThat;
    private StoreyButtonPanelAdapter storeyButtonPanel;

    @BeforeAll
    public static void initStoreys() {
        createStoreys(4);
    }

    @BeforeEach
    public void initElevator() {
        elevatorService = new ElevatorService(new ElevatorHardwareCommands(elevatorHardwareCommands, doorHardwareCommands));
        storeyButtonPanel = new StoreyButtonPanel(elevatorService);
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
    public void testWhenDownButtonPressedAtStorey3ElevatorMovesUpFromGround() {
        setUp.stationaryElevatorAt("GROUND");
        setUp.doorOpenAction();
        setUp.doorClosedAction();
        setUp.elevatorMoveUpAction();
        setUp.elevatorStopActionAtStoreys("1");

        storeyButtonPanel.downButtonPressed("1");

        assertThat.elevatorIsMovingUp();
        setUp.elevatorMovingToStoreys("1");
        assertThat.elevatorIsServingAtStorey("1");
    }

    @Test
    public void testWhenUpButtonPressedAtStorey1ElevatorMovesDownFrom3() {
        setUp.stationaryElevatorAt("3");
        setUp.doorOpenAction();
        setUp.doorClosedAction();
        setUp.elevatorMoveDownAction();
        setUp.elevatorStopActionAtStoreys("1");

        storeyButtonPanel.upButtonPressed("1");

        assertThat.elevatorIsMovingDown();
        setUp.elevatorMovingToStoreys("2", andThen("1"));
        assertThat.elevatorIsServingAtStorey("1");
    }

    @Test
    public void testUpMovingElevatorDoorOpensAt2WhenUpButtonPressedAt2() {
        setUp.stationaryElevatorAt("2");
        setUp.doorOpenAction();
        setUp.doorClosedAction();

        storeyButtonPanel.upButtonPressed("2");

        assertThat.elevatorIsServingAtStorey("2");
        assertThat(elevatorService.direction(), is(UP));
    }

    @Test
    public void testUpMovingElevatorDoorOpensAt2WhenDownButtonPressedAt2() {
        setUp.stationaryElevatorAt("2");
        setUp.doorOpenAction();
        setUp.doorClosedAction();

        storeyButtonPanel.downButtonPressed("2");

        assertThat.elevatorIsServingAtStorey("2");
        assertThat(elevatorService.direction(), is(DOWN));
    }
}
