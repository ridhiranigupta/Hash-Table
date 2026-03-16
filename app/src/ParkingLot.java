package src;

import java.util.*;

public class ParkingLot {

    private static class Spot {
        String licensePlate;
        long entryTime;
        boolean occupied;

        Spot() {
            this.licensePlate = null;
            this.occupied = false;
            this.entryTime = 0;
        }
    }

    private final Spot[] spots;
    private final int capacity;
    private int totalProbes = 0;
    private int totalParked = 0;

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        this.spots = new Spot[capacity];
        for (int i = 0; i < capacity; i++) spots[i] = new Spot();
    }

    // Simple hash function: sum of chars mod capacity
    private int hash(String licensePlate) {
        int hash = 0;
        for (char c : licensePlate.toCharArray()) {
            hash = (hash + c) % capacity;
        }
        return hash;
    }

    // Park vehicle, return spot number and probes
    public synchronized String parkVehicle(String licensePlate) {
        int preferredSpot = hash(licensePlate);
        int probeCount = 0;
        for (int i = 0; i < capacity; i++) {
            int spotIndex = (preferredSpot + i) % capacity;
            if (!spots[spotIndex].occupied) {
                spots[spotIndex].licensePlate = licensePlate;
                spots[spotIndex].occupied = true;
                spots[spotIndex].entryTime = System.currentTimeMillis();
                totalProbes += probeCount;
                totalParked++;
                return String.format("Assigned Spot #%d (%d probes)", spotIndex, probeCount);
            }
            probeCount++;
        }
        return "Parking lot full";
    }

    // Exit vehicle, return duration and fee
    public synchronized String exitVehicle(String licensePlate) {
        for (int i = 0; i < capacity; i++) {
            if (spots[i].occupied && spots[i].licensePlate.equals(licensePlate)) {
                long durationMs = System.currentTimeMillis() - spots[i].entryTime;
                double hours = durationMs / (1000.0 * 60 * 60);
                double fee = Math.ceil(hours) * 5.0; // $5 per hour
                spots[i].occupied = false;
                spots[i].licensePlate = null;
                spots[i].entryTime = 0;
                totalParked--;
                return String.format("Spot #%d freed, Duration: %.2f h, Fee: $%.2f", i, hours, fee);
            }
        }
        return "Vehicle not found";
    }

    // Get statistics
    public synchronized String getStatistics() {
        int occupiedCount = 0;
        for (Spot s : spots) if (s.occupied) occupiedCount++;
        double occupancy = (occupiedCount * 100.0 / capacity);
        double avgProbes = totalParked == 0 ? 0 : totalProbes * 1.0 / totalParked;
        return String.format("Occupancy: %.1f%%, Avg Probes: %.2f", occupancy, avgProbes);
    }

    // Demo main method
    public static void main(String[] args) throws InterruptedException {
        ParkingLot lot = new ParkingLot(500);

        System.out.println(lot.parkVehicle("ABC-1234")); // Assigned spot
        System.out.println(lot.parkVehicle("ABC-1235")); // May collide
        System.out.println(lot.parkVehicle("XYZ-9999")); // May collide

        Thread.sleep(2000); // simulate 2 seconds parked

        System.out.println(lot.exitVehicle("ABC-1234"));
        System.out.println(lot.getStatistics());
    }
}
