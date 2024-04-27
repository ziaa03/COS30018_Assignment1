package DVRS;

import java.util.*;

public class ParcelList
{
    public String name;
    public List<Parcel> parcels;

    public ParcelList(String name, List<Parcel> parcels)
    {
        this.name = name;
        this.parcels = new ArrayList<>(parcels);
    }
}