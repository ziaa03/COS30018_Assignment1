package DVRS;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.*;

public class MasterRoutingAgent extends Agent
{
    private List<ParcelList> parcelLists = new ArrayList<>();
    private Map<AID, Integer> agentCapacities = new HashMap<>();
    private PriorityQueue<Map.Entry<AID, Integer>> agentProposals = new PriorityQueue<>((a, b) -> b.getValue() - a.getValue());
    private int totalWeight;
    private String RegionName;
    private List<Integer> BestRoute;
    AID agentchosen;
    int number = 0;
    String[] abc = {"Region A", "Region B", "Region C", "Region D"};


    protected void setup()
    {
        // WORKING
        System.out.println("Master routing agent is ready.");
        System.out.println("\u001B[30m" + "-------------------------------------------" + "\u001B[0m");

        // cyclic behavior for parcelmessages from CA
        // & for vehiclemessages from DAs
        addBehaviour(new CyclicBehaviour(this)
        {
            public void action()
            {
                ACLMessage msg = receive();
                if (msg != null)
                {
                    System.out.println("Received message: " + msg.getContent());
                    // Check if it's a route reception confirmation message
//                    if (msg.getPerformative() == ACLMessage.CONFIRM && msg.getContent().equals("I received the route.")) {
//                        System.out.println("Confirmation received from Delivery Agent: " + msg.getContent());
//                    }
//                    else
//                    {
                    // Handle other types of messages
                    if (msg.getContent().startsWith("Parcel Region:"))
                    {
                        parseParcelMessage(msg);
                    }
                    else
                    {
                        parseVehicleMessage(msg);
                    }
//                    }
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
    private void parseParcelMessage(ACLMessage msg) {
        ParcelList parcelList = null;
        Utilities.printMessageWithoutAgent("\u001B[30m" + "\n----- PARCEL INFORMATION " + ": " + " ----- " + "\u001B[0m");
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
            if(number == 4) {
                number = 0;
            }
            // ParcelList obj with parcels and a name
            parcelList = new ParcelList(abc[number], parcels);

            // Add the list of parcels to parcelLists
            parcelLists.add(parcelList);

            // Now you can perform any necessary actions with the parsed parcels, such as broadcasting information or making decisions
            broadcastParcelInformation();
            number++;
        } catch (Exception e) {
            System.err.println("Error parsing parcel message: " + messageContent);
            e.printStackTrace();
        }
    }

    // WORKING
    // handles msgs containing capacity information, proposal responses and capacity updates from DAs
    private void parseVehicleMessage(ACLMessage msg)
    {
        // starts by printing out the received msg content and the sender's name
        // extracts the content of the message and stores it in the 'messageContent' variable
        String messageContent = msg.getContent();

        try {
            // Check if the message contains capacity information
            if (messageContent.startsWith("Capacity: ")) {
                // Print the received message content and the sender's name
                Utilities.printTitle();
                Utilities.printMessageWithoutAgent(msg.getContent() + " from " + "\u001B[36m" + msg.getSender().getLocalName() + "\u001B[0m");

                // Split the message content by "Capacity: "
                String[] messageParts = messageContent.split("Capacity: ");
                if (messageParts.length < 2) {
                    throw new IllegalArgumentException("Message does not contain capacity information correctly formatted.");
                }

                // Extract capacity
                String capacityPart = messageParts[1].trim();
                int capacity = Integer.parseInt(capacityPart);

                // Print the parsed capacity information
                System.out.println("Parsed Info: Capacity: " + capacity);

                // Store the capacity information provided by the specific delivery agent
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



    // broadcasting info about the parcels in the parcel list for that region to all delivery agents
    private void broadcastParcelInformation() {
        // Check if there are any parcel lists left to send
        if (!parcelLists.isEmpty()) {
            ParcelList parcelList = parcelLists.get(0);
            // Check if the parcel list is empty
            if (!parcelList.parcels.isEmpty()) {
                StringBuilder messageContent = new StringBuilder();
                messageContent.append("Parcel Region: ").append(parcelList.name).append("\n");
                totalWeight = 0;
                for (Parcel parcel : parcelList.parcels) {
                    messageContent.append("Parcel: ").append(parcel.name).append(", Weight: ").append(parcel.weight)
                            .append(", Location: (").append(parcel.x).append(", ").append(parcel.y).append(")\n");
                    totalWeight += parcel.weight;
                }
                System.out.println("\u001B[30m" +" \n----- Total Weight from all parcels in " + parcelList.name + ": " + totalWeight + " ----- " + "\u001B[0m");

                //broadcasts a CFP message to all DAs, allowing for bidding for handling the parcel list described in 'messageContent'
                ACLMessage cfpDeliveryMsg = new ACLMessage(ACLMessage.CFP);
                for (AID agent : agentCapacities.keySet()) {
                    cfpDeliveryMsg.addReceiver(agent);
                }
                cfpDeliveryMsg.setContent(messageContent.toString());
                send(cfpDeliveryMsg);

                parcelLists.remove(0);

                // Process parcel lists and find the best route
                processParcelLists(messageContent.toString());
            } else {
                System.out.println("Parcel list for region " + parcelList.name + " is empty.");
                // Remove the empty parcel list and proceed to the next one
                parcelLists.remove(0);
                // Continue to broadcast parcel information for the next parcel list
                broadcastParcelInformation();
            }
        } else {
            System.out.println("No more parcel lists to send.");
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

    protected void makeDecision()
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
                    System.out.println("Parcel list assigned to " + "\u001B[36m" + agent.getLocalName() + "\u001B[0m");
                    ACLMessage acceptMessage = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    acceptMessage.addReceiver(agent);
                    send(acceptMessage);
                    agentchosen = agent;
                } else {
                    System.out.println("Parcel list not assigned to " + "\u001B[36m" + agent.getLocalName() + "\u001B[0m");
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
        sendBestRoute(RegionName, BestRoute);
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
            RegionName = regionName;
            BestRoute = bestRoute;
        }
    }

    private void sendBestRoute(String regionName, List<Integer> bestRoute) {
        // Construct the message content with the best route information
        StringBuilder messageContent = new StringBuilder();
        messageContent.append("Best route in ").append(regionName).append(": ").append(bestRoute.toString());

        Utilities.printMessageWithoutAgent("\u001B[30m" + "\n----- Sending Best Route Information to: CustomerAgent ----- " + "\u001B[0m");

        // Create an ACLMessage to send the best route information
        ACLMessage routeMsgCust = new ACLMessage(ACLMessage.INFORM);
        routeMsgCust.setContent(messageContent.toString());
        routeMsgCust.addReceiver(new AID("CustomerAgent", AID.ISLOCALNAME));
        send(routeMsgCust);

        Utilities.printMessageWithoutAgent("\u001B[30m" + "\n----- Sending Best Route Information to: DeliveryAgent ----- " + "\u001B[0m");

        ACLMessage routeMsgDeli = new ACLMessage(ACLMessage.INFORM);
        routeMsgDeli.setContent(messageContent.toString());

        routeMsgDeli.addReceiver(agentchosen);

        send(routeMsgDeli);
    }

    protected void takeDown() {
        // Perform agent cleanup here, if needed
        System.out.println("MasterRoutingAgent " + getAID().getName() + " is terminating.");
    }
}