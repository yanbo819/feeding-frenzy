package cn.zjnu.demos;

import java.io.InputStream;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

abstract class Fish {
    protected double x, y;
    protected double dx, dy;
    protected double size;
    protected boolean alive;
    protected  final Image image; // This will hold the default image for the fish

    // Constructor to create a new fish
    public Fish(double x, double y, int sizeIndex) {
        this.x = x;
        this.y = y;
        this.size = calculateSize(sizeIndex);
        this.alive = true;
        this.image = loadImage(sizeIndex); // Load image based on size index

        double speedFactor = getSpeedFactor(sizeIndex);
        // Randomize initial direction for both dx and dy
        this.dx = (Math.random() * 2 - 1) * speedFactor; // From -speedFactor to +speedFactor
        this.dy = (Math.random() * 2 - 1) * speedFactor;
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

        // Handle bouncing at the edges (using main game's WIDTH and HEIGHT for consistency)
        if (x < 0 || x > 1024 - size) dx = -dx;
        if (y < 0 || y > 768 - size) dy = -dy;
    }

    // Method to render the fish
    public void render(GraphicsContext gc) {
        if (alive && image != null) { // Ensure image is not null before drawing
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
        if (sizeIndex >= 0 && sizeIndex <= 4) {
            return 20 + sizeIndex * 5;
        } else if (sizeIndex >= 5 && sizeIndex <= 18) {
            return 30 + (sizeIndex - 5) * 3;
        } else if (sizeIndex >= 19 && sizeIndex <= 23) {
            return 60 + (sizeIndex - 19) * 4;
        } else if (sizeIndex >= 24 && sizeIndex <= 37) {
            return 130 + (sizeIndex - 24) * 4;
        } else {
            return 20;
        }
    }

    private Image loadImage(int sizeIndex) {
        String imagePath;
        if (sizeIndex >= 0 && sizeIndex <= 4) {
            imagePath = "/images/fishIconh" + sizeIndex + ".png";
        } else if (sizeIndex >= 5 && sizeIndex <= 18) {
            imagePath = "/images/fishIcon" + sizeIndex + ".png";
        } else if (sizeIndex >= 19 && sizeIndex <= 23) {
            imagePath = "/images/fishIconL" + (sizeIndex - 19) + ".png";
        } else if (sizeIndex >= 24 && sizeIndex <= 37) { // This range uses the same "L" series, adjust as per available images
            imagePath = "/images/fishIconL" + (sizeIndex - 19) + ".png";
        } else {
            imagePath = "/images/fishIcon0.png";
        }

        InputStream stream = getClass().getResourceAsStream(imagePath);
        if (stream == null) {
            // If an image is not found, return null. The render method will handle it.
            return null;
        }
        return new Image(stream);
    }
}