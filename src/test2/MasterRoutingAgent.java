package test2;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class MasterRoutingAgent extends Agent
{
    private List<ParcelList> parcelLists = new ArrayList<>();
    private int[] vehicleLocation = {5, 5};
    private Map<AID, Integer> agentCapacities = new HashMap<>();
    private PriorityQueue<Map.Entry<AID, Integer>> agentProposals = new PriorityQueue<>((a, b) -> b.getValue() - a.getValue());
    private int totalWeight;

    protected void setup()
    {
        // WORKING
        System.out.println("Master routing agent is ready.");
        System.out.println("\u001B[30m" + "-------------------------------------------" + "\u001B[0m");

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

    // broadcasting info about the parcels in the parcel list for that region to all delivery agents
    private void broadcastParcelInformation()
    {
        // check if there are any parcel lists left to send
        // if parcel list is not empty means that there are parcel lists waiting to be sent
        if (!parcelLists.isEmpty())

        {
            ParcelList parcelList = parcelLists.get(0);
            StringBuilder messageContent = new StringBuilder();
            messageContent.append("Parcel Region: ").append(parcelList.name).append("\n");
            totalWeight = 0;
            for (Parcel parcel : parcelList.parcels) {
                messageContent.append("Parcel: ").append(parcel.name).append(", Weight: ").append(parcel.weight)
                        .append(", Location: (").append(parcel.x).append(", ").append(parcel.y).append(")\n");
                totalWeight += parcel.weight;
            }
            System.out.println("\u001B[30m" +" \n----- Total Weight from all parcels in " + parcelList.name + ": " + totalWeight + " ----- " + "\u001B[0m");

            ACLMessage cfpDeliveryMsg = new ACLMessage(ACLMessage.CFP);
            for (AID agent : agentCapacities.keySet()) {
                cfpDeliveryMsg.addReceiver(agent);
            }
            cfpDeliveryMsg.setContent(messageContent.toString());
            send(cfpDeliveryMsg);

            parcelLists.remove(0);

            // Process parcel lists and find the best route
            processParcelLists(messageContent.toString());
        }
    }

    // WORKING
    // handles msgs containing capacity information, proposal responses and capacity updates from DAs
    private void parseVehicleMessage(ACLMessage msg)
    {
        // starts by printing out the received msg content and the sender's name
        // extracts the content of the message and stores it in the 'messageContent' variable
        String messageContent = msg.getContent();

        try
        {
            // if messageContent starts w Capacity, the method assumes it contains both capacity and location information
            if (messageContent.startsWith("Capacity: "))
            {
                Utilities.printTitle();
                Utilities.printMessageWithoutAgent(msg.getContent() + " from " + "\u001B[36m" + msg.getSender().getLocalName() + "\u001B[0m");

                String[] messageParts = messageContent.split(", Location: ");
                if (messageParts.length < 2) {
                    throw new IllegalArgumentException("Message does not contain location information correctly formatted.");
                }

                // Extract capacity
                String capacityPart = messageParts[0].trim();
                String[] capacitySplit = capacityPart.split(": ");
                int capacity = Integer.parseInt(capacitySplit[1].trim());

                System.out.println("Parsed Info: Capacity: " + capacity + ", Location: (" + vehicleLocation[0] + ", " + vehicleLocation[1] + ")");

                // Store the capacity information
                // agents are stored in agentCapacities map when their capacities are received
                agentCapacities.put(msg.getSender(), capacity);
            }

            // handling messages received from DAs that contain proposal responses
            else if (messageContent.startsWith("Agent ")) {
                // Handle messages with proposal response
                System.out.println("Agent " + "\u001B[36m" + msg.getSender().getLocalName() + "\u001B[0m" + "\n" + messageContent);

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
        Utilities.printMessageWithoutAgent("\u001B[32m" + "Received parcel message" + "\u001B[0m" + " - " + msg.getContent() + " from " + "\u001B[36m" + msg.getSender().getLocalName() + "\u001B[0m");
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
        }
        catch (Exception e)
        {
            System.err.println("Error parsing parcel message: " + messageContent);
            e.printStackTrace();
        }
    }

    private void requestCapacities()
    {
        String[] agentNames = {"DeliveryAgent1", "DeliveryAgent2", "DeliveryAgent3", "DeliveryAgent4"};

        // Send a capacity request message to each known delivery agent
        for (String agentName : agentNames)
        {
            ACLMessage capacityRequest = new ACLMessage(ACLMessage.REQUEST);
            AID agentAID = new AID(agentName, AID.ISLOCALNAME);
            capacityRequest.addReceiver(agentAID);
            capacityRequest.setContent("Capacity request");
            send(capacityRequest);
        }
    }

    private void makeDecision()
    {
        System.out.println("\u001B[30m" + "\n----- Master routing agent -----" + "\u001B[0m");
        System.out.println("Making decision on parcel assignment...");

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
                    System.out.println("Sending parcel to " + "\u001B[32m" + agent.getLocalName() + "\u001B[0m" + " with capacity " + capacity + "\n");
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
                    System.out.println("Parcel assigned to " + "\u001B[36m" + agent.getLocalName() + "\u001B[0m");
                    ACLMessage acceptMessage = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    acceptMessage.addReceiver(agent);
                    send(acceptMessage);
                } else {
                    System.out.println("Parcel not assigned to " + "\u001B[36m" + agent.getLocalName() + "\u001B[0m");
                    ACLMessage rejectMessage = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                    rejectMessage.addReceiver(agent);
                    send(rejectMessage);
                }
            }
            // Clear agentProposals
            agentProposals.clear();
        }
        else
        {
            System.out.println("No proposals or capacities available.");
        }
    }

    private void processParcelLists(String content) {
        // Parse the content to obtain parcel lists
        String[] regions = content.split("Parcel Region: ");
        for (int i = 1; i < regions.length; i++) {
            String[] lines = regions[i].split("\n");
            String regionName = lines[0];
            List<Parcel> parcels = new ArrayList<>();
            for (int j = 1; j < lines.length; j++) {
                String line = lines[j];
                // Split each line by ", Weight: " to separate parcel name and weight
                String[] parts = line.split(", Weight: ");
                if (parts.length == 2) {
                    // Extract parcel name and weight
                    String parcelName = parts[0].split(": ")[1];
                    int parcelWeight = Integer.parseInt(parts[1].split(", Location: ")[0].trim());
                    // Extract parcel location
                    String location = parts[1].split(", Location: ")[1];
                    // Split the location string by the comma and extract x and y coordinates
                    String[] locationParts = location.substring(1, location.length() - 1).split(", ");
                    int parcelX = Integer.parseInt(locationParts[0]);
                    int parcelY = Integer.parseInt(locationParts[1]);
                    // Create a new Parcel object and add it to the list
                    parcels.add(new Parcel(parcelName, parcelWeight, parcelX, parcelY));
                } else {
                    System.err.println("Error parsing parcel message: " + line);
                }
            }

            // Create a ParcelList object
            ParcelList parcelList = new ParcelList(regionName, parcels);
            System.out.println("\u001B[30m" + "----- Created Parcel List: " + "\u001B[0m" + "\u001B[32m" + parcelList + "\u001B[0m" + " ----- ");

            // Print the distance matrix
            double[][] distanceMatrix = parcelList.distanceMatrix;
            // Print the distance matrix
            Utilities.printMessageWithoutAgent("Distance Matrix for " + regionName + ":");
            for (int k = 0; k < distanceMatrix.length; k++) {
                for (int l = 0; l < distanceMatrix[k].length; l++) {
                    System.out.printf("%.1f ", distanceMatrix[k][l]); // Print with one decimal place
                }
                System.out.println();
            }


            // Invoke GeneticAlgorithm to find the best route for this region
            List<Integer> bestRoute = GeneticAlgorithm.runGeneticAlgorithmForParcels(parcelList);

            // Print out the best route for this region
            System.out.println("\nBest route in " + regionName + ": " + bestRoute + "\n"); // Assuming the first route is the best
        }
    }

    protected void takeDown()
    {
        // Perform agent cleanup here, if needed
        System.out.println("MasterRoutingAgent " + getAID().getName() + " is terminating.");
    }
}