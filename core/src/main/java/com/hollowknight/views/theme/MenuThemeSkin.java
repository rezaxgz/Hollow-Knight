package com.hollowknight.views.theme;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Disposable;

public final class MenuThemeSkin implements Disposable {

    private static final String PREFERENCES_NAME = "hollow-knight-settings";
    private static final String THEME_KEY = "menuTheme";
    private static final String BASE_PATH = "ui/menu/";
    private static final String TRAJAN_FONT_PATH = "ui/fonts/TrajanPro-Regular.ttf";

    private static final int MENU_BODY_FONT_SIZE = 24;
    private static final int MENU_TITLE_FONT_SIZE = 34;
    private static final int MENU_SMALL_FONT_SIZE = 20;

    private static Cursor sharedCursor;
    private static boolean cursorLoaded;

    private final MenuThemeType theme;
    private final Skin skin;
    private final SpriteBatch backgroundBatch;
    private final Array<Texture> ownedTextures;

    private final Texture backgroundTexture;
    private final Texture saveBackgroundTexture;
    private final Texture titleLogoTexture;
    private final Texture titleOrnamentTexture;
    private final Texture borderTexture;
    private final Texture controllerPromptTexture;
    private final Texture mainBeamTexture;
    private final Texture voidBeamTexture;
    private final Texture soulOrbTexture;
    private final Texture soulGlowTexture;
    private final Texture magicOrbTexture;
    private final Texture healthMaskTexture;
    private final Texture vengefulSpiritTexture;
    private final Texture shadeSoulTexture;
    private final Texture howlingWraithsTexture;
    private final Texture abyssShriekTexture;
    private final Texture slotForgottenCrossroadsTexture;
    private final Texture slotCityOfTearsTexture;
    private final Texture slotAbyssTexture;
    private final Texture slotWhitePalaceTexture;
    private final Texture slotDirtmouthTexture;

    private float particleTime;

    private MenuThemeSkin(MenuThemeType theme) {
        this.theme = theme;
        this.ownedTextures = new Array<>();
        this.backgroundBatch = new SpriteBatch();

        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        backgroundTexture = load(BASE_PATH + "backgrounds/voidheart_menu_bg.png");
        saveBackgroundTexture = load(BASE_PATH + "backgrounds/save_background.png");
        titleLogoTexture = load(BASE_PATH + "common/vheart_title.png");
        titleOrnamentTexture = load(BASE_PATH + "common/title_ornament_large.png");
        borderTexture = load(BASE_PATH + "common/menu_border_black.png");
        controllerPromptTexture = load(BASE_PATH + "common/controller_prompt_bg.png");
        mainBeamTexture = load(BASE_PATH + "effects/main_menu_beam.png");
        voidBeamTexture = load(BASE_PATH + "effects/vheart_beam.png");
        soulOrbTexture = load(BASE_PATH + "icons/soul_orb_full.png");
        soulGlowTexture = load(BASE_PATH + "icons/soul_orb_glow.png");
        magicOrbTexture = load(BASE_PATH + "icons/magic_orb_small.png");
        healthMaskTexture = load(BASE_PATH + "icons/health_mask.png");
        vengefulSpiritTexture = load(BASE_PATH + "icons/spell_vengeful_spirit.png");
        shadeSoulTexture = load(BASE_PATH + "icons/spell_shade_soul.png");
        howlingWraithsTexture = load(BASE_PATH + "icons/spell_howling_wraiths.png");
        abyssShriekTexture = load(BASE_PATH + "icons/spell_abyss_shriek.png");
        slotForgottenCrossroadsTexture = load(BASE_PATH + "slots/area_forgotten_crossroads.png");
        slotCityOfTearsTexture = load(BASE_PATH + "slots/area_city_of_tears.png");
        slotAbyssTexture = load(BASE_PATH + "slots/area_abyss.png");
        slotWhitePalaceTexture = load(BASE_PATH + "slots/area_white_palace.png");
        slotDirtmouthTexture = load(BASE_PATH + "slots/area_dirtmouth.png");

        customizeSkin();
        applyCustomCursor();
    }

    public static MenuThemeSkin fromSettings() {
        String id = Gdx.app.getPreferences(PREFERENCES_NAME)
                .getString(THEME_KEY, MenuThemeType.VOIDHEART.getId());
        return new MenuThemeSkin(MenuThemeType.fromId(id));
    }

    public static MenuThemeSkin fromThemeId(String themeId) {
        return new MenuThemeSkin(MenuThemeType.fromId(themeId));
    }

    public Skin getSkin() {
        return skin;
    }

    public MenuThemeType getTheme() {
        return theme;
    }

    public Color titleColor() {
        switch (theme) {
            case ROYAL_GOLD:
                return new Color(0.95f, 0.70f, 0.22f, 1f);
            case CLASSIC_HOLLOW:
                return new Color(0.88f, 0.96f, 1f, 1f);
            case VOIDHEART:
            default:
                return new Color(0.92f, 0.94f, 0.98f, 1f);
        }
    }

    public Color highlightColor() {
        switch (theme) {
            case ROYAL_GOLD:
                return new Color(1f, 0.70f, 0.18f, 1f);
            case CLASSIC_HOLLOW:
                return new Color(0.45f, 0.82f, 1f, 1f);
            case VOIDHEART:
            default:
                return new Color(0.85f, 0.90f, 1f, 1f);
        }
    }

    public Color bodyColor() {
        switch (theme) {
            case ROYAL_GOLD:
                return new Color(0.82f, 0.75f, 0.64f, 1f);
            case CLASSIC_HOLLOW:
                return new Color(0.77f, 0.88f, 0.94f, 1f);
            case VOIDHEART:
            default:
                return new Color(0.78f, 0.80f, 0.86f, 1f);
        }
    }

    public void drawBackground(float delta, boolean saveScreen) {
        particleTime += delta;

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        backgroundBatch.begin();

        drawCovered(saveScreen ? saveBackgroundTexture : backgroundTexture, width, height, themeTint(1f));
        drawParticles(width, height);

        backgroundBatch.setColor(0f, 0f, 0f, theme == MenuThemeType.ROYAL_GOLD ? 0.45f : 0.28f);
        backgroundBatch.draw(backgroundTexture, 0f, 0f, width, height);

        backgroundBatch.setColor(Color.WHITE);
        backgroundBatch.end();
    }

    public Image createTitleLogo(float width) {
        Image image = new Image(drawable(titleLogoTexture));
        image.setColor(titleColor());
        image.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        image.setSize(width, width * 0.32f);
        return image;
    }

    public Image createOrnament(float width) {
        Image image = new Image(drawable(titleOrnamentTexture));
        image.setColor(highlightColor());
        image.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        image.setSize(width, width * 0.09f);
        return image;
    }

    public TextButton createMenuButton(String text) {
        TextButton button = new TextButton(text, skin);
        button.getLabel().setFontScale(1.0f);
        button.getLabel().setColor(bodyColor());
        return button;
    }

    private static void applyCustomCursor() {
        if (cursorLoaded) {
            if (sharedCursor != null)
                Gdx.graphics.setCursor(sharedCursor);
            return;
        }
        cursorLoaded = true;
        FileHandle cursorFile = Gdx.files.internal(BASE_PATH + "common/cursor.png");
        if (!cursorFile.exists())
            return;

        Pixmap cursorPixmap = null;
        try {
            cursorPixmap = new Pixmap(cursorFile);
            int hotspotX = Math.min(8, Math.max(0, cursorPixmap.getWidth() / 6));
            int hotspotY = Math.min(8, Math.max(0, cursorPixmap.getHeight() / 6));
            sharedCursor = Gdx.graphics.newCursor(cursorPixmap, hotspotX, hotspotY);
            Gdx.graphics.setCursor(sharedCursor);
        } catch (GdxRuntimeException exception) {
            sharedCursor = null;
        } finally {
            if (cursorPixmap != null)
                cursorPixmap.dispose();
        }
    }

    private Texture load(String path) {
        FileHandle file = Gdx.files.internal(path);
        if (!file.exists()) {
            Texture fallback = createFallbackTexture();
            ownedTextures.add(fallback);
            return fallback;
        }
        Texture texture = new Texture(file);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        ownedTextures.add(texture);
        return texture;
    }

    private BitmapFont createTrajanFont(int size, float borderWidth, int shadowOffsetX, int shadowOffsetY) {
        FileHandle fontFile = Gdx.files.internal(TRAJAN_FONT_PATH);
        if (!fontFile.exists())
            return null;

        FreeTypeFontGenerator generator = null;
        try {
            generator = new FreeTypeFontGenerator(fontFile);
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = size;
            parameter.minFilter = Texture.TextureFilter.Linear;
            parameter.magFilter = Texture.TextureFilter.Linear;
            parameter.borderWidth = borderWidth;
            parameter.borderColor = new Color(0f, 0f, 0f, 0.72f);
            parameter.shadowOffsetX = shadowOffsetX;
            parameter.shadowOffsetY = shadowOffsetY;
            parameter.shadowColor = new Color(0f, 0f, 0f, 0.62f);
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS
                    + "–—’‘“”…•◦؛،؟آابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهیءئۀة";

            BitmapFont font = generator.generateFont(parameter);
            font.getData().markupEnabled = true;
            return font;
        } catch (GdxRuntimeException exception) {
            return null;
        } finally {
            if (generator != null)
                generator.dispose();
        }
    }

    private BitmapFont createOrGetFont(String skinFontName, int size, float borderWidth) {
        BitmapFont trajanFont = createTrajanFont(size, borderWidth, 1, -1);
        if (trajanFont != null) {
            skin.add(skinFontName, trajanFont, BitmapFont.class);
            return trajanFont;
        }
        return skin.getFont(skinFontName);
    }

    private <T> T findStyle(String name, Class<T> styleType) {
        try {
            return skin.get(name, styleType);
        } catch (GdxRuntimeException exception) {
            return null;
        }
    }

    private void customizeSkin() {
        BitmapFont defaultFont = createOrGetFont("default", MENU_BODY_FONT_SIZE, 0.55f);
        BitmapFont bodyFont = createOrGetFont("font", MENU_BODY_FONT_SIZE, 0.55f);
        BitmapFont titleFont = createOrGetFont("window", MENU_TITLE_FONT_SIZE, 0.70f);

        Label.LabelStyle labelStyle = skin.get(Label.LabelStyle.class);
        labelStyle.font = bodyFont;
        labelStyle.fontColor = bodyColor();

        TextButton.TextButtonStyle buttonStyle = skin.get(TextButton.TextButtonStyle.class);
        buttonStyle.font = bodyFont;
        buttonStyle.fontColor = bodyColor();
        buttonStyle.overFontColor = highlightColor();
        buttonStyle.downFontColor = Color.WHITE;
        buttonStyle.checkedFontColor = highlightColor();
        buttonStyle.up = solidDrawable(0f, 0f, 0f, 0f);
        buttonStyle.down = solidDrawable(1f, 1f, 1f, 0.06f);
        buttonStyle.over = solidDrawable(1f, 1f, 1f, 0.035f);
    }

    private void drawCovered(Texture texture, float width, float height, Color color) {
        float textureRatio = (float) texture.getWidth() / Math.max(1f, texture.getHeight());
        float screenRatio = width / Math.max(1f, height);
        float drawWidth = width;
        float drawHeight = height;

        if (textureRatio > screenRatio) {
            drawWidth = height * textureRatio;
        } else {
            drawHeight = width / textureRatio;
        }
        float x = (width - drawWidth) / 2f;
        float y = (height - drawHeight) / 2f;

        backgroundBatch.setColor(color);
        backgroundBatch.draw(texture, x, y, drawWidth, drawHeight);
    }

    private void drawParticles(float width, float height) {
        Color color = highlightColor();
        for (int index = 0; index < 18; index++) {
            float seed = index * 37.17f;
            float x = (seed * 53f) % width;
            float y = ((seed * 29f) + particleTime * (12f + index)) % height;
            float size = 5f + (index % 4) * 2.3f;
            float alpha = 0.10f + (index % 5) * 0.028f;

            backgroundBatch.setColor(color.r, color.g, color.b, alpha);
            backgroundBatch.draw(soulGlowTexture, x, y, size, size);
        }
    }

    private Color themeTint(float alpha) {
        switch (theme) {
            case ROYAL_GOLD:
                return new Color(0.72f, 0.55f, 0.34f, alpha);
            case CLASSIC_HOLLOW:
                return new Color(0.55f, 0.80f, 1f, alpha);
            case VOIDHEART:
            default:
                return new Color(0.86f, 0.90f, 1f, alpha);
        }
    }

    private TextureRegionDrawable drawable(Texture texture) {
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private Drawable solidDrawable(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        ownedTextures.add(texture);
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private Texture createFallbackTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.CLEAR);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        backgroundBatch.dispose();
        if (skin != null)
            skin.dispose();
        for (Texture texture : ownedTextures)
            texture.dispose();
    }
}