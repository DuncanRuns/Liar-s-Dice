package me.duncanruns.liarsdice.logic;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LiarsDiceGame {
    private final int startingDice;
    private final Random random = new SecureRandom();
    private final MinecraftServer minecraftServer;
    private final List<Entity> relatedEntities;
    private final BlockPos tablePos;
    private final boolean wildOnes;
    private int stage;
    private int round;
    private Call lastCall;
    private List<DicePlayer> players;
    private DicePlayer currentPlayer;
    /*
    STAGES:
    0 - Not Started
    1 - Waiting for decision
    2 - Playing animation
    3 - Ended
     */


    public LiarsDiceGame(int startingDice, MinecraftServer minecraftServer, BlockPos tablePos, boolean wildOnes) {
        this.startingDice = startingDice;
        this.minecraftServer = minecraftServer;
        this.stage = 0;
        this.tablePos = tablePos;
        relatedEntities = new ArrayList<>();
        players = new ArrayList<>();
        currentPlayer = null;
        round = 0;
        this.wildOnes = wildOnes;
    }

    private void clearAllEntities() {
        for (Entity entity : relatedEntities) {
            entity.kill();
        }
        relatedEntities.clear();
    }

    public void end() {
        clearAllEntities();
        stage = 3;
        for (DicePlayer player : players) {
            player.tell(new LiteralText("Liar's dice game has ended."));
        }
    }

    public boolean isJoinable() {
        return stage == 0;
    }

    public boolean join(String playerName) {
        if (isJoinable()) {
            players.add(new DicePlayer(startingDice, playerName, minecraftServer));
            return true;
        }
        return false;
    }

    public DicePlayer getPlayer(String playerName) {
        for (DicePlayer player : players) {
            if (player.getPlayerName().equals(playerName)) {
                return player;
            }
        }
        return null;
    }

    public boolean hasPlayer(String playerName) {
        return getPlayer(playerName) != null;
    }

    public boolean leave(String playerName) {
        DicePlayer player = getPlayer(playerName);
        if (player != null) {
            players.remove(player);
            return true;
        }
        return false;
    }

    public boolean isPlayersTurn(String playerName) {
        return stage == 1 && currentPlayer != null && hasPlayer(playerName) && getPlayer(playerName).equals(currentPlayer);
    }

    public void tick() {
        switch (stage) {
            case 0:
                preGameTick();
                break;
            case 1:
                waitingForCall();
                break;
            case 2:
                break;
        }
    }

    private void waitingForCall() {
        for (DicePlayer player : players) {
            if (player.equals(currentPlayer)) {
                player.tellActionBar("It is your turn.", "green", true);
            } else {
                player.tellActionBar("It is not your turn.", "gray", false);
            }
        }
    }

    private void preGameTick() {
        tellAllPlayers(new LiteralText("Waiting for start... Total Players: " + players.size()), true, "white", true);
    }

    private void tellAllPlayers(Text message, boolean actionBar, String color, boolean bold) {
        if (actionBar) {
            for (DicePlayer player : players) {
                player.tellActionBar(message.asString(), color, bold);
            }
        } else {
            for (DicePlayer player : players) {
                player.tell(message);
            }
        }
    }

    private void tellAllPlayers(Text message) {
        tellAllPlayers(message, false, "", false);
    }

    private void tellAllPlayersCurrentRoll() {
        for (DicePlayer player : players) {
            player.tellCurrentRoll();
        }
    }

    public boolean hasEnded() {
        return stage == 3;
    }

    private void shufflePlayers() {
        List<DicePlayer> newList = new ArrayList<>();
        while (!players.isEmpty()) {
            DicePlayer player = players.get(random.nextInt(players.size()));
            newList.add(player);
            players.remove(player);
        }
        players = newList;
    }

    private void rollAllDice() {
        for (DicePlayer player : players) {
            player.rollDice(random);
        }
    }

    public int getTotalDice() {
        int total = 0;
        for (DicePlayer player : players) {
            total += player.getTotalDice();
        }
        return total;
    }

    private void nextRound() {
        round++;
        rollAllDice();
        lastCall = Call.STARTER;
        if (currentPlayer == null) {
            currentPlayer = players.get(0);
        }
        tellAllPlayers(new LiteralText("--------------------"));
        tellAllPlayers(new LiteralText("Round " + round).formatted(Formatting.BOLD).formatted(Formatting.AQUA));
        for (DicePlayer player : players) {
            Text playerText = new LiteralText(player.getPlayerName() + " - ");
            for (int i = 0; i < player.getTotalDice(); i++) {
                playerText.append(new LiteralText("■"));
            }
            if (player.equals(currentPlayer)) {
                playerText.append(new LiteralText(" <---").formatted(Formatting.GREEN));
            }
            tellAllPlayers(playerText);
        }
        tellAllPlayersCurrentRoll();
        stage = 1;
    }

    public boolean startGame() {
        if (players.size() < 2)
            return false;
        if (stage == 0) {
            shufflePlayers();
            nextRound();
            return true;
        }
        return false;
    }

    private CountResults countDice(int dice) {
        CountResults countResults = new CountResults();
        for (DicePlayer player : players) {
            int count = player.countDice(dice, wildOnes);
            if (count > 0) {
                countResults.add(player, count);
            }
        }
        return countResults;
    }

    public void makeCall(Call call) {
        if (call.liar) {
            DicePlayer loser;
            CountResults countResults = countDice(lastCall.dice);
            int actualAmount = countResults.getTotal();
            tellAllPlayers(new LiteralText(""));

            String summary = "";
            if (countResults.getPlayerList().size() == 0) {
                summary = "Nobody had any " + lastCall.dice + "'s.";
            } else {
                for (Pair<DicePlayer, Integer> pair : countResults.getPlayerList()) {
                    if (!summary.equals("")) {
                        summary += ", ";
                    }
                    int count = pair.getRight();
                    summary += pair.getLeft().getPlayerName() + " had " + (count == 1 ? "a " : pair.getRight() + " ") + lastCall.dice + (count == 1 ? "" : "'s");
                }
                summary += ", totalling " + actualAmount + " " + lastCall.dice + (actualAmount == 1 ? "" : "'s") + ".";
            }
            tellAllPlayers(new LiteralText(summary));

            if (lastCall.amount > actualAmount) {
                loser = lastCall.dicePlayer;
                assert loser != null;
                tellAllPlayers(new LiteralText(lastCall.dicePlayer.getPlayerName() + " was a liar!"));
            } else {
                loser = call.dicePlayer;
                assert loser != null;
                assert lastCall.dicePlayer != null;
                tellAllPlayers(new LiteralText(lastCall.dicePlayer.getPlayerName() + " was not a liar!"));
            }
            loser.loseDice();
            tellAllPlayers(new LiteralText(loser.getPlayerName() + " has lost a dice!"));
            if (loser.getTotalDice() == 0) {
                int index = players.indexOf(loser);
                index++;
                if (index >= players.size()) {
                    index = 0;
                }
                currentPlayer = players.get(index);
                players.remove(loser);
                tellAllPlayers(new LiteralText(loser.getPlayerName() + " has been removed from the game."));
                loser.tell(new LiteralText("You have lost liar's dice and have been removed from the game.").formatted(Formatting.RED).formatted(Formatting.BOLD));
            } else {
                currentPlayer = loser;
            }
            if (players.size() == 1) {
                DicePlayer winner = players.get(0);
                winner.tell(new LiteralText("You have won liar's dice!").formatted(Formatting.GREEN).formatted(Formatting.BOLD));
                end();
            } else {
                nextRound();
            }
        } else {
            lastCall = call;
            int index = players.indexOf(call.dicePlayer);
            index++;
            if (index >= players.size()) {
                index = 0;
            }
            currentPlayer = players.get(index);
        }
    }

    public Call getLastCall() {
        return lastCall;
    }

    public void givePlayerInfo(DicePlayer joinedPlayer) {
/*

        tellAllPlayers(new LiteralText("--------------------"));
        tellAllPlayers(new LiteralText("Round " + round).formatted(Formatting.BOLD).formatted(Formatting.AQUA));
        for (DicePlayer player : players) {
            Text playerText = new LiteralText(player.getPlayerName() + " - ");
            for (int i = 0; i < player.getTotalDice(); i++) {
                playerText.append(new LiteralText("■"));
            }
            if (player.equals(currentPlayer)) {
                playerText.append(new LiteralText(" <---").formatted(Formatting.GREEN));
            }
            tellAllPlayers(playerText);
        }
        tellAllPlayersCurrentRoll();
 */
        joinedPlayer.tell(new LiteralText("--------------------"));
        joinedPlayer.tell(new LiteralText("Round " + round).formatted(Formatting.BOLD).formatted(Formatting.AQUA));
        for (DicePlayer player : players) {
            Text playerText = new LiteralText(player.getPlayerName() + " - ");
            for (int i = 0; i < player.getTotalDice(); i++) {
                playerText.append(new LiteralText("■"));
            }
            if (player.equals(currentPlayer)) {
                playerText.append(new LiteralText(" <---").formatted(Formatting.GREEN));
            }
            joinedPlayer.tell(playerText);
        }
        joinedPlayer.tellCurrentRoll();
        if (!lastCall.equals(Call.STARTER)) {
            assert lastCall.dicePlayer != null;
            if (lastCall.liar) {
                joinedPlayer.tell(new LiteralText("<" + lastCall.dicePlayer.getPlayerName() + "> Liar!"));
            } else {
                joinedPlayer.tell(new LiteralText("<" + lastCall.dicePlayer.getPlayerName() + "> " + lastCall.amount + " " + lastCall.dice + (lastCall.amount == 1 ? "" : "'s")));
            }
        }
    }

    public boolean hasStarted() {
        return stage != 0;
    }
}
