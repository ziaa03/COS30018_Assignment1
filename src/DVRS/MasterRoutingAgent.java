package DVRS;

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
        System.out.println("Master routing agent is ready.");
        System.out.println("\u001B[30m" + "-------------------------------------------" + "\u001B[0m");

        addBehaviour(new CyclicBehaviour(this)
        {
            public void action()
            {
                ACLMessage msg = receive();
                if (msg != null)
                {
                    System.out.println("Received message: " + msg.getContent());
                    if (msg.getPerformative() == ACLMessage.CONFIRM && msg.getContent().equals("I received the route.")) {
                        System.out.println("Confirmation received from Delivery Agent: " + msg.getContent());
                    }
                    else
                    {
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
                if (agentProposals.isEmpty() && !parcelLists.isEmpty())
                {
                    broadcastParcelInformation();
                }
            }
        });

        requestCapacities();
    }

    private void broadcastParcelInformation()
    {
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
            processParcelLists(messageContent.toString());
        }
    }

    private void parseVehicleMessage(ACLMessage msg)
    {
        String messageContent = msg.getContent();

        try
        {
            if (messageContent.startsWith("Capacity: "))
            {
                Utilities.printTitle();
                Utilities.printMessageWithoutAgent(msg.getContent() + " from " + "\u001B[36m" + msg.getSender().getLocalName() + "\u001B[0m");

                String[] messageParts = messageContent.split(", Location: ");
                if (messageParts.length < 2) {
                    throw new IllegalArgumentException("Message does not contain location information correctly formatted.");
                }
                String capacityPart = messageParts[0].trim();
                String[] capacitySplit = capacityPart.split(": ");
                int capacity = Integer.parseInt(capacitySplit[1].trim());
                System.out.println("Parsed Info: Capacity: " + capacity + ", Location: (" + vehicleLocation[0] + ", " + vehicleLocation[1] + ")");
                agentCapacities.put(msg.getSender(), capacity);
            }

            else if (messageContent.startsWith("Agent ")) {
                System.out.println("Agent " + "\u001B[36m" + msg.getSender().getLocalName() + "\u001B[0m" + "\n" + messageContent);
                int proposalValue = Integer.parseInt(messageContent.split(" ")[4]);
                agentProposals.add(new AbstractMap.SimpleEntry<>(msg.getSender(), proposalValue));

                if (agentProposals.size() == agentCapacities.size()) {
                    makeDecision();
                    agentProposals.clear();
                }
            }

            else if (messageContent.startsWith("Current Capacity: ")) {
                String[] capacitySplit = messageContent.split(": ");
                int currentCapacity = Integer.parseInt(capacitySplit[1].trim());
                System.out.println("Updated Info: Current Capacity: " + currentCapacity);
                agentCapacities.put(msg.getSender(), currentCapacity);
            }
        } catch (Exception e) {
            System.err.println("Error parsing message: " + messageContent);
            e.printStackTrace();
        }
    }

    private void parseParcelMessage(ACLMessage msg) {
        ParcelList parcelList = null;
        Utilities.printMessageWithoutAgent("\u001B[30m" + "\n----- PARCEL INFORMATION " + ": " + " ----- " + "\u001B[0m");
        Utilities.printMessageWithoutAgent("\u001B[32m" + "Received parcel message" + "\u001B[0m" + " - " + msg.getContent() + " from " + "\u001B[36m" + msg.getSender().getLocalName() + "\u001B[0m");
        String messageContent = msg.getContent();

        try {
            String[] parcelMessages = messageContent.split("\n");
            List<Parcel> parcels = new ArrayList<>();

            for (String parcelMsg : parcelMessages) {
                if (parcelMsg.startsWith("Parcel:")) {
                    String[] messageParts = parcelMsg.split(", Weight: ");
                    String[] parcelAndWeightSplit = messageParts[0].split(": ");
                    String parcelName = parcelAndWeightSplit[1].trim();
                    int parcelWeight = Integer.parseInt(messageParts[1].split(", Location: ")[0].trim());
                    String locationPart = messageParts[1].split(", Location: ")[1].trim(); // "([x], [y])"
                    locationPart = locationPart.substring(1, locationPart.length() - 1); // "[x], [y]"
                    String[] coords = locationPart.split(",");
                    int x = Integer.parseInt(coords[0].trim());
                    int y = Integer.parseInt(coords[1].trim());
                    parcels.add(new Parcel(parcelName, parcelWeight, x, y));
                    System.out.println("Parsed Parcel Info: Parcel: " + parcelName + ", Weight: " + parcelWeight + ", Location: (" + x + ", " + y + ")");
                }
            }
            parcelList = new ParcelList("Parcel List", parcels);
            parcelLists.add(parcelList);
            broadcastParcelInformation();
        } catch (Exception e) {
            System.err.println("Error parsing parcel message: " + messageContent);
            e.printStackTrace();
        }
    }

    private void requestCapacities()
    {
        String[] agentNames = {"DeliveryAgent1", "DeliveryAgent2", "DeliveryAgent3", "DeliveryAgent4"};
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

        if (!agentProposals.isEmpty() && !agentCapacities.isEmpty()) {
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
                    agentCapacities.put(agent, capacity - parcelWeight);
                    assignedAgent = agent;
                    break;
                }
            }

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
            agentProposals.clear();
        }
        else
        {
            System.out.println("No proposals or capacities available.");
        }
    }

    private void processParcelLists(String content) {
        String[] regions = content.split("Parcel Region: ");
        for (int i = 1; i < regions.length; i++) {
            String[] lines = regions[i].split("\n");
            String regionName = lines[0];
            List<Parcel> parcels = new ArrayList<>();
            for (int j = 1; j < lines.length; j++) {
                String line = lines[j];
                String[] parts = line.split(", Weight: ");
                if (parts.length == 2) {
                    String parcelName = parts[0].split(": ")[1];
                    int parcelWeight = Integer.parseInt(parts[1].split(", Location: ")[0].trim());
                    String location = parts[1].split(", Location: ")[1];
                    String[] locationParts = location.substring(1, location.length() - 1).split(", ");
                    int parcelX = Integer.parseInt(locationParts[0]);
                    int parcelY = Integer.parseInt(locationParts[1]);
                    parcels.add(new Parcel(parcelName, parcelWeight, parcelX, parcelY));
                } else {
                    System.err.println("Error parsing parcel message: " + line);
                }
            }

            ParcelList parcelList = new ParcelList(regionName, parcels);
            System.out.println("\u001B[30m" + "----- Created Parcel List: " + "\u001B[0m" + "\u001B[32m" + parcelList + "\u001B[0m" + " ----- ");

            double[][] distanceMatrix = parcelList.distanceMatrix;
            Utilities.printMessageWithoutAgent("Distance Matrix for " + regionName + ":");
            for (int k = 0; k < distanceMatrix.length; k++) {
                for (int l = 0; l < distanceMatrix[k].length; l++) {
                    System.out.printf("%.1f ", distanceMatrix[k][l]);
                }
                System.out.println();
            }
            List<Integer> bestRoute = GeneticAlgorithm.runGeneticAlgorithmForParcels(parcelList);
            System.out.println("\nBest route in " + regionName + ": " + bestRoute + "\n");
            sendBestRoute(regionName, bestRoute);
        }
    }

    private void sendBestRoute(String regionName, List<Integer> bestRoute) {
        StringBuilder messageContent = new StringBuilder();
        messageContent.append("Best route in ").append(regionName).append(": ").append(bestRoute.toString());
        Utilities.printMessageWithoutAgent("\u001B[30m" + "\n----- Sending Best Route Information to: CustomerAgent ----- " + "\u001B[0m");
        ACLMessage routeMsg = new ACLMessage(ACLMessage.INFORM);
        routeMsg.setContent(messageContent.toString());
        routeMsg.addReceiver(new AID("CustomerAgent", AID.ISLOCALNAME));
        send(routeMsg);
    }

    protected void takeDown() {
        System.out.println("MasterRoutingAgent " + getAID().getName() + " is terminating.");
    }
}