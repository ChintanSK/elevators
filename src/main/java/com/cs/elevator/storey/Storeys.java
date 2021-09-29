package com.cs.elevator.storey;

import java.util.HashMap;
import java.util.Map;

public class Storeys {
    private final static Map<String, Storey> storeys = new HashMap<>();

    public static void addStorey(Integer storeyNumber) {
        storeys.put(storeyNumber.toString(), new Storey(storeyNumber));
    }

    public static void addStorey(Integer storeyNumber, String storeyCode) {
        storeys.put(storeyCode, new Storey(storeyNumber, storeyCode));
    }

    public static Storey getByCode(String storeyCode) {
        return storeys.get(storeyCode);
    }

}
