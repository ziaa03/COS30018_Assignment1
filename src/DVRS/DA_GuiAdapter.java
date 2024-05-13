package DVRS;

public class DA_GuiAdapter extends DeliveryAgent1
{
    @Override
    protected void setup() {
        super.setup();
        VRPGui.getInstance().setDeliveryAgent(this);
    }
}
