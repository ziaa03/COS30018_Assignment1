package DVRS;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

public class VRPGui extends JFrame {
    private JButton startGeneticAlgorithmButton;
    private JButton startProcess;
    private JButton showBestRoute;
    private JButton showParcelsButton; // new button
    private JTextArea resultArea;
    private JPanel mapPanel;
    private JPanel mainPanel;
    private static VRPGui instance;
    private CustomerAgent customerAgent;
    private DeliveryAgent1 deliveryAgent1;
    private MasterRoutingAgent masterRoutingAgent;
    private int currentParcelX;
    private int currentParcelY;
    private int currentParcelCapacity;

    public VRPGui(CustomerAgent customerAgent) {
        // Initialise components
        startGeneticAlgorithmButton = new JButton("Start Genetic Algorithm");
        showBestRoute = new JButton("Show Best Routes");
        showBestRoute.setEnabled(false);
        resultArea = new JTextArea(25, 50);
        resultArea.setEditable(false);

        // New button for showing parcels
        showParcelsButton = new JButton("Show Parcels");

        startProcess = new JButton("Start the Process");

        // Initialise agents
        this.customerAgent = customerAgent;

        // Map panel - for parcel points
        List<Integer> bestRoute = new ArrayList<>(); // Empty best route list
        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); // Overrides paint component to draw parcels on it using drawParcels method
                drawParcels(g, bestRoute);
            }
        };

        mainPanel = new JPanel(new BorderLayout());

        // Add the start button to the main frame
        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startPanel.add(startGeneticAlgorithmButton);
        add(startPanel, BorderLayout.CENTER);

        // Set up the main GUI components, but don't add them to the frame yet
        setupMainGuiComponents();

        // Action listener for the start button
        startGeneticAlgorithmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Remove the start button panel
                remove(startPanel);
                // Add the main GUI panel
                add(mainPanel, BorderLayout.CENTER);
                // Refresh the frame to display the new content
                revalidate();
                repaint();
            }
        });

        // Set consistent window size
        setPreferredSize(new Dimension(1500, 650));

        // Window setup
        setTitle("Delivery Vehicle Routing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        // Setup mouse listener for parcel placement
        setupMapPanelMouseListener();
    }

    private void setupMainGuiComponents() {
        // Panel for map and label
        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.add(mapPanel, BorderLayout.CENTER);

        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Delivery Vehicle Routing System GUI");
        labelPanel.add(label);
        mapContainer.add(labelPanel, BorderLayout.NORTH);

        JPanel buttonPanelMap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanelMap.add(showParcelsButton);
        buttonPanelMap.add(showBestRoute);

        mapContainer.add(buttonPanelMap, BorderLayout.SOUTH);

        mainPanel.add(mapContainer, BorderLayout.CENTER);

        // Panel for result area and Begin Routing button
        JPanel resultContainer = new JPanel(new BorderLayout());
        resultContainer.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        JPanel labelPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label2 = new JLabel(" ");
        labelPanel2.add(label2);
        resultContainer.add(labelPanel2, BorderLayout.NORTH);

        JPanel buttonPanelResult = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanelResult.add(showBestRoute);
        buttonPanelResult.add(startProcess);
        resultContainer.add(buttonPanelResult, BorderLayout.SOUTH);

        mainPanel.add(resultContainer, BorderLayout.EAST);

        JPanel instructionPanel = new JPanel();
        JTextArea instructionArea = new JTextArea(
                "INSTRUCTION MANUAL FOR DVRS\n" +
                        "1. Adding Parcels\n" +
                        "Click anywhere in the designated region on the map.\n" +
                        "A prompt will appear asking for the parcel details.\n" + "\n" +

                        "2. Viewing All Parcels\n" +
                        "Click the 'Show All Parcels' button.\n" +
                        "A list of all added parcels will be displayed.\n" + "\n" +

                        "3. Starting the Routing Process\n" +
                        "Once you have added all the parcels, you can initiate the routing process:\n" +
                        "Click the 'Start Routing Process' button.\n" +
                        "The system will begin calculating the optimal routes for delivery agents based on the added parcels.\n" + "\n" +

                        "4. Displaying Best Routes\n" +
                        "Click the 'Show Best Routes' button.\n" +
                        "The system will display the most efficient routes taken by each delivery agent to deliver the parcels."
        );
        instructionArea.setEditable(false); // make the text area read-only
        instructionPanel.add(instructionArea);
        add(instructionPanel, BorderLayout.NORTH);

        // Set preferred sizes for components
        resultArea.setPreferredSize(new Dimension(250, 500));
        mapPanel.setPreferredSize(new Dimension(500, 500));

        // When button is clicked, calls the sendParcelsToMRA - customer sends to MRA their locations
        showParcelsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendParcelsToMasterRoutingAgent();
            }
        });

        // When button is clicked, calls the sendParcelsToMRA - customer sends to MRA their locations
        showBestRoute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayBestRoutes();
                showBestRoute.setEnabled(true);
                mapPanel.repaint();
            }
        });

        startProcess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customerAgent.sendParcelsToMasterRoutingAgent();
                showBestRoute.setEnabled(true);
            }
        });
    }

//    public VRPGui(CustomerAgent customerAgent, DeliveryAgent1 deliveryAgent1, MasterRoutingAgent masterRoutingAgent) {
//        // initialise components
//        solveButton = new JButton("Begin Routing");
//        resultArea = new JTextArea(25, 50);
//        resultArea.setEditable(false);
//
//        // new button for showing parcels
//        showParcelsButton = new JButton("Show Parcels");
//
//        // initialise agents
//        this.customerAgent = customerAgent;
//        this.customerAgent.setup();
//        this.masterRoutingAgent = masterRoutingAgent;
//        this.masterRoutingAgent.setup();
//        this.deliveryAgent1 = deliveryAgent1;
//        this.deliveryAgent1.setup();
//
//        // map panel - for parcel pts
//        List<Integer> bestRoute = new ArrayList<>();   // empty best route list
//        mapPanel = new JPanel() {
//            @Override
//            protected void paintComponent(Graphics g) {
//                super.paintComponent(g);  // overrides paint component to draw parcels on it using drawparcels method
//                drawParcels(g, bestRoute);
//            }
//        };
//
//     // layout
//        setLayout(new BorderLayout());
//
//        // Panel for map and label
//        JPanel mapContainer = new JPanel(new BorderLayout());
//        mapContainer.add(mapPanel, BorderLayout.CENTER);
//
//        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        JLabel label = new JLabel("Delivery Vehicle Routing System GUI");
//        labelPanel.add(label);
//        mapContainer.add(labelPanel, BorderLayout.NORTH);
//
//        JPanel buttonPanelMap = new JPanel(new FlowLayout(FlowLayout.CENTER));
//        buttonPanelMap.add(showParcelsButton);
//        mapContainer.add(buttonPanelMap, BorderLayout.SOUTH);
//
//        add(mapContainer, BorderLayout.CENTER);
//
//
//        // Panel for result area and Begin Routing button
//        JPanel resultContainer = new JPanel(new BorderLayout());
//        resultContainer.add(new JScrollPane(resultArea), BorderLayout.CENTER);
//
//        JPanel labelPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        JLabel label2 = new JLabel(" ");
//        labelPanel2.add(label2);
//        resultContainer.add(labelPanel2, BorderLayout.NORTH);
//
//        JPanel buttonPanelResult = new JPanel(new FlowLayout(FlowLayout.CENTER));
//        buttonPanelResult.add(solveButton);
//        resultContainer.add(buttonPanelResult, BorderLayout.SOUTH);
//
//        add(resultContainer, BorderLayout.EAST);
//
//        // Set preferred sizes for components
//        resultArea.setPreferredSize(new Dimension(250, 500));
//        mapPanel.setPreferredSize(new Dimension(500, 500));
//
//
//
//        // when button is clicked, calls the sendParcelsToMRA - customer send to mra their locations
//        showParcelsButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                sendParcelsToMasterRoutingAgent();
//            }
//        });
//
//        // when button is clicked, calls the sendParcelsToMRA - customer send to mra their locations
//        solveButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                displayBestRoutes();
//            }
//        });
//
//
//
//        // window setup
//        setTitle("Delivery Vehicle Routing System");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        pack();
//
//        setPosition();
//    }

    private void setupMapPanelMouseListener() {
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Prompt the user to enter the parcel details
                String parcelName = JOptionPane.showInputDialog("Enter parcel name:");
                String weightInput = JOptionPane.showInputDialog("Enter parcel weight:");
                String xInput = JOptionPane.showInputDialog("Enter x coordinate (0-12):");
                String yInput = JOptionPane.showInputDialog("Enter y coordinate (0-12):");
                // Validate x and y coordinates
                try {
                    int x = Integer.parseInt(xInput);
                    int y = Integer.parseInt(yInput);
                    if (x < 0 || x > 12 || y < 0 || y > 12) {
                        JOptionPane.showMessageDialog(VRPGui.this, "Coordinates must be between 0 and 12.");
                        return; // Exit the method if coordinates are invalid
                    }
                    // Convert weight input to integer
                    int weight = Integer.parseInt(weightInput);
                    // Determine the region based on the coordinates (assuming you have a method for this)
                    String region = determineRegion(x, y);
                    // Create a new Parcel object with the captured information
                    Parcel newParcel = new Parcel(parcelName, weight, x, y);
                    // Add the new parcel to the CustomerAgent in the specified region
                    customerAgent.addParcel(newParcel, region);
                    // Repaint the map panel to display the new parcel
                    mapPanel.repaint();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(VRPGui.this, "Invalid input. Please enter valid integers.");
                }
            }
        });
    }


    // Method to determine the region based on the coordinates
    private String determineRegion(int x, int y) {
        if (x <= 5 && y <= 5) {
            return "Region A";
        } else if (x > 5 && y <= 5) {
            return "Region B";
        } else if (x <= 5 && y > 5) {
            return "Region C";
        } else {
            return "Region D";
        }
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
        Font regionLabelFont = new Font("Arial", Font.BOLD, 32);
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

        // Enable anti-aliasing for better text rendering
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw labels for the regions in the center of each region
        String[] regionLabels = {"Region A", "Region B", "Region C", "Region D"};
        int[][] labelPositions = {
                {separatorX / 2, separatorY / 2},
                {separatorX + separatorX / 2, separatorY / 2},
                {separatorX / 2, separatorY + separatorY / 2},
                {separatorX + separatorX / 2, separatorY + separatorY / 2}
        };

        // Set font transparent
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
        g2d.setColor(Color.BLACK);

        for (int i = 0; i < regionLabels.length; i++) {
            String label = regionLabels[i];
            int x = labelPositions[i][0];
            int y = labelPositions[i][1];
            FontMetrics fm = g.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getHeight();
            g2d.drawString(label, x - labelWidth / 2, y + labelHeight / 4);
        }

        // Reset transparency for parcels and their labels
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

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
            drawBestRoutes(g);
        }
    }

    private void drawBestRoutes(Graphics g) {
        String[] routes = resultArea.getText().split("\n");

        // Define regions
        String[] regions = {"Region A", "Region B", "Region C", "Region D"};
        int maxX = 12;
        int maxY = 12;

        int centerX = mapPanel.getWidth() / 2;
        int centerY = mapPanel.getHeight() / 2;

        // Loop through each region and draw routes
        for (String region : regions) {
            for (String routeLine : routes) {
                if (routeLine.startsWith("Best route in " + region)) {
                    // Extract route
                    String[] routeStrings = routeLine.split(":")[1].replaceAll("[\\[\\]]", "").split(",");
                    List<Integer> route = new ArrayList<>();
                    for (String s : routeStrings) {
                        route.add(Integer.parseInt(s.trim()));
                    }
                    int[] abc = {1,3,2,4};
                    // Draw arrows for the route starting and ending at the center
                    for (int i = 1; i < route.size() - 1; i++) {
                        int currentIndex = route.get(i)-1;
                        int nextIndex = route.get(i + 1)-1;
                        Parcel currentParcel = getParcelByIndex(region, currentIndex);
                        Parcel nextParcel = getParcelByIndex(region, nextIndex);
                        if (currentParcel != null && nextParcel != null) {
                            int x1 = translateX(currentParcel.getX(), maxX);
                            int y1 = translateY(currentParcel.getY(), maxY);
                            int x2 = translateX(nextParcel.getX(), maxX);
                            int y2 = translateY(nextParcel.getY(), maxY);
                            drawArrow(g, x1, y1, x2, y2);
                        }
                    }

                    // Draw arrow from center to the first parcel
                    if (!route.isEmpty()) {
                        Parcel firstParcel = getParcelByIndex(region, route.get(1)-1);
                        if (firstParcel != null) {
                            int x1 = centerX;
                            int y1 = centerY;
                            int x2 = translateX(firstParcel.getX(), maxX);
                            int y2 = translateY(firstParcel.getY(), maxY);
                            drawArrow(g, x1, y1, x2, y2);
                        }

                        // Draw arrow from the last parcel back to the center
                        Parcel lastParcel = getParcelByIndex(region, route.get(route.size() - 2)-1);
                        if (lastParcel != null) {
                            int x1 = translateX(lastParcel.getX(), maxX);
                            int y1 = translateY(lastParcel.getY(), maxY);
                            int x2 = centerX;
                            int y2 = centerY;
                            drawArrow(g, x1, y1, x2, y2);
                        }
                    }
                }
            }
        }
    }

    private Parcel getParcelByIndex(String region, int index) {
        List<Parcel> parcels = customerAgent.getParcelsInRegion(region);
        return (index >= 0 && index < parcels.size()) ? parcels.get(index) : null;
    }

    // Draw an arrow from (x1, y1) to (x2, y2)
    private void drawArrow(Graphics g, int x1, int y1, int x2, int y2) {
        int arrowSize = 6;
        int mx = (x1 + x2) / 2;
        int my = (y1 + y2) / 2;

        int dx = mx - x1, dy = my - y1;
        double D = Math.sqrt(dx * dx + dy * dy);
        double xm = D - arrowSize, xn = xm, ym = arrowSize, yn = -arrowSize, x;
        double sin = dy / D, cos = dx / D;

        x = xm * cos - ym * sin + x1;
        ym = xm * sin + ym * cos + y1;
        xm = x;

        x = xn * cos - yn * sin + x1;
        yn = xn * sin + yn * cos + y1;
        xn = x;

        int[] xpoints = {mx, (int) xm, (int) xn};
        int[] ypoints = {my, (int) ym, (int) yn};

        g.drawLine(x1, y1, mx, my);
        g.fillPolygon(xpoints, ypoints, 3);
        g.drawLine(mx, my, x2, y2);
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

//    public static VRPGui getInstance() {
//        if (instance == null) {
//            CustomerAgent customerAgent = new CustomerAgent();
//            DeliveryAgent1 deliveryAgent = new DeliveryAgent1();
//            MasterRoutingAgent masterRoutingAgent = new MasterRoutingAgent();
//            instance = new VRPGui(customerAgent, deliveryAgent, masterRoutingAgent);
//        }
//        return instance;
//    }
}
