package com.hollowknight.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.hollowknight.models.player.HealthMaskState;
import com.hollowknight.models.player.PlayerState;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class GameAssetManager {
    public static Skin skin;
    public static final HashMap<PlayerState, Animation<TextureRegion>> playerAnimationMap = new HashMap<>();
    public static final HashMap<HealthMaskState, Animation<TextureRegion>> healthAnimationMap = new HashMap<>();

    public static final Texture healthBar = new Texture("animation/HUD/HUD Cln_161.png");

    public static void init() {
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        for (PlayerState type : PlayerState.values()) {
            loadPlayerAnimation(type);
        }

        // Added initialization loop for health animations
        for (HealthMaskState type : HealthMaskState.values()) {
            loadHealthAnimation(type);
        }
    }

    private static void loadPlayerAnimation(PlayerState type) {
        Texture texture = new Texture(type.path);

        TextureRegion[][] split = TextureRegion.split(
                texture,
                texture.getWidth() / type.colCount,
                texture.getHeight() / type.rowCount);

        int frameCount = type.frameCount;
        TextureRegion[] frames = new TextureRegion[frameCount];

        int cols = split[0].length;

        for (int i = 0; i < frameCount; i++) {
            int row = i / cols;
            int col = i % cols;
            frames[i] = split[row][col];
        }

        if (type.isReversed) {
            Collections.reverse(Arrays.asList(frames));
        }

        Animation<TextureRegion> animation = new Animation<>(type.frameDuration, frames);
        switch (type.animationType) {
            case LOOP ->
                animation.setPlayMode(Animation.PlayMode.LOOP);

            case LOOP_PINGPONG ->
                animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

            case ONESHOT ->
                animation.setPlayMode(Animation.PlayMode.NORMAL);
        }
        playerAnimationMap.put(type, animation);
    }

    private static void loadHealthAnimation(HealthMaskState type) {
        Texture texture = new Texture(type.path);

        int tileWidth = texture.getWidth() / type.frameCount;
        int tileHeight = texture.getHeight();

        TextureRegion[][] split = TextureRegion.split(texture, tileWidth, tileHeight);
        TextureRegion[] frames = new TextureRegion[type.frameCount];

        // Extract frames from the single row
        for (int i = 0; i < type.frameCount; i++) {
            frames[i] = split[0][i];
        }

        Animation<TextureRegion> animation = new Animation<>(type.frameDuration, frames);

        animation.setPlayMode(type.mode);

        healthAnimationMap.put(type, animation);
    }
}