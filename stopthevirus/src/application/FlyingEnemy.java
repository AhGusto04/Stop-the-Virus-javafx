package application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.scene.layout.Pane;

public class FlyingEnemy extends Enemy {

    private final ImageView enemyView;
    private final Pane gameRoot; // Reference to the game root

    public FlyingEnemy(String imagePath, double x, double y, int width, int height, Pane gameRoot) {
        super(imagePath, x, y, width, height, null); // No gravity or platforms required

        // Load and scale the flying enemy image
        Image flyingEnemyImage = new Image(getClass().getResourceAsStream("/sprites/StopVirus_Flying-Enemy.gif"), width, height, false, true);
        this.enemyView = new ImageView(flyingEnemyImage);
        this.enemyView.setTranslateX(x);
        this.enemyView.setTranslateY(y);

        this.gameRoot = gameRoot; // Assign the game root reference
    }

    @Override
    public ImageView getView() {
        return enemyView;
    }

    @Override
    public void applyGravity() {
        // No gravity effect for flying enemies
    }

    @Override
    public void move(double cameraLeft, double cameraRight) {
        // FlyingEnemy doesn't move
    }

    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);

        if (!isAlive()) {
            playDeathAnimation(); // Play death animation
        }
    }

    private void playDeathAnimation() {
        // Play death animation using SpriteAnimator's logic
        Image[] explosionFrames = new Image[12];
        for (int i = 0; i < 12; i++) {
            explosionFrames[i] = new Image(getClass().getResourceAsStream(
                "/sprites/explosion-d" + (i + 1) + ".png"));
        }

        Timeline deathAnimation = new Timeline();
        for (int i = 0; i < 12; i++) {
            final int frameIndex = i;
            deathAnimation.getKeyFrames().add(new KeyFrame(
                Duration.seconds(i * (0.8 / 12)),
                event -> enemyView.setImage(explosionFrames[frameIndex])
            ));
        }

        deathAnimation.setCycleCount(1);
        deathAnimation.setOnFinished(event -> gameRoot.getChildren().remove(enemyView));
        deathAnimation.play();
    }
}
