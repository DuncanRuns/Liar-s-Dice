package me.duncanruns.liarsdice.mixin;

import me.duncanruns.liarsdice.LiarsDice;
import me.duncanruns.liarsdice.logic.DicePlayer;
import me.duncanruns.liarsdice.logic.LiarsDiceGame;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void updatePlayerMixin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
        if (LiarsDice.hasDiceGame()) {
            LiarsDiceGame game = LiarsDice.getLiarsDiceGame();
            if(game.hasPlayer(player.getGameProfile().getName())){
                DicePlayer dicePlayer = game.getPlayer(player.getGameProfile().getName());
                game.givePlayerInfo(dicePlayer);
            }
        }
    }
}
