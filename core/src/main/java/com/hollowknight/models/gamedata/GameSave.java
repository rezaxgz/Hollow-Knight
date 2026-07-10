package com.hollowknight.models.gamedata;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.hollowknight.models.enums.GameRegion;
import com.hollowknight.models.player.CharmType;
import com.hollowknight.models.player.Player;

public class GameSave implements Json.Serializable {
    public GameRegion currentRegion;
    public Player player;
    public String name;

    public int slot;

    // Progress Tracking Stats
    public long totalPassedTime = 0;
    public int numberOfEnemiesKilled = 0;
    public int numberOfDeaths = 0;

    public boolean bossFightActivated = false;
    public boolean bossFightCompleted = false;
    public int bossHp = 400;
    public String bossState = "IDLE";
    public float bossX = 0f;
    public float bossY = 0f;

    public GameSave() {
        // Required empty constructor for libGDX JSON deserialization
    }

    private GameSave(GameRegion currentRegion, Player player, String name, int slot) {
        this.currentRegion = currentRegion;
        this.player = player;
        this.name = name;
        this.slot = slot;
    }

    public static GameSave gameStart(int slot) {
        return new GameSave(GameRegion.FORGOTTEN_CROSSROADS,
                new Player(GameRegion.getSpawnPoint()),
                "new world", slot);
    }

    @Override
    public void write(Json json) {
        json.writeValue("currentRegion", currentRegion.name());
        // 1. Save Player Coordinates Manually
        json.writeValue("playerX", player.position.x);
        json.writeValue("playerY", player.position.y);
        json.writeValue("respawnX", player.respawnPosition.x);
        json.writeValue("respawnY", player.respawnPosition.y);

        json.writeValue("bossFightActivated", bossFightActivated);
        json.writeValue("bossFightCompleted", bossFightCompleted);
        json.writeValue("bossHp", bossHp);
        json.writeValue("bossState", bossState);
        json.writeValue("bossX", bossX);
        json.writeValue("bossY", bossY);

        // 2. Save Vitals
        json.writeValue("health", player.getVitals().getHealth());
        json.writeValue("souls", player.getVitals().getSouls());

        // 3. Save Equipped Charms
        String[] charms = new String[3];
        for (int i = 0; i < 3; i++) {
            charms[i] = player.charmNotches[i] != null ? player.charmNotches[i].name() : null;
        }
        json.writeValue("charmNotches", charms);

        // 4. Save World Stats
        json.writeValue("totalPassedTime", totalPassedTime);
        json.writeValue("numberOfEnemiesKilled", numberOfEnemiesKilled);
        json.writeValue("numberOfDeaths", numberOfDeaths);

        json.writeValue("worldName", name);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        this.currentRegion = GameRegion.valueOf(jsonData.getString("currentRegion", "FORGOTTEN_CROSSROADS"));
        // 1. Reconstruct the Player object and positions
        Vector2 respawn = new Vector2(jsonData.getFloat("respawnX"), jsonData.getFloat("respawnY"));
        this.player = new Player(respawn);
        this.player.position.set(jsonData.getFloat("playerX"), jsonData.getFloat("playerY"));

        // 2. Reconstruct Vitals cleanly (take damage until it matches the saved state)
        int loadedHealth = jsonData.getInt("health");
        while (this.player.getVitals().getHealth() > loadedHealth) {
            this.player.getVitals().takeDamage();
        }

        // Adjust souls based on default initialization
        int loadedSouls = jsonData.getInt("souls");
        this.player.getVitals().addSouls(loadedSouls - this.player.getVitals().getSouls());

        // 3. Load Charms
        JsonValue charmsArray = jsonData.get("charmNotches");
        if (charmsArray != null && charmsArray.isArray()) {
            for (int i = 0; i < 3; i++) {
                String charmName = charmsArray.get(i).asString();
                if (charmName != null) {
                    this.player.charmNotches[i] = CharmType.valueOf(charmName);
                }
            }
        }

        this.bossFightActivated = jsonData.getBoolean("bossFightActivated", false);
        this.bossFightCompleted = jsonData.getBoolean("bossFightCompleted", false);
        this.bossHp = jsonData.getInt("bossHp", 400);
        this.bossState = jsonData.getString("bossState", "IDLE");
        this.bossX = jsonData.getFloat("bossX", 0f);
        this.bossY = jsonData.getFloat("bossY", 0f);

        // 4. Load World Stats
        this.totalPassedTime = jsonData.getLong("totalPassedTime", 0);
        this.numberOfEnemiesKilled = jsonData.getInt("numberOfEnemiesKilled", 0);
        this.numberOfDeaths = jsonData.getInt("numberOfDeaths", 0);

        this.name = jsonData.getString("worldName", "new world");
    }

    public String getWorldName() {
        return name;
    }
}