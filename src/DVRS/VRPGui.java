package DVRS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VRPGui extends JFrame {
    private JButton solveButton;
    private JTextArea resultArea;

    public VRPGui() {
        // Initialize components
        solveButton = new JButton("Solve VRP");
        resultArea = new JTextArea(20, 50);
        resultArea.setEditable(false);

        // Layout
        setLayout(new FlowLayout());
        add(solveButton);
        add(new JScrollPane(resultArea));

        // Add action listener to button
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call your VRP solver here and output the result
                resultArea.setText("Solving VRP...");
                // ...
            }
        });

        // Add a label
        JLabel label = new JLabel("Delivery Vehicle Routing System GUI");
        add(label);

        // Set up the window
        setTitle("Delivery Vehicle Routing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new VRPGui();
            }
        });
    }
}
