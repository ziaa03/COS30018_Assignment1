package DVRS;

import java.awt.Color;

public class Mover implements Runnable {
    private RouteUI ui;
    private RandomMarker marker;
    private final int startX = 24, startY = 24;  // Starting position should not change

    public Mover(RouteUI ui, RandomMarker marker) {
        this.ui = ui;
        this.marker = marker;
    }

    @Override
    public void run() {
        int currentX = startX;  // Current position starts at the initial position
        int currentY = startY;
        while (true) {
            int targetX = marker.getCurrentX();
            int targetY = marker.getCurrentY();
            moveXAxisFirst(currentX, currentY, targetX, targetY); // Move x-axis first
            ui.updateGrid(targetX, targetY, Color.RED);
            try {
                Thread.sleep(1000); // Pause for visualization at the target
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            moveXAxisFirst(targetX, targetY, startX, startY); // Return using x-axis first
            try {
                Thread.sleep(1000); // Pause for visualization at the target
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            //marker.NewLocation(); // Move to a new location after return
            currentX = startX;  // Reset the current position to start position
            currentY = startY;
        }
    }

    private void moveXAxisFirst(int fromX, int fromY, int toX, int toY) {
        // Move along the x-axis first
        int dx = Integer.compare(toX, fromX);
        int dy = Integer.compare(toY, fromY);
        int px = fromX;
        int py = fromY;

        // Move horizontally to the target x coordinate
        while (px != toX) {
            try {
                Thread.sleep(50); // Slow down the movement for visualization
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ui.updateGrid(px, py, Color.WHITE); // Clear old path
            px += dx;
            ui.updateGrid(px, py, Color.GREEN); // Color the new path
        }

        // Move vertically to the target y coordinate
        while (py != toY) {
            try {
                Thread.sleep(50); // Slow down the movement for visualization
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ui.updateGrid(px, py, Color.WHITE); // Clear old path
            py += dy;
            ui.updateGrid(px, py, Color.GREEN); // Color the new path
        }
    }
}
