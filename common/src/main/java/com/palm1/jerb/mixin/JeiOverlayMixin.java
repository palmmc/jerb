package com.palm1.jerb.mixin;

import com.palm1.jerb.RecipeBrowserIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "mezz.jei.gui.overlay.IngredientListOverlay", remap = false)
public class JeiOverlayMixin {
    @Inject(method = "isListDisplayed", at = @At("HEAD"), cancellable = true)
    public void onIsListDisplayed(CallbackInfoReturnable<Boolean> cir) {
        if (RecipeBrowserIntegration.shouldDisableBrowserList()) {
            cir.setReturnValue(false);
        }
    }
}
