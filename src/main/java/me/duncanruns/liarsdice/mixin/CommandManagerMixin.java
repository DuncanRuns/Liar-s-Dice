package me.duncanruns.liarsdice.mixin;

import com.mojang.brigadier.CommandDispatcher;
import me.duncanruns.liarsdice.command.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public abstract class CommandManagerMixin {
    @Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addDiceCommandsMixin(boolean isDedicatedServer, CallbackInfo info) {
        NewDiceGameCommand.register(dispatcher);
        JoinDiceGameCommand.register(dispatcher);
        LeaveDiceGameCommand.register(dispatcher);
        EndDiceGameCommand.register(dispatcher);
        StartDiceGameCommand.register(dispatcher);
        SilentTitleCommand.register(dispatcher);
    }
}
