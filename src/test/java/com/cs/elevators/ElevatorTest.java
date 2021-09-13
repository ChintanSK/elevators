package com.cs.elevators;

import com.cs.elevators.door.DoorEventListener;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ElevatorTest {

    @Test
    public void testElevatorInitializedWithClosedDoor() {
        Elevator elevator = new Elevator();
        assertThat(elevator.door(), is(notNullValue()));
        assertThat(elevator, is(instanceOf(DoorEventListener.class)));
    }
}