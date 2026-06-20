package com.hollowknight.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.HashMap;

public class GameAssetManager {
    public static Skin skin;
    public static final HashMap<AnimationType, Animation<TextureRegion>> animationMap = new HashMap<>();

    public static void init() {
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        for (AnimationType type : AnimationType.values()) {
            loadAnimation(type);
        }
    }

    public static void loadAnimation(AnimationType type) {
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

        Animation<TextureRegion> animation = new Animation<>(1 / 30f, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        animationMap.put(type, animation);
    }
}
