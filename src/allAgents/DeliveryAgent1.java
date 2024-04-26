package allAgents;

public class DeliveryAgent1 extends DeliveryAgent
{
    protected void setup()
    {
        super.setup();
        System.out.println("DeliveryAgent1 " + getAID().getName() + " is ready.");

        maxCapacity = 40;
    }
}