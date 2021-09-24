package com.cs.elevator.storey;

import java.util.Objects;

public class Storey {
    public final Integer number;
    public final String name;

    public Storey(Integer number) {
        this(number, number.toString());
    }

    public Storey(Integer number, String name) {
        this.number = number;
        this.name = name;
    }

    public Integer number() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Storey storey = (Storey) o;
        return Objects.equals(number, storey.number);
    }

}
