package com.palm1.jerb.mixin;

import net.minecraft.client.gui.GuiGraphics;
import com.palm1.jerb.JerbTabButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeBookTabButton.class)
public abstract class RecipeBookTabButtonMixin implements JerbTabButton {
    @Unique
    private CreativeModeTab jerb$creativeTab;

    @Override
    public void jerb$setCreativeTab(CreativeModeTab tab) {
        this.jerb$creativeTab = tab;
        if (tab != null) {
            ((RecipeBookTabButton) (Object) this).setTooltip(Tooltip.create(tab.getDisplayName()));
        }
    }

    @Override
    public CreativeModeTab jerb$getCreativeTab() {
        return this.jerb$creativeTab;
    }

    @Inject(method = "renderIcon", at = @At("HEAD"), cancellable = true)
    public void onRenderIcon(GuiGraphics guiGraphics, ItemRenderer itemRenderer, CallbackInfo ci) {
        if (this.jerb$creativeTab != null) {
            ItemStack icon = this.jerb$creativeTab.getIconItem();
            int xOffset = ((RecipeBookTabButton) (Object) this).isStateTriggered() ? -2 : 0;
            int x = ((RecipeBookTabButton) (Object) this).getX() + 9 + xOffset;
            int y = ((RecipeBookTabButton) (Object) this).getY() + 5;
            guiGraphics.renderFakeItem(icon, x, y);
            ci.cancel();
        }
    }
}
