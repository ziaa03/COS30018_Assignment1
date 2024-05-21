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

    public VRPGui(CustomerAgent customerAgent) {
        startGeneticAlgorithmButton = new JButton("Start Genetic Algorithm");
        showBestRoute = new JButton("Show Best Routes");
        showBestRoute.setEnabled(false);
        resultArea = new JTextArea(25, 50);
        resultArea.setEditable(false);

        showParcelsButton = new JButton("Show Parcels");
        startProcess = new JButton("Start the Process");

        this.customerAgent = customerAgent;

        List<Integer> bestRoute = new ArrayList<>();
        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawParcels(g, bestRoute);
            }
        };

        mainPanel = new JPanel(new BorderLayout());
        JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        startPanel.add(startGeneticAlgorithmButton);
        add(startPanel, BorderLayout.CENTER);

        setupMainGuiComponents();

        startGeneticAlgorithmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remove(startPanel);
                add(mainPanel, BorderLayout.CENTER);
                revalidate();
                repaint();
            }
        });

        setPreferredSize(new Dimension(1500, 650));

        setTitle("Delivery Vehicle Routing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        setupMapPanelMouseListener();
    }

    private void setupMainGuiComponents() {
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
        instructionArea.setEditable(false);
        instructionPanel.add(instructionArea);
        add(instructionPanel, BorderLayout.NORTH);

        resultArea.setPreferredSize(new Dimension(250, 500));
        mapPanel.setPreferredSize(new Dimension(500, 500));

        showParcelsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendParcelsToMasterRoutingAgent();
            }
        });

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

    private void setupMapPanelMouseListener() {
        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String parcelName = JOptionPane.showInputDialog("Enter parcel name:");
                String weightInput = JOptionPane.showInputDialog("Enter parcel weight:");
                String xInput = JOptionPane.showInputDialog("Enter x coordinate (0-12):");
                String yInput = JOptionPane.showInputDialog("Enter y coordinate (0-12):");
                try {
                    int x = Integer.parseInt(xInput);
                    int y = Integer.parseInt(yInput);
                    if (x < 0 || x > 12 || y < 0 || y > 12) {
                        JOptionPane.showMessageDialog(VRPGui.this, "Coordinates must be between 0 and 12.");
                        return;
                    }
                    int weight = Integer.parseInt(weightInput);
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

        Color separatorLineColor = Color.BLACK;
        Font regionLabelFont = new Font("Arial", Font.BOLD, 32);
        g.setFont(regionLabelFont);
        List<ParcelList> parcelLists = customerAgent.getParcelLists();

        int maxX = 12;
        int maxY = 12;

        int separatorX = mapPanel.getWidth() / 2;
        int separatorY = mapPanel.getHeight() / 2;
        g.setColor(separatorLineColor);
        g.drawLine(separatorX, 0, separatorX, mapPanel.getHeight() - 1);
        g.drawLine(0, separatorY, mapPanel.getWidth() - 1, separatorY);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String[] regionLabels = {"Region A", "Region B", "Region C", "Region D"};
        int[][] labelPositions = {
                {separatorX / 2, separatorY / 2},
                {separatorX + separatorX / 2, separatorY / 2},
                {separatorX / 2, separatorY + separatorY / 2},
                {separatorX + separatorX / 2, separatorY + separatorY / 2}
        };

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

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

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
            drawBestRoutes(g);
        }
    }

    private void drawBestRoutes(Graphics g) {
        String[] routes = resultArea.getText().split("\n");

        String[] regions = {"Region A", "Region B", "Region C", "Region D"};
        int maxX = 12;
        int maxY = 12;

        int centerX = mapPanel.getWidth() / 2;
        int centerY = mapPanel.getHeight() / 2;

        for (String region : regions) {
            for (String routeLine : routes) {
                if (routeLine.startsWith("Best route in " + region)) {
                    String[] routeStrings = routeLine.split(":")[1].replaceAll("[\\[\\]]", "").split(",");
                    List<Integer> route = new ArrayList<>();
                    for (String s : routeStrings) {
                        route.add(Integer.parseInt(s.trim()));
                    }
                    int[] abc = {1,3,2,4};
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

    private Parcel getParcelByIndex(String region, int index) {
        List<Parcel> parcels = customerAgent.getParcelsInRegion(region);
        return (index >= 0 && index < parcels.size()) ? parcels.get(index) : null;
    }

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

    private void sendParcelsToMasterRoutingAgent() {
        List<ParcelList> parcelLists = customerAgent.getParcelLists();

        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (ParcelList parcelList : parcelLists) {
            for (Parcel parcel : parcelList.getParcels()) {
                maxX = Math.max(maxX, parcel.getX());
                maxY = Math.max(maxY, parcel.getY());
            }
        }

        StringBuilder custParcelPrintingInTextArea = new StringBuilder();

        for (ParcelList parcelList : parcelLists) {
            String parcelsInList = customerAgent.sendParcels(parcelList);
            custParcelPrintingInTextArea.append(parcelsInList).append("\n");
        }
        resultArea.setText(custParcelPrintingInTextArea.toString());
        mapPanel.repaint();
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
