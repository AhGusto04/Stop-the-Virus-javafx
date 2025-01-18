package application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.geometry.Bounds;
import javafx.util.Duration;

public class CannonBall {

    private final ImageView cannonBallView;
    private final double speed; // Speed of the cannonball
    private int health; // HP of the cannonball
    private boolean isAlive;
    private boolean isExploding; // Flag to prevent interaction during explosion
    private Timeline lifetimeTimer; // Timer to track the cannonball's lifetime

    public CannonBall(String imagePath, double x, double y, double speed, int health) {
        this.speed = speed;
        this.health = health;
        this.isAlive = true;
        this.isExploding = false;

        // Load and scale the cannonball image
        Image cannonBallImage = new Image(getClass().getResourceAsStream(imagePath), 50, 50, false, true);
        this.cannonBallView = new ImageView(cannonBallImage);
        this.cannonBallView.setTranslateX(x);
        this.cannonBallView.setTranslateY(y);

        // Flip the cannonball image if it moves to the right
        this.cannonBallView.setScaleX(speed > 0 ? 1 : -1);

        // Start lifetime timer
        startLifetimeTimer();
    }

    private void startLifetimeTimer() {
        lifetimeTimer = new Timeline(new KeyFrame(Duration.seconds(4), event -> destroy()));
        lifetimeTimer.setCycleCount(1);
        lifetimeTimer.play();
    }

    public ImageView getView() {
        return cannonBallView;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void takeDamage(int damage) {
        if (!isAlive || isExploding) return; // Skip damage if already destroyed or exploding
        health -= damage;
        if (health <= 0) {
            destroy();
        }
    }

    private void destroy() {
        if (!isAlive || isExploding) return; // Prevent multiple triggers
        isExploding = true; // Set the exploding state

        if (lifetimeTimer != null) {
            lifetimeTimer.stop();
        }

        // Show the explosion animation
        playDeathAnimation();
    }

    private void playDeathAnimation() {
        // Load the death animation gif
        Image deathAnimation = new Image(getClass().getResourceAsStream("/sprites/balldeath.gif"), 50, 50, false, true);
        cannonBallView.setImage(deathAnimation);

        // Create a timeline to remove the cannonball after the animation
        Timeline removeAnimation = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            isAlive = false; // Mark the cannonball as fully dead
            Pane parent = (Pane) cannonBallView.getParent();
            if (parent != null) {
                parent.getChildren().remove(cannonBallView);
            }
        }));
        removeAnimation.setCycleCount(1);
        removeAnimation.play();
    }

    public void updatePosition() {
        if (!isAlive || isExploding) return; // Stop movement if destroyed or exploding

        // Move the cannonball horizontally
        cannonBallView.setTranslateX(cannonBallView.getTranslateX() + speed);
    }

    public boolean checkCollision(ImageView player) {
        if (!isAlive || isExploding) return false; // No collision during explosion or after death

        Bounds cannonBallBounds = cannonBallView.getBoundsInParent();
        Bounds playerBounds = player.getBoundsInParent();

        return cannonBallBounds.intersects(playerBounds);
    }
}
