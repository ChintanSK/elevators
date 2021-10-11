package com.cs.elevator;

import com.cs.elevator.storey.Storey;
import com.cs.elevator.storey.Storeys;
import com.cs.elevator.util.AsyncTaskUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class ElevatorRequests {
    private final LinkedBlockingQueue<Storey> liveRequestRecord = new LinkedBlockingQueue<>();
    private final ConcurrentSkipListSet<Storey> requests = new ConcurrentSkipListSet<>(Comparator.comparing(Storey::number));

    public void markAsServed(String storey) {
        requests.remove(Storeys.getByCode(storey));
    }

    public Storey next(String storeyCode, ElevatorDirection direction) {
        Storey currentStorey = Storeys.getByCode(storeyCode);
        if (direction == ElevatorDirection.UP) {
            return requests.higher(currentStorey);
        } else {
            return requests.lower(currentStorey);
        }
    }

    public boolean empty() {
        return requests.isEmpty();
    }

    public boolean contains(String storeyCode) {
        return requests.contains(Storeys.getByCode(storeyCode));
    }

    public Set<String> view() {
        return Collections.unmodifiableSet(requests.stream().map(Storey::name).collect(Collectors.toSet()));
    }

    public void enqueueRequest(Storey storey) {
        AsyncTaskUtils.executeAsync(() -> liveRequestRecord.put(storey)).now();
    }

    public void acceptNextRequest() throws InterruptedException {
        requests.add(liveRequestRecord.take());
    }
}
