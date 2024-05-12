package test2;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.awt.Color;
import java.util.*;

public class CustomerAgent extends Agent {
    private RouteUI ui;
    private Random random;
    private List<ParcelList> parcelLists;  // Define the parcel lists at the class level
    private int currentListIndex = 0;      // Index to track current parcel list
    private int currentX, currentY; 

    public CustomerAgent(RouteUI ui) {
        this.ui = ui;
        this.random = new Random();
        initializeParcelLists(); // Initialize the parcel lists upon agent creation
        NewLocation();
    }

    private void initializeParcelLists() {
        // Initialization of parcel lists
        parcelLists = Arrays.asList(
                new ParcelList("Region A", Arrays.asList(
                        new Parcel("a", 5, 1, 1), new Parcel("b", 10, 3, 1),
                        new Parcel("c", 15, 0, 4), new Parcel("d", 20, 3, 3))),
                new ParcelList("Region B", Arrays.asList(
                        new Parcel("e", 40, 10, 2), new Parcel("f", 35, 7, 0),
                        new Parcel("g", 30, 10, 4), new Parcel("h", 25, 6, 3))),
                new ParcelList("Region C", Arrays.asList(
                        new Parcel("i", 45, 8, 6), new Parcel("j", 50, 8, 7),
                        new Parcel("k", 55, 10, 10), new Parcel("l", 60, 10, 6))),
                new ParcelList("Region D", Arrays.asList(
                        new Parcel("m", 80, 2, 10), new Parcel("n", 75, 0, 7),
                        new Parcel("o", 70, 1, 6), new Parcel("p", 65, 2, 8)))
        );
    }

    protected void setup() {
        System.out.println("\u001B[30m" + "--------- INITIALISATION STATUSES ---------" + "\u001B[0m");
        System.out.println("Customer agent is ready.");
        addBehaviour(new ParcelTicker(this, 5000)); // Add behavior with defined tick time
    }

    private class ParcelTicker extends TickerBehaviour {
        public ParcelTicker(Agent a, long period) {
            super(a, period);
        }

        protected void onTick() {
            if (currentListIndex < parcelLists.size()) {
                sendParcels(parcelLists.get(currentListIndex));
                NewLocation();  // Update location based on the current parcel list
                currentListIndex++;
            } else {
                stop();  // Stop the ticker behavior once all lists are processed
            }
        }
    }

    private void sendParcels(ParcelList parcelList) {
        StringBuilder messageContent = new StringBuilder("Parcel Region: ").append(parcelList.name).append("\n");
        int totalWeight = 0;
        for (Parcel parcel : parcelList.parcels) {
            messageContent.append("Parcel: ").append(parcel.name).append(", Weight: ")
                          .append(parcel.weight).append(", Location: (")
                          .append(parcel.x).append(", ").append(parcel.y).append(")\n");
            totalWeight += parcel.weight;
        }
        Utilities.printMessageWithoutAgent("\u001B[30m" + "\n----- Total weight of all parcels in " + parcelList.name + ": " + totalWeight + " ----- " + "\u001B[0m");
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("MasterRoutingAgent", AID.ISLOCALNAME));
        msg.setContent(messageContent.toString());
        send(msg);
    }

    public void NewLocation() {
        for (ParcelList currentParcelList : parcelLists) {  // Iterate over all parcel lists
            for (Parcel parcel : currentParcelList.getParcels()) {  // Iterate over all parcels in the current list
                int x = parcel.getX();  // Assuming getter method getX() is used
                int y = parcel.getY();  // Assuming getter method getY() is used
                ui.updateGrid(x, y, Color.BLUE);  // Update grid at each parcel's location
                currentX = x;  // Update currentX to the last processed parcel's X
                currentY = y;  // Update currentY to the last processed parcel's Y
            }
        }
    }

    
    public List<ParcelList> getParcelLists() {
        return this.parcelLists;  // Return a direct reference to the parcelLists
    }
    

    
}
