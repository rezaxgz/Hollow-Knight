package com.hollowknight.models.gamedata;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.hollowknight.models.enums.GameRegion;
import com.hollowknight.models.player.CharmType;
import com.hollowknight.models.player.Player;

public class GameSave implements Json.Serializable {

    // --- Core Game Data ---
    public GameRegion currentRegion;
    public Player player;
    public String name;
    public int slot;

    // --- Progress Tracking Stats ---
    public long totalPassedTime = 0;
    public int numberOfEnemiesKilled = 0;
    public int numberOfDeaths = 0;

    // --- Boss Fight State ---
    public boolean bossFightActivated = false;
    public boolean bossFightCompleted = false;
    public int bossHp = 400;
    public String bossState = "IDLE";
    public float bossX = 0f;
    public float bossY = 0f;

    // --- Initialization ---

    /**
     * Required empty constructor for libGDX JSON deserialization.
     */
    public GameSave() {
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

    // --- Serialization Methods ---

    @Override
    public void write(Json json) {
        // Core Data
        json.writeValue("currentRegion", currentRegion.name());
        json.writeValue("worldName", name);

        // Player Spatial Data
        json.writeValue("playerX", player.position.x);
        json.writeValue("playerY", player.position.y);
        json.writeValue("respawnX", player.respawnPosition.x);
        json.writeValue("respawnY", player.respawnPosition.y);

        // Player Vitals
        json.writeValue("health", player.getVitals().getHealth());
        json.writeValue("souls", player.getVitals().getSouls());

        // Equipped Charms
        String[] charms = new String[3];
        for (int i = 0; i < 3; i++) {
            charms[i] = player.charmNotches[i] != null ? player.charmNotches[i].name() : null;
        }
        json.writeValue("charmNotches", charms);

        // Boss State
        json.writeValue("bossFightActivated", bossFightActivated);
        json.writeValue("bossFightCompleted", bossFightCompleted);
        json.writeValue("bossHp", bossHp);
        json.writeValue("bossState", bossState);
        json.writeValue("bossX", bossX);
        json.writeValue("bossY", bossY);

        // World Stats
        json.writeValue("totalPassedTime", totalPassedTime);
        json.writeValue("numberOfEnemiesKilled", numberOfEnemiesKilled);
        json.writeValue("numberOfDeaths", numberOfDeaths);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        // Core Data
        this.currentRegion = GameRegion.valueOf(jsonData.getString("currentRegion", "FORGOTTEN_CROSSROADS"));
        this.name = jsonData.getString("worldName", "new world");

        // Player Spatial Data Reconstruct
        Vector2 respawn = new Vector2(jsonData.getFloat("respawnX"), jsonData.getFloat("respawnY"));
        this.player = new Player(respawn);
        this.player.position.set(jsonData.getFloat("playerX"), jsonData.getFloat("playerY"));

        // Player Vitals Reconstruct
        int loadedHealth = jsonData.getInt("health");
        while (this.player.getVitals().getHealth() > loadedHealth) {
            this.player.getVitals().takeDamage();
        }

        int loadedSouls = jsonData.getInt("souls");
        this.player.getVitals().addSouls(loadedSouls - this.player.getVitals().getSouls());

        // Equipped Charms Reconstruct
        JsonValue charmsArray = jsonData.get("charmNotches");
        if (charmsArray != null && charmsArray.isArray()) {
            for (int i = 0; i < 3; i++) {
                String charmName = charmsArray.get(i).asString();
                if (charmName != null) {
                    this.player.charmNotches[i] = CharmType.valueOf(charmName);
                }
            }
        }

        // Boss State Reconstruct
        this.bossFightActivated = jsonData.getBoolean("bossFightActivated", false);
        this.bossFightCompleted = jsonData.getBoolean("bossFightCompleted", false);
        this.bossHp = jsonData.getInt("bossHp", 400);
        this.bossState = jsonData.getString("bossState", "IDLE");
        this.bossX = jsonData.getFloat("bossX", 0f);
        this.bossY = jsonData.getFloat("bossY", 0f);

        // World Stats Reconstruct
        this.totalPassedTime = jsonData.getLong("totalPassedTime", 0);
        this.numberOfEnemiesKilled = jsonData.getInt("numberOfEnemiesKilled", 0);
        this.numberOfDeaths = jsonData.getInt("numberOfDeaths", 0);
    }

    // --- Accessors ---

    public String getWorldName() {
        return name;
    }
}