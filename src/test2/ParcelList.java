package test2;

import java.util.*;

public class ParcelList {
    public String name;
    public List<Parcel> parcels;
    public double[][] distanceMatrix;

    public ParcelList(String name, List<Parcel> parcels) {
        this.name = name;
        this.parcels = new ArrayList<>(parcels);
        this.distanceMatrix = calculateDistanceMatrix();
    }

    private double[][] calculateDistanceMatrix() {
        int numParcels = parcels.size();
        double[][] distances = new double[numParcels][numParcels];
        for (int i = 0; i < numParcels; i++) {
            Parcel parcel1 = parcels.get(i);
            for (int j = 0; j < numParcels; j++) {
                Parcel parcel2 = parcels.get(j);
                distances[i][j] = (double) calculateDistance(parcel1, parcel2);
            }
        }
        return distances;
    }
    
    public List<Parcel> getParcels() {
        return parcels;  // Returns the entire list of parcels
    }


    private double calculateDistance(Parcel parcel1, Parcel parcel2) {
        int deltaX = parcel1.x - parcel2.x;
        int deltaY = parcel1.y - parcel2.y;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
}