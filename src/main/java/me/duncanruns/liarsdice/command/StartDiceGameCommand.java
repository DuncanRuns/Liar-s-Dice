package me.duncanruns.liarsdice.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.duncanruns.liarsdice.LiarsDice;
import me.duncanruns.liarsdice.logic.LiarsDiceGame;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public abstract class StartDiceGameCommand {

    public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher) {
        commandDispatcher.register(CommandManager.literal("startliarsdice").requires(source -> source.hasPermissionLevel(3)).executes(context -> execute(context.getSource())));
    }

    private static int execute(ServerCommandSource source) throws CommandSyntaxException {
        if (LiarsDice.hasDiceGame() && !LiarsDice.getLiarsDiceGame().hasEnded()) {
            LiarsDiceGame game = LiarsDice.getLiarsDiceGame();
            if (game.startGame()) {
                if (!game.hasPlayer(source.getPlayer().getGameProfile().getName()))
                    source.sendFeedback(new LiteralText("Liar's dice game started. (Without you)"), true);
                return 1;
            } else {
                source.sendError(new LiteralText("Not enough players or game already started!").formatted(Formatting.RED));
                return 0;
            }
        }
        source.sendError(new LiteralText("No liar's dice game to start!").formatted(Formatting.RED));
        return 0;
    }
}
