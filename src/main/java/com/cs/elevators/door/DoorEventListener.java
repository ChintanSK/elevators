package com.cs.elevators.door;

public interface DoorEventListener {
    void onOpening();

    void onOpen();

    void onClosing();

    void onClosed();
}
