package DVRS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class VRPGui extends JFrame {
    private JButton solveButton;
    private JTextArea resultArea;
    private JPanel mapPanel;
    private static VRPGui instance;
    private CustomerAgent customerAgent;
    private DeliveryAgent1 deliveryAgent1;
    private MasterRoutingAgent masterRoutingAgent;

    public VRPGui(CustomerAgent customerAgent)
    {
        // Initialize components
        solveButton = new JButton("Start VRP");
        resultArea = new JTextArea(20, 50);
        resultArea.setEditable(false);

        // Initialize CustomerAgent
        this.customerAgent = customerAgent;
        this.customerAgent.setup();

        // Initialize map panel
        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawParcels(g);
            }
        };
        mapPanel.setPreferredSize(new Dimension(400, 400));
        add(mapPanel);

        // Layout
        setLayout(new FlowLayout());
        add(solveButton);
        add(new JScrollPane(resultArea));

        // Add action listener to button
        solveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                sendParcelsToMasterRoutingAgent();
            }
        });

        // Add a label
        JLabel label = new JLabel("Delivery Vehicle Routing System GUI");
        add(label, BorderLayout.SOUTH);

        // Set up the window
        setTitle("Delivery Vehicle Routing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    private void initializeCustomerAgent()
    {
        customerAgent = new CustomerAgent();
        customerAgent.setup();
    }

    public void setCustomerAgent(CustomerAgent customerAgent)
    {
        this.customerAgent = customerAgent;
    }

    public void setDeliveryAgent(DeliveryAgent1 deliveryAgent1)
    {
        this.deliveryAgent1 = deliveryAgent1;
    }

    public void setMasterRoutingAgent(MasterRoutingAgent masterRoutingAgent)
    {
        this.masterRoutingAgent = masterRoutingAgent;
    }

    private void drawParcels(Graphics g)
    {
        // Clear the map panel
        g.clearRect(0, 0, mapPanel.getWidth(), mapPanel.getHeight());

        // Draw a border around the map panel
        g.drawRect(0, 0, mapPanel.getWidth() - 1, mapPanel.getHeight() - 1);

        // Define the colors for the region labels and separator lines
        Color regionLabelColor = Color.BLACK;
        Color separatorLineColor = Color.BLACK;

        // Define the font for the region labels
        Font regionLabelFont = new Font("Arial", Font.BOLD, 12);
        g.setFont(regionLabelFont);

        // Define the space for the region labels
        int labelHeight = 20;
        int labelWidth = 60;

        // Get the parcel lists from the CustomerAgent
        List<ParcelList> parcelLists = customerAgent.getParcelLists();

        // Calculate the maximum x and y coordinates of all parcels
        int maxX = 12;
        int maxY = 12;

        // Draw horizontal and vertical separator lines at the middle of the map panel
        int separatorX = mapPanel.getWidth() / 2;
        int separatorY = mapPanel.getHeight() / 2;
        g.setColor(separatorLineColor);
        g.drawLine(separatorX, 0, separatorX, mapPanel.getHeight() - 1);
        g.drawLine(0, separatorY, mapPanel.getWidth() - 1, separatorY);

        // Draw labels for the regions
        g.setColor(regionLabelColor);
        g.drawString("Region A", 10, separatorY - 10);
        g.drawString("Region B", separatorX + 10, separatorY - 10);
        g.drawString("Region C", 10, mapPanel.getHeight() - 10);
        g.drawString("Region D", separatorX + 10, mapPanel.getHeight() - 10);

        // Loop through each ParcelList
        for (ParcelList parcelList : parcelLists) {
            // Loop through each Parcel in the ParcelList
            for (Parcel parcel : parcelList.getParcels()) {
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
        }
    }

    // Method to send parcel lists to MasterRoutingAgent using CustomerAgent, loop over parcellist and sendparcels() for each parcellist
    private void sendParcelsToMasterRoutingAgent()
    {
        // Get all ParcelLists from customerAgent
        List<ParcelList> parcelLists = customerAgent.getParcelLists();

        // Find the maximum x and y coordinates among all parcels
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (ParcelList parcelList : parcelLists) {
            for (Parcel parcel : parcelList.getParcels()) {
                maxX = Math.max(maxX, parcel.getX());
                maxY = Math.max(maxY, parcel.getY());
            }
        }

        // Initialize a StringBuilder to hold the results
        StringBuilder results = new StringBuilder();

        // Loop over each ParcelList and send it
        for (ParcelList parcelList : parcelLists)
        {
            // Call sendParcels method of CustomerAgent and get the result
            String result = customerAgent.sendParcels(parcelList);

            // Append the result to the results StringBuilder
            results.append(result).append("\n");
        }

        // Display the results in the resultArea
        resultArea.setText(results.toString());

        // Redraw the map panel
        mapPanel.repaint();
    }

    // Method to translate x coordinate to fit within the panel width
    private int translateX(int x, int maxX) {
        // Adjust x coordinate based on the panel size and the maximum x value
        return (int) ((double) x / maxX * (mapPanel.getWidth() - 1));
    }

    // Method to translate y coordinate to fit within the panel height
    private int translateY(int y, int maxY) {
        // Adjust y coordinate based on the panel size and the maximum y value
        return (int) ((double) y / maxY * (mapPanel.getHeight() - 1));
    }

    public static VRPGui getInstance()
    {
        if (instance == null)
        {
            CustomerAgent customerAgent = new CustomerAgent();
            instance = new VRPGui(customerAgent);
        }
        return instance;
    }
}
