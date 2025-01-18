// PauseManager

package application;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.HashMap;

public class PauseManager {

    public boolean isPaused = false;
    public Pane uiRoot;
    public AnimationTimer gameTimer;
    public Pane pauseOverlay;

    public PauseManager(Pane uiRoot, Scene scene, AnimationTimer gameTimer, HashMap<KeyCode, Boolean> keys) {
        this.uiRoot = uiRoot;
        this.gameTimer = gameTimer;
        initializePauseOverlay();

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                togglePause(keys);
            } else if (!isPaused) {
                keys.put(event.getCode(), true);
            }
        });

        scene.setOnKeyReleased(event -> {
            if (!isPaused) {
                keys.put(event.getCode(), false);
            }
        });
    }

    private void initializePauseOverlay() {
        pauseOverlay = new Pane();

        Rectangle background = new Rectangle(1280, 720, Color.rgb(0, 0, 0, 0.5));
        Text pauseText = new Text("");
        pauseText.setFont(Font.font(48));
        pauseText.setFill(Color.WHITE);
        pauseText.setTranslateX(560);
        pauseText.setTranslateY(360);

        pauseOverlay.getChildren().addAll(background, pauseText);
        pauseOverlay.setVisible(false);
        uiRoot.getChildren().add(pauseOverlay);
    }

    public void togglePause(HashMap<KeyCode, Boolean> keys) {
        if (isPaused) {
            resumeGame(keys);
        } else {
            pauseGame();
        }
    }

    public void pauseGame() {
        isPaused = true;
        gameTimer.stop();
        pauseOverlay.setVisible(true);
    }

    public void resumeGame(HashMap<KeyCode, Boolean> keys) {
        isPaused = false;
        gameTimer.start();
        pauseOverlay.setVisible(false);

        // Clear all keys to avoid unintended input on resume
        keys.clear();
    }

    public boolean isPaused() {
        return isPaused;
    }
}