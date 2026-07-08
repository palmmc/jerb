package com.palm1.jerb.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.emi.emi.config.EmiConfig", remap = false)
public class EmiConfigMixin {
    @Inject(method = "loadConfig", at = @At("TAIL"))
    private static void onLoadConfig(CallbackInfo ci) {
        try {
            dev.emi.emi.config.EmiConfig.recipeBookAction = dev.emi.emi.config.RecipeBookAction.DEFAULT;
        } catch (Throwable ignored) {}
    }
}
