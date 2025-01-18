package application;

import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FinishLine {

    private final ImageView finishView;

    public FinishLine(double x, double y) {
        // Load and scale the finish line image
        Image finishImage = new Image(getClass().getResourceAsStream("/sprites/cpu.png"), 120, 120, false, true);
        this.finishView = new ImageView(finishImage);
        this.finishView.setTranslateX(x);
        this.finishView.setTranslateY(y);
    }

    public ImageView getView() {
        return finishView;
    }

    public boolean checkCollision(ImageView player) {
        Bounds finishBounds = finishView.getBoundsInParent();
        Bounds playerBounds = player.getBoundsInParent();
        return finishBounds.intersects(playerBounds);
    }
}
