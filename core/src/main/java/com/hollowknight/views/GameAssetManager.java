package com.hollowknight.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.hollowknight.models.enemies.EnemyAnimations;
import com.hollowknight.models.player.HealthMaskState;
import com.hollowknight.models.player.PlayerAnimation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class GameAssetManager {
    public static Skin skin;
    public static final HashMap<PlayerAnimation, Animation<TextureRegion>> playerAnimationMap = new HashMap<>();
    public static final HashMap<HealthMaskState, Animation<TextureRegion>> healthAnimationMap = new HashMap<>();
    public static final HashMap<EnemyAnimations, Animation<TextureRegion>> enemyAnimationMap = new HashMap<>();

    public static final Texture healthBar = new Texture("animation/HUD/HUD Cln_161.png");

    public static final Texture[] soulsTextures = new Texture[19];

    public static void init() {
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        for (PlayerAnimation type : PlayerAnimation.values()) {
            loadPlayerAnimation(type);
        }

        // Added initialization loop for health animations
        for (HealthMaskState type : HealthMaskState.values()) {
            loadHealthAnimation(type);
        }

        loadSoulsTexture();
        loadGroundEnemyAnimations();
    }

    private static void loadPlayerAnimation(PlayerAnimation type) {
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

    private static void loadSoulsTexture() {
        for (int i = 237; i <= 255; i++) {
            soulsTextures[i - 237] = new Texture("animation/HUD/HUD Cln_" + i + ".png");
        }
    }

    private static void loadGroundEnemyAnimations() {
        for (EnemyAnimations type : EnemyAnimations.values()) {
            Texture texture = new Texture(type.path);

            TextureRegion[][] split = TextureRegion.split(
                    texture,
                    texture.getWidth() / type.frameCount,
                    texture.getHeight());

            int frameCount = type.frameCount;
            TextureRegion[] frames = new TextureRegion[frameCount];

            int cols = split[0].length;

            for (int i = 0; i < frameCount; i++) {
                int row = i / cols;
                int col = i % cols;
                frames[i] = split[row][col];
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
            enemyAnimationMap.put(type, animation);
        }
    }
}