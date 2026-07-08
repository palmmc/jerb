package com.palm1.jerb.mixin;

import com.palm1.jerb.RecipeBrowserIntegration;
import com.palm1.jerb.JerbRecipeBookPage;
import com.palm1.jerb.JerbRecipeButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(RecipeBookPage.class)
public abstract class RecipeBookPageMixin implements JerbRecipeBookPage {
    @Shadow
    @Final
    private List<RecipeButton> buttons;
    @Shadow
    @Final
    private StateSwitchingButton forwardButton;
    @Shadow
    @Final
    private StateSwitchingButton backButton;
    @Shadow
    private int currentPage;
    @Shadow
    private int totalPages;
    @Shadow
    private Minecraft minecraft;

    @Unique
    private List<ItemStack> jerb$items = new ArrayList<>();

    @Override
    public void jerb$setItems(List<ItemStack> items, boolean resetPage) {
        this.jerb$items = items != null ? items : new ArrayList<>();
        this.totalPages = (this.jerb$items.size() + 19) / 20;
        if (resetPage || this.currentPage >= this.totalPages) {
            this.currentPage = 0;
        }
        this.jerb$updateButtons();
    }

    @Override
    public List<ItemStack> jerb$getItems() {
        return this.jerb$items;
    }

    @Override
    public StateSwitchingButton jerb$getForwardButton() {
        return this.forwardButton;
    }

    @Override
    public StateSwitchingButton jerb$getBackButton() {
        return this.backButton;
    }

    @Override
    public int jerb$getCurrentPage() {
        return this.currentPage;
    }

    @Override
    public int jerb$getTotalPages() {
        return this.totalPages;
    }

    @Override
    public void jerb$setCurrentPage(int page) {
        this.currentPage = page;
    }

    @Override
    public void jerb$updateButtons() {
        int startIndex = this.currentPage * 20;
        for (int i = 0; i < this.buttons.size(); i++) {
            RecipeButton button = this.buttons.get(i);
            int index = startIndex + i;
            if (index < this.jerb$items.size()) {
                ItemStack stack = this.jerb$items.get(index);
                ((JerbRecipeButton) button).jerb$setItem(stack);
                button.visible = true;
            } else {
                ((JerbRecipeButton) button).jerb$setItem(null);
                button.visible = false;
            }
        }
        this.jerb$updateArrowButtons();
    }

    @Unique
    private void jerb$updateArrowButtons() {
        this.forwardButton.visible = this.totalPages > 1 && this.currentPage < this.totalPages - 1;
        this.backButton.visible = this.totalPages > 1 && this.currentPage > 0;
    }

    @Inject(method = "updateCollections", at = @At("HEAD"), cancellable = true)
    public void onUpdateCollections(List<RecipeCollection> collections, boolean resetPage, CallbackInfo ci) {
        if (RecipeBrowserIntegration.isActive() && this.jerb$items != null && !this.jerb$items.isEmpty()) {
            if (resetPage) {
                this.currentPage = 0;
            }
            this.totalPages = (this.jerb$items.size() + 19) / 20;
            if (this.currentPage >= this.totalPages) {
                this.currentPage = 0;
            }
            this.jerb$updateButtons();
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void onMouseClicked(double mouseX, double mouseY, int button, int x, int y, int width, int height,
            CallbackInfoReturnable<Boolean> cir) {
        if (RecipeBrowserIntegration.isActive() && this.minecraft.screen != null) {
            // Check forward/backward buttons first
            if (this.forwardButton.visible && mouseX >= this.forwardButton.getX()
                    && mouseX < this.forwardButton.getX() + this.forwardButton.getWidth()
                    && mouseY >= this.forwardButton.getY()
                    && mouseY < this.forwardButton.getY() + this.forwardButton.getHeight()) {
                if (button == 1) {
                    this.currentPage = this.totalPages - 1;
                } else if (button == 0) {
                    this.currentPage++;
                } else {
                    return;
                }
                net.minecraft.client.resources.sounds.SimpleSoundInstance sound = net.minecraft.client.resources.sounds.SimpleSoundInstance
                        .forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F);
                this.minecraft.getSoundManager().play(sound);
                this.jerb$updateButtons();
                cir.setReturnValue(true);
                return;
            }
            if (this.backButton.visible && mouseX >= this.backButton.getX()
                    && mouseX < this.backButton.getX() + this.backButton.getWidth() && mouseY >= this.backButton.getY()
                    && mouseY < this.backButton.getY() + this.backButton.getHeight()) {
                if (button == 1) {
                    this.currentPage = 0;
                } else if (button == 0) {
                    this.currentPage--;
                } else {
                    return;
                }
                net.minecraft.client.resources.sounds.SimpleSoundInstance sound = net.minecraft.client.resources.sounds.SimpleSoundInstance
                        .forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F);
                this.minecraft.getSoundManager().play(sound);
                this.jerb$updateButtons();
                cir.setReturnValue(true);
                return;
            }

            // Check recipe buttons click
            for (RecipeButton recipeButton : this.buttons) {
                if (recipeButton.visible && recipeButton.mouseClicked(mouseX, mouseY, button)) {
                    ItemStack stack = ((JerbRecipeButton) recipeButton).jerb$getItem();
                    if (stack != null && !stack.isEmpty()) {
                        if (button == 0) {
                            RecipeBrowserIntegration.showRecipes(stack);
                        } else if (button == 1) {
                            RecipeBrowserIntegration.showUsages(stack);
                        }
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        }
    }
}
