package DVRS;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class MasterRoutingAgent extends Agent {
    private List<ParcelList> parcelLists = new ArrayList<>();
    private int[] vehicleLocation = {25, 25}; // Location of the customer
    private Map<AID, Integer> agentCapacities = new HashMap<>();
    private Map<AID, Boolean> agentAvailability = new HashMap<>();
    private PriorityQueue<Map.Entry<AID, Integer>> agentProposals = new PriorityQueue<>((a, b) -> b.getValue() - a.getValue());
    private int totalWeight;

    protected void setup()
    {
        // WORKING
        System.out.println("Hello! MasterAgent " + getAID().getName() + " is ready.");

        addBehaviour(new CyclicBehaviour(this)
        {
            public void action()
            {
                ACLMessage msg = receive();
                if (msg != null)
                {
                    // Check if it's a route reception confirmation message
                    if (msg.getPerformative() == ACLMessage.CONFIRM && msg.getContent().equals("I received the route.")) {
                        System.out.println("Confirmation received from Delivery Agent: " + msg.getContent());
                    }
                    else
                    {
                        // Handle other types of messages
                        if (msg.getContent().startsWith("Parcel Region:"))
                        {
                            parseParcelMessage(msg);
                            // parseParcelMessageIntoLists(msg);
                        }
                        else
                        {
                            parseVehicleMessage(msg);
                        }
                    }
                }
                else
                {
                    block();
                }

                // If all proposals have been received and processed, send out the next parcel list
                if (agentProposals.isEmpty() && !parcelLists.isEmpty())
                {
                    broadcastParcelInformation();
                }
            }
        });

        requestCapacities();
    }

    // WORKING
    // broadcasts information about the selected parcel to all delivery agents
    private void broadcastParcelInformation()
    {
        // check if there are any parcel lists left to send
        // if parcel list is not empty means that there are parcel lists waiting to be sent

        if (!parcelLists.isEmpty())
        {
            // Get the first parcel list
            ParcelList parcelList = parcelLists.get(0);

            // Construct message content for the parcel list
            StringBuilder messageContent = new StringBuilder();
            // Append the name of the parcel region
            messageContent.append("Parcel Region: ").append(parcelList.name).append("\n");
            totalWeight = 0; // Initialize total weight for the current parcel list
            for (Parcel parcel : parcelList.parcels) {
                messageContent.append("Parcel: ").append(parcel.name).append(", Weight: ").append(parcel.weight)
                        .append(", Location: (").append(parcel.x).append(", ").append(parcel.y).append(")\n");
                totalWeight += parcel.weight; // Accumulate the weight of each parcel
            }
            // Print out the total weight for the current parcel list
            System.out.println("Total Weight from all parcels in " + parcelList.name + ": " + totalWeight);

            // Create ACL message for each delivery agent and add parcel list information
            ACLMessage cfpDeliveryMsg = new ACLMessage(ACLMessage.CFP);
            for (AID agent : agentCapacities.keySet()) {
                cfpDeliveryMsg.addReceiver(agent);
            }
            cfpDeliveryMsg.setContent(messageContent.toString());
            send(cfpDeliveryMsg);

            // Remove the list that was just sent
            parcelLists.remove(0);
        }
    }

    // WORKING
    // handles msgs containing capacity information, proposal responses and capacity updates from DAs
    private void parseVehicleMessage(ACLMessage msg) {
        //starts by printing out the received msg content and the sender's name
        System.out.println("Received vehicle's information message: " + msg.getContent() + " from " + msg.getSender().getName());

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

                System.out.println("Parsed Info: Capacity: " + capacity + ", Location: (" + vehicleLocation[0] + ", " + vehicleLocation[1] + ")");

                // Store the capacity information
                // agents are stored in agentCapacities map when their capacities are received
                agentCapacities.put(msg.getSender(), capacity);
            }

            // handling messages received from DAs that contain proposal responses
            else if (messageContent.startsWith("Agent ")) {
                // Handle messages with proposal response
                System.out.println("Agent " + msg.getSender().getName() + " " + messageContent);

                // Add the proposal to the queue
                int proposalValue = Integer.parseInt(messageContent.split(" ")[4]); // Assuming the proposal value is the third word in the message
                agentProposals.add(new AbstractMap.SimpleEntry<>(msg.getSender(), proposalValue));

                // If all proposals have been received for the current parcel list, make a decision
                if (agentProposals.size() == agentCapacities.size()) {
                    makeDecision();

                    // Clear the proposals for the next parcel list
                    agentProposals.clear();
                }
            }

            // handling messages received from DAs that contain capacity updates
            else if (messageContent.startsWith("Current Capacity: ")) {
                // Handle messages with capacity update
                String[] capacitySplit = messageContent.split(": ");
                int currentCapacity = Integer.parseInt(capacitySplit[1].trim());
                System.out.println("Updated Info: Current Capacity: " + currentCapacity);

                // Update the capacity information
                agentCapacities.put(msg.getSender(), currentCapacity);
            }
        } catch (Exception e) {
            System.err.println("Error parsing message: " + messageContent);
            e.printStackTrace();
        }
    }



    // WORKING
    private void parseParcelMessage(ACLMessage msg) {
        System.out.println("Received parcel message: " + msg.getContent() + " from " + msg.getSender().getName());
        String messageContent = msg.getContent();

        try {
            // Split the message by newline character to separate individual parcels
            String[] parcelMessages = messageContent.split("\n");

            // Create a new list to store the parcels from this message
            List<Parcel> parcels = new ArrayList<>();

            for (String parcelMsg : parcelMessages) {
                // Check if the line follows the expected format
                if (parcelMsg.startsWith("Parcel:")) {
                    // Extract parcel name, weight, and location for each parcel
                    String[] messageParts = parcelMsg.split(", Weight: ");
                    String[] parcelAndWeightSplit = messageParts[0].split(": ");
                    String parcelName = parcelAndWeightSplit[1].trim();
                    int parcelWeight = Integer.parseInt(messageParts[1].split(", Location: ")[0].trim());
                    String locationPart = messageParts[1].split(", Location: ")[1].trim(); // "([x], [y])"
                    locationPart = locationPart.substring(1, locationPart.length() - 1); // "[x], [y]"
                    String[] coords = locationPart.split(",");
                    int x = Integer.parseInt(coords[0].trim());
                    int y = Integer.parseInt(coords[1].trim());

                    // Create a new Parcel object and add it to the list
                    parcels.add(new Parcel(parcelName, parcelWeight, x, y));

                    // Print the parsed parcel info to the console
                    System.out.println("Parsed Parcel Info: Parcel: " + parcelName + ", Weight: " + parcelWeight + ", Location: (" + x + ", " + y + ")");
                }
            }

            // ParcelList obj with parcels and a name
            ParcelList parcelList = new ParcelList("Parcel List", parcels);

            // Add the list of parcels to parcelLists
            parcelLists.add(parcelList);

            // Now you can perform any necessary actions with the parsed parcels, such as broadcasting information or making decisions
            broadcastParcelInformation();
        } catch (Exception e) {
            System.err.println("Error parsing parcel message: " + messageContent);
            e.printStackTrace();
        }
    }

//    // COMBINED WITH parseParcelMessage
//    private void parseParcelMessageIntoLists(ACLMessage msg) {
//        String content = msg.getContent();
//        String[] lines = content.split("\n");
//        String regionName = lines[0].split(": ")[1];
//        List<Parcel> parcels = new ArrayList<>();
//        for (int i = 1; i < lines.length; i++) {
//            if (lines[i].startsWith("Parcel:")) {
//                String[] messageParts = lines[i].split(", Weight: ");
//                String[] parcelAndWeightSplit = messageParts[0].split(": ");
//                String name = parcelAndWeightSplit[1].trim();
//                int weight = Integer.parseInt(messageParts[1].split(", Location: ")[0].trim());
//                String locationPart = messageParts[1].split(", Location: ")[1].trim(); // "([x], [y])"
//                locationPart = locationPart.substring(1, locationPart.length() - 1); // "[x], [y]"
//                String[] coords = locationPart.split(",");
//                int x = Integer.parseInt(coords[0].trim());
//                int y = Integer.parseInt(coords[1].trim());
//                parcels.add(new Parcel(name, weight, x, y));
//            }
//        }
//        parcelLists.add(new ParcelList(regionName, parcels));
//    }

    // WORKING
    private void requestCapacities()
    {
        // Define known delivery agent names
        String[] agentNames = {"DeliveryAgent1", "DeliveryAgent2", "DeliveryAgent3", "DeliveryAgent4"};

        // Send a capacity request message to each known delivery agent
        for (String agentName : agentNames) {
            ACLMessage capacityRequest = new ACLMessage(ACLMessage.REQUEST);
            AID agentAID = new AID(agentName, AID.ISLOCALNAME);
            capacityRequest.addReceiver(agentAID);
            capacityRequest.setContent("Capacity request");
            send(capacityRequest);

            // Randomize availability (for demonstration, you can modify this)
            boolean isAvailable = new Random().nextBoolean();
            agentAvailability.put(agentAID, isAvailable);
        }
    }

    private void makeDecision() {
        System.out.println("\nMaking decision on parcel assignment...");

        // Check if there are any proposals and capacities available
        if (!agentProposals.isEmpty() && !agentCapacities.isEmpty()) {
            // Find the highest capacity among all agents
            List<AID> agentsWithMaxCapacity = new ArrayList<>();
            int maxCapacity = Integer.MIN_VALUE;
            for (Map.Entry<AID, Integer> entry : agentCapacities.entrySet()) {
                int capacity = entry.getValue();
                if (capacity > maxCapacity) {
                    maxCapacity = capacity;
                    agentsWithMaxCapacity.clear();
                    agentsWithMaxCapacity.add(entry.getKey());
                } else if (capacity == maxCapacity) {
                    agentsWithMaxCapacity.add(entry.getKey());
                }
            }

            // Shuffle the list of agents with the highest capacity to randomize selection
            Collections.shuffle(agentsWithMaxCapacity);

            AID assignedAgent = null;
            int parcelWeight = totalWeight;
            // Assign parcel to the first suitable agent
            for (AID agent : agentsWithMaxCapacity) {
                Integer capacity = agentCapacities.get(agent);
                if (capacity != null && capacity >= parcelWeight) {
                    System.out.println("Sending parcel to " + agent.getLocalName() + " with capacity " + capacity);
                    ACLMessage acceptMessage = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    acceptMessage.addReceiver(agent);
                    send(acceptMessage);

                    // Update the agent's capacity
                    agentCapacities.put(agent, capacity - parcelWeight);
                    assignedAgent = agent; // Mark this agent as having received the parcel
                    break;
                }
            }

            // Print messages for every delivery agent, whether assigned or not
            for (Map.Entry<AID, Integer> entry : agentCapacities.entrySet()) {
                AID agent = entry.getKey();
                if (agent.equals(assignedAgent)) {
                    System.out.println("Parcel assigned to " + agent.getLocalName());
                    ACLMessage acceptMessage = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    acceptMessage.addReceiver(agent);
                    send(acceptMessage);
                } else {
                    System.out.println("Parcel not assigned to " + agent.getLocalName());
                    ACLMessage rejectMessage = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                    rejectMessage.addReceiver(agent);
                    send(rejectMessage);
                }
            }

            // Clear agentProposals
            agentProposals.clear();
        } else {
            System.out.println("No proposals or capacities available.");
        }
    }


    protected void takeDown() {
        // Perform agent cleanup here, if needed
        System.out.println("MasterRoutingAgent " + getAID().getName() + " is terminating.");
    }
}