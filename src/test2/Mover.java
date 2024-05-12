package test2;

import java.awt.Color;

public class Mover implements Runnable {
    private RouteUI ui;
    private CustomerAgent marker;
    private final int startX = 5, startY = 5;  // Starting position should not change

    public Mover(RouteUI ui, CustomerAgent marker) {
        this.ui = ui;
        this.marker = marker;
    }

    @Override
    public void run() {
        int currentX = startX;  // Current position starts at the initial position
        int currentY = startY;
        while (true) {
//        	int lastX = startX;
//            int lastY = startY;
            
            for(int j = 0; j < marker.getParcelLists().size();j++){
            	currentX = startX;  // Current position starts at the initial position
                currentY = startY;
            	
	            for(int i = 0; i < marker.getParcelLists().get(0).getParcels().size(); i++) {
	            	int targetX = marker.getParcelLists().get(j).getParcels().get(i).getX();
	            	int targetY = marker.getParcelLists().get(j).getParcels().get(i).getY();
	            	moveXAxisFirst(currentX, currentY, targetX, targetY);
	            	ui.updateGrid(targetX, targetY, Color.RED);
	            	currentX = targetX;
	            	currentY = targetY;
	            	
	            	try {
	                    Thread.sleep(1000); // Pause for visualization at the target
	                } catch (InterruptedException e) {
	                    Thread.currentThread().interrupt();
	                }
	            	
//	            	lastX = targetX;
//	                lastY = targetY;
	            	
	            }
//	            moveXAxisFirst(currentX, currentY, targetX, targetY); // Move x-axis first
//	            ui.updateGrid(targetX, targetY, Color.RED);
//	            moveXAxisFirst(currentX, currentY, targetX, targetY); // Move x-axis first
            
	            moveXAxisFirst(currentX, currentY, startX, startY); // Return using x-axis first
	            try {
	                Thread.sleep(1000); // Pause for visualization at the target
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	            }
            
            }
            marker.NewLocation(); // Move to a new location after return
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
                Thread.sleep(150); // Slow down the movement for visualization
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
                Thread.sleep(150); // Slow down the movement for visualization
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ui.updateGrid(px, py, Color.WHITE); // Clear old path
            py += dy;
            ui.updateGrid(px, py, Color.GREEN); // Color the new path
        }
    }
}
