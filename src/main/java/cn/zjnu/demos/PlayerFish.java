package cn.zjnu.demos;

import java.io.InputStream;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

class PlayerFish extends Fish {
    private double playerDx, playerDy;
    private boolean facingRight;
    private final Image smallRightImage, mediumRightImage, largeRightImage;
    private final Image smallLeftImage, mediumLeftImage, largeLeftImage;
    private Image currentDisplayedImage; // Renamed to avoid conflict with Fish.image
    private final FeedingFrenzy game;
    private final int screenWidth;
    private final int screenHeight;

    public PlayerFish(double x, double y, FeedingFrenzy game, int width, int height) {
        super(x, y, 5); // Initialize with a base size index
        this.size = 40; // Set player's specific starting size after super call
        this.screenWidth = width;
        this.screenHeight = height;

        this.playerDx = 0;
        this.playerDy = 0;
        this.facingRight = true;
        this.game = game;

        // Load images for player fish
        smallRightImage = loadImage("/images/PlayerFishsmall2r.png");
        mediumRightImage = loadImage("/images/PlayerFishmiddle2r.png");
        largeRightImage = loadImage("/images/PlayerFishlarge2r.png");
        smallLeftImage = loadImage("/images/PlayerFishsmall.png");
        mediumLeftImage = loadImage("/images/PlayerFishmiddle.png");
        largeLeftImage = loadImage("/images/PlayerFishlarge.png");

        this.currentDisplayedImage = mediumRightImage; // Start with the medium right image
    }

    private Image loadImage(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            return null;
        }
        return new Image(stream);
    }

    public void setDx(double dx) {
        this.playerDx = dx;
        if (dx > 0) {
            facingRight = true;
        } else if (dx < 0) {
            facingRight = false;
        }
        updateImage();
    }

    public void setDy(double dy) {
        this.playerDy = dy;
    }

    @Override
    public void move() {
        x += playerDx;
        y += playerDy;

        // Detect collision with screen edges and switch scenes
        if (x < 0) {
            game.changeScene("LEFT");
        } else if (x > screenWidth - size) {
            game.changeScene("RIGHT");
        } else if (y < 0) {
            game.changeScene("UP");
        } else if (y > screenHeight - size) {
            game.changeScene("DOWN");
        }
    }

    public void grow() {
        size += 5; // Increase the size by a more significant amount
        updateImage();
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    private void updateImage() {
        // Choose the image based on size and direction
        if (size <= 60) { // Updated size for small fish image
            currentDisplayedImage = facingRight ? smallRightImage : smallLeftImage;
        } else if (size <= 120) { // Updated size for medium fish image
            currentDisplayedImage = facingRight ? mediumRightImage : mediumLeftImage;
        } else { // Large size fish image
            currentDisplayedImage = facingRight ? largeRightImage : largeLeftImage;
        }
    }

    @Override
    public boolean intersects(Fish other) {
        double distance = Math.sqrt(Math.pow(x - other.getX(), 2) + Math.pow(y - other.getY(), 2));
        return distance < (size + other.getSize()) / 2;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (currentDisplayedImage != null) {
            gc.drawImage(currentDisplayedImage, x, y, size, size);
        }
    }
}