package me.duncanruns.liarsdice.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.duncanruns.liarsdice.LiarsDice;
import me.duncanruns.liarsdice.logic.LiarsDiceGame;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public abstract class NewDiceGameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> commandDispatcher) {
        commandDispatcher.register(CommandManager.literal("newliarsdice").requires(source -> source.hasPermissionLevel(3)).executes(
                context -> execute(context.getSource(), 5)
        ).then(
                CommandManager.argument("startingDice", IntegerArgumentType.integer(1, 10)).executes(
                        context -> execute(context.getSource(), IntegerArgumentType.getInteger(context, "startingDice"))
                )
        ));
    }

    private static int execute(ServerCommandSource source, int startingDice) {
        LiarsDice.setLiarsDiceGame(new LiarsDiceGame(startingDice, source.getMinecraftServer(), new BlockPos(source.getPosition()).add(0, -1, 0), true));
        source.getMinecraftServer().getPlayerManager().broadcastChatMessage(new LiteralText("A liar's dice game has been created."), false);
        source.sendFeedback(new LiteralText("Liar's dice game successfully created.").formatted(Formatting.GREEN), true);
        return 1;
    }
}
