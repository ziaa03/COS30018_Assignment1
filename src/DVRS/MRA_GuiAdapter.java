package DVRS;

public class MRA_GuiAdapter extends MasterRoutingAgent
{
    @Override
    protected void setup() {
        super.setup();
        VRPGui.getInstance().setMasterRoutingAgent(this);
    }
}
