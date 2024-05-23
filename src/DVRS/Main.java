package DVRS;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

//The Main class initializes and starts the JADE platform and creates necessary agent containers and agents.
public class Main {
    public static void main(String[] args)
    {
        Profile profile = new ProfileImpl(); // Create a new profile for the JADE platform.
        profile.setParameter(Profile.GUI, "true"); // Enable the GUI for visualization purposes.
        Runtime runtime = Runtime.instance(); // Create a new JADE runtime instance.
        AgentContainer container = runtime.createMainContainer(profile); // Create the main container for agents based on the profile.

        try
        {
        	// Create and start the CustomerAgent
            AgentController customerAgent = container.createNewAgent("customerAgent", "DVRS.CA_GuiAdapter", new Object[0]);
            customerAgent.start();

            // Create and start DeliveryAgent1
            AgentController deliveryAgent1 = container.createNewAgent("deliveryAgent1", "DVRS.DeliveryAgent1", new Object[0]);
            deliveryAgent1.start();
            
            // Create and start DeliveryAgent2
            AgentController deliveryAgent2 = container.createNewAgent("deliveryAgent2", "DVRS.DeliveryAgent1", new Object[0]);
            deliveryAgent2.start();

            // Create and start DeliveryAgent3
            AgentController deliveryAgent3 = container.createNewAgent("deliveryAgent3", "DVRS.DeliveryAgent1", new Object[0]);
            deliveryAgent3.start();

            // Create and start DeliveryAgent4
            AgentController deliveryAgent4 = container.createNewAgent("deliveryAgent4", "DVRS.DeliveryAgent1", new Object[0]);
            deliveryAgent4.start();
            
            // Create and start the MasterRoutingAgent
            AgentController masterRoutingAgent = container.createNewAgent("masterRoutingAgent", "DVRS.MasterRoutingAgent", new Object[0]);
            masterRoutingAgent.start();
        }
        catch (StaleProxyException e)
        {
        	// Handle stale proxy exception if agent creation fails.
            e.printStackTrace();
        }
    }
}
