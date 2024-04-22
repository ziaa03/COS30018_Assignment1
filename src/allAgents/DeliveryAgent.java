package allAgents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class DeliveryAgent extends Agent {

    private int capacityConstraint = 8; // Example capacity constraint
    private final int[] location = {25, 25}; // Constant location set to (25, 25)

    protected void setup() {
        System.out.println("DeliveryAgent " + getAID().getName() + " is ready at location: (" + location[0] + ", " + location[1] + ")");

        // CyclicBehaviour to handle different types of messages
        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                // Handle capacity request
                MessageTemplate mtCapacity = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage capacityMsg = myAgent.receive(mtCapacity);
                if (capacityMsg != null) {
                    System.out.println("Received capacity request from " + capacityMsg.getSender().getName());
                    ACLMessage reply = capacityMsg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Capacity: " + capacityConstraint + ", Location: (" + location[0] + ", " + location[1] + ")");
                    send(reply);
                } else {
                    // Handle parcel and route information
                    MessageTemplate mtParcel = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                    ACLMessage parcelMsg = myAgent.receive(mtParcel);
                    if (parcelMsg != null) {
                        System.out.println("Parcel and route information received: " + parcelMsg.getContent() + " from " + parcelMsg.getSender().getName());
                        // Respond if route information is received
                        if (parcelMsg.getContent().contains("Path from Delivery to Customer:")) {
                            ACLMessage routeReply = parcelMsg.createReply();
                            routeReply.setPerformative(ACLMessage.CONFIRM);
                            routeReply.setContent("I receive the route");
                            send(routeReply);
                        }
                    } else {
                        block();
                    }
                }
            }
        });
    }
}
