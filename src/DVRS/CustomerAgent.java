package DVRS;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;


public class CustomerAgent extends Agent
{
    private List<ParcelList> parcelLists; // Declare parcelLists as an instance variable
    private List<String> bestRoutes = new ArrayList();

    public void setup()
    {
        System.out.println("\u001B[30m" + "--------- INITIALISATION STATUSES ---------" + "\u001B[0m");
        System.out.println("Customer agent is ready.");

        // Define four lists of parcels with names
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

        // Add ticker behavior to periodically send parcel lists
        //addBehaviour(new ParcelTicker(this, parcelLists));
        startGUI(CustomerAgent.this);
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

                    }

                } else {
                    block();
                }
            }
        });
    }

    private static void startGUI(CustomerAgent customerAgent)
    {
        // start the gui on the even dispatch thread
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                VRPGui gui = new VRPGui(customerAgent); // Pass the instance to the VRPGui constructor
                gui.setVisible(true); // Make the GUI visible here
            }
        });
    }

    public void sendParcelsToMasterRoutingAgent() {
        for (ParcelList parcelList : parcelLists) {
            sendParcels(parcelList);
        }
        System.out.print("debugging print to check if this function is passed through");
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
//        // Print out the total weight for the current parcel list
//        Utilities.printMessageWithoutAgent("\u001B[30m" + "\n----- Total weight of all parcels in " + parcelList.name + ": " + totalWeight + " ----- " + "\u001B[0m");

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

    public void addParcel(Parcel newParcel, String region) {
        // Add the new parcel to the appropriate parcel list
        for (ParcelList parcelList : parcelLists) {
            if (parcelList.name.equals(region)) {
                parcelList.addParcel(newParcel); // Add the parcel to this region only
                break;
            }
        }
    }

    public void printBestRoutes() {
        System.out.println("Current Best Routes:");
        for (String route : bestRoutes) {
            System.out.println(route);
        }
        VRPGui.setBestRoutes(bestRoutes);
    }
}