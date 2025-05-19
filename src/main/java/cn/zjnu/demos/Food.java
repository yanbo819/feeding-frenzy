package cn.zjnu.demos;

import java.util.Random;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Food {
    private static final Random rand = new Random();
    private double x, y;
    private boolean active;
    
    public Food() {
        respawn();
    }
    
    public void respawn() {
        x = rand.nextInt(760) + 20;
        y = rand.nextInt(560) + 20;
        active = true;
    }
    
    public void render(GraphicsContext gc) {
        if(active) {
            gc.setFill(Color.GOLD);
            gc.fillOval(x, y, 20, 20);
        }
    }
    
    public boolean checkCollision(PlayerFish player) {
        return player.getX() < x + 20 && 
               player.getX() + player.getSize() > x &&
               player.getY() < y + 20 && 
               player.getY() + player.getSize() > y;
    }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}