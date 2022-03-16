package me.duncanruns.liarsdice.command;

import com.mojang.brigadier.CommandDispatcher;
import me.duncanruns.liarsdice.LiarsDice;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public abstract class EndDiceGameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher) {
        commandDispatcher.register(CommandManager.literal("endliarsdice").requires(source -> source.hasPermissionLevel(3)).executes(context -> execute(context.getSource())));
    }

    private static int execute(ServerCommandSource source) {
        if (LiarsDice.getLiarsDiceGame() != null) {
            LiarsDice.setLiarsDiceGame(null);
            source.sendFeedback(new LiteralText("Ended liar's dice."), true);
            return 1;
        }
        source.sendError(new LiteralText("No liar's dice game to end!").formatted(Formatting.RED));
        return 0;
    }
}
