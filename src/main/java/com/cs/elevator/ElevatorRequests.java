package com.cs.elevator;

import com.cs.elevator.storey.Storey;
import com.cs.elevator.storey.Storeys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class ElevatorRequests {

    private final ConcurrentSkipListSet<Storey> requests = new ConcurrentSkipListSet<>(Comparator.comparing(Storey::number));

    public void add(String storeyCode) {
        requests.add(Storeys.getByCode(storeyCode));
    }

    public void remove(String storeyCode) {
        requests.remove(Storeys.getByCode(storeyCode));
    }

    public List<Storey> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(requests));
    }

    public Storey next(String storeyCode, ElevatorDirection direction) {
        Storey currentStorey = Storeys.getByCode(storeyCode);
        if (direction == ElevatorDirection.UP) {
            return requests.ceiling(currentStorey);
        } else {
            return requests.floor(currentStorey);
        }
    }

    public boolean hasNext() {
        return !requests.isEmpty();
    }
}
