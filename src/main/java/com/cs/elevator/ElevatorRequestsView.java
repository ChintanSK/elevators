package com.cs.elevator;

public interface ElevatorRequestsView {
    int size();

    boolean empty();

    boolean contains(String storeyCode);
}
