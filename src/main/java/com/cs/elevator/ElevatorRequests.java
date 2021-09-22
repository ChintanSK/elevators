package com.cs.elevator;

import java.util.*;

public class ElevatorRequests {

    private final Set<String> requests = new TreeSet<>();

    public void add(String storeyCode) {
        requests.add(storeyCode);
    }

    public List<String> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(requests));
    }
}
