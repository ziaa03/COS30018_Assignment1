package allAgents;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import java.util.Random;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class CustomerAgent extends Agent {

    private Parcel[] parcels = {
            new Parcel("a", 5),
            new Parcel("b", 6),
            new Parcel("c", 7),
            new Parcel("d", 8),
            new Parcel("e", 9),
            new Parcel("f", 10),
            new Parcel("g", 11),
            new Parcel("h", 12),
            new Parcel("i", 13),
            new Parcel("j", 14)
    };

    protected void setup() {
        Random rand = new Random();
        System.out.println("Customer Agent initialized.");

        long period = 3000 + rand.nextInt(2000);
        addBehaviour(new ParcelTicker(this, period));
    }

    private class ParcelTicker extends TickerBehaviour {
        private int[][] locations = new int[10][2]; // Array to store locations of 10 parcels

        public ParcelTicker(Agent a, long period) {
            super(a, period);
        }

        protected void onTick() {
            Random rand = new Random();

            // Randomize locations for all parcels
            for (int i = 0; i < 10; i++) {
                locations[i][0] = rand.nextInt(50); // x-coordinate
                locations[i][1] = rand.nextInt(50); // y-coordinate
            }

            // Construct message content with information of all parcels
            StringBuilder messageContent = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                Parcel parcel = parcels[i];
                messageContent.append("Parcel: ").append(parcel.name).append(", Weight: ").append(parcel.weight)
                        .append(", Location: (").append(locations[i][0]).append(", ").append(locations[i][1]).append(")\n");
            }

            // Create ACL message and send to MasterRoutingAgent
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("MasterRoutingAgent", AID.ISLOCALNAME));
            msg.setContent(messageContent.toString());
            send(msg);
        }
    }

    private class Parcel {
        String name;
        int weight;

        Parcel(String name, int weight) {
            this.name = name;
            this.weight = weight;
        }
    }
}
