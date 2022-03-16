package me.duncanruns.liarsdice.logic;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DicePlayer {
    private final String playerName;
    private final MinecraftServer minecraftServer;
    private List<Integer> rolls;
    private int totalDice;


    public DicePlayer(int totalDice, String playerName, MinecraftServer minecraftServer) {
        this.totalDice = totalDice;
        this.playerName = playerName;
        this.minecraftServer = minecraftServer;
    }

    public void rollDice(Random random) {
        rolls = new ArrayList<>();
        for (int i = 0; i < totalDice; i++) {
            rolls.add(random.nextInt(6) + 1);
        }
    }

    public ServerPlayerEntity getEntity() {
        return minecraftServer.getPlayerManager().getPlayer(playerName);
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isPlayerPresent() {
        return getEntity() != null;
    }

    public Text getCurrentRollString(int highlightedNum, boolean wildOnes) {
        Text text = new LiteralText("");
        boolean a = false;
        for (int i : rolls) {
            if (a) {
                text = text.append(" | ");
            } else {
                a = true;
            }
            if (i == highlightedNum || (wildOnes && i == 1)) {
                text.append(new LiteralText(String.valueOf(i)).formatted(Formatting.GREEN));
            } else {
                text.append(new LiteralText(String.valueOf(i)));
            }
        }
        return text;
    }

    public Text getCurrentRollString(boolean wildOnes) {
        return getCurrentRollString(0, wildOnes);
    }

    public void tellCurrentRoll() {
        if (isPlayerPresent()) {
            getEntity().sendMessage(new LiteralText("--------------------"));
            getEntity().sendMessage(new LiteralText("Current Roll:"));
            getEntity().sendMessage(getCurrentRollString(false));
            getEntity().sendMessage(new LiteralText("--------------------"));
        }
    }

    public void tell(Text message) {
        if (isPlayerPresent()) {
            getEntity().sendMessage(message);
        }
    }

    public void tellActionBar(String message, String color, boolean bold) {
        if (isPlayerPresent()) {
            minecraftServer.getCommandManager().execute(minecraftServer.getCommandSource(), "title " + playerName + " actionbar {\"text\":\"" + message + "\",\"bold\":" + bold + ",\"color\":\"" + color + "\"}");
        }
    }

    public int getTotalDice() {
        return totalDice;
    }

    public int countDice(int dice, boolean wildOnes) {
        int count = 0;
        for (int i : rolls) {
            if (dice == i || (wildOnes && i == 1)) {
                count++;
            }
        }
        return count;
    }

    public void loseDice() {
        totalDice--;
    }
}
