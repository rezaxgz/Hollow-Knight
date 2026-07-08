package com.hollowknight.views.actors.modals;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.hollowknight.controller.GameController;
import com.hollowknight.models.player.CharmType;
import com.hollowknight.models.player.Player;
import com.hollowknight.views.GameAssetManager;

public class InventoryModal extends Modal {
    private Table mainTable;
    private Label nameLabel;
    private Label descLabel;
    private InputListener stageListener;

    public InventoryModal() {
        super();
        mainTable = new Table();
        add(mainTable).expand().fill();

        // Setup hover labels using the main UI skin
        nameLabel = new Label("", skin);
        nameLabel.setAlignment(Align.center);

        descLabel = new Label("", skin);
        descLabel.setAlignment(Align.center);
        descLabel.setWrap(true);

        rebuildUI();
    }

    private void rebuildUI() {
        mainTable.clear();

        Player player = GameController.getInstance().world.player;
        CharmType[] activeCharms = player.charmNotches;

        // --- ACTIVE CHARMS (TOP) ---
        Table activeSlotsTable = new Table();
        activeSlotsTable.defaults().size(80, 80).pad(10);

        for (int i = 0; i < activeCharms.length; i++) {
            final int slotIndex = i;
            final CharmType charm = activeCharms[i];

            if (charm != null) {
                Image charmImg = new Image(new TextureRegionDrawable(GameAssetManager.charmLogos.get(charm)));
                charmImg.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        // Unequip charm
                        player.charmNotches[slotIndex] = null;
                        rebuildUI();
                    }

                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        nameLabel.setText(charm.name);
                        descLabel.setText(charm.description);
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        nameLabel.setText("");
                        descLabel.setText("");
                    }
                });
                activeSlotsTable.add(charmImg);
            } else {
                // Render an empty slot placeholder (Dimmed window drawable)
                Image emptySlot = new Image(skin.getDrawable("window"));
                emptySlot.setColor(Color.DARK_GRAY);
                activeSlotsTable.add(emptySlot);
            }
        }

        // --- ALL CHARMS (BOTTOM) ---
        Table allCharmsTable = new Table();
        allCharmsTable.defaults().size(60, 60).pad(10);
        int cols = 0;

        for (final CharmType charm : CharmType.values()) {
            Image charmImg = new Image(new TextureRegionDrawable(GameAssetManager.charmLogos.get(charm)));

            // Check if this charm is currently active
            boolean isEquipped = false;
            for (CharmType c : activeCharms) {
                if (c == charm) {
                    isEquipped = true;
                    break;
                }
            }

            if (isEquipped) {
                // Dim the equipped charms in the main list
                charmImg.setColor(Color.DARK_GRAY);
            } else {
                charmImg.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        // Find first empty slot to equip
                        for (int j = 0; j < activeCharms.length; j++) {
                            if (activeCharms[j] == null) {
                                activeCharms[j] = charm;
                                rebuildUI();
                                break;
                            }
                        }
                    }

                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        nameLabel.setText(charm.name);
                        descLabel.setText(charm.description);
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        nameLabel.setText("");
                        descLabel.setText("");
                    }
                });
            }

            allCharmsTable.add(charmImg);
            cols++;
            // Wrap to a new row every 4 charms
            if (cols % 4 == 0) {
                allCharmsTable.row();
            }
        }

        // Assemble the Main UI layout
        mainTable.add(new Label("ACTIVE CHARMS", skin)).padBottom(10).row();
        mainTable.add(activeSlotsTable).padBottom(30).row();
        mainTable.add(new Label("ALL CHARMS", skin)).padBottom(10).row();
        mainTable.add(allCharmsTable).padBottom(20).row();
        mainTable.add(nameLabel).width(500).padBottom(5).row();
        mainTable.add(descLabel).width(500).height(50);
    }

    @Override
    public void show() {
        super.show();

        // Setup Stage listener to handle closing when 'I' or 'ESCAPE' is pressed
        stageListener = new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.I || keycode == Input.Keys.ESCAPE) {
                    GameController.getInstance().isPaused = false;
                    hide();
                    return true;
                }
                return false;
            }
        };

        if (this.getStage() != null) {
            this.getStage().addListener(stageListener);
        }
    }

    @Override
    public void hide() {
        // Remove the stage listener cleanly when the modal is closed
        if (this.getStage() != null && stageListener != null) {
            this.getStage().removeListener(stageListener);
        }
        super.hide();
    }
}