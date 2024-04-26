package allAgents;

public class DeliveryAgent3 extends DeliveryAgent
{
    protected void setup()
    {
        super.setup();
        System.out.println("DeliveryAgent3 " + getAID().getName() + " is ready.");

        maxCapacity = 30;
    }
}