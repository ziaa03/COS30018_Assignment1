package DVRS;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

//The GUI for the Delivery Vehicle Routing System
public class VRPGui extends JFrame {
	// GUI components declaration
    private JButton startGeneticAlgorithmButton;
    private JButton startProcess;
    private JButton showBestRoute;
    private JButton showParcelsButton;
    public static JTextArea resultArea;
    private JPanel mapPanel;
    private JPanel mainPanel;
    private CustomerAgent customerAgent;
    
    // Constructor
    public VRPGui(CustomerAgent customerAgent) {
    	// Initialize GUI components
        startGeneticAlgorithmButton = new JButton("Start Delivery Vehicle Routing System");
        showBestRoute = new JButton("Show Best Routes");
        showBestRoute.setEnabled(false);
        resultArea = new JTextArea(25, 50);
        resultArea.setEditable(false);
        showParcelsButton = new JButton("Show Parcels");
        startProcess = new JButton("Start the Process");
        this.customerAgent = customerAgent;
        List<Integer> bestRoute = new ArrayList<>();
        
        // Create map panel to display parcels
        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawParcels(g, bestRoute); // Call method to draw parcels on the map
            }
        };
        
        // Create main panel to contain GUI components
        mainPanel = new JPanel(new BorderLayout());
        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startPanel.add(startGeneticAlgorithmButton); // Add start button to start panel
        add(startPanel, BorderLayout.CENTER); // Add start panel to the center of the frame
        
        // Set up GUI components
        setupMainGuiComponents(); // Call method to set up main GUI components
        
        // Action listener for start button
        startGeneticAlgorithmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remove(startPanel); // Remove start panel
                add(mainPanel, BorderLayout.CENTER); // Add main panel to the center
                revalidate(); // Revalidate the frame
                repaint(); // Repaint the frame
            }
        });
        
        // Set frame properties
        setPreferredSize(new Dimension(1500, 650));
        setTitle("Delivery Vehicle Routing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setupMapPanelMouseListener(); // Call method to set up mouse listener for map panel
    }
    
    // Method to set up the main GUI components
    private void setupMainGuiComponents() {
    	// Creating a container panel for the map panel
        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.add(mapPanel, BorderLayout.CENTER); // Adding the map panel to the center of the container panel
        
        // Creating a label panel for the title of the GUI
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Delivery Vehicle Routing System GUI");
        labelPanel.add(label); // Adding the title label to the label panel
        mapContainer.add(labelPanel, BorderLayout.NORTH); // Adding the label panel to the north of the container panel
        
        // Creating a button panel for buttons related to the map
        JPanel buttonPanelMap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanelMap.add(showParcelsButton); // Adding the "Show Parcels" button to the map button panel
        buttonPanelMap.add(showBestRoute); // Adding the "Show Best Routes" button to the map button panel
        mapContainer.add(buttonPanelMap, BorderLayout.SOUTH); // Adding the map button panel to the south of the container panel

        // Adding the map container panel to the main panel's center
        mainPanel.add(mapContainer, BorderLayout.CENTER);
        
        // Creating a container panel for the result area
        JPanel resultContainer = new JPanel(new BorderLayout());
        resultContainer.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        
        // Creating a label panel for spacing above the result area
        JPanel labelPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label2 = new JLabel(" ");
        labelPanel2.add(label2); // Adding an empty label to the label panel
        resultContainer.add(labelPanel2, BorderLayout.NORTH); // Adding the label panel to the north of the result container
        
        // Creating a button panel for buttons related to the result area
        JPanel buttonPanelResult = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanelResult.add(showBestRoute); // Adding the "Show Best Routes" button to the result button panel
        buttonPanelResult.add(startProcess); // Adding the "Start Process" button to the result button panel
        resultContainer.add(buttonPanelResult, BorderLayout.SOUTH); // Adding the result button panel to the south of the result container
        
        // Adding the result container panel to the main panel's east
        mainPanel.add(resultContainer, BorderLayout.EAST);
        
        // Creating an instruction panel to display instructions for using the GUI
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
        instructionArea.setEditable(false); // Setting the instruction area as non-editable
        instructionPanel.add(instructionArea); // Adding the instruction area to the instruction panel
        add(instructionPanel, BorderLayout.NORTH); // Adding the instruction panel to the north of the frame
        
        // Setting preferred sizes for the result area and map panel
        resultArea.setPreferredSize(new Dimension(250, 500));
        mapPanel.setPreferredSize(new Dimension(500, 500));
        
        // Action listeners for the buttons
        showParcelsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayParcelList(); // Call method to display the list of parcels
            }
        });

        showBestRoute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayBestRoutes(); // Call method to display the best routes
                showBestRoute.setEnabled(true); // Enable the "Show Best Routes" button
                mapPanel.repaint(); // Repaint the map panel
            }
        });

        startProcess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customerAgent.sendParcelsToMasterRoutingAgent(); // Send parcels to the master routing agent
                showBestRoute.setEnabled(true); // Enable the "Show Best Routes" button
            }
        });
    }
    
    // Method to set up a mouse listener for the map panel, allowing users to add parcels by clicking on the map
    private void setupMapPanelMouseListener() {
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Prompt the user to input parcel details
                String parcelName = JOptionPane.showInputDialog("Enter parcel name:");
                String weightInput = JOptionPane.showInputDialog("Enter parcel weight:");
                String xInput = JOptionPane.showInputDialog("Enter x coordinate (0-12):");
                String yInput = JOptionPane.showInputDialog("Enter y coordinate (0-12):");
                try {
                	// Parse the input coordinates
                    int x = Integer.parseInt(xInput);
                    int y = Integer.parseInt(yInput);
                    // Validate the coordinates
                    if (x < 0 || x > 12 || y < 0 || y > 12) {
                        JOptionPane.showMessageDialog(VRPGui.this, "Coordinates must be between 0 and 12.");
                        return;
                    }
                    // Parse the parcel weight
                    int weight = Integer.parseInt(weightInput);
                    
                    // Determine the region based on the coordinates
                    String region = determineRegion(x, y);
                 
                    // Create a new parcel object
                    Parcel newParcel = new Parcel(parcelName, weight, x, y);
                    
                    // Add the parcel to the customer agent's list
                    customerAgent.addParcel(newParcel, region);
                    mapPanel.repaint(); // Repaint the map panel to reflect changes
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(VRPGui.this, "Invalid input. Please enter valid integers.");
                }
            }
        });
    }
    
    // Method to determine the region based on the given x and y coordinates
    private String determineRegion(int x, int y) {
        if (x <= 5 && y <= 5) {
            return "Region A"; // Region A: (0,0) to (5,5)
        } else if (x > 5 && y <= 5) {
            return "Region B"; // Region B: (6,0) to (12,5)
        } else if (x <= 5 && y > 5) {
            return "Region C"; // Region C: (0,6) to (5,12)
        } else {
            return "Region D"; // Region D: (6,6) to (12,12)
        }
    }
    
    // Method to draw parcels on the map panel
    private void drawParcels(Graphics g, List<Integer> currentRoute) {
    	// Clear the map panel
        g.clearRect(0, 0, mapPanel.getWidth(), mapPanel.getHeight());
        
        // Draw the outline of the map panel
        g.drawRect(0, 0, mapPanel.getWidth() - 1, mapPanel.getHeight() - 1);

        // Set up variables for drawing regions and labels
        Color separatorLineColor = Color.BLACK;
        Font regionLabelFont = new Font("Arial", Font.BOLD, 32);
        g.setFont(regionLabelFont);
        List<ParcelList> parcelLists = customerAgent.getParcelLists();

        // Define maximum x and y coordinates
        int maxX = 12;
        int maxY = 12;

        // Calculate separator line coordinates
        int separatorX = mapPanel.getWidth() / 2;
        int separatorY = mapPanel.getHeight() / 2;
        g.setColor(separatorLineColor);
        g.drawLine(separatorX, 0, separatorX, mapPanel.getHeight() - 1);
        g.drawLine(0, separatorY, mapPanel.getWidth() - 1, separatorY);

        // Set up graphics for rendering
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw region labels with transparency
        String[] regionLabels = {"Region A", "Region B", "Region C", "Region D"};
        int[][] labelPositions = {
                {separatorX / 2, separatorY / 2},
                {separatorX + separatorX / 2, separatorY / 2},
                {separatorX / 2, separatorY + separatorY / 2},
                {separatorX + separatorX / 2, separatorY + separatorY / 2}
        };

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
        g2d.setColor(Color.BLACK);

        // Draw region labels
        for (int i = 0; i < regionLabels.length; i++) {
            String label = regionLabels[i];
            int x = labelPositions[i][0];
            int y = labelPositions[i][1];
            FontMetrics fm = g.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getHeight();
            g2d.drawString(label, x - labelWidth / 2, y + labelHeight / 4);
        }
        
        // Reset graphics transparency
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // Iterate through parcel lists and draw parcels
        for (ParcelList parcelList : parcelLists) {
            List<Parcel> parcels = parcelList.getParcels();

            for (Parcel parcel : parcels) {
            	// Determine the region of the parcel
                int region = (parcel.getX() > maxX / 2 ? 1 : 0) + (parcel.getY() > maxY / 2 ? 2 : 0);
                int regionWidth = (region % 2 == 0) ? separatorX : mapPanel.getWidth() - separatorX;
                int regionHeight = (region < 2) ? separatorY : mapPanel.getHeight() - separatorY;
                int regionX = (region % 2 == 0) ? 0 : separatorX;
                int regionY = (region < 2) ? 0 : separatorY;

                // Calculate parcel position within the region
                int x = regionX + (int) ((double) (parcel.getX() - (region % 2) * maxX / 2) / (maxX / 2) * regionWidth);
                int y = regionY + (int) ((double) (parcel.getY() - (region / 2) * maxY / 2) / (maxY / 2) * regionHeight);
                
                // Draw the parcel
                g.setColor(Color.BLUE);
                g.fillRect(x, y, 6, 6); // Draw a blue rectangle to represent the parcel
                Font parcelLabelFont = new Font("Arial", Font.PLAIN, 12);
                g.setFont(parcelLabelFont);
                g.setColor(Color.BLACK);
                g.drawString(parcel.getName(), x + 10, y + 10); // Draw the name of the parcel near its position
            }
            // Draw the best routes on the map
            drawBestRoutes(g); // Call the method to draw the best routes on the map
        }
    }

    // Method to draw the best routes on the map panel
    private void drawBestRoutes(Graphics g) {
    	// Retrieve the current route information
        String[] routes = resultArea.getText().split("\n");

        // Define regions and maximum coordinates
        String[] regions = {"Region A", "Region B", "Region C", "Region D"};
        int maxX = 12;
        int maxY = 12;
        
        // Calculate the center coordinates of the map panel
        int centerX = mapPanel.getWidth() / 2;
        int centerY = mapPanel.getHeight() / 2;

        // Iterate through regions
        for (String region : regions) {
        	// Iterate through route lines
            for (String routeLine : routes) {
            	// Check if the route line corresponds to the current region
                if (routeLine.startsWith("Best route in " + region)) {
                	// Extract route information from the line
                    String[] routeStrings = routeLine.split(":")[1].replaceAll("[\\[\\]]", "").split(",");
                    List<Integer> route = new ArrayList<>();
                    for (String s : routeStrings) {
                        route.add(Integer.parseInt(s.trim()));
                    }
                    // Iterate through parcels in the route
                    for (int i = 1; i < route.size() - 1; i++) {
                        int currentIndex = route.get(i)-1;
                        int nextIndex = route.get(i + 1)-1;
                        // Get current and next parcels
                        Parcel currentParcel = getParcelByIndex(region, currentIndex);
                        Parcel nextParcel = getParcelByIndex(region, nextIndex);
                        // Draw arrow between current and next parcels if they exist
                        if (currentParcel != null && nextParcel != null) {
                            int x1 = translateX(currentParcel.getX(), maxX);
                            int y1 = translateY(currentParcel.getY(), maxY);
                            int x2 = translateX(nextParcel.getX(), maxX);
                            int y2 = translateY(nextParcel.getY(), maxY);
                            drawArrow(g, x1, y1, x2, y2);
                        }
                    }
                    // Draw arrows from center to first and last parcels in the route
                    if (!route.isEmpty()) {
                        Parcel firstParcel = getParcelByIndex(region, route.get(1)-1);
                        if (firstParcel != null) {
                            int x1 = centerX;
                            int y1 = centerY;
                            int x2 = translateX(firstParcel.getX(), maxX);
                            int y2 = translateY(firstParcel.getY(), maxY);
                            drawArrow(g, x1, y1, x2, y2);
                        }

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
    
    // Method to retrieve a parcel from a region based on its index
    private Parcel getParcelByIndex(String region, int index) {
        List<Parcel> parcels = customerAgent.getParcelsInRegion(region);
        return (index >= 0 && index < parcels.size()) ? parcels.get(index) : null;
    }
    
    // Method to draw an arrow between two points
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
    
    // Method to translate x-coordinate to map panel coordinate
    private int translateX(int x, int maxX) {
        return (int) ((double) x / maxX * (mapPanel.getWidth() - 1));
    }
    
    // Method to translate y-coordinate to map panel coordinate
    private int translateY(int y, int maxY) {
        return (int) ((double) y / maxY * (mapPanel.getHeight() - 1));
    }

    private static List<String> bestRoutes;

    public static void setBestRoutes(List<String> newBestRoutes) {
        bestRoutes = newBestRoutes;
    }
    
    // Method to display the best routes in the result area
    private void displayBestRoutes() {
    	// Clear the result area
        resultArea.setText("");
        // Check if there are any best routes
        if (bestRoutes.isEmpty()) {
            resultArea.append("No best routes received yet.\n");
        } else {
        	// Display the best routes
            resultArea.append("Best Routes:\n");
            for (String route : bestRoutes) {
                resultArea.append(route + "\n");
            }
        }
    }
    
    // Method to display the list of parcels in the result area
    public void displayParcelList() {
    	// Retrieve the parcel lists from the customer agent
        List<ParcelList> parcelLists = customerAgent.getParcelLists();
        // Build a string containing parcel information
        StringBuilder parcelListText = new StringBuilder();

        for (ParcelList parcelList : parcelLists) {
            parcelListText.append("Parcel Region: ").append(parcelList.name).append("\n");
            for (Parcel parcel : parcelList.parcels) {
                parcelListText.append("Parcel: ").append(parcel.name).append(", Weight: ").append(parcel.weight)
                        .append(", Location: (").append(parcel.x).append(", ").append(parcel.y).append(")\n");
            }
            parcelListText.append("\n"); 
        }
        Utilities.printToGui(parcelListText.toString());
    }
}
