package test2;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.awt.Color;
import java.util.*;

public class CustomerAgent extends Agent
{
	private RouteUI ui;
	private Random random;
	private List<ParcelList> parcelLists;
	private int currentListIndex = 0;
	private int currentX, currentY;
	
	public CustomerAgent() {
        super();
        // Optionally initialize fields with default values
        this.ui = new RouteUI(); // Assuming default initialization is acceptable
        this.random = new Random();
        initializeParcelLists();
        updateLocation();
    }

	public CustomerAgent(RouteUI ui) {
        this.ui = ui;
        this.random = new Random();
        initializeParcelLists();
        updateLocation();
    }
	
    protected void setup()
    {
        System.out.println("\u001B[30m" + "--------- INITIALISATION STATUSES ---------" + "\u001B[0m");
        System.out.println("Customer agent is ready.");

        initializeParcelLists();

        // Add ticker behavior to periodically send parcel lists
        addBehaviour(new ParcelTicker(this, parcelLists));
    }
    
    private void initializeParcelLists() {
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

    private class ParcelTicker extends TickerBehaviour {
        private List<ParcelList> parcelLists;
        

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

    private void sendParcels(ParcelList parcelList) {
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
    }
    
    public void updateLocation() {
        if (!parcelLists.isEmpty()) {
            for (ParcelList currentParcelList : parcelLists) {
                for (Parcel parcel : currentParcelList.getParcels()) {
                    int x = parcel.getX();
                    int y = parcel.getY();
                    if (x != currentX || y != currentY) {
                        ui.updateGrid(x, y, Color.BLUE);
                        currentX = x;
                        currentY = y;
                    }
                }
            }
        }
    }

    public List<ParcelList> getParcelLists() {
        return this.parcelLists;
    }
    
}