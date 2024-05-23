package DVRS;

import java.util.*;

//The ParcelList class represents a list of parcels within a specific region,
//and it also contains a distance matrix that calculates the distances between each pair of parcels.
public class ParcelList {
    public String name; // The name of the region this ParcelList belongs to
    public List<Parcel> parcels; // A list of parcels in the region
    public double[][] distanceMatrix; // A 2D array representing the distance matrix between parcels
    
    // Constructor to initialize a ParcelList with a name and a list of parcels
    public ParcelList(String name, List<Parcel> parcels) {
        this.name = name;
        this.parcels = new ArrayList<>(parcels);
        this.distanceMatrix = calculateDistanceMatrix(); // Calculate and store the distance matrix upon initialization
    }
    
    // Private method to calculate the distance matrix for the list of parcels
    private double[][] calculateDistanceMatrix() {
        int numParcels = parcels.size();
        double[][] distances = new double[numParcels][numParcels];
        for (int i = 0; i < numParcels; i++) {
            Parcel parcel1 = parcels.get(i);
            for (int j = 0; j < numParcels; j++) {
                Parcel parcel2 = parcels.get(j);
                // Calculate the distance between parcel1 and parcel2
                distances[i][j] = (double) calculateDistance(parcel1, parcel2);
            }
        }
        return distances;
    }
    
    // Private method to calculate the distance between two parcels using the Euclidean distance formula
    private double calculateDistance(Parcel parcel1, Parcel parcel2) {
        int deltaX = parcel1.x - parcel2.x;
        int deltaY = parcel1.y - parcel2.y;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
    
    // Getter method to retrieve the list of parcels
    public List<Parcel> getParcels()
    {
        return parcels;
    }
    
    // Method to add a new parcel to the list
    public void addParcel(Parcel parcel) {
        parcels.add(parcel);
    }
    
    // Getter method to retrieve the name of the region
    public String getRegionName() {
        return name;
    }

}