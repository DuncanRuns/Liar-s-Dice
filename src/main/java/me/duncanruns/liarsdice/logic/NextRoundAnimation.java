package me.duncanruns.liarsdice.logic;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class NextRoundAnimation extends Animation {

    private final LiarsDiceGame game;
    private final Text summaryText, liarText, lostDiceText, removedFromGameText;
    private final DicePlayer completeLoser;
    private final boolean someoneGetsRemoved;

    public NextRoundAnimation(LiarsDiceGame game, Text summaryText, Text liarText, Text lostDiceText, @Nullable Text removedFromGameText, @Nullable DicePlayer completeLoser) {
        super();
        this.game = game;
        this.summaryText = summaryText;
        this.liarText = liarText;
        this.lostDiceText = lostDiceText;
        this.removedFromGameText = removedFromGameText;
        this.completeLoser = completeLoser;
        this.someoneGetsRemoved = completeLoser != null && removedFromGameText != null;
    }

    @Override
    public void tick() {
        super.tick();
        if (!someoneGetsRemoved) {
            if (count == 30) {
                game.tellEveryone(summaryText);
            } else if (count == 60) {
                game.tellEveryone(liarText);
            } else if (count == 90) {
                game.tellEveryone(lostDiceText);
            } else if (count == 150) {
                game.checkNextRound();
            }
        } else {
            assert completeLoser != null && removedFromGameText != null;
            if (count == 30) {
                game.tellEveryone(summaryText);
            } else if (count == 60) {
                game.tellEveryone(liarText);
            } else if (count == 90) {
                game.tellEveryone(lostDiceText);
            } else if (count == 120) {
                game.tellEveryone(removedFromGameText);
                completeLoser.tell(new LiteralText("You have lost liar's dice and have been removed from the game.").formatted(Formatting.RED).formatted(Formatting.BOLD));
            } else if (count == 180) {
                game.checkNextRound();
            }
        }
    }

    @Override
    public int getLength() {
        return someoneGetsRemoved ? 180 : 150;
    }
}
