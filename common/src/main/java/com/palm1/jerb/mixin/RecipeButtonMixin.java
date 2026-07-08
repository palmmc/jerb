package com.palm1.jerb.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import com.palm1.jerb.JerbRecipeButton;
import com.palm1.jerb.RecipeBrowserIntegration;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "getDisplayStack", at = @At("HEAD"), cancellable = true)
    public void onGetDisplayStack(CallbackInfoReturnable<ItemStack> cir) {
        if (this.jerb$item != null) {
            cir.setReturnValue(this.jerb$item);
        }
    }

    @Inject(method = "extractWidgetRenderState", at = @At("HEAD"), cancellable = true)
    public void onExtractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick,
            CallbackInfo ci) {
        if (this.jerb$item != null) {
            Identifier sprite;
            boolean craftable = RecipeBrowserIntegration.isCraftable(this.jerb$item.getItem());
            if (this.isHovered()) {
                sprite = Identifier.withDefaultNamespace(
                        craftable ? "recipe_book/slot_many_craftable" : "recipe_book/slot_many_uncraftable");
            } else {
                sprite = Identifier.withDefaultNamespace(
                        craftable ? "recipe_book/slot_craftable" : "recipe_book/slot_uncraftable");
            }
            extractor.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, sprite, this.getX(),
                    this.getY(), this.width, this.height);
            extractor.fakeItem(this.jerb$item, this.getX() + 4, this.getY() + 4);
            ci.cancel();
        }
    }

    @Inject(method = "getTooltipText", at = @At("HEAD"), cancellable = true)
    public void onGetTooltipText(ItemStack stack, CallbackInfoReturnable<List<Component>> cir) {
        if (this.jerb$item != null) {
            Screen screen = Minecraft.getInstance().gui.screen();
            if (screen != null) {
                cir.setReturnValue(
                        screen.getTooltipFromItem(Minecraft.getInstance(), this.jerb$item));
            }
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
