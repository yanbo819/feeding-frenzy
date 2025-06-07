package cn.zjnu.demos;

import javafx.scene.canvas.GraphicsContext;

class EnemyFish extends Fish {
    private boolean facingRight; // Only need facingRight, no separate Image fields

    public EnemyFish(double x, double y, int sizeIndex) {
        super(x, y, sizeIndex); // Fish constructor loads the image into this.image

        // Determine initial direction randomly based on current dx
        // If dx is positive, it's facing right. If negative, it's facing left.
        facingRight = (dx >= 0);
    }

    @Override
    public void move() {
        super.move();
        // Update facing direction based on current dx
        if (dx > 0 && !facingRight) {
            facingRight = true;
        } else if (dx < 0 && facingRight) {
            facingRight = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (alive && image != null) { // Use the inherited 'image' from Fish class
            gc.save(); // Save the current graphics context state

            if (!facingRight) {
                // Apply a horizontal flip transformation
                // The pivot point for scaling is the center of the image
                gc.translate(x + size / 2, y + size / 2); // Move origin to center of fish
                gc.scale(-1, 1); // Flip horizontally
                gc.translate(-(x + size / 2), -(y + size / 2)); // Move origin back
            }
            // Draw the image. If flipped, it will be drawn mirrored.
            gc.drawImage(image, x, y, size, size);
            gc.restore(); // Restore the graphics context to its previous state (undoes flip)
        }
    }
}