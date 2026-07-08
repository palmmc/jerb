package com.palm1.jerb.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import com.palm1.jerb.JerbRecipeButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
import java.util.List;

@Mixin(RecipeButton.class)
public abstract class RecipeButtonMixin extends AbstractWidget implements JerbRecipeButton {
    protected RecipeButtonMixin(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    @Unique
    private ItemStack jerb$item;

    @Override
    public void jerb$setItem(ItemStack item) {
        this.jerb$item = item;
    }

    @Override
    public ItemStack jerb$getItem() {
        return this.jerb$item;
    }

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    public void onRenderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.jerb$item != null) {
            ResourceLocation sprite;
            boolean craftable = com.palm1.jerb.RecipeBrowserIntegration.isCraftable(this.jerb$item.getItem());
            if (this.isHovered()) {
                sprite = ResourceLocation.withDefaultNamespace(
                        craftable ? "recipe_book/slot_many_craftable" : "recipe_book/slot_many_uncraftable");
            } else {
                sprite = ResourceLocation.withDefaultNamespace(
                        craftable ? "recipe_book/slot_craftable" : "recipe_book/slot_uncraftable");
            }
            guiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.width, this.height);
            guiGraphics.renderFakeItem(this.jerb$item, this.getX() + 4, this.getY() + 4);
            ci.cancel();
        }
    }

    @Inject(method = "getTooltipText", at = @At("HEAD"), cancellable = true)
    public void onGetTooltipText(CallbackInfoReturnable<List<Component>> cir) {
        if (this.jerb$item != null) {
            Screen screen = net.minecraft.client.Minecraft.getInstance().screen;
            if (screen != null) {
                cir.setReturnValue(
                        screen.getTooltipFromItem(net.minecraft.client.Minecraft.getInstance(), this.jerb$item));
            }
        }
    }

    @Inject(method = "getOrderedRecipes", at = @At("HEAD"), cancellable = true)
    public void onGetOrderedRecipes(CallbackInfoReturnable<List<RecipeHolder<?>>> cir) {
        if (this.jerb$item != null) {
            cir.setReturnValue(List.of());
        }
    }

    @Inject(method = "updateWidgetNarration", at = @At("HEAD"), cancellable = true)
    public void onUpdateWidgetNarration(NarrationElementOutput narrationElementOutput, CallbackInfo ci) {
        if (this.jerb$item != null) {
            narrationElementOutput.add(NarratedElementType.TITLE, this.jerb$item.getHoverName());
            ci.cancel();
        }
    }
}
