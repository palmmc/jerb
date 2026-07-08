package com.palm1.jerb.mixin;

import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerRecipeBook.class)
public abstract class ServerRecipeBookMixin extends RecipeBook {
    @Shadow
    protected java.util.Set<ResourceKey<Recipe<?>>> known;

    @Shadow
    protected java.util.Set<ResourceKey<Recipe<?>>> highlight;

    @Inject(method = "pack", at = @At("HEAD"), cancellable = true)
    public void onPack(CallbackInfoReturnable<ServerRecipeBook.Packed> cir) {
        cir.setReturnValue(new ServerRecipeBook.Packed(this.getBookSettings().copy(), List.of(), List.of()));
    }

    @Inject(method = "loadUntrusted", at = @At("HEAD"), cancellable = true)
    public void onLoadUntrusted(ServerRecipeBook.Packed packed,
            java.util.function.Predicate<ResourceKey<Recipe<?>>> recipeLookup,
            CallbackInfo ci) {
        this.getBookSettings().replaceFrom(packed.settings());
        this.known.clear();
        this.highlight.clear();
        ci.cancel();
    }

    @Inject(method = "sendInitialRecipeBook", at = @At("HEAD"))
    public void onSendInitialRecipeBook(ServerPlayer pPlayer, CallbackInfo ci) {
        this.known.clear();
        this.highlight.clear();
        pPlayer.level().getServer().getRecipeManager().getRecipes().forEach(holder -> this.known.add(holder.id()));
    }
}
