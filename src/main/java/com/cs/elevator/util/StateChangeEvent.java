package com.cs.elevator.util;

public interface StateChangeEvent<T> {
    T oldState();

    T newState();
}
