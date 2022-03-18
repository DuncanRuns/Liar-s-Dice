package me.duncanruns.liarsdice.logic;

public class DiceCall {
    public static final DiceCall STARTER = new DiceCall();

    public final int amount, dice;
    public final boolean liar, valid, outOfRange;
    public final DicePlayer dicePlayer;

    public DiceCall(String input, int maxDice, DiceCall lastCall, DicePlayer dicePlayer) {
        this.dicePlayer = dicePlayer;
        if (input.toLowerCase().startsWith("liar")) {
            if (lastCall.equals(STARTER)) {
                valid = liar = false;
            } else {
                valid = liar = true;
            }
            dice = amount = 0;
        } else {
            if (input.endsWith("'s")) {
                input = input.substring(0, input.length() - 2);
            } else if (input.endsWith("s")) {
                input = input.substring(0, input.length() - 1);
            }

            String[] args = input.split(" ");

            if (args.length > 2) {
                valid = liar = false;
                dice = amount = 0;
            } else {
                int amount = 0, dice = 0;
                boolean failed = false;
                try {
                    amount = Integer.parseInt(args[0]);
                    dice = Integer.parseInt(args[1]);
                } catch (Exception ignored) {
                    failed = true;
                }
                if (!failed) {
                    boolean valid = true;
                    if (dice < lastCall.dice || dice > 6) {
                        valid = false;
                    } else if (amount < 1 || amount > maxDice) {
                        valid = false;
                    } else if (dice == lastCall.dice && amount <= lastCall.amount) {
                        valid = false;
                    }
                    this.valid = valid;
                    outOfRange = !this.valid;
                    liar = false;
                    this.amount = amount;
                    this.dice = dice;
                    return;
                } else {
                    valid = liar = false;
                    this.dice = this.amount = 0;
                }
            }
        }
        outOfRange = false;
    }

    private DiceCall() {
        amount = dice = 0;
        valid = true;
        liar = outOfRange = false;
        dicePlayer = null;
    }
}
