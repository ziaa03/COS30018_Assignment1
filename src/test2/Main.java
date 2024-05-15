package test2;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.tools.sniffer.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Main {
    public static void main(String[] args)
    {
        // Set up the JADE container
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true");
        Runtime runtime = Runtime.instance();
        AgentContainer container = runtime.createMainContainer(profile);

        try
        {
            // Create and add the CustomerAgent with GUI
            AgentController customerAgent = container.createNewAgent("customerAgent", "test.CA_GuiAdapter", new Object[0]);
            customerAgent.start();

            // Create and add multiple instances of the DeliveryAgent
            AgentController deliveryAgent1 = container.createNewAgent("deliveryAgent1", "test.DeliveryAgent1", new Object[0]);
            deliveryAgent1.start();

            AgentController deliveryAgent2 = container.createNewAgent("deliveryAgent2", "test.DeliveryAgent1", new Object[0]);
            deliveryAgent2.start();

            AgentController deliveryAgent3 = container.createNewAgent("deliveryAgent3", "test.DeliveryAgent1", new Object[0]);
            deliveryAgent3.start();

            AgentController deliveryAgent4 = container.createNewAgent("deliveryAgent4", "test.DeliveryAgent1", new Object[0]);
            deliveryAgent4.start();

            // Create and add the MasterRoutingAgent
            AgentController masterRoutingAgent = container.createNewAgent("masterRoutingAgent", "test.MasterRoutingAgent", new Object[0]);
            masterRoutingAgent.start();

            // start the gui
            //startGUI();

        }
        catch (StaleProxyException e)
        {
            // Print stack trace if an exception occurs
            e.printStackTrace();
        }
    }

//    private static void startGUI()
//    {
//        // start the gui on the even dispatch thread
//        javax.swing.SwingUtilities.invokeLater(new Runnable()
//        {
//            public void run()
//            {
//                VRPGui gui = VRPGui.getInstance(); // Get the instance with arguments
//                gui.setVisible(true); // Make the GUI visible here
//            }
//        });
//    }

}
