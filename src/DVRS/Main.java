package DVRS;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.tools.sniffer.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import java.awt.Color;


public class Main {
    public static void main(String[] args)
    {
        // Set up the JADE container
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.GUI, "true");
        Runtime runtime = Runtime.instance();
        AgentContainer container = runtime.createMainContainer(profile);

        try {
            // Create and add the CustomerAgent
            AgentController customerAgent = container.createNewAgent("customerAgent", "DVRS.CustomerAgent", new Object[0]);
            customerAgent.start();

            // Create and add multiple instances of the DeliveryAgent
            AgentController deliveryAgent1 = container.createNewAgent("deliveryAgent1", "DVRS.DeliveryAgent1", new Object[0]);
            deliveryAgent1.start();

            AgentController deliveryAgent2 = container.createNewAgent("deliveryAgent2", "DVRS.DeliveryAgent1", new Object[0]);
            deliveryAgent2.start();

            AgentController deliveryAgent3 = container.createNewAgent("deliveryAgent3", "DVRS.DeliveryAgent1", new Object[0]);
            deliveryAgent3.start();

            AgentController deliveryAgent4 = container.createNewAgent("deliveryAgent4", "DVRS.DeliveryAgent1", new Object[0]);
            deliveryAgent4.start();

            // Create and add the MasterRoutingAgent
            AgentController masterRoutingAgent = container.createNewAgent("masterRoutingAgent", "DVRS.MasterRoutingAgent", new Object[0]);
            masterRoutingAgent.start();
            
            RouteUI ui = new RouteUI();
            ui.setVisible(true);
            RandomMarker marker = new RandomMarker(ui);
            Mover mover = new Mover(ui, marker);
            new Thread(mover).start();
            

        } catch (StaleProxyException e)
        {
            // Print stack trace if an exception occurs
            e.printStackTrace();
        }
        
        
        
        
    }
}
