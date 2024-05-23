package DVRS;

// The Parcel class represents a parcel with a name, weight, and location coordinates (x, y).
public class Parcel {
    public String name; // The name of the parcel
    public int weight; // The weight of the parcel
    public int x; // The x-coordinate of the parcel's location
    public int y; // The y-coordinate of the parcel's location
    
    // Constructor to initialize a Parcel object with its name, weight, and location
    public Parcel(String name, int weight, int x, int y)
    {
        this.name = name;
        this.weight = weight;
        this.x = x;
        this.y = y;
    }
    
    // Getter method to retrieve the name of the parcel
    public String getName()
    {
        return name;
    }
    
    // Getter method to retrieve the weight of the parcel
    public int getWeight()
    {
        return weight;
    }

    // Getter method to retrieve the x-coordinate of the parcel's location
    public int getX()
    {
        return x;
    }

    // Getter method to retrieve the y-coordinate of the parcel's location
    public int getY()
    {
        return y;
    }
    
    // Setter method to update the x-coordinate of the parcel's location
    public void setX(int x)
    {
        this.x = x;
    }
    
    // Setter method to update the y-coordinate of the parcel's location
    public void setY(int y)
    {
        this.y = y;
    }
}