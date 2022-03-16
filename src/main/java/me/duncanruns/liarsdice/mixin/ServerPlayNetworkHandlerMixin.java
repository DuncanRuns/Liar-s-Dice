package me.duncanruns.liarsdice.mixin;

import me.duncanruns.liarsdice.LiarsDice;
import me.duncanruns.liarsdice.logic.Call;
import me.duncanruns.liarsdice.logic.LiarsDiceGame;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "onChatMessage", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Z)V"), cancellable = true)
    private void onChatMixin(ChatMessageC2SPacket packet, CallbackInfo info) {
        if (LiarsDice.hasDiceGame()) {
            LiarsDiceGame game = LiarsDice.getLiarsDiceGame();
            if (!game.hasEnded() && game.hasStarted()) {
                String playerName = player.getGameProfile().getName();
                if (game.hasPlayer(playerName)) {
                    if (game.isPlayersTurn(playerName)) {
                        Call call = new Call(packet.getChatMessage(), game.getTotalDice(), game.getLastCall(), game.getPlayer(playerName));
                        if (call.valid) {
                            if (call.liar) {
                                server.getPlayerManager().broadcastChatMessage(new LiteralText("<" + playerName + "> Liar!"), false);
                            } else {
                                server.getPlayerManager().broadcastChatMessage(new LiteralText("<" + playerName + "> " + call.amount + " " + call.dice + (call.amount == 1 ? "" : "'s")), false);
                            }
                            game.makeCall(call);
                        } else {
                            player.sendMessage(new LiteralText("Invalid call!").formatted(Formatting.RED));
                            if (call.outOfRange) {
                                Call lastCall = game.getLastCall();
                                player.sendMessage(new LiteralText("You must guess an amount between 1 and " + game.getTotalDice() + ".").formatted(Formatting.RED));
                                player.sendMessage(new LiteralText("You must guess a dice between " + lastCall.dice + " and 6.").formatted(Formatting.RED));
                                player.sendMessage(new LiteralText("If you are guessing dice " + lastCall.dice + ", you must guess at least an amount of " + (lastCall.amount + 1) + ".").formatted(Formatting.RED));
                                player.sendMessage(new LiteralText("If it is impossible to meet all the requirements above, you must call liar. You can also call liar at any point except the start of a round.").formatted(Formatting.RED));
                            } else {
                                player.sendMessage(new LiteralText("Examples:").formatted(Formatting.RED));
                                player.sendMessage(new LiteralText("- \"liar\"").formatted(Formatting.RED));
                                player.sendMessage(new LiteralText("- \"4 6's\"").formatted(Formatting.RED));
                                player.sendMessage(new LiteralText("- \"3 2s\"").formatted(Formatting.RED));
                            }
                        }
                    } else {
                        player.sendMessage(new LiteralText("It is not your turn yet!").formatted(Formatting.RED));
                    }
                } else {
                    player.sendMessage(new LiteralText("There is a liar's dice game running!").formatted(Formatting.RED));
                }
                info.cancel();
            }
        }
    }
}
