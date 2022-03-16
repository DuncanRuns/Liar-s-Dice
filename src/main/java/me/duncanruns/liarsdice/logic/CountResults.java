package me.duncanruns.liarsdice.logic;

import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CountResults {
    private final List<Pair<DicePlayer, Integer>> playerList;
    private int total;

    public CountResults() {
        total = 0;
        playerList = new ArrayList<>();
    }

    public void add(DicePlayer player, int count) {
        total += count;
        playerList.add(new Pair<>(player, count));
    }

    public List<Pair<DicePlayer, Integer>> getPlayerList() {
        return Collections.unmodifiableList(playerList);
    }

    public int getTotal() {
        return total;
    }
}
