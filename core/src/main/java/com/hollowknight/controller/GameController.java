package com.hollowknight.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.hollowknight.models.Constants;
import com.hollowknight.models.player.CharmType;
import com.hollowknight.models.settings.Controls;
import com.hollowknight.models.settings.GameActionType;
import com.hollowknight.models.settings.GameCheat;
import com.hollowknight.models.settings.Settings;
import com.hollowknight.models.world.GameWorld;
import com.hollowknight.views.actors.modals.BossDefeatModal;
import com.hollowknight.views.actors.modals.InventoryModal;
import com.hollowknight.views.actors.modals.PauseModal;

public class GameController {
    private static GameController instance;
    public GameWorld world;
    PlayerController playerController;
    public boolean isPaused = false;

    public static GameController init(GameWorld world) {
        instance = new GameController(world);
        return instance;
    }

    public static GameController getInstance() {
        return instance;
    }

    private GameController(GameWorld world) {
        this.world = world;
        this.playerController = new PlayerController(world.player);
    }

    public void update(float delta) {
        if (isPaused)
            return;
        world.update(delta);
        if (world.bossJustDefeated) {
            world.bossJustDefeated = false; // Reset flag so it doesn't loop
            isPaused = true;

            // Spawn the victory modal and pass the world to read stats
            BossDefeatModal victoryModal = new BossDefeatModal(world);
            victoryModal.show();
        }
    }

    public boolean handleKeyDown(int keycode) {
        Controls controls = Settings.getInstance().getControls();
        // debug variables
        boolean isDebug = false;
        if (keycode == Input.Keys.NUMPAD_8) {
            Constants.y++;
            isDebug = true;
        } else if (keycode == Input.Keys.NUMPAD_2) {
            isDebug = true;
            Constants.y--;
        } else if (keycode == Input.Keys.NUMPAD_6) {
            isDebug = true;
            Constants.x++;
        } else if (keycode == Input.Keys.NUMPAD_4) {
            isDebug = true;
            Constants.x--;
        } else if (keycode == Input.Keys.NUMPAD_9) {
            Constants.y1++;
            isDebug = true;
        } else if (keycode == Input.Keys.NUMPAD_3) {
            isDebug = true;
            Constants.y1--;
        } else if (keycode == Input.Keys.NUMPAD_7) {
            isDebug = true;
            Constants.x1++;
        } else if (keycode == Input.Keys.NUMPAD_1) {
            isDebug = true;
            Constants.x1--;
        } else if (keycode == Input.Keys.NUMPAD_5) {
            Constants.flag = !Constants.flag;
            isDebug = true;
        }
        if (isDebug) {
            System.out.printf("(%d, %d, %d, %d)\n", Constants.x, Constants.y, Constants.x1, Constants.y1);
        }
        if (keycode == Input.Keys.ESCAPE) {
            if (isPaused)
                return false;
            isPaused = true;
            PauseModal pauseModal = new PauseModal();
            pauseModal.show();
        } else if (keycode == Input.Keys.I) {
            if (isPaused)
                return false;
            isPaused = true;
            InventoryModal inventoryModal = new InventoryModal();
            inventoryModal.show();
        }

        if (isPaused)
            return false;

        // Only check for and apply cheats if Ctrl is held
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            GameCheat cheat = controls.getCheat(keycode);

            if (cheat != null) {
                world.applyCheat(cheat);
                return false;
            }
        }

        if (keycode <= Input.Keys.NUM_8 && keycode >= Input.Keys.NUM_1) {
            int i = keycode - Input.Keys.NUM_1;
            world.player.charmNotches[0] = CharmType.values()[i];
            System.out.println("activated " + i);
        } else if (keycode == Input.Keys.NUM_0) {
            world.player.charmNotches[0] = null;
        }

        GameActionType action = controls.getAction(keycode);
        if (action != null) {
            world.player.doAction(action);
        }

        if (world.zote != null) {
            if ((keycode == controls.interact || keycode == Input.Keys.ENTER) && world.zote.playerIsClose) {
                world.zote.interact();
            }
        }

        return false;
    }

    public boolean handleKeyUp(int keycode) {
        Controls controls = Settings.getInstance().getControls();
        if (keycode == controls.right) {
            world.player.stopMoving(Constants.RIGHT_DIRECTION);
        } else if (keycode == controls.left) {
            world.player.stopMoving(Constants.LEFT_DIRECTION);
        } else if (keycode == controls.focus) {
            world.player.stopFocus();
        } else if (keycode == controls.up) {
            world.player.stopVerticalMovement(Constants.UP_DIRECTION);
        } else if (keycode == controls.down) {
            world.player.stopVerticalMovement(Constants.DOWN_DIRECTION);
        }
        return false;
    }
}
