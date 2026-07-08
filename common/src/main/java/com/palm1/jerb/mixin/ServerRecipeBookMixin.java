package com.palm1.jerb.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerRecipeBook.class)
public abstract class ServerRecipeBookMixin extends RecipeBook {
    @Inject(method = "toNbt", at = @At("HEAD"), cancellable = true)
    public void onSave(CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = new CompoundTag();
        this.getBookSettings().write(tag);
        cir.setReturnValue(tag);
    }

    @Inject(method = "fromNbt", at = @At("HEAD"), cancellable = true)
    public void onLoad(CompoundTag pTag, RecipeManager pRecipeManager, CallbackInfo ci) {
        this.setBookSettings(net.minecraft.stats.RecipeBookSettings.read(pTag));
        this.known.clear();
        this.highlight.clear();
        pRecipeManager.getRecipeIds().forEach(this.known::add);
        ci.cancel();
    }

    @Inject(method = "sendRecipes", at = @At("HEAD"), cancellable = true)
    public void onSendRecipes(ClientboundRecipePacket.State pState, ServerPlayer pPlayer,
            List<ResourceLocation> pRecipes, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "sendInitialRecipeBook", at = @At("HEAD"), cancellable = true)
    public void onSendInitialRecipeBook(ServerPlayer pPlayer, CallbackInfo ci) {
        List<ResourceLocation> allRecipes = pPlayer.server.getRecipeManager().getRecipeIds().toList();
        pPlayer.connection.send(new ClientboundRecipePacket(ClientboundRecipePacket.State.INIT, allRecipes,
                java.util.Collections.emptyList(), this.getBookSettings()));
        ci.cancel();
    }
}
