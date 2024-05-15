package test2;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import test.VRPGui;

import java.util.*;

public class CustomerAgent extends Agent
{
    private List<ParcelList> parcelLists; // Declare parcelLists as an instance variable
    private List<String> bestRoutes = new ArrayList();

    protected void setup()
    {
        System.out.println("\u001B[30m" + "--------- INITIALISATION STATUSES ---------" + "\u001B[0m");
        System.out.println("Customer agent is ready.");

        // Define four lists of parcels with names
        parcelLists = Arrays.asList(
                new ParcelList("Region A", Arrays.asList(
                        new Parcel("a", 5, 1, 1), new Parcel("b", 10, 3, 1), new Parcel("c", 15, 0, 4), new Parcel("d", 20, 3, 3)
                )),
                new ParcelList("Region B", Arrays.asList(
                        new Parcel("e", 40, 10, 2), new Parcel("f", 35, 7, 0), new Parcel("g", 30, 10, 4), new Parcel("h", 25, 6, 3)
                )),
                new ParcelList("Region C", Arrays.asList(
                        new Parcel("i", 45, 8, 6), new Parcel("j", 50, 8, 7), new Parcel("k", 55, 10, 10), new Parcel("l", 60, 10, 6)
                )),
                new ParcelList("Region D", Arrays.asList(
                        new Parcel("m", 80, 2, 10), new Parcel("n", 75, 0, 7), new Parcel("o", 70, 1, 6), new Parcel("p", 65, 2, 8), new Parcel("toki", 60, 3, 8)
                ))
        );

        // Add ticker behavior to periodically send parcel lists
        addBehaviour(new ParcelTicker(this, parcelLists));

        // Add behavior to handle incoming best route messages
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    // Parse the best route
                    String bestRoute = msg.getContent();
                    System.out.println("Customer Agent has received: " + bestRoute + " from " + msg.getSender().getName());
                    addBestRoute(bestRoute);
                    printBestRoutes();
                    
                    if(bestRoutes != null) {
                    	//VRPGui.getAbc(abc);
                    startGUI();
                    }

                } else {
                    block();
                }
            }
        });
    }
    
    private static void startGUI()
    {
        // start the gui on the even dispatch thread
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                VRPGui gui = VRPGui.getInstance(); // Get the instance with arguments
                gui.setVisible(true); // Make the GUI visible here
            }
        });
    }

    private class ParcelTicker extends TickerBehaviour {
        private List<ParcelList> parcelLists;
        private int currentListIndex = 0;

        public ParcelTicker(Agent a, List<ParcelList> parcelLists) {
            super(a, 5000); // Ticker interval: 5000 milliseconds (adjust as needed)
            this.parcelLists = parcelLists;
        }

        protected void onTick() {
            if (currentListIndex < parcelLists.size()) {
                sendParcels(parcelLists.get(currentListIndex));
                currentListIndex++;
            } else {
                // Stop the ticker behavior once all parcel lists are sent
                stop();
            }
        }
    }

    protected String sendParcels(ParcelList parcelList)
    {
        // Construct message content with information of parcels in the list
        StringBuilder messageContent = new StringBuilder();
        // Append the name of the parcel region
        messageContent.append("Parcel Region: ").append(parcelList.name).append("\n");
        int totalWeight = 0; // Initialize total weight for the current parcel list
        for (Parcel parcel : parcelList.parcels) {
            messageContent.append("Parcel: ").append(parcel.name).append(", Weight: ").append(parcel.weight)
                    .append(", Location: (").append(parcel.x).append(", ").append(parcel.y).append(")\n");
            totalWeight += parcel.weight; // Accumulate the weight of each parcel
        }
        // Print out the total weight for the current parcel list
        Utilities.printMessageWithoutAgent("\u001B[30m" + "\n----- Total weight of all parcels in " + parcelList.name + ": " + totalWeight + " ----- " + "\u001B[0m");

        // Create ACL message and send to MasterRoutingAgent
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("MasterRoutingAgent", AID.ISLOCALNAME));
        msg.setContent(messageContent.toString());
        send(msg);

        // Return the message content as a String
        return messageContent.toString();
    }

    // Method to get the list of best routes
    public List<String> getBestRoutes() {
        return bestRoutes;
    }

    public void addBestRoute(String route) {
        bestRoutes.add(route);
        System.out.println("Best route added: " + route);
    }


    public List<ParcelList> getParcelLists()
    {
        return parcelLists;
    }

    public void printBestRoutes() {
        System.out.println("Current Best Routes:");
        for (String route : bestRoutes) {
            System.out.println(route);
        }
        VRPGui.setBestRoutes(bestRoutes);
    }
}