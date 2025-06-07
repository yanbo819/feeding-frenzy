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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class FeedingFrenzy extends Application {
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    private static final int MAX_FISHES = 20;
    private static final int WIN_SCORE = 1000;
    private static final int FOOD_ITEM_SCORE = 10; // Score gained from eating a food item
    private static final int FOOD_SIZE = 20; // Size of the food item on screen

    private int score;
    private ResourceBundle messages;
    private Locale currentLocale = Locale.CHINA;
    private GraphicsContext gc;
    private List<Fish> fishes;
    private PlayerFish player;
    private Food food; // Food instance
    private boolean gameOver;
    private Image[] backgrounds;
    private int currentScene;
    private final Random rand = new Random();
    private Button restartButton;
    private Stage languageStage;
    private Stage gameStage;

    private MediaPlayer mediaPlayer;
    private MediaPlayer touchSoundPlayer;
    private MediaPlayer eatSoundPlayer;
    private MediaPlayer dieSoundPlayer;

    private long gameStartTime;
    private AnimationTimer gameLoopTimer;
    private boolean isGamePaused;
    private long pausedElapsedTime;
    private final long totalGameTime = 60 * 1_000_000_000L; // 1 minute in nanoseconds
    private Button settingsButton;
    private VBox settingsMenu;
    private Button resumeButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        showLanguageSelection();
    }

    private void startMainGame() {
        if (gameStage != null && gameStage.isShowing()) {
            gameStage.close();
            stop();
        }

        gameStage = new Stage();
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

        settingsButton = new Button(messages.getString("settings"));
        settingsButton.setLayoutX(WIDTH - settingsButton.prefWidth(-1) - 20);
        settingsButton.setLayoutY(20);
        settingsButton.setStyle("-fx-font-size: 16px; -fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 5 15;");
        settingsButton.setOnAction(e -> toggleSettingsMenu());
        root.getChildren().add(settingsButton);

        settingsMenu = new VBox(25);
        settingsMenu.setAlignment(Pos.CENTER);
        settingsMenu.setPrefSize(350, 400);
        settingsMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-background-radius: 20; -fx-padding: 40; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 20, 0, 0, 0);");
        settingsMenu.setVisible(false);
        settingsMenu.setLayoutX((WIDTH - settingsMenu.getPrefWidth()) / 2);
        settingsMenu.setLayoutY((HEIGHT - settingsMenu.getPrefHeight()) / 2);

        String settingsButtonStyle = "-fx-font-size: 22px; -fx-background-color: linear-gradient(to bottom, #7a5cdb, #5a3ba1); -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 12 30;";
        String settingsHoverStyle = "-fx-background-color: linear-gradient(to bottom, #8c6fe9, #6c4ccf); -fx-scale-x: 1.03; -fx-scale-y: 1.03;";

        resumeButton = new Button(messages.getString("continueGame"));
        resumeButton.setStyle(settingsButtonStyle);
        resumeButton.setOnMouseEntered(e -> resumeButton.setStyle(settingsButtonStyle + settingsHoverStyle));
        resumeButton.setOnMouseExited(e -> resumeButton.setStyle(settingsButtonStyle));
        resumeButton.setOnAction(e -> toggleSettingsMenu());

        Button languageSettingsButton = new Button(messages.getString("language"));
        languageSettingsButton.setStyle(settingsButtonStyle);
        languageSettingsButton.setOnMouseEntered(e -> languageSettingsButton.setStyle(settingsButtonStyle + settingsHoverStyle));
        languageSettingsButton.setOnMouseExited(e -> languageSettingsButton.setStyle(settingsButtonStyle));
        languageSettingsButton.setOnAction(e -> {
            showLanguageSelection();
            gameStage.close();
            stop();
        });

        Button exitGameButton = new Button(messages.getString("exit"));
        exitGameButton.setStyle(settingsButtonStyle);
        exitGameButton.setOnMouseEntered(e -> exitGameButton.setStyle(settingsButtonStyle + settingsHoverStyle));
        exitGameButton.setOnMouseExited(e -> exitGameButton.setStyle(settingsButtonStyle));
        exitGameButton.setOnAction(e -> {
            gameStage.close();
            showLanguageSelection();
            stop();
        });

        settingsMenu.getChildren().addAll(resumeButton, languageSettingsButton, exitGameButton);
        root.getChildren().add(settingsMenu);

        backgrounds = new Image[3];
        backgrounds[0] = new Image(getClass().getResourceAsStream("/images/background1.jpg"));
        backgrounds[1] = new Image(getClass().getResourceAsStream("/images/background2.jpg"));
        backgrounds[2] = new Image(getClass().getResourceAsStream("/images/background3.jpg"));

        currentScene = 0;
        initializeGame();

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(this::handleKeyPress);
        scene.setOnKeyReleased(this::handleKeyRelease);
        gameStage.setScene(scene);
        gameStage.show();

        gameLoopTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isGamePaused && !gameOver) {
                    long currentElapsedTime = now - gameStartTime;
                    long remainingTimeNanos = totalGameTime - currentElapsedTime;

                    if (remainingTimeNanos <= 0) {
                        gameOver = true;
                        gameLoopTimer.stop();
                    }
                    updateGame();
                }
                renderGame();
            }
        };
        gameLoopTimer.start();
    }

    private void toggleSettingsMenu() {
        boolean wasVisible = settingsMenu.isVisible();
        settingsMenu.setVisible(!wasVisible);
        isGamePaused = !isGamePaused;

        if (isGamePaused) {
            pausedElapsedTime = System.nanoTime() - gameStartTime;
            gameLoopTimer.stop();
            if (mediaPlayer != null) mediaPlayer.pause();
        } else {
            gameStartTime = System.nanoTime() - pausedElapsedTime;
            gameLoopTimer.start();
            if (mediaPlayer != null) mediaPlayer.play();
        }
    }

    private void initializeMusic() {
        try {
            String musicPath = getClass().getResource("sounds/background_music.wav").toExternalForm();
            Media media = new Media(musicPath);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(0.5);
            mediaPlayer.play();
        } catch (Exception e) {
            // Error loading or playing music
        }
    }

    private void initializeSoundEffects() {
        try {
            String touchSoundPath = getClass().getResource("sounds/A_touch.wav").toExternalForm();
            Media touchSoundMedia = new Media(touchSoundPath);
            touchSoundPlayer = new MediaPlayer(touchSoundMedia);
            touchSoundPlayer.setVolume(0.7);

            String eatSoundPath = getClass().getResource("sounds/A_add.wav").toExternalForm();
            Media eatSoundMedia = new Media(eatSoundPath);
            eatSoundPlayer = new MediaPlayer(eatSoundMedia);
            eatSoundPlayer.setVolume(0.7);

            String dieSoundPath = getClass().getResource("sounds/A_die.wav").toExternalForm();
            Media dieSoundMedia = new Media(dieSoundPath);
            dieSoundPlayer = new MediaPlayer(dieSoundMedia);
            dieSoundPlayer.setVolume(0.7);
        } catch (Exception e) {
            // Error loading sound effects
        }
    }

    private void handleKeyPress(KeyEvent event) {
        if (isGamePaused && event.getCode() != KeyCode.ESCAPE) {
            return;
        }

        KeyCode code = event.getCode();
        switch (code) {
            case UP, W -> player.setDy(-2);
            case DOWN, S -> player.setDy(2);
            case LEFT, A -> player.setDx(-2);
            case RIGHT, D -> player.setDx(2);
            case M -> toggleMusic();
            case ESCAPE -> {
                if (settingsMenu.isVisible()) {
                    toggleSettingsMenu();
                }
            }
            default -> {
            }
        }
    }

    private void handleKeyRelease(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.W || code == KeyCode.S) {
            player.setDy(0);
        }
        if (code == KeyCode.LEFT || code == KeyCode.RIGHT || code == KeyCode.A || code == KeyCode.D) {
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
        if (gameLoopTimer != null) {
            gameLoopTimer.stop();
        }
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
        food = new Food(WIDTH, HEIGHT, FOOD_SIZE);
        gameOver = false;
        score = 50;
        isGamePaused = false;
        gameStartTime = System.nanoTime();
        if (gameLoopTimer != null) {
            gameLoopTimer.start();
        }
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
        if (settingsMenu != null) {
            settingsMenu.setVisible(false);
        }
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
        if (gameOver || isGamePaused)
            return;

        player.move();
        fishes.forEach(Fish::move);

        if (food.isActive() && food.checkCollision(player)) {
            food.respawn(WIDTH, HEIGHT);
            score += FOOD_ITEM_SCORE;
        }

        fishes.removeIf(fish -> {
            if (fish.isAlive() && player.intersects(fish)) {
                if (player.getSize() > fish.getSize()) {
                    playSound(eatSoundPlayer);
                    player.grow();
                    score += 50;
                    if (score >= WIN_SCORE) {
                        gameOver = true;
                    }
                    return true;
                } else {
                    playSound(touchSoundPlayer);
                    score -= 50;
                    if (score <= 0) {
                        gameOver = true;
                        score = 0;
                        playSound(dieSoundPlayer);
                    }
                    return true;
                }
            }
            return false;
        });

        while (fishes.size() < MAX_FISHES) {
            spawnSpecificSizeFishes(1, 0, 23);
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

        long remainingTimeNanos = totalGameTime - (System.nanoTime() - gameStartTime);
        long remainingSeconds = Math.max(0, remainingTimeNanos / 1_000_000_000L);
        long minutes = remainingSeconds / 60;
        long seconds = remainingSeconds % 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        gc.fillText(messages.getString("time") + timeString, 20, 70);

        if (gameOver) {
            gc.setFill(score >= WIN_SCORE ? Color.GREEN : Color.RED);
            gc.setFont(new Font("Arial", 48));
            String message;
            if (score >= WIN_SCORE) {
                message = messages.getString("win");
            } else {
                if (remainingTimeNanos <= 0) {
                    message = messages.getString("timeUpLose");
                } else {
                    message = messages.getString("gameOver");
                }
            }
            double textWidth = message.length() * gc.getFont().getSize() * 0.6;
            gc.fillText(message, (WIDTH - textWidth) / 2, HEIGHT / 2 - 30);
            restartButton.setVisible(true);
            settingsButton.setVisible(false);
            if (gameLoopTimer != null) {
                gameLoopTimer.stop();
            }
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        } else if (isGamePaused) {
            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Arial", 48));
            String pausedMessage = messages.getString("paused");
            double textWidth = pausedMessage.length() * gc.getFont().getSize() * 0.6;
            gc.fillText(pausedMessage, (WIDTH - textWidth) / 2, HEIGHT / 2 - 30);
        } else {
            restartButton.setVisible(false);
            settingsButton.setVisible(true);
            player.render(gc);
            fishes.forEach(fish -> fish.render(gc));
            food.render(gc);
        }
    }

    private void restartGame() {
        initializeGame();
        currentScene = 0;
        restartButton.setVisible(false);
        gameLoopTimer.start();
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
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
            double x = rand.nextInt(WIDTH - 50) + 25;
            double y = rand.nextInt(HEIGHT - 50) + 25;
            fishes.add(new EnemyFish(x, y, rand.nextInt(max - min + 1) + min));
        }
        while (fishes.size() > MAX_FISHES) {
            fishes.remove(0);
        }
    }

    private void showLanguageSelection() {
        if (languageStage == null) {
            languageStage = new Stage();
        }
        StackPane root = new StackPane();

        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #5d3ba1, #3a1d6e);");

        VBox mainBox = new VBox(30);
        mainBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Feeding Frenzy");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 10, 0, 0, 0);");
        mainBox.getChildren().add(titleLabel);

        Button startGameButton = new Button();
        String buttonStyle = "-fx-font-size: 24px; -fx-background-color: linear-gradient(to bottom, #6a4bbc, #4a2d96); "
                + "-fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 15 40; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 8, 0, 0, 2);";
        String hoverStyle = "-fx-background-color: linear-gradient(to bottom, #7b5acd, #5b3ea6); -fx-scale-x: 1.05; -fx-scale-y: 1.05;";

        messages = ResourceBundle.getBundle("cn.zjnu.demos.messages", currentLocale);
        startGameButton.setText(messages.getString("startGame"));
        startGameButton.setStyle(buttonStyle);
        startGameButton.setOnMouseEntered(e -> startGameButton.setStyle(buttonStyle + hoverStyle));
        startGameButton.setOnMouseExited(e -> startGameButton.setStyle(buttonStyle));

        Button englishBtn = new Button("English");
        englishBtn.setStyle(buttonStyle);
        englishBtn.setOnMouseEntered(e -> englishBtn.setStyle(buttonStyle + hoverStyle));
        englishBtn.setOnMouseExited(e -> englishBtn.setStyle(buttonStyle));

        Button chineseBtn = new Button("中文");
        chineseBtn.setStyle(buttonStyle);
        chineseBtn.setOnMouseEntered(e -> chineseBtn.setStyle(buttonStyle + hoverStyle));
        chineseBtn.setOnMouseExited(e -> chineseBtn.setStyle(buttonStyle));

        Button exitAppButton = new Button(messages.getString("exitApp"));
        exitAppButton.setStyle(buttonStyle);
        exitAppButton.setOnMouseEntered(e -> exitAppButton.setStyle(buttonStyle + hoverStyle));
        exitAppButton.setOnMouseExited(e -> exitAppButton.setStyle(buttonStyle));

        englishBtn.setOnAction(e -> {
            currentLocale = Locale.ENGLISH;
            messages = ResourceBundle.getBundle("cn.zjnu.demos.messages", currentLocale);
            startGameButton.setText(messages.getString("startGame"));
            exitAppButton.setText(messages.getString("exitApp"));
        });

        chineseBtn.setOnAction(e -> {
            currentLocale = Locale.SIMPLIFIED_CHINESE;
            messages = ResourceBundle.getBundle("cn.zjnu.demos.messages", currentLocale);
            startGameButton.setText(messages.getString("startGame"));
            exitAppButton.setText(messages.getString("exitApp"));
        });

        startGameButton.setOnAction(e -> {
            startMainGame();
            languageStage.hide();
        });

        exitAppButton.setOnAction(e -> System.exit(0));

        mainBox.getChildren().addAll(startGameButton, englishBtn, chineseBtn, exitAppButton);
        root.getChildren().add(mainBox);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        languageStage.setScene(scene);
        languageStage.setTitle("Feeding Frenzy - Main Menu");
        languageStage.show();
    }

    public void changeScene(String direction) {
        fishes.clear();
        spawnBalancedFishes(MAX_FISHES);
        switch (direction) {
            case "LEFT" -> {
                player.setX(WIDTH - player.getSize());
                currentScene = (currentScene + 1) % backgrounds.length;
            }
            case "RIGHT" -> {
                player.setX(player.getSize());
                currentScene = (currentScene + 1) % backgrounds.length;
            }
            case "UP" -> {
                player.setY(HEIGHT - player.getSize());
                currentScene = (currentScene + 1) % backgrounds.length;
            }
            case "DOWN" -> {
                player.setY(player.getSize());
                currentScene = (currentScene + 1) % backgrounds.length;
            }
        }
    }
}