package com.hollowknight.models.npc;

import java.util.List;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hollowknight.controller.AudioController;
import com.hollowknight.models.player.Player;
import com.hollowknight.views.GameAssetManager;

public class Zote {

    // --- Core Properties ---
    public Vector2 position;
    public ZoteAnimation animation = ZoteAnimation.IDLE;
    public float animationTime = 0;
    public final float WIDTH = 64f;
    public final float HEIGHT = 64f;
    public boolean isFacingRight = false;

    // --- Interaction States ---
    public boolean playerIsClose = false;
    public boolean isTalking = false;

    // --- Anger Properties ---
    public boolean isAngry = false;
    public float angryTimer = 0;
    public final float ANGRY_DURATION = 3.5f * 2f;
    public final float MOVE_SPEED = 150f;

    // --- Dialogue Properties ---
    public int dialogueIndex = 0;
    public boolean hasCompletedFirstDialogue = false;
    public int currentRuleIndex = 0;
    public String currentText = "";

    public String[] dialogues = {
            "Look at meeeee!",
            "I'm as helpless as a kitten up a tree",
            "And I feel like I'm clinging to a cloud",
            "I can't understand I get misty just holding your hand",
            "Misty!!!!!!!!"
    };

    public final String[] rules = {
            "Precept One: 'Always Win Your Battles'.\nLosing a battle earns you nothing and teaches you nothing. Win your battles, or don't engage in them at all!\n",
            "Precept Two: 'Never Let Them Laugh at You'.\nFools laugh at everything, even at their superiors. But beware, laughter isn't harmless! Laughter spreads like a disease, and soon everyone is laughing at you.\nYou need to strike at the source of this perverse merriment quickly to stop it from spreading.\n",
            "Precept Three: 'Always Be Rested'.\nFighting and adventuring take their toll on your body. When you rest, your body strengthens and repairs itself. The longer you rest, the stronger you become.\n",
            "Precept Four: 'Forget Your Past'.\nThe past is painful, and thinking about your past can only bring you misery. Think about something else instead, such as the future, or some food.\n",
            "Precept Five: 'Strength Beats Strength'.\nIs your opponent strong? No matter! Simply overcome their strength with even more strength, and they'll soon be defeated.\n",
            "Precept Six: 'Choose Your Own Fate'.\nOur elders teach that our fate is chosen for us before we are even born. I disagree.\n",
            "Precept Seven: 'Mourn Not the Dead'.\nWhen we die, do things get better for us or worse? There's no way to tell, so we shouldn't bother mourning. Or celebrating for that matter.\n",
            "Precept Eight: 'Travel Alone'.\nYou can rely on nobody, and nobody will always be loyal. Therefore, nobody should be your constant companion.\n",
            "Precept Nine: 'Keep Your Home Tidy'.\nYour home is where you keep your most prized possession - yourself. Therefore, you should make an effort to keep it nice and clean.\n",
            "Precept Ten: 'Keep Your Weapon Sharp'.\nI make sure that my weapon, 'Life Ender', is kept well-sharpened at all times. This makes it much easier to cut things.\n",
            "Precept Eleven: 'Mothers Will Always Betray You'.\nThis precept explains itself.\n",
            "Precept Twelve: 'Keep Your Cloak Dry'.\nIf your cloak gets wet, dry it as soon as you can. Wearing wet cloaks is unpleasant, and can lead to illness.\n",
            "Precept Thirteen: 'Never Be Afraid'.\nFear can only hold you back. Facing your fears can be a tremendous effort. Therefore, you should just not be afraid in the first place.\n",
            "Precept Fourteen: 'Respect Your Superiors'.\nIf someone is your superior in strength or intellect or both, you need to show them your respect. Don't ignore them or laugh at them.\n",
            "Precept Fifteen: 'One Foe, One Blow'.\nYou should only use a single blow to defeat an enemy. Any more is a waste. Also, by counting your blows as you fight, you'll know how many foes you've defeated.\n",
            "Precept Sixteen: 'Don't Hesitate'.\nOnce you've made a decision, carry it out and don't look back. You'll achieve much more this way.\n",
            "Precept Seventeen: 'Believe In Your Strength'.\nOthers may doubt you, but there's someone you can always trust. Yourself. Make sure to believe in your own strength, and you will never falter.\n",
            "Precept Eighteen: 'Seek Truth in the Darkness'.\nThis precept also explains itself.\n",
            "Precept Nineteen: 'If You Try, Succeed'.\nIf you're going to attempt something, make sure you achieve it. If you do not succeed, then you have actually failed! Avoid this at all costs.\n",
            "Precept Twenty: 'Speak Only the Truth'.\nWhen speaking to someone, it is courteous and also efficient to speak truthfully. Beware though that speaking truthfully may make you enemies. This is something you'll have to bear."
    };

    // --- Initialization ---
    public Zote(Vector2 position) {
        this.position = position;
    }

    // --- Core Update Loop ---
    public void update(float delta, Player player, List<Rectangle> solidBlocks) {
        animationTime += delta;

        float distance = position.dst(player.position);
        playerIsClose = distance < 200f;

        if (isAngry) {
            angryTimer -= delta;

            if (animationTime >= ZoteAnimation.ATTACK.totalDuration) {
                animationTime -= ZoteAnimation.ATTACK.totalDuration;
            }

            if (angryTimer <= 0) {
                isAngry = false;
                animation = ZoteAnimation.IDLE;
                GameAssetManager.zoteAngry.stop();
            } else {
                updateAggressiveMovement(delta, player, solidBlocks);
            }
        } else {
            updatePassiveState();
        }
    }

    // --- AI & State Management ---
    private void updateAggressiveMovement(float delta, Player player, List<Rectangle> solidBlocks) {
        float dir = Math.signum(player.position.x - position.x);
        if (dir != 0) {
            isFacingRight = (dir > 0);
            float nextX = position.x + dir * MOVE_SPEED * delta;

            boolean hasFloor = false;
            boolean hitsWall = false;

            float checkX = dir > 0 ? nextX + WIDTH : nextX;
            Rectangle floorCheck = new Rectangle(checkX, position.y - 5f, 1f, 5f);
            Rectangle wallCheck = new Rectangle(nextX, position.y, WIDTH, HEIGHT);

            for (Rectangle block : solidBlocks) {
                if (block.overlaps(floorCheck)) {
                    hasFloor = true;
                }
                if (block.overlaps(wallCheck)) {
                    hitsWall = true;
                }
            }

            if (hasFloor && !hitsWall) {
                position.x = nextX;
            }
        }
    }

    private void updatePassiveState() {
        if (!playerIsClose && isTalking) {
            isTalking = false;
            animation = ZoteAnimation.IDLE;
            dialogueIndex = 0;
        }
        if (isTalking && animation == ZoteAnimation.IDLE) {
            animation = ZoteAnimation.TALK;
            animationTime = 0;
        }
    }

    public void triggerAnger() {
        if (!isAngry) {
            AudioController.getInstance().playSfxLoop(GameAssetManager.zoteAngry);
        }
        isAngry = true;
        angryTimer = ANGRY_DURATION;
        animation = ZoteAnimation.ATTACK;
        animationTime = 0;
        isTalking = false;
    }

    // --- Interactions ---
    public void interact() {
        if (!hasCompletedFirstDialogue) {
            if (!isTalking) {
                isTalking = true;
                dialogueIndex = 0;
            } else {
                dialogueIndex++;
                if (dialogueIndex >= dialogues.length) {
                    isTalking = false;
                    hasCompletedFirstDialogue = true;
                }
            }
        } else {
            if (!isTalking) {
                isTalking = true;
                currentRuleIndex = MathUtils.random(0, rules.length - 1);
            } else {
                isTalking = false;
            }
        }

        if (isTalking) {
            AudioController.getInstance().playRandomZoteVoice();
        }
    }

    public void advanceDialogue() {
        if (isTalking) {
            dialogueIndex++;
            if (dialogueIndex >= dialogues.length) {
                isTalking = false;
                animation = ZoteAnimation.IDLE;
                dialogueIndex = 0;
            }
        }
    }

    // --- Properties ---
    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, WIDTH, HEIGHT);
    }

    public String getText() {
        if (hasCompletedFirstDialogue) {
            return dialogues[dialogueIndex];
        } else {
            return rules[currentRuleIndex];
        }
    }
}