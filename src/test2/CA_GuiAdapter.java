package test2;

public class CA_GuiAdapter extends CustomerAgent
{
    @Override
    protected void setup() {
        super.setup();
        VRPGui.getInstance();
    }
}
