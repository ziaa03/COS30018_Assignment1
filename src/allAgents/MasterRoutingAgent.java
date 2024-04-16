package subTest1;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class MasterRoutingAgent extends Agent {
    private int selectedParcelWeight; // Example weight
    private String selectedParcelName; // Example name
    private int[] customerLocation = new int[2]; // Location of the customer

    protected void setup() {
        System.out.println("Hello! MasterAgent " + getAID().getName() + " is ready.");

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                	// Check if it's a route reception confirmation message
                    if (msg.getPerformative() == ACLMessage.CONFIRM && msg.getContent().equals("I receive the route")) {
                        System.out.println("Confirmation received from Delivery Agent: " + msg.getContent());
                    } else {
                        // Handle other types of messages
                        parseMessage(msg);
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void parseMessage(ACLMessage msg) {
        System.out.println("Received message: " + msg.getContent() + " from " + msg.getSender().getName());
        String messageContent = msg.getContent();

        try {
            // Expected message format: "Parcel drawn: [name] Weight: [weight] units Location: ([x], [y])"
            // First, split the message on "Location: " to isolate the location from the rest
            String[] messageParts = messageContent.split(" Location: ");
            if (messageParts.length < 2) {
                throw new IllegalArgumentException("Message does not contain location information correctly formatted.");
            }
            String locationPart = messageParts[1].trim(); // "(x, y)"
            locationPart = locationPart.substring(1, locationPart.length() - 1); // "x, y"
            String[] coords = locationPart.split(",");
            customerLocation[0] = Integer.parseInt(coords[0].trim());
            customerLocation[1] = Integer.parseInt(coords[1].trim());

            // Now handle the first part for parcel name and weight
            String parcelInfo = messageParts[0].trim();
            int weightStartIndex = parcelInfo.indexOf("Weight:");
            if (weightStartIndex == -1) {
                throw new IllegalArgumentException("Message does not contain weight information correctly formatted.");
            }
            String parcelNamePart = parcelInfo.substring(0, weightStartIndex).replace("Parcel drawn:", "").trim();
            String weightPart = parcelInfo.substring(weightStartIndex).replace("Weight:", "").replace("units", "").trim();

            selectedParcelName = parcelNamePart;
            selectedParcelWeight = Integer.parseInt(weightPart);

            System.out.println("Parsed Info: Parcel Name: " + selectedParcelName + ", Weight: " + selectedParcelWeight + ", Location: (" + customerLocation[0] + ", " + customerLocation[1] + ")");
            requestCapacity();
        } catch (Exception e) {
            System.err.println("Error parsing message: " + messageContent);
            e.printStackTrace();
        }
    }


    private void requestCapacity() {
        ACLMessage capacityRequest = new ACLMessage(ACLMessage.REQUEST);
        capacityRequest.addReceiver(new AID("DeliveryAgent", AID.ISLOCALNAME));
        capacityRequest.setContent("Requesting capacity");
        send(capacityRequest);

        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage capacityReply = blockingReceive(mt);
        if (capacityReply != null) {
            String replyContent = capacityReply.getContent();
            String[] replyParts = replyContent.split(", Location: ");
            int capacity = Integer.parseInt(replyParts[0].split(": ")[1].trim());
            if (selectedParcelWeight <= capacity) {
                System.out.println("Sending parcel to DeliveryAgent at location " + replyParts[1]);
                String pathSteps = calculateSteps(replyParts[1].trim());
                sendParcel(pathSteps);
            } else {
                System.out.println("Parcel weight exceeds DeliveryAgent's capacity. Refusing delivery.");
            }
        }
    }

    private String calculateSteps(String deliveryLocation) {
        StringBuilder pathBuilder = new StringBuilder();
        String[] locParts = deliveryLocation.substring(1, deliveryLocation.length() - 1).split(", ");
        int deliveryX = Integer.parseInt(locParts[0]);
        int deliveryY = Integer.parseInt(locParts[1]);
        int customerX = customerLocation[0];
        int customerY = customerLocation[1];

        pathBuilder.append("Path from Delivery to Customer:\n");
        int step = 0;
        for (int x = deliveryX; x != customerX; x += (customerX > deliveryX) ? 1 : -1) {
            pathBuilder.append("Step " + step++ + ": (" + x + "," + deliveryY + ")\n");
        }
        for (int y = deliveryY; y != customerY; y += (customerY > deliveryY) ? 1 : -1) {
            pathBuilder.append("Step " + step++ + ": (" + customerX + "," + y + ")\n");
        }
        pathBuilder.append("Step " + step++ + ": (" + customerX + "," + customerY + ")\n");

        pathBuilder.append("Path from Customer back to Delivery:\n");
        for (int y = customerY; y != deliveryY; y += (deliveryY > customerY) ? 1 : -1) {
            pathBuilder.append("Step " + step++ + ": (" + customerX + "," + y + ")\n");
        }
        for (int x = customerX; x != deliveryX; x += (deliveryX > customerX) ? 1 : -1) {
            pathBuilder.append("Step " + step++ + ": (" + x + "," + deliveryY + ")\n");
        }
        pathBuilder.append("Step " + step++ + ": (" + deliveryX + "," + deliveryY + ")\n");

        return pathBuilder.toString();
    }

    private void sendParcel(String pathSteps) {
        ACLMessage deliveryMsg = new ACLMessage(ACLMessage.INFORM);
        deliveryMsg.addReceiver(new AID("DeliveryAgent", AID.ISLOCALNAME));
        deliveryMsg.setContent("Parcel: " + selectedParcelName + ", Weight: " + selectedParcelWeight + "\n" + pathSteps);
        send(deliveryMsg);
    }
}
