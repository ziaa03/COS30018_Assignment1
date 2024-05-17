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
    private JButton startProcess;
    private JButton showBestRoute;
    private JButton solveButton;
    private JButton showParcelsButton; // new button
    private JTextArea resultArea;
    private JPanel mapPanel;
    private static VRPGui instance;
    private CustomerAgent customerAgent;
    private DeliveryAgent1 deliveryAgent1;
    private MasterRoutingAgent masterRoutingAgent;

    public VRPGui(CustomerAgent customerAgent)
    {
        startProcess = new JButton("Start Routing Process");
        showParcelsButton = new JButton("Show All Parcels");
        showBestRoute = new JButton("Show Best Routes");

        resultArea = new JTextArea(25, 50);
        resultArea.setEditable(false);

        this.customerAgent = customerAgent;

        List<Integer> bestRoute = new ArrayList<>();
        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawParcels(g, bestRoute);
            }
        };

        setLayout(new BorderLayout());

        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.add(mapPanel, BorderLayout.CENTER);

        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Delivery Vehicle Routing System GUI");
        labelPanel.add(label);
        mapContainer.add(labelPanel, BorderLayout.NORTH);

        // Create the top panel and add it to the frame
        JPanel topPanel = new JPanel();
        JLabel topLabel = new JLabel("Region A  |  Region B");
        topPanel.add(topLabel);
        mapContainer.add(topPanel, BorderLayout.NORTH);

        // Create the bottom panel and add it to the frame
        JPanel bottomPanel = new JPanel();
        JLabel bottomLabel = new JLabel("Region D  |  Region C");
        bottomPanel.add(bottomLabel);
        mapContainer.add(bottomPanel, BorderLayout.SOUTH);

        JPanel buttonPanelMap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanelMap.add(startProcess);
        mapContainer.add(buttonPanelMap, BorderLayout.SOUTH);

        add(mapContainer, BorderLayout.CENTER);

        JPanel resultContainer = new JPanel(new BorderLayout());
        resultContainer.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        JPanel labelPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label2 = new JLabel(" ");
        labelPanel2.add(label2);
        resultContainer.add(labelPanel2, BorderLayout.NORTH);

        JPanel buttonPanelResult = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanelResult.add(showParcelsButton);
        buttonPanelResult.add(showBestRoute);
        resultContainer.add(buttonPanelResult, BorderLayout.SOUTH);

        add(resultContainer, BorderLayout.EAST);

        resultArea.setPreferredSize(new Dimension(250, 500));
        mapPanel.setPreferredSize(new Dimension(500, 500));

        showParcelsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayParcels();
            }
        });

        showBestRoute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayBestRoutes();
            }
        });

        startProcess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                customerAgent.sendParcelsToMasterRoutingAgent();
            }
        });

        setTitle("Delivery Vehicle Routing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        setupMapPanelMouseListener();

    }

    private void setupMapPanelMouseListener() {
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String parcelName = JOptionPane.showInputDialog("Enter parcel name:");
                String weightInput = JOptionPane.showInputDialog("Enter parcel weight:");
                String xInput = JOptionPane.showInputDialog("Enter x coordinate:");
                String yInput = JOptionPane.showInputDialog("Enter y coordinate:");

                try {
                    int weight = Integer.parseInt(weightInput);
                    int x = Integer.parseInt(xInput);
                    int y = Integer.parseInt(yInput);
                    String region = determineRegion(x, y);
                    Parcel newParcel = new Parcel(parcelName, weight, x, y);
                    customerAgent.addParcel(newParcel, region);
                    mapPanel.repaint();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(VRPGui.this, "Invalid input. Please enter valid integers.");
                }
            }
        });
    }

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

        g.clearRect(0, 0, mapPanel.getWidth(), mapPanel.getHeight());
        g.drawRect(0, 0, mapPanel.getWidth() - 1, mapPanel.getHeight() - 1);


        Color regionLabelColor = Color.BLACK;
        Color separatorLineColor = Color.BLACK;

        Font regionLabelFont = new Font("Arial", Font.BOLD, 12);
        g.setFont(regionLabelFont);

        List<ParcelList> parcelLists = customerAgent.getParcelLists();

        int maxX = 12;
        int maxY = 12;

        int separatorX = mapPanel.getWidth() / 2;
        int separatorY = mapPanel.getHeight() / 2;
        g.setColor(separatorLineColor);
        g.drawLine(separatorX, 0, separatorX, mapPanel.getHeight() - 1);
        g.drawLine(0, separatorY, mapPanel.getWidth() - 1, separatorY);

//        g.setColor(regionLabelColor);
//        g.drawString("Region A", 10, separatorY - 10);
//        g.drawString("Region B", separatorX + 10, separatorY - 10);
//        g.drawString("Region D", 10, mapPanel.getHeight() - 10);
//        g.drawString("Region C", separatorX + 10, mapPanel.getHeight() - 10);

        for (ParcelList parcelList : parcelLists) {

            List<Parcel> parcels = parcelList.getParcels();

            for (Parcel parcel : parcels) {
                int region = (parcel.getX() > maxX / 2 ? 1 : 0) + (parcel.getY() > maxY / 2 ? 2 : 0);
                int regionWidth = (region % 2 == 0) ? separatorX : mapPanel.getWidth() - separatorX;
                int regionHeight = (region < 2) ? separatorY : mapPanel.getHeight() - separatorY;
                int regionX = (region % 2 == 0) ? 0 : separatorX;
                int regionY = (region < 2) ? 0 : separatorY;
                int x = regionX + (int) ((double) (parcel.getX() - (region % 2) * maxX / 2) / (maxX / 2) * regionWidth);
                int y = regionY + (int) ((double) (parcel.getY() - (region / 2) * maxY / 2) / (maxY / 2) * regionHeight);

                g.setColor(Color.BLUE);
                g.fillRect(x, y, 6, 6);

                Font parcelLabelFont = new Font("Arial", Font.PLAIN, 12);

                g.setFont(parcelLabelFont);

                g.setColor(Color.BLACK);
                g.drawString(parcel.getName(), x + 10, y + 10);
            }

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

    public void displayParcels() {
        List<ParcelList> parcelLists = customerAgent.getParcelLists();
        StringBuilder parcelListText = new StringBuilder();

        for (ParcelList parcelList : parcelLists)
        {
            parcelListText.append("Parcel Region: ").append(parcelList.name).append("\n");
            for (Parcel parcel : parcelList.parcels)
            {
                parcelListText.append("Parcel: ").append(parcel.name).append(", Weight: ").append(parcel.weight)
                        .append(", Location: (").append(parcel.x).append(", ").append(parcel.y).append(")\n");
            }
            parcelListText.append("\n");
        }
        resultArea.setText(parcelListText.toString());
    }

    private int translateX(int x, int maxX) {
        return (int) ((double) x / maxX * (mapPanel.getWidth() - 1));
    }

    private int translateY(int y, int maxY) {
        return (int) ((double) y / maxY * (mapPanel.getHeight() - 1));
    }

    private static List<String> bestRoutes;

    public static void setBestRoutes(List<String> newBestRoutes) {
        bestRoutes = newBestRoutes;
    }

    private void displayBestRoutes() {
        resultArea.setText("");
        if (bestRoutes.isEmpty()) {
            resultArea.append("No best routes received yet.\n");
        } else {
            resultArea.append("Best Routes:\n");
            for (String route : bestRoutes) {
                resultArea.append(route + "\n");
            }
        }
    }
}
