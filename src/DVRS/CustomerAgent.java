package DVRS;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

// CustomerAgent class manages parcels for different regions and sends them to the MasterRoutingAgent
public class CustomerAgent extends Agent
{
	// List to hold ParcelList objects, each representing a collection of parcels for a specific region
    private List<ParcelList> parcelLists;
    
    // List to store the best routes for parcel delivery, received from the MasterRoutingAgent
    private List<String> bestRoutes = new ArrayList();

    // initializes the agent, sets up the GUI, adds Cyclic Behavior to handle incoming messages
    public void setup()
    {
        System.out.println("\u001B[30m" + "--------- INITIALISATION STATUSES ---------" + "\u001B[0m");
        System.out.println("Customer agent is ready.");

        parcelLists = Arrays.asList(
                new ParcelList("Region A", Arrays.asList(
                )),
                new ParcelList("Region B", Arrays.asList(
                )),
                new ParcelList("Region C", Arrays.asList(
                )),
                new ParcelList("Region D", Arrays.asList(
                ))
        );

        startGUI(CustomerAgent.this);

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    String bestRoute = msg.getContent();
                    System.out.println("Customer Agent has received: " + bestRoute + " from " + msg.getSender().getName());
                    if(bestRoutes.size()>4) {
                        bestRoutes.clear();
                    }
                    addBestRoute(bestRoute);
                    printBestRoutes();
                } else {
                    block();
                }
            }
        });
    }
    
    // A static method to initialize and show the GUI for the agent
    private static void startGUI(CustomerAgent customerAgent)
    {
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                VRPGui gui = new VRPGui(customerAgent);
                gui.setVisible(true);
            }
        });
    }
    
    // Sends all parcels to the MasterRoutingAgent
    public void sendParcelsToMasterRoutingAgent() {
        for (ParcelList parcelList : parcelLists) {
            sendParcels(parcelList);
        }
    }
    
    // Sends the parcels of a specific region to the MasterRoutingAgent
    // Constructs message content with parcel details
    // ACLMessage msg: Creates and sends an ACLMessage to the MRA with the constructed content
    protected String sendParcels(ParcelList parcelList)
    {
        StringBuilder messageContent = new StringBuilder();
        messageContent.append("Parcel Region: ").append(parcelList.name).append("\n");
        int totalWeight = 0; // Initialize total weight for the current parcel list
        for (Parcel parcel : parcelList.parcels) {
            messageContent.append("Parcel: ").append(parcel.name).append(", Weight: ").append(parcel.weight)
                    .append(", Location: (").append(parcel.x).append(", ").append(parcel.y).append(")\n");
            totalWeight += parcel.weight; // Accumulate the weight of each parcel
        }
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("MasterRoutingAgent", AID.ISLOCALNAME));
        msg.setContent(messageContent.toString());
        send(msg);
        return messageContent.toString();
    }
    
    // Returns the list of best routes
    public List<String> getBestRoutes() {
        return bestRoutes;
    }
    
    // Retrieves parcels for a specific region
    public List<Parcel> getParcelsInRegion(String region) {
        List<Parcel> parcelsInRegion = new ArrayList<>();
        for (ParcelList parcelList : getParcelLists()) {
            if (parcelList.getRegionName().equals(region)) {
                parcelsInRegion.addAll(parcelList.getParcels());
            }
        }
        return parcelsInRegion;
    }
    
    // Adds a new best route to the list, ensuring no duplicate regions
    public void addBestRoute(String route) {
        for (String newRoute : bestRoutes) {
            if (newRoute.contains("Region A") && route.contains("Region A")) {
                bestRoutes.removeIf(route1 -> route1.contains("Region A"));
                break;
            }
            if (newRoute.contains("Region B")&& route.contains("Region B")) {
                bestRoutes.removeIf(route1 -> route1.contains("Region B"));
                break;
            }
            if (newRoute.contains("Region C")&& route.contains("Region C")) {
                bestRoutes.removeIf(route1 -> route1.contains("Region C"));
                break;
            }
            if (newRoute.contains("Region D")&& route.contains("Region D")) {
                bestRoutes.removeIf(route1 -> route1.contains("Region D"));
                break;
            }
        }
        bestRoutes.add(route);
        System.out.println("Best route added: " + route);
    }
    
    // Returns the list of parcel lists
    public List<ParcelList> getParcelLists()
    {
        return parcelLists;
    }
    
    // Adds a new parcel to a specified region
    public void addParcel(Parcel newParcel, String region) {
        for (ParcelList parcelList : parcelLists) {
            if (parcelList.name.equals(region)) {
                parcelList.addParcel(newParcel);
                break;
            }
        }
    }
    
    // Prints the current best routes and updates the GUI
    public void printBestRoutes() {
        System.out.println("Current Best Routes:");
        for (String route : bestRoutes) {
            System.out.println(route);
        }
        VRPGui.setBestRoutes(bestRoutes);
    }
}