package cn.zjnu.demos;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    private Stage mainStage;

    // Music players
    private MediaPlayer mediaPlayer;
    private MediaPlayer touchSoundPlayer;
    private MediaPlayer eatSoundPlayer;
    private MediaPlayer dieSoundPlayer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        showLanguageSelection();
    }

    private void startMainGame() {
        Stage gameStage = new Stage();
        gameStage.setTitle("Feeding Frenzy");
        Group root = new Group();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);
        gc = canvas.getGraphicsContext2D();

        initializeMusic();
        initializeSoundEffects();

        messages = ResourceBundle.getBundle("cn.zjnu.demos.messages", currentLocale);

        restartButton = new Button();
        restartButton.setLayoutX(WIDTH / 2 - 70);
        restartButton.setLayoutY(HEIGHT / 2 + 50);
        restartButton.setStyle("-fx-font-size: 20px; -fx-background-color: linear-gradient(to bottom, #5d3ba1, #3a1d6e); -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 10 20;");
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
        gameStage.setScene(scene);
        gameStage.show();

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
            String musicPath = getClass().getResource("sounds/background_music.wav").toString();
            Media media = new Media(musicPath);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(0.5);
            mediaPlayer.play();
        } catch (Exception e) {
            System.err.println("Error loading or playing music: " + e.getMessage());
        }
    }

    private void initializeSoundEffects() {
        try {
            String touchSoundPath = getClass().getResource("sounds/A_touch.wav").toString();
            Media touchSoundMedia = new Media(touchSoundPath);
            touchSoundPlayer = new MediaPlayer(touchSoundMedia);
            touchSoundPlayer.setVolume(0.7);

            String eatSoundPath = getClass().getResource("sounds/A_add.wav").toString();
            Media eatSoundMedia = new Media(eatSoundPath);
            eatSoundPlayer = new MediaPlayer(eatSoundMedia);
            eatSoundPlayer.setVolume(0.7);

            String dieSoundPath = getClass().getResource("sounds/A_die.wav").toString();
            Media dieSoundMedia = new Media(dieSoundPath);
            dieSoundPlayer = new MediaPlayer(dieSoundMedia);
            dieSoundPlayer.setVolume(0.7);
        } catch (Exception e) {
            System.err.println("Error loading sound effects: " + e.getMessage());
        }
    }

    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();
        switch (code) {
            case UP -> player.setDy(-2);
            case DOWN -> player.setDy(2);
            case LEFT -> player.setDx(-2);
            case RIGHT -> player.setDx(2);
            case L -> toggleLanguage();
            case M -> toggleMusic();
            default -> {
            }
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
        if (touchSoundPlayer != null) {
            touchSoundPlayer.dispose();
        }
        if (eatSoundPlayer != null) {
            eatSoundPlayer.dispose();
        }
        if (dieSoundPlayer != null) {
            dieSoundPlayer.dispose();
        }
    }

    private void initializeGame() {
        player = new PlayerFish(WIDTH / 2, HEIGHT / 2, this, WIDTH, HEIGHT);
        fishes = new ArrayList<>();
        spawnBalancedFishes(MAX_FISHES);
        gameOver = false;
        score = 50;
    }

    private void playSound(MediaPlayer soundPlayer) {
        if (soundPlayer != null) {
            if (soundPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                soundPlayer.stop();
            }
            soundPlayer.play();
        }
    }

    private void updateGame() {
        if (gameOver)
            return;

        player.move();
        fishes.forEach(Fish::move);

        for (Fish fish : new ArrayList<>(fishes)) {
            if (fish.isAlive() && player.intersects(fish)) {
                if (player.getSize() > fish.getSize()) {
                    playSound(eatSoundPlayer);
                    player.grow();
                    fish.setAlive(false);
                    score += 50;
                    if (score >= WIN_SCORE * 10) {
                        gameOver = true;
                    }
                } else {
                    playSound(touchSoundPlayer);
                    score -= 50;
                    fish.setAlive(false);
                    if (score <= 0) {
                        gameOver = true;
                        score = 0;
                        playSound(dieSoundPlayer);
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
        StackPane root = new StackPane();
        
        // Create gradient background
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #5d3ba1, #3a1d6e);");
        
        VBox mainBox = new VBox(20);
        mainBox.setAlignment(Pos.CENTER);
        
        Button settingsButton = new Button("Settings");
        settingsButton.setStyle("-fx-font-size: 25px; -fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 10; -fx-padding: 10 30;");
        
        VBox settingsMenu = new VBox(15);
        settingsMenu.setAlignment(Pos.CENTER);
        settingsMenu.setVisible(false);
        
        Button languageButton = new Button("Language");
        languageButton.setStyle("-fx-font-size: 25px; -fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 8 20;");
        
        VBox languageOptions = new VBox(10);
        languageOptions.setAlignment(Pos.CENTER);
        languageOptions.setVisible(false);
        
        Button englishBtn = new Button("English");
        Button chineseBtn = new Button("中文");
        Button exitBtn = new Button("Exit");
        
        // Style all buttons
        String buttonStyle = "-fx-font-size: 25px; -fx-background-color: linear-gradient(to bottom, #6a4bbc, #4a2d96); "
                + "-fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 10 25; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 1);";
        
        englishBtn.setStyle(buttonStyle);
        chineseBtn.setStyle(buttonStyle);
        exitBtn.setStyle(buttonStyle);
        
        // Add hover effects
        String hoverStyle = "-fx-background-color: linear-gradient(to bottom, #7b5acd, #5b3ea6);";
        englishBtn.setOnMouseEntered(e -> englishBtn.setStyle(buttonStyle + hoverStyle));
        englishBtn.setOnMouseExited(e -> englishBtn.setStyle(buttonStyle));
        chineseBtn.setOnMouseEntered(e -> chineseBtn.setStyle(buttonStyle + hoverStyle));
        chineseBtn.setOnMouseExited(e -> chineseBtn.setStyle(buttonStyle));
        exitBtn.setOnMouseEntered(e -> exitBtn.setStyle(buttonStyle + hoverStyle));
        exitBtn.setOnMouseExited(e -> exitBtn.setStyle(buttonStyle));
        
        Button startGameButton = new Button("Start Game");
        startGameButton.setStyle(buttonStyle);
        startGameButton.setOnMouseEntered(e -> startGameButton.setStyle(buttonStyle + hoverStyle));
        startGameButton.setOnMouseExited(e -> startGameButton.setStyle(buttonStyle));
        
        // Button actions
        settingsButton.setOnAction(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(300), settingsMenu);
            ft.setFromValue(settingsMenu.isVisible() ? 1.0 : 0.0);
            ft.setToValue(settingsMenu.isVisible() ? 0.0 : 1.0);
            ft.setOnFinished(evt -> settingsMenu.setVisible(!settingsMenu.isVisible()));
            ft.play();
        });
        
        languageButton.setOnAction(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(300), languageOptions);
            ft.setFromValue(languageOptions.isVisible() ? 1.0 : 0.0);
            ft.setToValue(languageOptions.isVisible() ? 0.0 : 1.0);
            ft.setOnFinished(evt -> languageOptions.setVisible(!languageOptions.isVisible()));
            ft.play();
        });
        
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
        
        startGameButton.setOnAction(e -> {
            startMainGame();
            languageStage.close();
        });
        
        exitBtn.setOnAction(e -> {
            System.exit(0);
        });
        
        // Build the UI hierarchy
        languageOptions.getChildren().addAll(englishBtn, chineseBtn);
        settingsMenu.getChildren().addAll(languageButton, languageOptions, exitBtn);
        mainBox.getChildren().addAll(startGameButton, settingsButton, settingsMenu);
        root.getChildren().add(mainBox);
        
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        languageStage.setScene(scene);
        languageStage.setTitle("Feeding Frenzy - Main Menu");
        languageStage.show();
    }

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
        switch (direction) {
            case "LEFT" -> {
                player.setX(WIDTH - player.getSize());
                currentScene = 0;
            }
            case "RIGHT" -> {
                player.setX(player.getSize());
                currentScene = 1;
            }
            case "UP" -> {
                player.setY(HEIGHT - player.getSize());
                currentScene = 2;
            }
            case "DOWN" -> {
                player.setY(player.getSize());
                currentScene = 0;
            }
        }
    }
}