package com.hollowknight.models.gamedata;

import com.hollowknight.models.Player;
import com.hollowknight.models.enums.GameLevel;

public class GameSave {
    public GameLevel gameLevel;
    public Player player;

    private GameSave(GameLevel gameLevel, Player player) {
        this.gameLevel = gameLevel;
        this.player = player;
    }

    public static GameSave gameStart() {
        return new GameSave(GameLevel.FORGOTTEN_CROSSROADS, new Player(GameLevel.FORGOTTEN_CROSSROADS.getSpawnPoint()));
    }
}
