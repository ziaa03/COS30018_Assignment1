package allAgents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Random;

public class DeliveryAgent extends Agent
{
    protected AID mraAID;
    protected int maxCapacity = 50; // Example capacity constraint
    protected int[] location = {25, 25}; // Constant location set to (25, 25)
    protected int currentCapacity = 0;

    protected void setup()
    {
        System.out.println("DeliveryAgent " + getAID().getName() + " is ready at location: (" + location[0] + ", " + location[1] + ")");

        // CyclicBehaviour to handle different types of messages
        addBehaviour(new CyclicBehaviour(this)
        {
            public void action()
            {
                // Handle capacity request
                MessageTemplate mtCapacity = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage capacityMsg = myAgent.receive(mtCapacity);
                if (capacityMsg != null)
                {
                    System.out.println("Received capacity request from " + capacityMsg.getSender().getName());
                    ACLMessage reply = capacityMsg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Capacity: " + maxCapacity + ", Location: (" + location[0] + ", " + location[1] + ")");
                    send(reply);
                }
                else
                {
                    // Handle parcel and route information
                    MessageTemplate mtParcel = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                    ACLMessage parcelMsg = myAgent.receive(mtParcel);
                    if (parcelMsg != null) {
                        System.out.println("Parcel and route information received: " + parcelMsg.getContent() + " from " + parcelMsg.getSender().getName());
                        // Extract parcel weight from the message content
                        int parcelWeight = extractParcelWeight(parcelMsg.getContent());
                        // Update capacity when a parcel message is received
                        updateCapacity(parcelWeight, mraAID);
                        // Respond if route information is received
                        if (parcelMsg.getContent().contains("Path from Delivery Agent to Customer's Location:")) {
                            ACLMessage routeReply = parcelMsg.createReply();
                            routeReply.setPerformative(ACLMessage.CONFIRM);
                            routeReply.setContent("I received the route.");
                            send(routeReply);
                        }
                    }
                    else
                    {
                        block();
                    }
                }

                // Decision to refuse or accept delivery request from the mra
                MessageTemplate msgTempCFP = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                ACLMessage cfpMsg = myAgent.receive(msgTempCFP);

                // check if message is received
                if (cfpMsg != null) {
                    System.out.println("Received Call for Proposal from " + cfpMsg.getSender().getName());
                    boolean decision = decisionCriteria(cfpMsg);
                    ACLMessage reply = cfpMsg.createReply();

                    if (decision) {
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        reply.setContent("Agent accepts the proposal: 1");
                    } else {
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        reply.setContent("Agent rejects the proposal: 0");
                    }
                    send(reply);
                } else {
                    block();
                }
            }

            private int extractParcelWeight(String parcelMsgWeight)
            {
                String[] parts = parcelMsgWeight.split("Weight: ");
                if (parts.length >= 2)
                {
                    return Integer.parseInt(parts[1].trim());
                }
                else
                {
                    throw new IllegalArgumentException("Parcel message does not contain weight information.");
                }
            }

            private boolean decisionCriteria(ACLMessage cfp)
            {
                String deliveryDetails = cfp.getContent();
                try {
                    String[] parts = deliveryDetails.split(", ");
                    String weightPart = parts[1]; // "Weight: 8"
                    String[] weightParts = weightPart.split(": ");
                    int requiredCapacity = Integer.parseInt(weightParts[1]); // 8

                    // Check if the agent has sufficient capacity to handle the parcel
                    if (requiredCapacity <= (maxCapacity - currentCapacity))
                    {
                        return true; // Accept the proposal
                    } else {
                        return false; // Reject the proposal
                    }
                }
                catch (NumberFormatException e)
                {
                    System.err.println("Error parsing required capacity: " + deliveryDetails);
                    e.printStackTrace();
                    return false; // Default to rejection in case of parsing error
                }
            }
        });
    }

    protected void updateCapacity(int parcelWeight, AID mraAID)
    {
        int newCapacity = currentCapacity + parcelWeight;
        if (newCapacity <= maxCapacity) {
            currentCapacity = newCapacity;

            // Inform the MRA about the capacity update (replace "mraAID" with actual AID)
            ACLMessage capacityUpdateMsg = new ACLMessage(ACLMessage.INFORM);
            capacityUpdateMsg.addReceiver(mraAID);
            capacityUpdateMsg.setContent("Current Capacity: " + currentCapacity);
            send(capacityUpdateMsg);
        } else {
            System.out.println("Parcel weight exceeds the maximum capacity.");
        }
    }

}
