package test2;

import java.awt.Color;
import java.util.Random;

public class RandomMarker {
    private RouteUI ui;
    private Random random;
    private int currentX, currentY;

    public RandomMarker(RouteUI ui) {
        this.ui = ui;
        this.random = new Random();
        NewLocation();
    }

    public void NewLocation() {
        if (currentX >= 0 && currentY >= 0) {
            ui.updateGrid(currentX, currentY, Color.WHITE); // Reset the old location
        }
        currentX = random.nextInt(RouteUI.ROW_SIZE);
        currentY = random.nextInt(RouteUI.COL_SIZE );
        ui.updateGrid(currentX, currentY, Color.BLUE);
    }

    public int getCurrentX() {
        return currentX;
    }

    public int getCurrentY() {
        return currentY;
    }
}
