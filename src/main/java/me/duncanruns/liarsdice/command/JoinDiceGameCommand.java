package me.duncanruns.liarsdice.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.duncanruns.liarsdice.LiarsDice;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public abstract class JoinDiceGameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher) {
        commandDispatcher.register(CommandManager.literal("joinliarsdice").executes(context -> execute(context.getSource())));
    }

    private static int execute(ServerCommandSource source) throws CommandSyntaxException {
        try {
            String name = source.getPlayer().getGameProfile().getName();

            if (LiarsDice.getLiarsDiceGame() != null) {

                if (LiarsDice.getLiarsDiceGame().getPlayer(source.getPlayer().getGameProfile().getName()) != null) {
                    source.sendError(new LiteralText("You are already in the game!").formatted(Formatting.RED));
                    return 0;
                }

                if (LiarsDice.getLiarsDiceGame().join(source.getPlayer().getGameProfile().getName())) {
                    source.sendFeedback(new LiteralText("Successfully joined liar's dice."), false);
                    return 1;
                }
                source.sendError(new LiteralText("Could not join game! (In progress or ended)").formatted(Formatting.RED));
                return 0;
            }
            source.sendError(new LiteralText("No liar's dice to join!").formatted(Formatting.RED));
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            source.sendError(new LiteralText("Error occured.").formatted(Formatting.RED));
            return 0;
        }
    }
}
