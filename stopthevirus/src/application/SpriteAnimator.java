package application;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class SpriteAnimator {

    private final ImageView playerImageView;
    private final Image walk1;
    private final Image walk2;
    private final Image jumpImage;
    private final Image idleImage;
    private final Image[] attackImages;
    private final Image downAttackImage;

    private final Timeline walkAnimation;
    private final Timeline attackAnimation;
    private final Timeline invincibilityFlash;

    private boolean isWalking = false;
    private boolean isAttacking = false;
    private boolean isJumping = false;
    private boolean isFlashing = false;
    private boolean facingRight = true;

    private int currentAttackFrame = 0;

    public SpriteAnimator(ImageView playerImageView) {
        this.playerImageView = playerImageView;

        // Load sprites
        this.walk1 = new Image(getClass().getResourceAsStream("/sprites/walk1.png"));
        this.walk2 = new Image(getClass().getResourceAsStream("/sprites/walk2.png"));
        this.jumpImage = new Image(getClass().getResourceAsStream("/sprites/jump.png"));
        this.idleImage = new Image(getClass().getResourceAsStream("/sprites/player-idle.png"));
        this.downAttackImage = new Image(getClass().getResourceAsStream("/sprites/StopVirus_Down-Air-Attack.png"));

        this.attackImages = new Image[]{
            new Image(getClass().getResourceAsStream("/sprites/attack1.png")),
            new Image(getClass().getResourceAsStream("/sprites/attack2.png")),
            new Image(getClass().getResourceAsStream("/sprites/attack3.png"))
        };

        // Setup animations
        this.walkAnimation = new Timeline(
            new KeyFrame(Duration.seconds(0.15), event -> toggleWalkImage())
        );
        walkAnimation.setCycleCount(Timeline.INDEFINITE);

        this.attackAnimation = new Timeline(
            new KeyFrame(Duration.seconds(0.17), event -> toggleAttackImage())
        );
        attackAnimation.setCycleCount(attackImages.length);
        attackAnimation.setOnFinished(event -> finishAttack());

        this.invincibilityFlash = new Timeline(
            new KeyFrame(Duration.seconds(0.1), event -> toggleTransparency())
        );
        invincibilityFlash.setCycleCount(Timeline.INDEFINITE);
    }

    private void toggleWalkImage() {
        if (playerImageView.getImage() == walk1) {
            playerImageView.setImage(walk2);
        } else {
            playerImageView.setImage(walk1);
        }
    }

    private void toggleAttackImage() {
        if (currentAttackFrame < attackImages.length) {
            playerImageView.setImage(attackImages[currentAttackFrame]);
            currentAttackFrame++;
        }
    }

    public void startWalking(boolean movingRight) {
        if (!isWalking && !isAttacking && !isJumping) {
            playerImageView.setImage(walk1);
            walkAnimation.play();
            isWalking = true;
        }
        setDirection(movingRight);
    }

    public void stopWalking() {
        if (isWalking) {
            walkAnimation.stop();
            if (!isAttacking && !isJumping) {
                playerImageView.setImage(idleImage);
            }
            isWalking = false;
        }
    }

    public void startJumping() {
        if (!isJumping) {
            stopWalking();
            playerImageView.setImage(jumpImage);
            isJumping = true;
        }
    }

    public void stopJumping() {
        if (isJumping) {
            isJumping = false;
            if (isWalking) {
                startWalking(facingRight);
            } else if (!isAttacking) {
                playerImageView.setImage(idleImage);
            }
        }
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public boolean isJumping() {
        return isJumping;
    }

    public void startAttack() {
        if (!isAttacking) {
            isAttacking = true;
            currentAttackFrame = 0;

            // Adjust attack animation to last at least 0.5 seconds
            attackAnimation.stop();
            attackAnimation.getKeyFrames().clear();

            // Duration for each frame to make total animation last 0.5 seconds
            double frameDuration = 0.5 / attackImages.length;

            for (int i = 0; i < attackImages.length; i++) {
                final int frameIndex = i;
                attackAnimation.getKeyFrames().add(new KeyFrame(
                    Duration.seconds(i * frameDuration),
                    event -> playerImageView.setImage(attackImages[frameIndex])
                ));
            }

            // Ensure attack animation finishes after the last frame
            attackAnimation.setOnFinished(event -> finishAttack());
            attackAnimation.setCycleCount(1); // Single cycle for the animation

            attackAnimation.playFromStart();
        }
    }


    public void stopAttack() {
        attackAnimation.stop();
        isAttacking = false;
        currentAttackFrame = 0;

        // Resume walking or idle based on state
        if (isWalking) {
            startWalking(facingRight);
        } else if (isJumping) {
            playerImageView.setImage(jumpImage);
        } else {
            playerImageView.setImage(idleImage);
        }
    }

    private void finishAttack() {
        isAttacking = false;
        currentAttackFrame = 0;
        if (isWalking) {
            startWalking(facingRight);
        } else if (isJumping) {
            playerImageView.setImage(jumpImage);
        } else {
            playerImageView.setImage(idleImage);
        }
    }

    public void startDownwardAttack() {
        if (!isAttacking) {
            stopWalking(); // Stop walking animation
            stopJumping(); // Stop jumping animation
            isAttacking = true;
            isDownwardAttack = true;
            playerImageView.setImage(downAttackImage);
        }
    }

    public void finishDownwardAttack() {
        isAttacking = false;
        isDownwardAttack = false;

        // Resume appropriate animation
        if (isJumping) {
            playerImageView.setImage(jumpImage);
        } else if (isWalking) {
            startWalking(facingRight);
        } else {
            playerImageView.setImage(idleImage);
        }
    }
    
    private boolean isDownwardAttack = false;

    public boolean isDownwardAttack() {
        return isDownwardAttack;
    }


    public void setDirection(boolean movingRight) {
        if (facingRight != movingRight) {
            facingRight = movingRight;
            playerImageView.setScaleX(facingRight ? 1 : -1);
        }
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    private void toggleTransparency() {
        if (isFlashing) {
            playerImageView.setOpacity(playerImageView.getOpacity() == 1.0 ? 0.5 : 1.0);
        }
    }

    public void startInvincibilityEffect() {
        isFlashing = true;
        invincibilityFlash.playFromStart();
    }

    public void stopInvincibilityEffect() {
        isFlashing = false;
        playerImageView.setOpacity(1.0);
        invincibilityFlash.stop();
    }

    public void playDeathAnimation(ImageView enemyImageView) {
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
                event -> enemyImageView.setImage(explosionFrames[frameIndex])
            ));
        }

        deathAnimation.setCycleCount(1);
        deathAnimation.setOnFinished(event -> enemyImageView.setVisible(false));
        deathAnimation.play();
    }
}
