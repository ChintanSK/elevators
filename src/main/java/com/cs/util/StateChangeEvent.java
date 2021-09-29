package com.cs.util;

public interface StateChangeEvent<T> {
    T oldState();

    T newState();
}
