package com.hollowknight.views.actors.modals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.hollowknight.models.achievements.Achievement;
import com.hollowknight.views.GameAssetManager;

public class AchievementPopup extends Table {

    public AchievementPopup(Achievement achievement) {
        // 1. Notice we removed setFillParent(true) and the top/right alignment.
        // This class is now just the physical dimensions of the single notification
        // box.

        // Create the dark background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.1f, 0.1f, 0.1f, 0.9f));
        pixmap.fill();
        this.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(pixmap))));
        pixmap.dispose();

        this.pad(15);

        // Setup Text
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.fontColor = Color.GOLD;
        titleStyle.font = GameAssetManager.hollowSkin.getFont("Hollowfont"); // Replace with your custom font when ready

        Label titleLabel = new Label("ACHIEVEMENT UNLOCKED\n" + achievement.title, titleStyle);
        titleLabel.setAlignment(Align.center);

        this.add(titleLabel);

        // Fade in, hold, fade out, then destroy this specific actor
        this.getColor().a = 0;
        this.addAction(Actions.sequence(
                Actions.fadeIn(0.5f),
                Actions.delay(3.0f),
                Actions.fadeOut(0.5f),
                Actions.removeActor()));
    }

    public static void show(Stage stage, Achievement achievement) {
        if (stage == null)
            return;

        // 1. Search the stage for our master container
        Table container = stage.getRoot().findActor("achievement_container");

        // 2. If it doesn't exist yet (first achievement), create it and anchor it to
        // the screen
        if (container == null) {
            container = new Table();
            container.setName("achievement_container");
            container.setFillParent(true);
            container.top().right().padTop(20).padRight(20);
            stage.addActor(container);
        }

        // 3. Create the actual popup UI
        AchievementPopup popup = new AchievementPopup(achievement);

        // 4. Add the popup to the master container.
        // The .row() command tells Scene2D that the next item added should go
        // underneath this one.
        // The .padBottom(10) gives a nice gap between simultaneous popups.
        container.add(popup).right().padBottom(10).row();
    }
}