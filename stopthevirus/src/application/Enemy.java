// Enemy.java

package application;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

public class Enemy {

    protected ImageView enemyImageView; // The visual representation of the enemy
    protected Point2D velocity = new Point2D(0, 0); // Velocity for applying gravity
    protected boolean isDead = false; // Tracks if the enemy is dead
    protected boolean isActive = false; // Enemy starts inactive by default
    protected List<Node> platforms; // List of platforms for collision detection

    private int health; // Enemy's health
    private boolean movingRight; // Tracks the direction of movement
    private final double speed = 1.5; // Horizontal movement speed

    public Enemy(String imagePath, double x, double y, int width, int height, List<Node> platforms) {
        this.health = 50; // Initial health
        this.movingRight = false; // Default direction is left
        this.platforms = platforms; // Platforms for collision detection

        // Initialize the enemy's image
        Image enemyImage = new Image(getClass().getResourceAsStream(imagePath), width, height, false, true);
        this.enemyImageView = new ImageView(enemyImage);
        this.enemyImageView.setTranslateX(x);
        this.enemyImageView.setTranslateY(y);
        this.enemyImageView.setFitWidth(width);
        this.enemyImageView.setFitHeight(height);
    }

    public ImageView getView() {
        return enemyImageView;
    }

    public void applyGravity() {
        if (!isActive || isDead) return; // Skip logic if inactive or dead
        velocity = velocity.add(0, 0.5); // Gravity effect
        enemyImageView.setTranslateY(enemyImageView.getTranslateY() + velocity.getY());

        // Stop falling if the enemy intersects a platform
        for (Node platform : platforms) {
            Bounds platformBounds = platform.getBoundsInParent();
            Bounds enemyBounds = enemyImageView.getBoundsInParent();
            if (enemyBounds.intersects(platformBounds)) {
                stopFalling();
                enemyImageView.setTranslateY(platformBounds.getMinY() - enemyImageView.getFitHeight());
                break;
            }
        }
    }

    public void stopFalling() {
        velocity = new Point2D(0, 0);
    }

    public void move(double cameraLeft, double cameraRight) {
        if (isDead) return; // Stop all movement if dead

        // Activate the enemy if within camera range
        if (!isActive &&
            enemyImageView.getBoundsInParent().getMaxX() >= cameraLeft &&
            enemyImageView.getBoundsInParent().getMinX() <= cameraRight) {
            isActive = true;
        }

        if (!isActive) return; // Skip movement if not active

        double dx = movingRight ? speed : -speed; // Movement direction
        double nextX = enemyImageView.getTranslateX() + dx;

        // Predict the enemy's next position bounds
        double futureLeft = nextX;
        double futureRight = nextX + enemyImageView.getFitWidth();

        // Horizontal collision detection
        boolean horizontalCollision = false;
        for (Node platform : platforms) {
            Bounds platformBounds = platform.getBoundsInParent();
            Bounds enemyBounds = enemyImageView.getBoundsInParent();

            if (futureRight > platformBounds.getMinX() &&
                futureLeft < platformBounds.getMaxX() &&
                enemyBounds.getMaxY() > platformBounds.getMinY() &&
                enemyBounds.getMinY() < platformBounds.getMaxY()) {
                horizontalCollision = true;
                break;
            }
        }

        if (horizontalCollision) {
            changeDirection(); // Turn around on collision
            return; // Stop further movement in this direction
        }

        // Check if there is a floor under the enemy's next position
        boolean onPlatform = false;
        for (Node platform : platforms) {
            Bounds platformBounds = platform.getBoundsInParent();
            double enemyBottom = enemyImageView.getBoundsInParent().getMaxY();

            if (futureRight > platformBounds.getMinX() &&
                futureLeft < platformBounds.getMaxX() &&
                enemyBottom >= platformBounds.getMinY() &&
                enemyBottom <= platformBounds.getMinY() + 5) {
                onPlatform = true;
                break;
            }
        }

        if (!onPlatform) {
            changeDirection(); // Turn around at the edge of a platform
        } else {
            enemyImageView.setTranslateX(nextX); // Move the enemy
        }
    }

    public void changeDirection() {
        if (isDead) return; // Skip logic if dead
        movingRight = !movingRight; // Toggle direction
        enemyImageView.setScaleX(movingRight ? 1 : -1); // Flip the image horizontally
    }

    public void takeDamage(int damage) {
        if (!isDead) {
            health -= damage;
            if (health <= 0) {
                kill(); // Kill the enemy if health drops to zero
            }
        }
    }

    public void kill() {
        this.isDead = true;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isActive() {
        return isActive;
    }
}
