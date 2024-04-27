package allAgents;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.AbstractMap;

public class MasterRoutingAgent extends Agent {
    private Map<AID, Integer> agentCapacities = new HashMap<>();
    private PriorityQueue<Map.Entry<AID, Integer>> agentProposals = new PriorityQueue<>((a, b) -> b.getValue() - a.getValue());

    protected void setup() {
        System.out.println("Hello! MasterAgent " + getAID().getName() + " is ready.");

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    // Check if it's a route reception confirmation message
                    if (msg.getPerformative() == ACLMessage.CONFIRM && msg.getContent().equals("I received the route.")) {
                        System.out.println("Confirmation received from Delivery Agent: " + msg.getContent());
                    } else {
                        // Handle other types of messages
                        if (msg.getContent().startsWith("Parcel:")) {
                            parseParcelMessage(msg);
                        } else {
                            parseDeliveryMessage(msg);
                        }
                    }
                } else {
                    block();
                }
            }
        });

        requestCapacities();
    }

    private void broadcastParcelInformation(String[] selectedParcelNames, int[] selectedParcelWeights, int[][] customerLocations) {
        int totalWeight = 0; // Variable to store the total weight of all parcels

        for (int i = 0; i < selectedParcelNames.length; i++) {
            String selectedParcelName = selectedParcelNames[i];
            int selectedParcelWeight = selectedParcelWeights[i];
            int[] customerLocation = customerLocations[i]; // Corrected to array type

            // Add the weight of the current parcel to the total weight
            totalWeight += selectedParcelWeight;

            // Iterate over each delivery agent and send a message for the current parcel
            for (Map.Entry<AID, Integer> entry : agentCapacities.entrySet()) {
                AID agent = entry.getKey();
                int capacity = entry.getValue();

                // Construct message content for the parcel
                String messageContent = "Parcel: " + selectedParcelName + ", Weight: " + selectedParcelWeight + ", Location: (" + customerLocation[0] + ", " + customerLocation[1] + "), Total Weight: " + totalWeight;

                // CFP (calls for proposals or responses from the receivers)
                ACLMessage cfpDeliveryMsg = new ACLMessage(ACLMessage.CFP);
                cfpDeliveryMsg.addReceiver(agent);
                cfpDeliveryMsg.setContent(messageContent);
                send(cfpDeliveryMsg);
            }
        }
    }

    // parsing messages received by the MRA that are not related to parcel information
    // handles msgs containing capacity information, proposal responses and capacity updates from DAs
    private void parseDeliveryMessage(ACLMessage msg) {
        //starts by printing out the received msg content and the sender's name
        System.out.println("Received message: " + msg.getContent() + " from " + msg.getSender().getName());

        // extracts the content of the message and stores it in the 'messageContent' variable
        String messageContent = msg.getContent();

        try {
            // if messageContent starts w Capacity, the method assumes it contains both capacity and location information
            if (messageContent.startsWith("Capacity: ")) {
                // Handle messages with capacity and location information
                String[] messageParts = messageContent.split(", Location: ");
                if (messageParts.length < 2) {
                    throw new IllegalArgumentException("Message does not contain location information correctly formatted.");
                }

                // Extract capacity
                String capacityPart = messageParts[0].trim(); // "Capacity: [value]"
                String[] capacitySplit = capacityPart.split(": ");
                int capacity = Integer.parseInt(capacitySplit[1].trim());

                // removed extract location

                System.out.println("Parsed Info: Capacity: " + capacity);

                // Store the capacity information
                // agents are stored in agentCapacities map when their capacities are received
                agentCapacities.put(msg.getSender(), capacity);
            } else if (messageContent.startsWith("Agent ")) {
                // Handle messages with proposal response
                System.out.println("Agent " + msg.getSender().getName() + " " + messageContent);

                // Add the proposal to the queue
                int proposalValue = Integer.parseInt(messageContent.split(" ")[4]); // Assuming the proposal value is the third word in the message
                agentProposals.add(new AbstractMap.SimpleEntry<>(msg.getSender(), proposalValue));

                // If all proposals have been received, make a decision
                if (agentProposals.size() == agentCapacities.size()) {
                    makeDecision();
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing message: " + messageContent);
            e.printStackTrace();
        }
    }

    //This method is focused solely on handling parcel-related messages and ensuring that parcel information is properly processed and broadcasted.
    private void parseParcelMessage(ACLMessage msg) {
        System.out.println("Received parcel message: " + msg.getContent() + " from " + msg.getSender().getName());
        String messageContent = msg.getContent();

        try {
            // Expected message format: "Parcel: [name], Weight: [value], Location: ([x], [y])"
            String[] parcelsInfo = messageContent.split("\n");

            for (String parcelInfo : parcelsInfo) {
                String[] messageParts = parcelInfo.split(", Location: ");
                if (messageParts.length < 2) {
                    throw new IllegalArgumentException("Parcel message does not contain location information correctly formatted.");
                }

                // Extract parcel name and weight
                String parcelAndWeightPart = messageParts[0].trim(); // "Parcel: [name], Weight: [value]"
                String[] parcelAndWeightSplit = parcelAndWeightPart.split(", Weight: ");
                String selectedParcelName = parcelAndWeightSplit[0].split(": ")[1].trim();
                int selectedParcelWeight = Integer.parseInt(parcelAndWeightSplit[1].trim());

                // Extract location
                String locationPart = messageParts[1].trim(); // "([x], [y])"
                locationPart = locationPart.substring(1, locationPart.length() - 1); // "[x], [y]"
                String[] coords = locationPart.split(",");
                int[] customerLocation = {Integer.parseInt(coords[0].trim()), Integer.parseInt(coords[1].trim())};

                System.out.println("Parsed Parcel Info: Parcel: " + selectedParcelName + ", Weight: " + selectedParcelWeight + ", Location: (" + customerLocation[0] + ", " + customerLocation[1] + ")");

                // After parsing the parcel information, it calls the broadcastParcelLocation() method to broadcast the parcel details to all delivery agents.
                broadcastParcelInformation(new String[]{selectedParcelName}, new int[]{selectedParcelWeight}, new int[][]{customerLocation});
            }
        } catch (Exception e) {
            System.err.println("Error parsing parcel message: " + messageContent);
            e.printStackTrace();
        }
    }

    private void requestCapacities() {
        // Define known delivery agent names
        String[] agentNames = {"DeliveryAgent", "DeliveryAgent1", "DeliveryAgent2", "DeliveryAgent3"};

        // Send a capacity request message to each known delivery agent
        for (String agentName : agentNames) {
            ACLMessage capacityRequest = new ACLMessage(ACLMessage.REQUEST);
            AID agentAID = new AID(agentName, AID.ISLOCALNAME);
            capacityRequest.addReceiver(agentAID);
            capacityRequest.setContent("Capacity request");
            send(capacityRequest);
        }
    }

    private void makeDecision() {
        System.out.println("\nMaking decision on parcel assignment...");

        // Check if there are any proposals and capacities available
        if (!agentProposals.isEmpty() && !agentCapacities.isEmpty()) {
            while (!agentProposals.isEmpty()) {
                // agentProposals.poll() to retrieve the top entry (proposal) from the agentProposals priority queue
                // this operation removes the entry from the queue
                Map.Entry<AID, Integer> entry = agentProposals.poll();
                // retrieved entry is stored in the variable "entry" which is a "Map.Entry"
                // containing an agent's AID entry.getKey and its proposal value entry.getValue
                AID agent = entry.getKey();
                int parcelWeight = entry.getValue();

                // retrieves agent's capacity
                Integer capacity = agentCapacities.get(agent);
                if (capacity != null && capacity >= parcelWeight) {
                    System.out.println("Sending parcel to " + agent.getLocalName() + " with capacity " + capacity);
                    // if agent has sufficient capacity, a message is constructed to accept the proposal using "ACLMessage.ACCEPT_PROPOSAL"
                    ACLMessage reply = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    reply.addReceiver(agent);
                    send(reply);

                    // Update the agent's capacity
                    // agentCapacities.put(agent, capacity - parcelWeight);
                    return; // Exit the loop after sending the parcel to the first suitable agent
                } else {
                    System.out.println(agent.getLocalName() + " doesn't have sufficient capacity.");
                }
            }
        }

        // If no suitable agent is found, inform accordingly
        System.out.println("No suitable agent available for parcel assignment.");
    }

    protected void takeDown() {
        // Perform agent cleanup here, if needed
        System.out.println("MasterRoutingAgent " + getAID().getName() + " is terminating.");
    }
}
