package DVRS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeliveryRoute implements Comparable<DeliveryRoute> {
    private List<Parcel> parcels; // List of parcels in the route
    private int fitness; // Fitness value of the route

    // Constructor
    public DeliveryRoute(List<Parcel> parcels) {
        this.parcels = parcels;
    }

    // Method to calculate the fitness of the route
    public void calculateFitness(int[][] travelPrices) {
        // Implement fitness calculation based on travel prices, distance, time, etc.
        // For simplicity, let's calculate fitness as the total weight of parcels in the route
        fitness = 0;
        for (Parcel parcel : parcels) {
            fitness += parcel.getWeight();
        }
    }

    // Getter method for fitness
    public int getFitness() {
        return fitness;
    }

    // Comparable interface implementation for sorting
    @Override
    public int compareTo(DeliveryRoute otherRoute) {
        // Compare routes based on fitness values
        return Integer.compare(this.fitness, otherRoute.fitness);
    }
}
