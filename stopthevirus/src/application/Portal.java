//Portal.java

package application;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Bounds;

public class Portal {
    private final ImageView portalView;
    private final String targetLevel;

    public Portal(String targetLevel, double x, double y) {
        this.targetLevel = targetLevel;

        // Load and scale portal image
        Image portalImage = new Image(getClass().getResourceAsStream("/sprites/portal.gif"), 60, 60, false, true);
        this.portalView = new ImageView(portalImage);
        this.portalView.setTranslateX(x);
        this.portalView.setTranslateY(y);
    }

    public ImageView getView() {
        return portalView;
    }

    public boolean checkCollision(ImageView player) {
        Bounds portalBounds = portalView.getBoundsInParent();
        Bounds playerBounds = player.getBoundsInParent();
        return portalBounds.intersects(playerBounds);
    }

    public String getTargetLevel() {
        return targetLevel;
    }
}
