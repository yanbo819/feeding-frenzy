package cn.zjnu.demos;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class PlayerFish extends Fish {
    private double playerDx, playerDy;
    private boolean facingRight;
    private final Image smallRightImage, mediumRightImage, largeRightImage;
    private final Image smallLeftImage, mediumLeftImage, largeLeftImage;
    private Image image;
    private final FeedingFrenzy game; // Reference to the game instance
    private final int width;
    private final int height;

    public PlayerFish(double x, double y, FeedingFrenzy game, int width, int height) {
        super(x, y, 5); // Initialize with sizeIndex = 5 (larger than the smallest fish)
        this.size = 40; // Starting size
        this.width = width;
        this.height = height;

        this.playerDx = 0;
        this.playerDy = 0;
        this.size = 40; // Start with a larger size to be initially bigger than small fishes
        this.facingRight = true;
        this.game = game; // Store reference to the game instance

        // Load images for player fish based on new provided filenames
        smallRightImage = loadImage("/images/PlayerFishsmall2r.png");
        mediumRightImage = loadImage("/images/PlayerFishmiddle2r.png");
        largeRightImage = loadImage("/images/PlayerFishlarge2r.png");
        smallLeftImage = loadImage("/images/PlayerFishsmall.png");
        mediumLeftImage = loadImage("/images/PlayerFishmiddle.png");
        largeLeftImage = loadImage("/images/PlayerFishlarge.png");

        this.image = mediumRightImage; // Start with the medium right image, because initial size is medium

        // Debug check to verify images
        if (this.image.isError()) {
            System.err.println("Error: Player image not loaded properly.");
        }
    }

    private Image loadImage(String path) {
        Image img = new Image(getClass().getResourceAsStream(path));
        if (img.isError()) {
            System.err.println("Error loading image: " + path);
        } else {
            System.out.println("Successfully loaded image: " + path);
        }
        return img;
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
        } else if (x > width - size) {
            game.changeScene("RIGHT");
        } else if (y < 0) {
            game.changeScene("UP");
        } else if (y > height - size) {
            game.changeScene("DOWN");
        }
    }

    public void grow() {
        size += 1; // Increase the size more each time to quickly become larger than other fishes
        updateImage();
    }

    public void setX(double x) {
        this.x = x; // Set the new x position for the player fish
    }

    public void setY(double y) {
        this.y = y; // Set the new y position for the player fish
    }

    private void updateImage() {
        // Choose the image based on size and direction
        if (size <= 40) { // Updated size for small
            image = facingRight ? smallRightImage : smallLeftImage;
        } else if (size <= 80) { // Updated size for medium
            image = facingRight ? mediumRightImage : mediumLeftImage;
        } else { // Large size
            image = facingRight ? largeRightImage : largeLeftImage;
        }

        System.out.println("Player image updated to: " + (facingRight ? "Right" : "Left") + " image for size " + size);
    }

    @Override
    public boolean intersects(Fish other) {
        double distance = Math.sqrt(Math.pow(x - other.getX(), 2) + Math.pow(y - other.getY(), 2));
        return distance < (size + other.getSize()) / 2;
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(image, x, y, size, size);
    }
}
/* i need you help me to do the fish more biger ane */
