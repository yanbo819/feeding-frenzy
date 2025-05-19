package cn.zjnu.demos;

import java.io.InputStream;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Fish {
    protected double x, y;
    protected double dx, dy; 
    protected double size;
    protected boolean alive;
    private Image image;

    // Constructor to create a new fish
    public Fish(double x, double y, int sizeIndex) {
        this.x = x;
        this.y = y;
        this.size = calculateSize(sizeIndex); 
        this.alive = true;
        this.image = loadImage(sizeIndex); 

        double speedFactor = getSpeedFactor(sizeIndex);
        this.dx = (Math.random() - 0.3) * speedFactor; 
        this.dy = (Math.random() - 0.3) * speedFactor; 

        System.out.println("Fish created. Size index: " + sizeIndex + ", Size: " + size + ", Speed: (" + dx + ", " + dy + ")");
    }

    private double getSpeedFactor(int sizeIndex) {
     
        if (sizeIndex >= 0 && sizeIndex <= 4) {
            return 4; 
        } else if (sizeIndex >= 5 && sizeIndex <= 18) {
            return 3;
        } else if (sizeIndex >= 19 && sizeIndex <= 23) {
            return 2; 
        } else if (sizeIndex >= 24 && sizeIndex <= 37) {
            return 1.5; 
        } else {
            return 3; 
        }
    }

    
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSize() {
        return size;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void move() {
        x += dx;
        y += dy;

        // Handle bouncing at the edges
        if (x < 0 || x > 800 - size) dx = -dx; // Reverse direction on x-axis
        if (y < 0 || y > 600 - size) dy = -dy; // Reverse direction on y-axis
    }

    // Method to render the fish
    public void render(GraphicsContext gc) {
        if (alive) {
            gc.drawImage(image, x, y, size, size);
        }
    }

    // Method to check if the fish intersects with another fish
    public boolean intersects(Fish other) {
        double distance = Math.sqrt(Math.pow(x - other.getX(), 2) + Math.pow(y - other.getY(), 2));
        return distance < (size + other.getSize()) / 2;
    }

    // Method to calculate the size of the fish based on the size index
    private double calculateSize(int sizeIndex) {
        // Calculate sizes for different fish types
        if (sizeIndex >= 0 && sizeIndex <= 4) {
            return 20 + sizeIndex * 5; // Small fish sizes: 20, 25, 30, 35, 40
        } else if (sizeIndex >= 5 && sizeIndex <= 18) {
            return 30 + (sizeIndex - 5) * 3; // Medium fish sizes: 50, 53, ..., 89
        } else if (sizeIndex >= 19 && sizeIndex <= 23) {
        	return 60 + (sizeIndex - 19) * 4; // Big fish sizes: 100, 105, 110, 115, 120
        } else if (sizeIndex >= 24 && sizeIndex <= 37) {
            return 130 + (sizeIndex - 24) * 4; // Large fish sizes: 130, 134, ..., 186
        } else {
            return 20; // Default size
        }
    }

    private Image loadImage(int sizeIndex) {
        String imagePath;
        if (sizeIndex >= 0 && sizeIndex <= 4) {
            imagePath = "/cn/zjnu/demos/images/fishIconh" + sizeIndex + ".png";
        } else if (sizeIndex >= 5 && sizeIndex <= 18) {
            imagePath = "/cn/zjnu/demos/images/fishIcon" + sizeIndex + ".png";
        } else if (sizeIndex >= 19 && sizeIndex <= 23) {
            imagePath = "/cn/zjnu/demos/images/fishIconL" + (sizeIndex - 19) + ".png";
        } else if (sizeIndex >= 24 && sizeIndex <= 37) {
            imagePath = "/cn/zjnu/demos/images/fishIconL" + (sizeIndex - 19) + ".png";
        } else {
            imagePath = "/cn/zjnu/demos/images/fishIcon0.png";
        }
        
        System.out.println("Attempting to load: " + imagePath);  // Debug log
        InputStream stream = getClass().getResourceAsStream(imagePath);
        
        if (stream == null) {
            System.err.println("Failed to load image at: " + imagePath);
            return null;
        }
        
        return new Image(stream);
    }
}
