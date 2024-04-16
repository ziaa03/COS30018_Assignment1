package subTest1;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import java.util.Random;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.awt.Color;

class Parcel {
    String name;
    int weight; // Weight in some unit, e.g., kilograms

    Parcel(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }
}

public class CustomerAgent extends Agent {

    private Parcel[] parcels = {
        new Parcel("a", 5),
        new Parcel("b", 6),
        new Parcel("c", 7),
        new Parcel("d", 8),
        new Parcel("e", 9)
    };
    
    private RouteUI ui; // Reference to the UI
    
    

    protected void setup() {
        // Initialize the location once at the beginning for initial setup
        Random rand = new Random();
        System.out.println("Agent initialized.");

        // Add TickerBehaviour with a period between 3000 to 5000 milliseconds
        long period = 3000 + rand.nextInt(2000); // Random period between 3 to 5 seconds
        addBehaviour(new ParcelTicker(this, period));
    }

    private class ParcelTicker extends TickerBehaviour {
        private int[] location = new int[2]; // Location attribute as an array [x, y]

        public ParcelTicker(Agent a, long period) {
            super(a, period);
        }

        protected void onTick() {
            Random rand = new Random();

            // Randomize location each tick
            location[0] = rand.nextInt(50); // x-coordinate
            location[1] = rand.nextInt(50); // y-coordinate

            // Randomly select a parcel
            int parcelIndex = rand.nextInt(parcels.length);
            Parcel selectedParcel = parcels[parcelIndex];
            
            // Print out the selected parcel and its weight
            System.out.println("Pending delivery: " + selectedParcel.name + " Weight: " + selectedParcel.weight + " Location: (" + location[0] + ", " + location[1] + ")");
            
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("MasterRoutingAgent", AID.ISLOCALNAME));
            // Including location in the message content
            msg.setContent("Parcel drawn: " + selectedParcel.name + " Weight: " + selectedParcel.weight + " units" + " Location: (" + location[0] + ", " + location[1] + ")");
            send(msg);
            
            // Update UI with the location of the new parcel
            if (ui != null) {
                ui.updateGrid(location[0], location[1], Color.GREEN);
            }
            
        }
    }
}
