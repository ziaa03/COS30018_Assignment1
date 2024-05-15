package DVRS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

public class VRPGui extends JFrame {
    private JButton solveButton;
    private JButton showParcelsButton; // new button
    private JTextArea resultArea;
    private JPanel mapPanel;
    private static VRPGui instance;
    private CustomerAgent customerAgent;
    private DeliveryAgent1 deliveryAgent1;
    private MasterRoutingAgent masterRoutingAgent;

    public VRPGui(CustomerAgent customerAgent, DeliveryAgent1 deliveryAgent1, MasterRoutingAgent masterRoutingAgent) {
        // initialise components
        solveButton = new JButton("Begin Routing");
        resultArea = new JTextArea(25, 50);
        resultArea.setEditable(false);

        // new button for showing parcels
        showParcelsButton = new JButton("Show Parcels");

        // initialise agents
        this.customerAgent = customerAgent;
        this.customerAgent.setup();
        this.masterRoutingAgent = masterRoutingAgent;
        this.masterRoutingAgent.setup();
        this.deliveryAgent1 = deliveryAgent1;
        this.deliveryAgent1.setup();

        // map panel - for parcel pts
        List<Integer> bestRoute = new ArrayList<>();   // empty best route list
        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);  // overrides paint component to draw parcels on it using drawparcels method
                drawParcels(g, bestRoute);
            }
        };

     // layout
        setLayout(new BorderLayout());

        // Panel for map and label
        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.add(mapPanel, BorderLayout.CENTER);

        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Delivery Vehicle Routing System GUI");
        labelPanel.add(label);
        mapContainer.add(labelPanel, BorderLayout.NORTH);

        JPanel buttonPanelMap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanelMap.add(showParcelsButton);
        mapContainer.add(buttonPanelMap, BorderLayout.SOUTH);

        add(mapContainer, BorderLayout.CENTER);

        
        // Panel for result area and Begin Routing button
        JPanel resultContainer = new JPanel(new BorderLayout());
        resultContainer.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        
        JPanel labelPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label2 = new JLabel(" ");
        labelPanel2.add(label2);
        resultContainer.add(labelPanel2, BorderLayout.NORTH);
        
        JPanel buttonPanelResult = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanelResult.add(solveButton);
        resultContainer.add(buttonPanelResult, BorderLayout.SOUTH);

        add(resultContainer, BorderLayout.EAST);

        // Set preferred sizes for components
        resultArea.setPreferredSize(new Dimension(250, 500));
        mapPanel.setPreferredSize(new Dimension(500, 500));



        // when button is clicked, calls the sendParcelsToMRA - customer send to mra their locations
        showParcelsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendParcelsToMasterRoutingAgent();
            }
        });

        // when button is clicked, calls the sendParcelsToMRA - customer send to mra their locations
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayBestRoutes();
            }
        });

        

        // window setup
        setTitle("Delivery Vehicle Routing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        setPosition();
    }

    private void drawParcels(Graphics g, List<Integer> currentRoute) {
        // clear map panel before redrawing any parcels or routes - to get a clean canvas (old content might be visible)
        g.clearRect(0, 0, mapPanel.getWidth(), mapPanel.getHeight());

        // draw a border around the map panel
        g.drawRect(0, 0, mapPanel.getWidth() - 1, mapPanel.getHeight() - 1);

        // colors for the region labels and separator lines
        Color regionLabelColor = Color.BLACK;
        Color separatorLineColor = Color.BLACK;

        // region labels font
        Font regionLabelFont = new Font("Arial", Font.BOLD, 12);
        g.setFont(regionLabelFont);

        // get parcel lists from the CustomerAgent
        List<ParcelList> parcelLists = customerAgent.getParcelLists();

        // calculate the maximum x and y coordinates of all parcels
        int maxX = 12;
        int maxY = 12;

        // draw horizontal and vertical separator lines at the middle of the map panel
        int separatorX = mapPanel.getWidth() / 2;
        int separatorY = mapPanel.getHeight() / 2;
        g.setColor(separatorLineColor);
        g.drawLine(separatorX, 0, separatorX, mapPanel.getHeight() - 1);
        g.drawLine(0, separatorY, mapPanel.getWidth() - 1, separatorY);

        // draw labels for the regions
        g.setColor(regionLabelColor);
        g.drawString("Region A", 10, separatorY - 10);
        g.drawString("Region B", separatorX + 10, separatorY - 10);
        g.drawString("Region C", 10, mapPanel.getHeight() - 10);
        g.drawString("Region D", separatorX + 10, mapPanel.getHeight() - 10);

        // Loop through each ParcelList
        for (ParcelList parcelList : parcelLists) {
            // Get the parcels for this ParcelList
            List<Parcel> parcels = parcelList.getParcels();

            // Loop through each Parcel in the ParcelList
            for (Parcel parcel : parcels) {
                // Determine the region of the parcel based on its coordinates
                int region = (parcel.getX() > maxX / 2 ? 1 : 0) + (parcel.getY() > maxY / 2 ? 2 : 0);

                // Calculate the width and height of the region
                int regionWidth = (region % 2 == 0) ? separatorX : mapPanel.getWidth() - separatorX;
                int regionHeight = (region < 2) ? separatorY : mapPanel.getHeight() - separatorY;

                // Calculate the x and y coordinates of the region
                int regionX = (region % 2 == 0) ? 0 : separatorX;
                int regionY = (region < 2) ? 0 : separatorY;

                // Translate the x and y coordinates of the parcel to fit within the region
                int x = regionX + (int) ((double) (parcel.getX() - (region % 2) * maxX / 2) / (maxX / 2) * regionWidth);
                int y = regionY + (int) ((double) (parcel.getY() - (region / 2) * maxY / 2) / (maxY / 2) * regionHeight);

                // Draw a dot at the translated coordinates
                g.setColor(Color.BLUE);
                g.fillRect(x, y, 6, 6);

                // Define the font for the parcel labels
                Font parcelLabelFont = new Font("Arial", Font.PLAIN, 12); // adjust the size as needed

                // Set the font before drawing the parcel labels
                g.setFont(parcelLabelFont);

                // Draw the label of the parcel next to the dot
                g.setColor(Color.BLACK);
                g.drawString(parcel.getName(), x + 10, y + 10);
            }

            // Draw the current step of the best route
            g.setColor(Color.RED);
            if (currentRoute != null) {
                for (int i = 0; i < currentRoute.size() - 1; i++) {
                    int index1 = currentRoute.get(i);
                    int index2 = currentRoute.get(i + 1);
                    if (index1 < parcels.size() && index2 < parcels.size()) {
                        Parcel parcel1 = parcels.get(index1);
                        Parcel parcel2 = parcels.get(index2);
                        int x1 = translateX(parcel1.getX(), maxX);
                        int y1 = translateY(parcel1.getY(), maxY);
                        int x2 = translateX(parcel2.getX(), maxX);
                        int y2 = translateY(parcel2.getY(), maxY);
                        g.drawLine(x1, y1, x2, y2);
                    } else {
                        System.out.println("Invalid index in best route: " + index1 + ", " + index2);
                    }
                }
            }
        }
    }

    // send parcel lists to MRA using CA, loop over parcellist and sendparcels() for each parcellist
    private void sendParcelsToMasterRoutingAgent() {
        // get all parcel lists from the CA
        List<ParcelList> parcelLists = customerAgent.getParcelLists();

        // find the max x and y coordinates among all parcels - determine scaling factor for translating coordinates
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (ParcelList parcelList : parcelLists) {
            for (Parcel parcel : parcelList.getParcels()) {
                maxX = Math.max(maxX, parcel.getX());
                maxY = Math.max(maxY, parcel.getY());
            }
        }

        // hold the results
        StringBuilder custParcelPrintingInTextArea = new StringBuilder();

        // loop over each parcel list and sends it using the sendparcels method in CA class
        for (ParcelList parcelList : parcelLists) {
            // call sendParcels method of CA and get the result
            String parcelsInList = customerAgent.sendParcels(parcelList);

            // add to printing list to be shown in text area
            custParcelPrintingInTextArea.append(parcelsInList).append("\n");
        }

        // print results (parcel info) in the text area (scrollable area)
        resultArea.setText(custParcelPrintingInTextArea.toString());

        // redraw the map panel
        mapPanel.repaint();
    }

    // translate x coordinate to fit within the panel width
    private int translateX(int x, int maxX) {
        // adjust x coordinate based on the panel size and the maximum x value
        return (int) ((double) x / maxX * (mapPanel.getWidth() - 1));
    }

    // translate y coordinate to fit within the panel height
    private int translateY(int y, int maxY) {
        // adjust y coordinate based on the panel size and the maximum y value
        return (int) ((double) y / maxY * (mapPanel.getHeight() - 1));
    }

    // set the DA's to the middle, following the coordinates of the middle of the map panel
    public void setPosition() {
        // set the default position for the delivery agents (middle of the map panel)
        deliveryAgent1.setPosition(mapPanel.getWidth() / 2, mapPanel.getHeight() / 2);
    }

    private static List<String> bestRoutes;

    public static void setBestRoutes(List<String> newBestRoutes) {
        bestRoutes = newBestRoutes;
    }

    private void displayBestRoutes() {
        resultArea.setText(""); // Clear the text area
        if (bestRoutes.isEmpty()) {
            resultArea.append("No best routes received yet.\n");
        } else {
            resultArea.append("Best Routes:\n");
            for (String route : bestRoutes) {
                resultArea.append(route + "\n");
            }
        }
    }

    public static VRPGui getInstance() {
        if (instance == null) {
            CustomerAgent customerAgent = new CustomerAgent();
            DeliveryAgent1 deliveryAgent = new DeliveryAgent1();
            MasterRoutingAgent masterRoutingAgent = new MasterRoutingAgent();
            instance = new VRPGui(customerAgent, deliveryAgent, masterRoutingAgent);
        }
        return instance;
    }
}
