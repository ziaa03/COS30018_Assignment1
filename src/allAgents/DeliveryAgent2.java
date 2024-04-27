package allAgents;

public class DeliveryAgent2 extends DeliveryAgent
{
    protected void setup()
    {
        super.setup();
        System.out.println("DeliveryAgent2 " + getAID().getName() + " is ready.");

        maxCapacity = 60;
    }
}