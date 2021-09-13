package com.cs.elevators;

import com.cs.elevators.door.Door;
import com.cs.elevators.door.DoorEventListener;

public class Elevator implements DoorEventListener {

    private final Door door = Door.aClosedDoor(this);

    @Override
    public void onOpening() {

    }

    @Override
    public void onOpen() {

    }

    @Override
    public void onClosing() {

    }

    @Override
    public void onClosed() {

    }

    public Door door() {
        return door;
    }
}
