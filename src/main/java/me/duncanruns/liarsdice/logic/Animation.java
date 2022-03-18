package me.duncanruns.liarsdice.logic;

public abstract class Animation {
    protected int count = 0;

    public void tick() {
        count++;
    }

    public int getCount() {
        return count;
    }

    public boolean isDone() {
        return count > getLength();
    }

    public int getNextStage() {
        return 1;
    }

    public abstract int getLength();
}
