package cn.zjnu.demos;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class FeedingFrenzy extends Application {
    private static final int WIDTH = 1180;
    private static final int HEIGHT = 800;
    private static final int MAX_FISHES = 20;

    private int score;
    private ResourceBundle messages;
    private Locale currentLocale = Locale.CHINA;
    private static final int WIN_SCORE = 100;

    private GraphicsContext gc;
    private List<Fish> fishes;
    private PlayerFish player;
    private boolean gameOver;
    private Image[] backgrounds;
    private int currentScene;
    private final Random rand = new Random();
    private Button restartButton;
    private Stage languageStage;

    // Music player
    private MediaPlayer mediaPlayer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Feeding Frenzy");

        showLanguageSelection();
    }

    private void startMainGame() {
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Feeding Frenzy");
        Group root = new Group();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);
        gc = canvas.getGraphicsContext2D();

        initializeMusic();

        messages = ResourceBundle.getBundle("cn.zjnu.demos.messages", currentLocale);

        restartButton = new Button();
        restartButton.setLayoutX(WIDTH / 2 - 70);
        restartButton.setLayoutY(HEIGHT / 2 + 50);
        restartButton.setStyle("-fx-font-size: 20px; -fx-background-color:rgb(93, 59, 161); -fx-text-fill: white;");
        restartButton.setFont(new Font("Arial", 20));
        restartButton.setVisible(false);
        restartButton.setOnAction(e -> restartGame());
        restartButton.setText(messages.getString("restart"));
        root.getChildren().add(restartButton);

        backgrounds = new Image[3];
        backgrounds[0] = loadImage("/images/background1.jpg");
        backgrounds[1] = loadImage("/images/background2.jpg");
        backgrounds[2] = loadImage("/images/background3.jpg");

        currentScene = 0;
        initializeGame();

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(this::handleKeyPress);
        scene.setOnKeyReleased(this::handleKeyRelease);
        primaryStage.setScene(scene);
        primaryStage.show();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame();
                renderGame();
            }
        }.start();
    }

    private void initializeMusic() {
        try {
            // Load the music file
            String musicPath = getClass().getResource("sounds/background_music.mp3").toString();
            Media media = new Media(musicPath);

            // Create a MediaPlayer instance
            mediaPlayer = new MediaPlayer(media);

            // Set up the MediaPlayer
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop the music indefinitely
            mediaPlayer.setVolume(0.5); // Set the volume (0.0 to 1.0)

            // Play the music
            mediaPlayer.play();
        } catch (Exception e) {
            System.err.println("Error loading or playing music: " + e.getMessage());
        }
    }

    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.UP) {
            player.setDy(-2);
        } else if (code == KeyCode.DOWN) {
            player.setDy(2);
        } else if (code == KeyCode.LEFT) {
            player.setDx(-2);
        } else if (code == KeyCode.RIGHT) {
            player.setDx(2);
        } else if (code == KeyCode.L) {
            toggleLanguage();
        } else if (code == KeyCode.M) { // Press 'M' to toggle music
            toggleMusic();
        }
    }

    private void handleKeyRelease(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.UP || code == KeyCode.DOWN) {
            player.setDy(0);
        }
        if (code == KeyCode.LEFT || code == KeyCode.RIGHT) {
            player.setDx(0);
        }
    }

    private void toggleMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.play();
            }
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }

    private void initializeGame() {
        player = new PlayerFish(WIDTH / 2, HEIGHT / 2, this, WIDTH, HEIGHT);
        fishes = new ArrayList<>();
        spawnBalancedFishes(MAX_FISHES);
        gameOver = false;
        score = 0; // Start with 0 points
    }

    private void updateGame() {
        if (gameOver)
            return;

        
        player.move();
        fishes.forEach(Fish::move);

        for (Fish fish : new ArrayList<>(fishes)) {
            if (fish.isAlive() && player.intersects(fish)) {
                if (player.getSize() > fish.getSize()) {
                    player.grow();
                    fish.setAlive(false);
                    score += 50;
                    if (score >= WIN_SCORE * 10) { // Win when reaching 400 points
                        gameOver = true;
                    }
                } else {
                    score -= 50;
                    fish.setAlive(false);
                    if (score <= 0) {
                        gameOver = true;
                        score = 0;
                    }
                }
            }
        }
    }

    private void renderGame() {
        gc.clearRect(0, 0, WIDTH, HEIGHT);
        if (backgrounds[currentScene] != null) {
            gc.drawImage(backgrounds[currentScene], 0, 0, WIDTH, HEIGHT);
        }

        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 24));
        gc.fillText(messages.getString("score") + score, 20, 40);

        if (gameOver) {
            gc.setFill(score >= WIN_SCORE * 2 ? Color.GREEN : Color.RED);
            gc.setFont(new Font("Arial", 48));
            String message = score >= WIN_SCORE * 2 ? messages.getString("win") : messages.getString("gameOver");
            gc.fillText(message, WIDTH / 2 - 120, HEIGHT / 2);
            restartButton.setVisible(true);
        } else {
            restartButton.setVisible(false);
            player.render(gc);
            fishes.forEach(fish -> fish.render(gc));
        }
    }

    private void restartGame() {
        initializeGame();
        currentScene = 0;
        restartButton.setVisible(false);
    }

    private void spawnBalancedFishes(int count) {
        int small = (int) (count * 0.7);
        int medium = (int) (count * 0.2);
        int large = count - small - medium;

        spawnSpecificSizeFishes(small, 0, 4);
        spawnSpecificSizeFishes(medium, 5, 18);
        spawnSpecificSizeFishes(large, 19, 23);
    }

    private void spawnSpecificSizeFishes(int count, int min, int max) {
        for (int i = 0; i < count; i++) {
            fishes.add(new Fish(
                    rand.nextInt(WIDTH),
                    rand.nextInt(HEIGHT),
                    rand.nextInt(max - min + 1) + min));

            // Remove old fish if exceeding limits
            while (fishes.size() > MAX_FISHES) {
                fishes.remove(0);
            }
        }
    }

    private Image loadImage(String path) {
        try {
            System.out.println("Loading image: " + path);
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("Error loading image: " + path);
            return null;
        }
    }

    private void showLanguageSelection() {
        languageStage = new Stage();
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);

        Button englishBtn = new Button("English");
        Button chineseBtn = new Button("中文");
        englishBtn.setPrefSize(200, 50);
        chineseBtn.setPrefSize(200, 50);
        englishBtn.setFont(new Font("Arial", 20));
        chineseBtn.setFont(new Font("Arial", 20));

        if (currentLocale.equals(Locale.ENGLISH)) {
            englishBtn.setStyle("-fx-background-color: rgb(93, 59, 161); -fx-text-fill: white;");
            chineseBtn.setStyle("-fx-background-color: rgb(255, 255, 255); -fx-text-fill: black;");
        } else {
            englishBtn.setStyle("-fx-background-color: rgb(255, 255, 255); -fx-text-fill: black;");
            chineseBtn.setStyle("-fx-background-color: rgb(93, 59, 161); -fx-text-fill: white;");
        }

        englishBtn.setOnAction(e -> {
            currentLocale = Locale.ENGLISH;
            startMainGame();
            languageStage.close();
        });

        chineseBtn.setOnAction(e -> {
            currentLocale = Locale.SIMPLIFIED_CHINESE;
            startMainGame();
            languageStage.close();
        });

        root.getChildren().addAll(englishBtn, chineseBtn);
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        languageStage.setScene(scene);
        languageStage.setTitle("Select Language");
        languageStage.show();
    }

    // Add this method to toggle language at runtime
    private void toggleLanguage() {
        if (currentLocale.equals(Locale.ENGLISH)) {
            currentLocale = Locale.SIMPLIFIED_CHINESE;
        } else {
            currentLocale = Locale.ENGLISH;
        }
        messages = ResourceBundle.getBundle("cn.zjnu.demos.messages", currentLocale);
        restartButton.setText(messages.getString("restart"));
    }

    public void changeScene(String direction) {
        fishes.clear();
        spawnBalancedFishes(MAX_FISHES);
        if (direction.equals("LEFT")) {
            player.setX(WIDTH - player.getSize());
            currentScene = 0;
        } else if (direction.equals("RIGHT")) {
            player.setX(player.getSize());
            currentScene = 1;
        } else if (direction.equals("UP")) {
            player.setY(HEIGHT - player.getSize());
            currentScene = 2;
        } else if (direction.equals("DOWN")) {
            player.setY(player.getSize());
            currentScene = 0;
        }
    }
}