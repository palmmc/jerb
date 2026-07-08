package com.palm1.jerb.mixin;

import com.palm1.jerb.RecipeBrowserIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "me.shedaniel.rei.impl.client.REIRuntimeImpl", remap = false)
public class ReiOverlayMixin {
    @Inject(method = "isOverlayVisible", at = @At("HEAD"), cancellable = true)
    public void onIsOverlayVisible(CallbackInfoReturnable<Boolean> cir) {
        if (RecipeBrowserIntegration.shouldDisableBrowserList()) {
            cir.setReturnValue(false);
        }
    }
}
