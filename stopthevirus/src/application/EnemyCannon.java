package application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import java.util.List;

public class EnemyCannon extends Enemy {

    private static final int SHOOT_INTERVAL = 3; // Seconds
    private final ImageView cannonImageView;
    private Timeline shootTimer;
    private final List<CannonBall> projectiles;
    private final Pane gameRoot; // Reference to the game root for cannonballs
    private ImageView player; // Reference to the player for orientation logic
    private boolean active = false; // Tracks if the cannon is active

    public EnemyCannon(String imagePath, double x, double y, int width, int height, List<CannonBall> projectiles, Pane gameRoot) {
        super(imagePath, x, y, width, height, null); // Pass null for platforms since it doesn't collide
        this.projectiles = projectiles;
        this.gameRoot = gameRoot;

        // Initialize the cannon's image view
        Image cannonImage = new Image(getClass().getResourceAsStream(imagePath), width, height, false, true);
        this.cannonImageView = new ImageView(cannonImage);
        this.cannonImageView.setTranslateX(x);
        this.cannonImageView.setTranslateY(y);
        this.cannonImageView.setFitWidth(60); // Match width
        this.cannonImageView.setFitHeight(60); // Match height

        // Always face left by default
        this.cannonImageView.setScaleX(-1);

        // Initialize the shoot timer but do not start it immediately
        initializeShootTimer();
    }

    public void setPlayer(ImageView player) {
        this.player = player; // Assign the player reference
    }

    private void initializeShootTimer() {
        shootTimer = new Timeline(new KeyFrame(Duration.seconds(SHOOT_INTERVAL), event -> {
            if (active) {
                shootCannonball();
            }
        }));
        shootTimer.setCycleCount(Timeline.INDEFINITE);
        shootTimer.play();
    }

    public void stopShootTimer() {
        if (shootTimer != null) {
            shootTimer.stop();
            shootTimer = null; // Clear the timer reference
        }
    }

    public void restartShootTimer() {
        stopShootTimer(); // Stop any existing timer to avoid duplication
        initializeShootTimer(); // Restart the timer
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public ImageView getView() {
        return cannonImageView;
    }

    private void shootCannonball() {
        // Only shoot if the cannon is active and on-screen
        Bounds cannonBounds = cannonImageView.getBoundsInParent();
        if (cannonBounds.getMaxX() < 0 || cannonBounds.getMinX() > 4000) {
            return;
        }

        // Spawn a cannonball firing in the current direction
        double launchX = cannonImageView.getTranslateX() + (cannonImageView.getScaleX() > 0 ? 20 : -20); // Adjust for direction
        double launchY = cannonImageView.getTranslateY() + cannonImageView.getFitHeight() / 4;

        CannonBall cannonBall = new CannonBall(
            "/sprites/StopVirus_CannonBall.gif",
            launchX,
            launchY,
            cannonImageView.getScaleX() > 0 ? 5 : -5, // Speed based on direction
            50 // Initial HP
        );

        projectiles.add(cannonBall);

        // Add the cannonball to the game root so it's visible
        gameRoot.getChildren().add(cannonBall.getView());
    }

    @Override
    public void applyGravity() {
        // No gravity for the cannon, it stays in its initial position
    }

    @Override
    public void move(double cameraLeft, double cameraRight) {
        // Activate only when within camera bounds
        if (!active &&
            cannonImageView.getBoundsInParent().getMaxX() >= cameraLeft &&
            cannonImageView.getBoundsInParent().getMinX() <= cameraRight) {
            setActive(true);
        } else if (active &&
                   (cannonImageView.getBoundsInParent().getMaxX() < cameraLeft ||
                    cannonImageView.getBoundsInParent().getMinX() > cameraRight)) {
            setActive(false); // Deactivate if out of bounds
        }

        // Ensure the cannon faces the player
        if (player != null) {
            double playerX = player.getBoundsInParent().getMinX();
            double cannonX = cannonImageView.getBoundsInParent().getMinX();
            cannonImageView.setScaleX(playerX > cannonX ? 1 : -1); // Flip based on player's position
        }
    }

    @Override
    public void takeDamage(int damage) {
        // Do nothing; cannon is immune to attacks
    }

    @Override
    public void kill() {
        // Do nothing; cannon cannot be killed
    }
}
