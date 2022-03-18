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
    private DiceCall lastCall;
    private List<DicePlayer> players;
    private DicePlayer currentPlayer;
    private Animation animation;

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
        this.relatedEntities = new ArrayList<>();
        this.players = new ArrayList<>();
        this.currentPlayer = null;
        this.round = 0;
        this.wildOnes = wildOnes;
        this.animation = null;
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
        tellEveryone(new LiteralText("Liar's dice game has ended."));
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
                animation.tick();
                if (animation.isDone())
                    stage = animation.getNextStage();
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

    public void tellAllPlayers(Text message, boolean actionBar, String color, boolean bold) {
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

    public void tellAllPlayers(Text message) {
        tellAllPlayers(message, false, "", false);
    }

    public void tellEveryone(Text message) {
        minecraftServer.getPlayerManager().broadcastChatMessage(message, false);
    }

    public void tellAllPlayersCurrentRoll() {
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

    public void nextRound() {
        round++;
        rollAllDice();
        lastCall = DiceCall.STARTER;
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
        // Cup shake animation instead of stage = 1.
    }

    public boolean startGame() {
        if (players.size() < 2) return false;
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

    public void checkNextRound() {
        if (players.size() == 1) {
            DicePlayer winner = players.get(0);
            winner.tell(new LiteralText("You have won liar's dice!").formatted(Formatting.GREEN).formatted(Formatting.BOLD));
            end();
        } else {
            nextRound();
        }
    }

    public void makeCall(DiceCall call) {
        if (call.liar) {
            Text summaryText, liarText, lostDiceText;
            Text removedFromGameText = null;
            DicePlayer completeLoser = null;

            DicePlayer loser;
            CountResults countResults = countDice(lastCall.dice);
            int actualAmount = countResults.getTotal();
            tellEveryone(new LiteralText(""));
            StringBuilder summary = new StringBuilder();
            if (countResults.getPlayerList().size() == 0) {
                summary = new StringBuilder("Nobody had any " + lastCall.dice + "'s.");
            } else {
                for (Pair<DicePlayer, Integer> pair : countResults.getPlayerList()) {
                    if (!summary.toString().equals("")) {
                        summary.append(", ");
                    }
                    int count = pair.getRight();
                    summary.append(pair.getLeft().getPlayerName()).append(" had ").append(count == 1 ? "a " : pair.getRight() + " ").append(lastCall.dice).append(count == 1 ? "" : "'s");
                }
                summary.append(", totalling ").append(actualAmount).append(" ").append(lastCall.dice).append(actualAmount == 1 ? "" : "'s").append(".");
            }
            summaryText = new LiteralText(summary.toString());
            if (lastCall.amount > actualAmount) {
                loser = lastCall.dicePlayer;
                assert loser != null;
                liarText = new LiteralText(lastCall.dicePlayer.getPlayerName() + " was a liar!");
            } else {
                loser = call.dicePlayer;
                assert loser != null;
                assert lastCall.dicePlayer != null;
                liarText = new LiteralText(lastCall.dicePlayer.getPlayerName() + " was not a liar!");
            }
            loser.loseDice();
            lostDiceText = new LiteralText(loser.getPlayerName() + " has lost a dice and now has " + loser.getTotalDice() + "!");
            if (loser.getTotalDice() == 0) {
                int index = players.indexOf(loser);
                index++;
                if (index >= players.size()) {
                    index = 0;
                }
                currentPlayer = players.get(index);
                players.remove(loser);
                removedFromGameText = new LiteralText(loser.getPlayerName() + " has been removed from the game.");
                completeLoser = loser;
            } else {
                currentPlayer = loser;
            }
            animation = new LiarCallAnimation(this, summaryText, liarText, lostDiceText, removedFromGameText, completeLoser);
            stage = 2;
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

    public DiceCall getLastCall() {
        return lastCall;
    }

    public void givePlayerInfo(DicePlayer joinedPlayer) {
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
        if (!lastCall.equals(DiceCall.STARTER)) {
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
