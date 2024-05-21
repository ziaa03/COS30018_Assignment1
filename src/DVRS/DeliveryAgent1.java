package DVRS;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.*;

public class DeliveryAgent1 extends Agent
{
    protected AID mraAID;
    protected int maxCapacity = 500;
    protected int[] location = {5, 5};
    protected int currentCapacity = 0;
    protected static int agentCount = 0;

    protected void setup()
    {
        mraAID = new AID("masterRoutingAgent", AID.ISLOCALNAME);
        agentCount++;
        if (agentCount == 4) {
            System.out.println("All delivery agents initialized and are ready at depot (starting point): (" + location[0] + ", " + location[1] + ")");
        }

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mtCapacity = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage capacityMsg = myAgent.receive(mtCapacity);
                if (capacityMsg != null) {
                    Utilities.printMessage(myAgent,"Received capacity request from " + capacityMsg.getSender().getLocalName() + "\n");
                    ACLMessage reply = capacityMsg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Capacity: " + maxCapacity);
                    send(reply);
                }
                else
                {
                    block();
                }
            }
        });

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate mtParcel = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage parcelMsg = myAgent.receive(mtParcel);
                if (parcelMsg != null) {
                    System.out.println("Parcel and route information received: " + parcelMsg.getContent() + " from " + parcelMsg.getSender().getName());
                    if (parcelMsg.getContent().startsWith("Parcel Weight:")) {
                        // Handle parcel information
                        int parcelWeight = extractParcelWeight(parcelMsg.getContent());
                        updateCapacity(parcelWeight, mraAID);
                    } else if (parcelMsg.getContent().startsWith("Best route in ")) {
                        // Handle route information
                        handleBestRoute(parcelMsg.getContent());
                    }
                } else {
                    block();
                }
            }
        });

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                MessageTemplate msgTempCFP = MessageTemplate.MatchPerformative(ACLMessage.CFP);
                ACLMessage cfpMsg = myAgent.receive(msgTempCFP);
                if (cfpMsg != null) {
                    Utilities.printMessage(myAgent,"Received Call for Proposal from " + cfpMsg.getSender().getLocalName() + "\n");
                    boolean decision = decisionCriteria(cfpMsg);
                    ACLMessage reply = cfpMsg.createReply();
                    Random rand = new Random();
                    boolean acceptRequest = rand.nextDouble() <= 0.8;
                    if (acceptRequest) {
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
        });
    }

    private int extractParcelWeight(String parcelMsgWeight)
    {
        // WORKING
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
            String weightPart = parts[1];
            String[] weightParts = weightPart.split(": ");
            int requiredCapacity = Integer.parseInt(weightParts[1]);
            if (requiredCapacity <= (maxCapacity - currentCapacity))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (NumberFormatException e)
        {
            System.err.println("Error parsing required capacity: " + deliveryDetails);
            e.printStackTrace();
            return false;
        }
    }

    protected void updateCapacity(int parcelWeight, AID mraAID)
    {
        int newCapacity = currentCapacity + parcelWeight;
        if (newCapacity <= maxCapacity) {
            currentCapacity = newCapacity;
            ACLMessage capacityUpdateMsg = new ACLMessage(ACLMessage.INFORM);
            capacityUpdateMsg.addReceiver(mraAID);
            capacityUpdateMsg.setContent("Current Capacity: " + currentCapacity);
            send(capacityUpdateMsg);
        } else {
            System.out.println("Parcel weight exceeds the maximum capacity.");
        }
    }

    public void setPosition(int x, int y) {
        this.location[0] = x;
        this.location[1] = y;
    }

    private void handleBestRoute(String content) {
        String[] parts = content.split(": ");
        String regionName = parts[0].substring(12);
        String bestRoute = parts[1];
        System.out.println("Delivery Agent has received: " + bestRoute);
    }

}