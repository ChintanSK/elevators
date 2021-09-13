package com.cs.elevators.door;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.cs.elevators.door.Door.DoorStatus.*;
import static com.cs.elevators.door.Door.aClosedDoor;
import static com.cs.elevators.door.Door.anOpenDoor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoorTest {

    @Mock
    private DoorEventListener doorEventListener;

    @Test
    void testNewDoor() {
        Door door = aClosedDoor(doorEventListener);
        assertThat(doorEventListener, is(notNullValue()));
        assertThat(door.status(), is(CLOSED));
    }

    @Test
    @Timeout(6)
    void testDoorOpen() throws Exception {
        Door door = aClosedDoor(doorEventListener);
        door.open();
        waitIfStatus(door, CLOSED);
        assertThat(door.status(), is(OPENING));
        simulateDoorOpenedSignal(door);
        waitIfStatus(door, OPENING);
        assertThat(door.status(), is(OPEN));
        InOrder eventOrder = inOrder(doorEventListener);
        eventOrder.verify(doorEventListener, times(1)).onOpening();
        eventOrder.verify(doorEventListener, times(1)).onOpen();
    }

    @Test
    @Timeout(6)
    void testDoorClose() throws Exception {
        Door door = anOpenDoor(doorEventListener);
        door.close();
        waitIfStatus(door, OPEN);
        assertThat(door.status(), is(CLOSING));
        simulateDoorClosedSignal(door);
        waitIfStatus(door, CLOSING);
        assertThat(door.status(), is(CLOSED));
        InOrder eventOrder = inOrder(doorEventListener);
        eventOrder.verify(doorEventListener, times(1)).onClosing();
        eventOrder.verify(doorEventListener, times(1)).onClosed();
    }

    @Test
    @Timeout(11)
    void testDoorClosesAfterOpeningWhenNoInterrupt() throws Exception {
        Door door = aClosedDoor(doorEventListener);
        door.open();
        waitIfStatus(door, CLOSED);
        assertThat(door.status(), is(OPENING));
        simulateDoorOpenedSignal(door);
        waitIfStatus(door, OPENING);
        assertThat(door.status(), is(OPEN));
        waitIfStatus(door, OPEN);
        assertThat(door.status(), is(CLOSING));
        simulateDoorClosedSignal(door);
        waitIfStatus(door, CLOSING);
        assertThat(door.status(), is(CLOSED));
        InOrder eventOrder = inOrder(doorEventListener);
        eventOrder.verify(doorEventListener, times(1)).onOpening();
        eventOrder.verify(doorEventListener, times(1)).onOpen();
        eventOrder.verify(doorEventListener, times(1)).onClosing();
        eventOrder.verify(doorEventListener, times(1)).onClosed();
    }

    @Test
    @Timeout(11)
    public void testDoorInterruptedWhileClosing() throws Exception {
        Door door = anOpenDoor(doorEventListener);
        door.close();
        waitIfStatus(door, OPEN);
        assertThat(door.status(), is(CLOSING));
        door.open();
        waitIfStatus(door, CLOSING);
        assertThat(door.status(), is(OPENING));
        verify(doorEventListener, times(0)).onClosed();
        InOrder eventOrder = inOrder(doorEventListener);
        eventOrder.verify(doorEventListener, times(1)).onClosing();
        eventOrder.verify(doorEventListener, times(1)).onOpening();
    }

    @Test
    @Timeout(16)
    public void testDoorClosesAgainAfterOpeningDueToInterrupt() throws Exception {
        Door door = anOpenDoor(doorEventListener);
        door.close();
        waitIfStatus(door, OPEN);
        assertThat(door.status(), is(CLOSING));
        door.open();
        waitIfStatus(door, CLOSING);
        assertThat(door.status(), is(OPENING));
        simulateDoorOpenedSignal(door);
        waitIfStatus(door, OPENING);
        assertThat(door.status(), is(OPEN));
        waitIfStatus(door, OPEN);
        assertThat(door.status(), is(CLOSING));
        InOrder eventOrder = inOrder(doorEventListener);
        eventOrder.verify(doorEventListener, times(1)).onClosing();
        eventOrder.verify(doorEventListener, times(1)).onOpening();
        eventOrder.verify(doorEventListener, times(1)).onOpen();
        eventOrder.verify(doorEventListener, times(1)).onClosing();
    }


    @Test
    @Timeout(6)
    public void testDoorCannotCloseWhileOpening() throws Exception {
        Door door = aClosedDoor(doorEventListener);
        door.open();
        waitIfStatus(door, CLOSED);
        assertThat(door.status(), is(OPENING));
        door.close();
        verify(doorEventListener, times(0)).onClosing();
    }

    private void simulateDoorOpenedSignal(Door door) throws InterruptedException {
        Thread.sleep(2500L);
        door.doorOpened();
    }

    private void simulateDoorClosedSignal(Door door) throws InterruptedException {
        Thread.sleep(2500L);
        door.doorClosed();
    }

    private void waitIfStatus(Door door, Door.DoorStatus doorStatus) throws InterruptedException {
        while (door.status().equals(doorStatus)) {
            Thread.sleep(250L);
        }
    }

}
