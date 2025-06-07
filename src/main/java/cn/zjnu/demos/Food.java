package cn.zjnu.demos;

import java.util.Random;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

class Food {
    private static final Random rand = new Random();
    private double x, y;
    private boolean active;
    private int screenWidth, screenHeight;
    private int foodSize;

    public Food(int screenWidth, int screenHeight, int foodSize) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.foodSize = foodSize;
        initPosition();
        active = true;
    }

    public void respawn(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        initPosition();
        active = true;
    }

    // Private helper to initialize position
    private void initPosition() {
        // Ensure food spawns within bounds, not too close to edges
        x = rand.nextInt(screenWidth - foodSize * 2) + foodSize;
        y = rand.nextInt(screenHeight - foodSize * 2) + foodSize;
    }

    public void render(GraphicsContext gc) {
        if(active) {
            gc.setFill(Color.GOLD);
            gc.fillOval(x, y, foodSize, foodSize);
        }
    }

    public boolean checkCollision(PlayerFish player) {
        // Simple rectangular collision check for food and player
        return player.getX() < x + foodSize &&
               player.getX() + player.getSize() > x &&
               player.getY() < y + foodSize &&
               player.getY() + player.getSize() > y;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getFoodSize() { return foodSize; }
    public void setFoodSize(int foodSize) { this.foodSize = foodSize; }
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
    public void setScreenWidth(int screenWidth) { this.screenWidth = screenWidth; }
    public void setScreenHeight(int screenHeight) { this.screenHeight = screenHeight; }
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public void setPosition(double x, double y, int foodSize) {
        this.x = x;
        this.y = y;
        this.foodSize = foodSize;
    }
    public void setPosition(int x, int y, int foodSize) {
        this.x = x;
        this.y = y;
        this.foodSize = foodSize;
    }
    public void setActive() {
        this.active = true;
    }
    public void setInactive() {
        this.active = false;
    }
}