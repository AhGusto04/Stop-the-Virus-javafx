// Main.java

package application;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class Main extends Application {

    private HashMap<KeyCode, Boolean> keys = new HashMap<>();
    private ArrayList<Node> platforms = new ArrayList<>();
    private List<FinishLine> finishLines = new ArrayList<>();
    private Pane appRoot = new Pane();
    private Pane gameRoot = new Pane();
    private Pane uiRoot = new Pane();
    private Node player;
    private Point2D playerVelocity = new Point2D(0, 0);
    private boolean canJump = true;
    private int levelWidth;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<double[]> initialEnemyPositions = new ArrayList<>();
    private SpriteAnimator spriteAnimator;
    private PauseManager pauseManager;
    private ImageView backgroundView;
    private ImageView backgroundView2;
    private ImageView backgroundView3;
    private int playerHP = 100;
    private Rectangle hpBar;
    private Text hpText;
    private AudioPlayer audioPlayer;
    private boolean isAttacking = false;
    private boolean isInvincible = false;
    private Timeline invincibilityTimer;
    private List<CannonBall> projectiles = new ArrayList<>();
    private List<Portal> portals = new ArrayList<>();
    private final double Sky_Layer_scroll = 0.3;
    private final double Deep_Layer_scroll = 0.15;
    private final double Shallow_Layer_scroll = 0.18;
    private final String Sky_layer = "Sky_layer.png";
    private final String Deep_layer = "Deep_layer.png";
    private final String Shallow_layer = "Shallow_layer.png";

    @Override
    public void start(Stage primaryStage) throws Exception {
        initContent(); // Initialize game content
        initializeInvincibilityTimer();

        audioPlayer = new AudioPlayer();
        audioPlayer.loadBackgroundMusic("/sprites/ost1.mp3");
        audioPlayer.setVolume(0.5);
        audioPlayer.playBackgroundMusic();

        double windowWidth = 1280; // Game window width
        double windowHeight = 755; // Game window height

        // Root group for the scene
        Group rootGroup = new Group(appRoot);
        Scene scene = new Scene(rootGroup, windowWidth, windowHeight);

        // Configure scene key events
        scene.setOnKeyPressed(event -> keys.put(event.getCode(), true));
        scene.setOnKeyReleased(event -> keys.put(event.getCode(), false));

        // Configure the primary stage
        primaryStage.setTitle("Stop the Virus!");
        primaryStage.setScene(scene);

        // Set windowed mode
        primaryStage.setResizable(false); // Disable resizing if necessary
        primaryStage.setWidth(windowWidth); // Fixed width
        primaryStage.setHeight(windowHeight); // Fixed height
        primaryStage.show();

        // Start game loop
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (pauseManager == null || !pauseManager.isPaused()) {
                    update(); // Call the update method here
                }
            }
        };

        timer.start();

        pauseManager = new PauseManager(uiRoot, scene, timer, keys);
    }

    // Entry point for starting the game
    public void startGame(Stage primaryStage) {
        try {
            start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initContent() {
        // Load and set up parallax backgrounds
        backgroundView = loadBGImage(Sky_layer);
        backgroundView2 = loadBGImage(Deep_layer);
        backgroundView3 = loadBGImage(Shallow_layer);
        loadBG(backgroundView, backgroundView2, backgroundView3);
        levelWidth = LevelData.Level1[0].length() * 60;

        for (int i = 0; i < LevelData.Level1.length; i++) {
            String line = LevelData.Level1[i];
            for (int j = 0; j < line.length(); j++) {
                switch (line.charAt(j)) {
                    case '0':
                        // Empty space, do nothing
                        break;
                    case '1':
                        // Create a block platform
                        Node platform = createEntity(j * 60, i * 60, 60, 60, "/sprites/blocktexture.png");
                        platforms.add(platform);
                        break;
                    case '2': // Create a regular enemy
                        double enemyX = j * 60;
                        double enemyY = i * 60;
                        Enemy enemy = new Enemy("/sprites/enemy1.png", enemyX, enemyY, 60, 60, platforms);
                        enemies.add(enemy);
                        initialEnemyPositions.add(new double[]{enemyX, enemyY, 60, 60, 1}); // Add 1 for regular enemy
                        gameRoot.getChildren().add(enemy.getView());
                        break;
                    case '3': // Create an EnemyCannon
                        double cannonX = j * 60;
                        double cannonY = i * 60;
                        EnemyCannon cannon = new EnemyCannon(
                            "/sprites/StopVirus_EnemyCannon.png",
                            cannonX,
                            cannonY,
                            60,
                            60,
                            projectiles,
                            gameRoot
                        );
                        cannon.setPlayer((ImageView) player); // Set the player reference
                        enemies.add(cannon);
                        initialEnemyPositions.add(new double[]{cannonX, cannonY, 60, 60, 2}); // Add 2 for EnemyCannon
                        gameRoot.getChildren().add(cannon.getView());
                        break;
                    case '4': // Add flying enemy
                        double flyingEnemyX = j * 60;
                        double flyingEnemyY = i * 60;
                        FlyingEnemy flyingEnemy = new FlyingEnemy(
                            "/sprites/StopVirus_Flying-Enemy.gif",
                            flyingEnemyX,
                            flyingEnemyY,
                            60,
                            60,
                            gameRoot
                        );
                        enemies.add(flyingEnemy); // Add to the enemy list
                        initialEnemyPositions.add(new double[]{flyingEnemyX, flyingEnemyY, 60, 60, 3}); // Add 3 for FlyingEnemy
                        gameRoot.getChildren().add(flyingEnemy.getView());
                        break;
                    case '5': // Create a portal
                        Portal portal = new Portal("Level2", j * 60, i * 60); // Link to Level2
                        gameRoot.getChildren().add(portal.getView());
                        portals.add(portal); // Add to the list of portals
                        break;
                    case '6': // Finish Line
                        FinishLine finishLine = new FinishLine(j * 60, i * 60);
                        gameRoot.getChildren().add(finishLine.getView());
                        finishLines.add(finishLine); // Add to a list of finish lines
                        break;
                }
            }
        }

        // Create the player
        player = createPlayerWithImage(0, 600, 40, 40);
        spriteAnimator = new SpriteAnimator((ImageView) player);

        player.translateXProperty().addListener((obs, old, newValue) -> {
            int offset = newValue.intValue();
            if (offset > 640 && offset < levelWidth - 640) {
                gameRoot.setLayoutX(-(offset - 640));
                // Update background layers for parallax
                backgroundView.setTranslateX(-(offset - 640) * Sky_Layer_scroll);
                backgroundView2.setTranslateX(-(offset - 640) * Deep_Layer_scroll);
                backgroundView3.setTranslateX(-(offset - 640) * Shallow_Layer_scroll);
            }
        });

        // Add UI elements
        hpBar = new Rectangle(200, 20, Color.RED);
        hpBar.setTranslateX(20);
        hpBar.setTranslateY(20);

        hpText = new Text("HP: 100");
        hpText.setFont(Font.font(18));
        hpText.setFill(Color.WHITE);
        hpText.setTranslateX(20);
        hpText.setTranslateY(55);

        uiRoot.getChildren().addAll(hpBar, hpText);
        appRoot.getChildren().addAll(gameRoot, uiRoot);
    }

    private void update() {
        boolean movingRight = true;
        boolean moving = false;

        // Handle player movement
        if (isPressed(KeyCode.W) && player.getTranslateY() >= 5) {
            jumpPlayer(); // Trigger jump logic
        }
        if (isPressed(KeyCode.A) && player.getTranslateX() >= 5) {
            movePlayerX(-5); // Move left
            moving = true;
            movingRight = false; // Facing left
        }
        if (isPressed(KeyCode.D) && player.getTranslateX() + 40 <= levelWidth - 5) {
            movePlayerX(5); // Move right
            moving = true;
            movingRight = true; // Facing right
        }

        // Handle walking animation
        if (moving && !spriteAnimator.isAttacking() && !spriteAnimator.isDownwardAttack()) {
            spriteAnimator.startWalking(movingRight);
        } else if (!moving && !spriteAnimator.isJumping() && !spriteAnimator.isAttacking()) {
            spriteAnimator.stopWalking();
        }

        // Handle attack logic and animations
        if (isPressed(KeyCode.SPACE)) {
            if (!isAttacking) { // Trigger attack hitbox once per attack
                isAttacking = true;
                createAttackHitbox();
            }
            spriteAnimator.startAttack();
        } else {
            isAttacking = false;
            spriteAnimator.stopAttack();
        }

        // Handle downward attack
        if (isPressed(KeyCode.S) && !canJump) { // Trigger only in mid-air
            if (!isAttacking) { // Ensure downward attack hitbox is created once
                isAttacking = true;
                createDownwardAttackHitbox();
            }
            spriteAnimator.startDownwardAttack();
        }

        // Stop downward attack when player lands
        if (canJump && spriteAnimator.isDownwardAttack()) {
            spriteAnimator.finishDownwardAttack();
            isAttacking = false; // Reset attack state
        }

        // Apply gravity
        if (playerVelocity.getY() < 10) {
            playerVelocity = playerVelocity.add(0, 1);
        }
        movePlayerY((int) playerVelocity.getY());

        // Check if the player falls out of bounds
        if (player.getTranslateY() > 1000) {
            decreaseHP(4); // Apply damage for falling out of bounds
        }

        // Define camera range for activation
        double cameraLeft = -gameRoot.getLayoutX();
        double cameraRight = cameraLeft + 1280;

        // Update enemies and check for collisions with the player
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                continue; // Skip dead enemies
            }

            // Check collision with the player
            if (enemy.getView().getBoundsInParent().intersects(player.getBoundsInParent())) {
                takeDamageFromEnemy(10); // Inflict damage to the player
            }

            if (enemy instanceof EnemyCannon) {
                EnemyCannon cannon = (EnemyCannon) enemy;
                cannon.getView().setScaleX(player.getTranslateX() > cannon.getView().getTranslateX() ? 1 : -1);
            }

            enemy.move(cameraLeft, cameraRight); // Move enemy within active range
        }

        // Update projectiles
        updateCannonBalls();

        // Check for portal collision
        for (Portal portal : portals) {
            if (portal.checkCollision((ImageView) player)) {
                switchToLevel(portal.getTargetLevel());
                return; // Exit the update loop to prevent unnecessary processing
            }
        }
        
        for (FinishLine finishLine : finishLines) {
            if (finishLine.checkCollision((ImageView) player)) {
                showVictoryMessage();
                return; // Exit the update loop to prevent further processing
            }
        }

        // Check if the player is dead
        if (playerHP <= 0) {
            resetGame();
            return; // Skip further updates after resetting
        }

        // Handle attacks (regular and downward)
        handleAttack();
    }

    // New method for updating cannonballs
    private void updateCannonBalls() {
    	// Handle projectiles (cannonballs)
    	// Handle projectiles (cannonballs)
    	List<CannonBall> toRemove = new ArrayList<>();
    	for (CannonBall cannonBall : projectiles) {
    	    cannonBall.updatePosition();

    	    // Check collision with player
    	    if (cannonBall.checkCollision((ImageView) player)) {
    	        takeDamageFromEnemy(20); // Cannonball deals 20 damage
    	        toRemove.add(cannonBall);
    	    }

    	    // Remove cannonball if it moves off-screen or is destroyed
    	    if (!cannonBall.isAlive()) {
    	        toRemove.add(cannonBall);
    	    }
    	}

    	// Remove destroyed projectiles
    	projectiles.removeAll(toRemove);
    	gameRoot.getChildren().removeAll(
    	    toRemove.stream().map(CannonBall::getView).toList()
    	);
    }
    
    private void handleAttack() {
        // Regular attack with SPACE key
        if (isPressed(KeyCode.SPACE) && !spriteAnimator.isAttacking()) {
            spriteAnimator.startAttack();
        } else if (!isPressed(KeyCode.SPACE)) {
            spriteAnimator.stopAttack(); // Stop attack when key is released
        }

        // Downward attack with S key
        if (isPressed(KeyCode.S) && !canJump && !spriteAnimator.isAttacking()) {
            spriteAnimator.startDownwardAttack();
        }

        // Ensure downward attack transitions correctly when the player lands
        if (canJump && spriteAnimator.isAttacking() && spriteAnimator.isDownwardAttack()) {
            spriteAnimator.finishDownwardAttack();
        }
    }

    private void createDownwardAttackHitbox() {
        double playerWidth = player.getBoundsInParent().getWidth();
        double playerHeight = player.getBoundsInParent().getHeight();

        double hitboxWidth = playerWidth * 0.5; // Adjust hitbox size
        double hitboxHeight = playerHeight * 0.3;

        double playerX = player.getTranslateX();
        double playerY = player.getTranslateY();
        Rectangle downwardHitbox = new Rectangle(
            playerX + (playerWidth - hitboxWidth) / 2,
            playerY + playerHeight,
            hitboxWidth,
            hitboxHeight
        );

        // Check collision with enemies
        for (Enemy enemy : new ArrayList<>(enemies)) {
            if (downwardHitbox.getBoundsInParent().intersects(enemy.getView().getBoundsInParent())) {
                enemy.takeDamage(50);

                // Bounce logic
                playerVelocity = new Point2D(playerVelocity.getX(), -29); // Bounce upward
                canJump = false; // Temporarily disable jumping

                if (!enemy.isAlive()) { // Check if the enemy is killed
                    spriteAnimator.playDeathAnimation((ImageView) enemy.getView());
                    audioPlayer.playEnemyDeathSound();

                    // Schedule enemy removal
                    Timeline removeEnemy = new Timeline(new KeyFrame(
                        Duration.seconds(1),
                        evt -> {
                            gameRoot.getChildren().remove(enemy.getView());
                            enemies.remove(enemy);
                        }
                    ));
                    removeEnemy.setCycleCount(1);
                    removeEnemy.play();
                }
            }
        }

        // Check collision with cannonballs
        List<CannonBall> toRemove = new ArrayList<>();
        for (CannonBall cannonBall : new ArrayList<>(projectiles)) {
            if (downwardHitbox.getBoundsInParent().intersects(cannonBall.getView().getBoundsInParent())) {
                cannonBall.takeDamage(50); // Destroy the cannonball
                playerVelocity = new Point2D(playerVelocity.getX(), -29); // Bounce upward
                canJump = false;

                if (!cannonBall.isAlive()) {
                    toRemove.add(cannonBall);
                }
            }
        }

        // Remove destroyed cannonballs
        projectiles.removeAll(toRemove);
        gameRoot.getChildren().removeAll(
            toRemove.stream().map(CannonBall::getView).toList()
        );
    }

    private void createAttackHitbox() {
        double playerX = player.getTranslateX();
        double playerY = player.getTranslateY();
        double offsetX = spriteAnimator.isFacingRight() ? 40 : -40;

        Rectangle hitbox = new Rectangle(playerX + offsetX, playerY, 40, 40);

        // Check collision with enemies
        for (Enemy enemy : new ArrayList<>(enemies)) {
            if (hitbox.getBoundsInParent().intersects(enemy.getView().getBoundsInParent())) {
                enemy.takeDamage(50);
                if (!enemy.isAlive()) {
                    spriteAnimator.playDeathAnimation((ImageView) enemy.getView());
                    audioPlayer.playEnemyDeathSound();

                    Timeline removeEnemy = new Timeline(new KeyFrame(
                        Duration.seconds(1),
                        evt -> {
                            gameRoot.getChildren().remove(enemy.getView());
                            enemies.remove(enemy);
                        }
                    ));
                    removeEnemy.setCycleCount(1);
                    removeEnemy.play();
                }
            }
        }

        // Check collision with cannonballs
        List<CannonBall> toRemove = new ArrayList<>();
        for (CannonBall cannonBall : new ArrayList<>(projectiles)) {
            if (hitbox.getBoundsInParent().intersects(cannonBall.getView().getBoundsInParent())) {
                cannonBall.takeDamage(50); // Apply damage to the cannonball
                if (!cannonBall.isAlive()) {
                    toRemove.add(cannonBall);
                }
            }
        }

        // Remove destroyed cannonballs
        projectiles.removeAll(toRemove);
        gameRoot.getChildren().removeAll(
            toRemove.stream().map(CannonBall::getView).toList()
        );
    }


    private void takeDamageFromEnemy(int damage) {
        if (!isInvincible) {
            decreaseHP(damage);
            spriteAnimator.startInvincibilityEffect();
            isInvincible = true;
            invincibilityTimer.playFromStart();

            double bounceDistance = spriteAnimator.isFacingRight() ? -30 : 30;
            player.setTranslateX(player.getTranslateX() + bounceDistance);
        }
    }

    private void initializeInvincibilityTimer() {
        invincibilityTimer = new Timeline(new KeyFrame(Duration.seconds(2), event -> endInvincibility()));
        invincibilityTimer.setCycleCount(1);
    }

    private void endInvincibility() {
        isInvincible = false;
        spriteAnimator.stopInvincibilityEffect();
    }

    private void decreaseHP(int amount) {
        playerHP -= amount;
        if (playerHP < 0) playerHP = 0;

        hpBar.setWidth(200 * (playerHP / 100.0));
        hpText.setText("HP: " + playerHP);
    }

    private void resetGame() {
        // Play player death sound
        if (playerHP <= 0 && audioPlayer != null) {
            audioPlayer.playPlayerDeathSound(); // Ensure this method exists in AudioPlayer
        }

        // Reset player properties
        player.setTranslateX(0);
        player.setTranslateY(600);
        playerVelocity = new Point2D(0, 0);
        canJump = true;
        playerHP = 100;

        // Reset HP bar and text display
        hpBar.setWidth(200);
        hpText.setText("HP: 100");

        // Reset game layout and background position
        gameRoot.setLayoutX(0);
        backgroundView.setTranslateX(0);
        backgroundView2.setTranslateX(0);
        backgroundView3.setTranslateX(0);

        // Stop timers and remove all current enemies
        for (Enemy enemy : enemies) {
            if (enemy instanceof EnemyCannon) {
                ((EnemyCannon) enemy).stopShootTimer(); // Stop the shooting timer
            }
            gameRoot.getChildren().remove(enemy.getView()); // Remove enemy view from the stage
        }
        enemies.clear(); // Clear the list of enemies

        // Remove all current cannonballs from the game root
        for (CannonBall cannonBall : projectiles) {
            gameRoot.getChildren().remove(cannonBall.getView());
        }
        projectiles.clear(); // Clear the list of projectiles

        // Recreate and reposition each enemy using initial positions
        for (double[] pos : initialEnemyPositions) {
            Enemy enemy;
            switch ((int) pos[4]) {
                case 1: // Regular enemy
                    enemy = new Enemy("/sprites/enemy1.png", pos[0], pos[1], (int) pos[2], (int) pos[3], platforms);
                    break;
                case 2: // Cannon enemy
                    EnemyCannon cannon = new EnemyCannon(
                        "/sprites/StopVirus_EnemyCannon.png",
                        pos[0],
                        pos[1],
                        (int) pos[2],
                        (int) pos[3],
                        projectiles,
                        gameRoot
                    );
                    cannon.setPlayer((ImageView) player); // Update the player reference
                    cannon.setActive(false); // Ensure cannons are inactive initially
                    enemy = cannon;
                    break;
                case 3: // Flying enemy
                    FlyingEnemy flyingEnemy = new FlyingEnemy(
                        "/sprites/StopVirus_Flying-Enemy.gif",
                        pos[0],
                        pos[1],
                        (int) pos[2],
                        (int) pos[3],
                        gameRoot
                    );
                    enemy = flyingEnemy;
                    break;
                default:
                    throw new IllegalStateException("Unknown enemy type in initialEnemyPositions");
            }
            enemies.add(enemy); // Add the recreated enemy to the enemies list
            gameRoot.getChildren().add(enemy.getView()); // Add the enemy view back to the game root
        }

        // Reset player to Stage 1
        switchToLevel("Level1");
    }


    private void movePlayerX(int value) {
        boolean movingRight = value > 0;

        for (int i = 0; i < Math.abs(value); i++) {
            player.setTranslateX(player.getTranslateX() + (movingRight ? 1 : -1));

            boolean collisionDetected = false;
            for (Node platform : platforms) {
                if (player.getBoundsInParent().intersects(platform.getBoundsInParent())) {
                    double playerMinY = player.getBoundsInParent().getMinY();
                    double playerMaxY = player.getBoundsInParent().getMaxY();
                    double platformMinY = platform.getBoundsInParent().getMinY();
                    double platformMaxY = platform.getBoundsInParent().getMaxY();

                    if (playerMaxY > platformMinY && playerMinY < platformMaxY) {
                        collisionDetected = true;
                        break;
                    }
                }
            }

            if (collisionDetected) {
                player.setTranslateX(player.getTranslateX() + (movingRight ? -1 : 1));
                return;
            }
        }
    }

    private void movePlayerY(int value) {
        boolean movingDown = value > 0;

        for (int i = 0; i < Math.abs(value); i++) {
            for (Node platform : platforms) {
                if (player.getBoundsInParent().intersects(platform.getBoundsInParent())) {
                    if (movingDown) {
                        if (player.getTranslateY() + player.getBoundsInParent().getHeight() == platform.getTranslateY()) {
                            canJump = true;
                            spriteAnimator.stopJumping();
                            return;
                        }
                    } else {
                        if (player.getTranslateY() == platform.getTranslateY() + platform.getBoundsInParent().getHeight()) {
                            return;
                        }
                    }
                }
            }
            player.setTranslateY(player.getTranslateY() + (movingDown ? 1 : -1));
        }
    }

    private void jumpPlayer() {
        if (canJump) {
            playerVelocity = playerVelocity.add(0, -35);
            spriteAnimator.startJumping();
            canJump = false;
        }
    }

    private Node createPlayerWithImage(int x, int y, int w, int h) {
        String resourcePath = "/sprites/player-idle.png";
        try {
            Image playerImage = new Image(getClass().getResourceAsStream(resourcePath), w * 1.3, h * 1.3, false, true); // Scale by 30%
            ImageView playerView = new ImageView(playerImage);
            playerView.setTranslateX(x);
            playerView.setTranslateY(y);

            playerView.setFitWidth(w * 1.3); // Adjust the width
            playerView.setFitHeight(h * 1.3); // Adjust the height
            playerView.setScaleX(1);
            playerView.setScaleY(1);

            gameRoot.getChildren().add(playerView);
            return playerView;
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
            return null;
        }
    }
    
    private void switchToLevel(String levelName) {
        // Stop timers for all existing EnemyCannon objects
        for (Enemy enemy : enemies) {
            if (enemy instanceof EnemyCannon) {
                ((EnemyCannon) enemy).stopShootTimer();
            }
        }

        // Clear current game elements
        gameRoot.getChildren().clear();
        portals.clear();
        enemies.clear();
        projectiles.clear();
        platforms.clear();

        // Re-add the backgrounds for the new level
        backgroundView = loadBGImage(Sky_layer);
        backgroundView2 = loadBGImage(Deep_layer);
        backgroundView3 = loadBGImage(Shallow_layer);
        loadBG(backgroundView, backgroundView2, backgroundView3);

        // Re-load the specified level
        switch (levelName) {
            case "Level1":
                loadLevel(LevelData.Level1, "Level2"); // Provide next level for portals
                break;
            case "Level2":
                loadLevel(LevelData.Level2, "Level3"); // Provide next level for portals
                break;
            case "Level3":
                loadLevel(LevelData.Level3, "Level4"); // Provide next level for portals
                break;
            case "Level4":
                loadLevel(LevelData.Level4, "Level5"); // Provide next level for portals
                break;
            case "Level5":
                loadLevel(LevelData.Level5, null); // Final level
                break;
            default:
                throw new IllegalArgumentException("Unknown level: " + levelName);
        }

        // Reset player position
        player.setTranslateX(0);
        player.setTranslateY(600);
        playerVelocity = new Point2D(0, 0);
        canJump = true;

        // Re-add the player to the gameRoot
        gameRoot.getChildren().add(player);

        // Reset camera and background position
        gameRoot.setLayoutX(0);
        backgroundView.setTranslateX(0);
        backgroundView2.setTranslateX(0);
        backgroundView3.setTranslateX(0);

        // Restart timers for EnemyCannon instances in the new level
        for (Enemy enemy : enemies) {
            if (enemy instanceof EnemyCannon) {
                ((EnemyCannon) enemy).setPlayer((ImageView) player); // Update the player reference
                ((EnemyCannon) enemy).restartShootTimer(); // Restart shoot timer
            }
        }
    }

    private void loadLevel(String[] levelData, String nextLevel) {
        for (int i = 0; i < levelData.length; i++) {
            String line = levelData[i];
            for (int j = 0; j < line.length(); j++) {
                switch (line.charAt(j)) {
                    case '0':
                        // Empty space
                        break;
                    case '1':
                        Node platform = createEntity(j * 60, i * 60, 60, 60, "/sprites/blocktexture.png");
                        platforms.add(platform);
                        break;
                    case '2': // Regular enemy
                        Enemy enemy = new Enemy("/sprites/enemy1.png", j * 60, i * 60, 60, 60, platforms);
                        enemies.add(enemy);
                        gameRoot.getChildren().add(enemy.getView());
                        break;
                    case '3': // EnemyCannon
                        EnemyCannon cannon = new EnemyCannon(
                            "/sprites/StopVirus_EnemyCannon.png",
                            j * 60,
                            i * 60,
                            60,
                            60,
                            projectiles,
                            gameRoot
                        );
                        cannon.setPlayer((ImageView) player);
                        enemies.add(cannon);
                        gameRoot.getChildren().add(cannon.getView());
                        break;
                    case '4': // Add flying enemy
                        FlyingEnemy flyingEnemy = new FlyingEnemy(
                            "/sprites/StopVirus_Flying-Enemy.gif",
                            j * 60,
                            i * 60,
                            60,
                            60,
                            gameRoot
                        );
                        enemies.add(flyingEnemy); // Add to the enemy list
                        gameRoot.getChildren().add(flyingEnemy.getView());
                        break;
                    case '5': // Portal
                        String targetLevel = nextLevel != null ? nextLevel : "Level1";
                        Portal portal = new Portal(targetLevel, j * 60, i * 60);
                        gameRoot.getChildren().add(portal.getView());
                        portals.add(portal);
                        break;
                    case '6': // Finish Line
                        FinishLine finishLine = new FinishLine(j * 60, i * 60);
                        gameRoot.getChildren().add(finishLine.getView());
                        finishLines.add(finishLine); // Add to a list of finish lines
                        break;
                }
            }
        }
    }

    private Node createEntity(int x, int y, int w, int h, String imagePath) {
        Image image = new Image(getClass().getResourceAsStream(imagePath));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(w);
        imageView.setFitHeight(h);
        imageView.setTranslateX(x);
        imageView.setTranslateY(y);
        gameRoot.getChildren().add(imageView);
        return imageView;
    }
    
    private ImageView loadBGImage(String path) {
        try {
            Image background = new Image(getClass().getResourceAsStream("/sprites/" + path), 4020, 1080, false, true);
            return new ImageView(background);
        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());
            return null;
        }
    }

    private void loadBG(ImageView image, ImageView image2, ImageView image3) {
        gameRoot.getChildren().add(0, image);
        gameRoot.getChildren().add(1, image2);
        gameRoot.getChildren().add(2, image3);
    }
    
    private void showVictoryMessage() {
        // Pause the game
        if (pauseManager != null) {
            pauseManager.pauseGame(); // Use the existing pause logic
        }

        // Create and display the victory message
        Text victoryText = new Text("You have successfully taken control over this computer.\nThank you for playing!");
        victoryText.setFont(Font.font("Arial", 36));
        victoryText.setFill(Color.WHITE);
        victoryText.setTranslateX(200); // Position at the center of the screen
        victoryText.setTranslateY(300);

        uiRoot.getChildren().add(victoryText);
    }


    private boolean isPressed(KeyCode key) {
        return keys.getOrDefault(key, false);
    }

    @Override
    public void stop() {
        if (audioPlayer != null) {
            audioPlayer.stopBackgroundMusic();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}