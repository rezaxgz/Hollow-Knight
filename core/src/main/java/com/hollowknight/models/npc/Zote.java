package com.hollowknight.models.npc;

import com.badlogic.gdx.math.Vector2;
import com.hollowknight.models.player.Player;

public class Zote {
    public Vector2 position;
    public ZoteAnimation animation = ZoteAnimation.IDLE;
    public float animationTime = 0;

    // Interaction states
    public boolean playerIsClose = false;
    public boolean isTalking = false;

    // Dialogue properties
    public int dialogueIndex = 0;
    public String[] dialogues = {
            "Look at meeeee!",
            "I'm as helpless as a kitten up a tree",
            "And I feel like I'm clinging to a cloud",
            "I can't understand I get misty just holding your hand",
            "Misty!!!!!!!!"
    };

    public Zote(Vector2 position) {
        this.position = position;
    }

    public void update(float delta, Player player) {
        animationTime += delta;

        // Calculate distance to player (adjust 100f to fit your game's scale)
        float distance = position.dst(player.position);
        playerIsClose = distance < 200f;

        // Automatically cancel talking if the player walks away
        if (!playerIsClose && isTalking) {
            isTalking = false;
            animation = ZoteAnimation.IDLE;
            dialogueIndex = 0;
        }
    }

    public void interact() {
        if (playerIsClose && !isTalking) {
            isTalking = true;
            animation = ZoteAnimation.TALK;
            dialogueIndex = 0;
            animationTime = 0; // Reset animation time for smooth transition
        }
    }

    public void advanceDialogue() {
        if (isTalking) {
            dialogueIndex++;
            if (dialogueIndex >= dialogues.length) {
                // End dialogue sequence
                isTalking = false;
                animation = ZoteAnimation.IDLE;
                dialogueIndex = 0;
            }
        }
    }
}