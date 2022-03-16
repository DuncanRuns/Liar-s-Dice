package me.duncanruns.liarsdice.mixin;

import me.duncanruns.liarsdice.LiarsDice;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "tickWorlds", at = @At("TAIL"))
    private void diceGameTickMixin(BooleanSupplier shouldKeepTicking, CallbackInfo info) {
        if (LiarsDice.getLiarsDiceGame() != null) {
            LiarsDice.getLiarsDiceGame().tick();
        }
    }
}
