package com.hollowknight.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
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
import com.hollowknight.models.player.CharmType;
import com.hollowknight.models.player.HealthMaskState;
import com.hollowknight.models.player.PlayerAnimation;
import com.hollowknight.models.player.PlayerEffectAnimation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class GameAssetManager {
    public static Skin skin;
    public static Skin hollowSkin;
    public static final HashMap<PlayerAnimation, Animation<TextureRegion>> playerAnimationMap = new HashMap<>();
    public static final HashMap<HealthMaskState, Animation<TextureRegion>> healthAnimationMap = new HashMap<>();
    public static final HashMap<EnemyAnimations, Animation<TextureRegion>> enemyAnimationMap = new HashMap<>();
    public static final HashMap<PlayerEffectAnimation, Animation<TextureRegion>> playerEffectAnimationMap = new HashMap<>();
    public static final HashMap<ZoteAnimation, Animation<TextureRegion>> zoteAnimationMap = new HashMap<>();
    public static final HashMap<CharmType, Texture> charmLogos = new HashMap<>();

    public static final Texture healthBar = new Texture("animation/HUD/HUD Cln_161.png");
    public static final Texture gateTexture = new Texture("sprites/bossDoor.png");
    public static final Texture pixelTexture = new Texture("pixle.png");

    public static Cursor customCursor;
    public static Cursor blankCursor;

    public static final Texture[] soulsTextures = new Texture[19];

    public static TextureRegion[] laserTexture = new TextureRegion[4];
    public static TextureRegion laserStartTexture;

    public static final Texture eButton = new Texture("E_button.png");

    // Music
    public static Music menuBgm;
    public static Music greenPathMusic;
    public static Music crossRoadsMusic;
    public static Music falseKnightMusic;
    public static Music gameEndMusic;

    // sfx
    public static Sound jumpSfx;
    public static Sound dashSfx;
    public static Sound knightHurtSfx;
    public static Sound evadeSfx;
    public static Sound fallingSfx;
    public static Sound walkSfx;
    public static Sound deathSfx;
    public static Sound focusChargingSfx;
    public static Sound focusHealSfx;
    public static Sound focusReadySfx;
    public static Sound enemyHurtSfx;
    public static Music wallSlideSfx;
    public static Sound wallJumpSfx;

    public static Sound[] slashSfxs = new Sound[5];
    public static Sound[] soulSfxs = new Sound[7];

    public static Sound[] zoteVoices = new Sound[6];

    public static void init() {
        loadSkins();
        loadMusic();
        loadSfx();
        loadZoteVoices();

        loadCursors();

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

        loadCharmLogos();
    }

    public static void loadCursors() {

        Pixmap cursorPixmap = new Pixmap(Gdx.files.internal("Cursor.png"));

        customCursor = Gdx.graphics.newCursor(cursorPixmap, 0, 0);
        cursorPixmap.dispose();

        Pixmap blankPixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        blankPixmap.setBlending(Pixmap.Blending.None);

        // Explicitly set the color to fully transparent and fill the pixmap
        blankPixmap.setColor(Color.CLEAR);
        blankPixmap.fill();

        blankCursor = Gdx.graphics.newCursor(blankPixmap, 0, 0);
        blankPixmap.dispose();
    }

    private static void loadCharmLogos() {
        for (CharmType c : CharmType.values()) {
            charmLogos.put(c, new Texture(c.path));
        }
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
        for (PlayerEffectAnimation effect : PlayerEffectAnimation.values()) {
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

    private static void loadSfx() {
        jumpSfx = Gdx.audio.newSound(Gdx.files.internal("audio/hero_jump.wav"));
        dashSfx = Gdx.audio.newSound(Gdx.files.internal("audio/hero_dash.wav"));
        knightHurtSfx = Gdx.audio.newSound(Gdx.files.internal("audio/hero_damage.wav"));
        evadeSfx = Gdx.audio.newSound(Gdx.files.internal("audio/hero_evade.wav"));
        fallingSfx = Gdx.audio.newSound(Gdx.files.internal("audio/hero_falling.wav"));
        walkSfx = Gdx.audio.newSound(Gdx.files.internal("audio/hero_walk_footsteps_stone.wav"));
        deathSfx = Gdx.audio.newSound(Gdx.files.internal("audio/hero_death.wav"));
        focusChargingSfx = Gdx.audio.newSound(Gdx.files.internal("audio/focus_health_charging.wav"));
        focusHealSfx = Gdx.audio.newSound(Gdx.files.internal("audio/focus_health_heal.wav"));
        focusReadySfx = Gdx.audio.newSound(Gdx.files.internal("audio/focus_ready.wav"));
        enemyHurtSfx = Gdx.audio.newSound(Gdx.files.internal("audio/enemy_damage.wav"));
        wallSlideSfx = Gdx.audio.newMusic(Gdx.files.internal("audio/hero_wall_slide.wav"));
        wallJumpSfx = Gdx.audio.newSound(Gdx.files.internal("audio/hero_wall_jump.wav"));

        for (int i = 1; i <= 5; i++) {
            slashSfxs[i - 1] = Gdx.audio.newSound(Gdx.files.internal("audio/sword_" + i + ".wav"));
        }

        for (int i = 1; i <= 7; i++) {
            soulSfxs[i - 1] = Gdx.audio.newSound(Gdx.files.internal("audio/soul_pickup_" + i + ".wav"));
        }
    }

    private static void loadMusic() {
        menuBgm = Gdx.audio.newMusic(Gdx.files.internal("audio/mainMenuSong.wav"));
        crossRoadsMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/crossroads.wav"));
        greenPathMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/Greenpath.mp3"));
        falseKnightMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/False Knight.mp3"));
        gameEndMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/Game End.mp3"));
    }

    private static void loadZoteVoices() {
        for (int i = 0; i < 5; i++) {
            zoteVoices[i] = Gdx.audio.newSound(Gdx.files.internal("audio/Zote/Zote_0" + (i + 1) + ".wav"));
        }
        zoteVoices[5] = Gdx.audio.newSound(Gdx.files.internal("audio/Zote/Zote_03 #030084.wav"));
    }
}