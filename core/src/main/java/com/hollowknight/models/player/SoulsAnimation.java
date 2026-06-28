package com.hollowknight.models.player;

public class SoulsAnimation {
    private int startFrom;
    private int endOn;
    private float time;
    private float curTime;

    public boolean isOver() {
        return curTime > time;
    }

    public int getSouls() {
        return startFrom + (int) ((curTime / time) * (float) (endOn - startFrom));
    }

    public void update(float delta) {
        curTime += delta;
    }

    public SoulsAnimation(int startFrom, int endOn, float time) {
        this.startFrom = startFrom;
        this.endOn = endOn;
        this.time = time;
        this.curTime = 0;
    }

    public int getEnd() {
        return endOn;
    }
}
