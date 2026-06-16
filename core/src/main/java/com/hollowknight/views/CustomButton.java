package com.hollowknight.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class CustomButton {
    public static Button create(String normal, String hovered) {
        Texture buttonNormal = new Texture(Gdx.files.internal(normal));
        Texture buttonHovered = new Texture(Gdx.files.internal(hovered));

        TextureRegionDrawable normalDrawable = new TextureRegionDrawable(new TextureRegion(buttonNormal));
        TextureRegionDrawable hoveredDrawable = new TextureRegionDrawable(new TextureRegion(buttonHovered));

        ButtonStyle buttonStyle = new ButtonStyle();
        buttonStyle.up = normalDrawable;
        buttonStyle.over = hoveredDrawable;

        return new Button(buttonStyle);
    }
}