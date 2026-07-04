package com.hollowknight.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.hollowknight.models.enemies.EnemyAnimations;
import com.hollowknight.models.npc.ZoteAnimation;
import com.hollowknight.models.player.HealthMaskState;
import com.hollowknight.models.player.PlayerAnimation;
import com.hollowknight.models.player.PlayerEffect;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class GameAssetManager {
    public static Skin skin;
    public static Skin hollowSkin;
    public static final HashMap<PlayerAnimation, Animation<TextureRegion>> playerAnimationMap = new HashMap<>();
    public static final HashMap<HealthMaskState, Animation<TextureRegion>> healthAnimationMap = new HashMap<>();
    public static final HashMap<EnemyAnimations, Animation<TextureRegion>> enemyAnimationMap = new HashMap<>();
    public static final HashMap<PlayerEffect, Animation<TextureRegion>> playerEffectAnimationMap = new HashMap<>();
    public static final HashMap<ZoteAnimation, Animation<TextureRegion>> zoteAnimationMap = new HashMap<>();

    public static final Texture healthBar = new Texture("animation/HUD/HUD Cln_161.png");

    public static final Texture[] soulsTextures = new Texture[19];

    public static TextureRegion[] laserTexture = new TextureRegion[4];
    public static TextureRegion laserStartTexture;

    public static final Texture eButton = new Texture("E_button.png");

    public static void init() {
        loadSkins();

        for (PlayerAnimation type : PlayerAnimation.values()) {
            loadPlayerAnimation(type);
        }

        // Added initialization loop for health animations
        for (HealthMaskState type : HealthMaskState.values()) {
            loadHealthAnimation(type);
        }

        loadSoulsTexture();
        loadGroundEnemyAnimations();

        loadPlayerEffectAnimations();
        loadLaserTexture();

        loadZoteAnimations();
    }

    private static void loadSkins() {
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        hollowSkin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("ui/General/HollowSkin.atlas"));
        hollowSkin.addRegions(atlas);

        // 2. Generate the BitmapFont from the TTF file
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("ui/General/TrajanPro-Regular.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 24; // Set your desired font size here
        BitmapFont generatedFont = generator.generateFont(parameter);
        generator.dispose();
        hollowSkin.add("Hollowfont", generatedFont, BitmapFont.class);

        hollowSkin.load(Gdx.files.internal("ui/General/HollowSkin.json"));
    }

    private static void loadZoteAnimations() {
        for (ZoteAnimation type : ZoteAnimation.values()) {
            Texture texture = new Texture(type.path);

            // Split based on ZoteAnimation's frameCount properties
            TextureRegion[][] split = TextureRegion.split(
                    texture,
                    texture.getWidth() / type.frameCount,
                    texture.getHeight());

            TextureRegion[] frames = new TextureRegion[type.frameCount];
            for (int i = 0; i < type.frameCount; i++) {
                frames[i] = split[0][i]; // Assuming Zote's sprites are on a single row
            }

            Animation<TextureRegion> animation = new Animation<>(type.frameDuration, frames);

            switch (type.type) {
                case LOOP -> animation.setPlayMode(Animation.PlayMode.LOOP);
                case LOOP_PINGPONG -> animation.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
                case ONESHOT -> animation.setPlayMode(Animation.PlayMode.NORMAL);
            }

            zoteAnimationMap.put(type, animation);
        }
    }

    private static void loadLaserTexture() {
        Texture effectsAtlas = new Texture("animation/Crystallized/atlas0 #25304.png");
        laserStartTexture = new TextureRegion(effectsAtlas, 328, 216, 54, 54);
        laserTexture[0] = new TextureRegion(effectsAtlas, 0, 22, 114, 32);
        laserTexture[1] = new TextureRegion(effectsAtlas, 119, 24, 114, 32);
        laserTexture[2] = new TextureRegion(effectsAtlas, 119, 164, 114, 25);
        laserTexture[3] = new TextureRegion(effectsAtlas, 210, 105, 114, 28);
    }

    private static void loadPlayerEffectAnimations() {
        for (PlayerEffect effect : PlayerEffect.values()) {
            Texture texture = new Texture(effect.path);

            int tileWidth = texture.getWidth() / effect.frameCount;
            int tileHeight = texture.getHeight();

            TextureRegion[][] split = TextureRegion.split(texture, tileWidth, tileHeight);
            TextureRegion[] frames = new TextureRegion[effect.frameCount];

            // Extract frames from the single row
            for (int i = 0; i < effect.frameCount; i++) {
                frames[i] = split[0][i];
            }

            Animation<TextureRegion> animation = new Animation<>(effect.frameDuration, frames);

            animation.setPlayMode(PlayMode.NORMAL);

            playerEffectAnimationMap.put(effect, animation);
        }
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