package DVRS;

import javax.swing.*;
import java.awt.*;

public class RouteUI extends JFrame {
    private JPanel[][] cells;  // Array to store references to cell panels
    private JPanel gridPanel;
    private static final int GRID_SIZE = 11*5;
    public static final int ROW_SIZE = 50;
    public static final int COL_SIZE = 50;

    // Constructor to initialize the JFrame with a grid panel
    public RouteUI() {
        gridPanel = new JPanel();
        gridPanel.setLayout(new GridLayout(GRID_SIZE, GRID_SIZE, 0, 0)); // Set layout and gaps
        cells = new JPanel[GRID_SIZE][GRID_SIZE]; // Initialize the array to store JPanel references

        // Initialize grid cells as individual panels
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                JPanel cell = new JPanel();
                cell.setBorder(BorderFactory.createLineBorder(Color.gray));
                cell.setBackground(Color.white); // Set default cell color
                cells[i][j] = cell;  // Store the cell reference in the array
                gridPanel.add(cell);
            }
        }
        
        // Add the grid panel to the JFrame
        this.add(gridPanel);
        this.setTitle("50x50 Grid Panel");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 800); // Set the size of the JFrame
        this.setLocationRelativeTo(null); // Center the JFrame on the screen
        //this.updateGrid(42, 24, Color.orange);
    }

    // Method to set color of a specific cell
    public void updateGrid(int row, int col, Color color) {
        if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
            cells[row][col].setBackground(color);
        } else {
            System.out.println("Invalid row or column index");
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            RouteUI ui = new RouteUI();
            ui.setVisible(true);
            //ui.updateGrid(24, 24, Color.orange); // Set the color of the cell at (25, 25) to red
        });
    }
}
