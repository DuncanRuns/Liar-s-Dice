package me.duncanruns.liarsdice.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.duncanruns.liarsdice.LiarsDice;
import me.duncanruns.liarsdice.logic.LiarsDiceGame;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public abstract class LeaveDiceGameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher) {
        commandDispatcher.register(CommandManager.literal("leaveliarsdice").executes(context -> execute(context.getSource())));
    }

    private static int execute(ServerCommandSource source) throws CommandSyntaxException {
        if (LiarsDice.hasDiceGame()) {
            if(LiarsDice.getLiarsDiceGame().leave(source.getPlayer().getGameProfile().getName())){
                source.sendFeedback(new LiteralText("You left liar's dice."),false);
                return 1;
            }
            source.sendError(new LiteralText("You are not in a liar's dice game!").formatted(Formatting.RED));
            return 0;
        }
        source.sendError(new LiteralText("No liar's dice game to leave!").formatted(Formatting.RED));
        return 0;
    }
}
