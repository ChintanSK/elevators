package com.cs.elevator;

public enum ElevatorDirection {
    UP {
        public ElevatorDirection toggle() {
            return DOWN;
        }
    }, DOWN {
        public ElevatorDirection toggle() {
            return UP;
        }
    };

    public abstract ElevatorDirection toggle();
}
