package com.palm1.jerb.mixin;

import com.palm1.jerb.RecipeBrowserIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.emi.emi.screen.EmiScreenManager", remap = false)
public class EmiOverlayMixin {
    @Inject(method = "isDisabled", at = @At("HEAD"), cancellable = true)
    private static void onIsDisabled(CallbackInfoReturnable<Boolean> cir) {
        if (RecipeBrowserIntegration.shouldDisableBrowserList()) {
            cir.setReturnValue(true);
        }
    }
}
