package test2;

public class Parcel {
    public String name;
    public int weight;
    public int x;
    public int y;

    public Parcel(String name, int weight, int x, int y)
    {
        this.name = name;
        this.weight = weight;
        this.x = x;
        this.y = y;
    }

    // getters
    public String getName()
    {
        return name;
    }

    public int getWeight()
    {
        return weight;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }
}