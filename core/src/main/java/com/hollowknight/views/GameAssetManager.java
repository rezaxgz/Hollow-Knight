package com.hollowknight.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.hollowknight.models.PlayerState;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class GameAssetManager {
    public static Skin skin;
    public static final HashMap<PlayerState, Animation<TextureRegion>> animationMap = new HashMap<>();

    public static void init() {
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        for (PlayerState type : PlayerState.values()) {
            loadAnimation(type);
        }
    }

    public static void loadAnimation(PlayerState type) {
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
        animationMap.put(type, animation);
    }
}
