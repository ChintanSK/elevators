package com.cs.elevator;

import com.cs.elevator.storey.Storey;
import com.cs.elevator.storey.Storeys;
import com.cs.elevator.util.ScheduledTask;

import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;

public class ElevatorRequests {
    private final LinkedBlockingQueue<Storey> liveRequestRecord = new LinkedBlockingQueue<>();
    private final ConcurrentSkipListSet<Storey> requests = new ConcurrentSkipListSet<>(Comparator.comparing(Storey::number));

    public void markAsServed(String storey) {
        requests.remove(Storeys.getByCode(storey));
    }

    public int size() {
        return requests.size();
    }

    public Storey next(String storeyCode, ElevatorDirection direction) {
        Storey currentStorey = Storeys.getByCode(storeyCode);
        if (direction == ElevatorDirection.UP) {
            return requests.higher(currentStorey);
        } else {
            return requests.lower(currentStorey);
        }
    }

    public boolean hasMore() {
        return !requests.isEmpty();
    }

    public boolean contains(String storeyCode) {
        return requests.contains(Storeys.getByCode(storeyCode));
    }

    public void enqueueRequest(Storey storey) {
        ScheduledTask.execute(() -> {
            liveRequestRecord.put(storey);
            return null;
        }).now();
    }

    public void serveNext() {
        try {
            requests.add(liveRequestRecord.take());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
