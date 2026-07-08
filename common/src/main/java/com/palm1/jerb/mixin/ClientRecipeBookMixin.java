package com.palm1.jerb.mixin;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.core.RegistryAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookMixin {
    @Inject(method = "setupCollections", at = @At("HEAD"), cancellable = true)
    public void onSetup(Iterable<?> iterable, RegistryAccess registryAccess, CallbackInfo ci) {
        ci.cancel();
    }
}
