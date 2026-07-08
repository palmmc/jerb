package com.palm1.jerb.mixin;

import com.palm1.jerb.RecipeBrowserIntegration;
import com.palm1.jerb.JerbRecipeBookPage;
import com.palm1.jerb.JerbTabButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ImageButton;

import java.util.ArrayList;
import java.util.List;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {
    @Shadow
    private RecipeBookPage recipeBookPage;
    @Shadow
    private RecipeBookTabButton selectedTab;
    @Shadow
    private EditBox searchBox;
    @Shadow
    @org.spongepowered.asm.mixin.Final
    private List<RecipeBookTabButton> tabButtons;
    @Shadow
    private int xOffset;
    @Shadow
    private int width;
    @Shadow
    private int height;

    @Shadow
    public abstract boolean isVisible();

    @Shadow
    protected abstract void updateCollections(boolean resetPage, boolean otherParam);

    @Shadow
    protected RecipeBookMenu menu;
    @Shadow
    @org.spongepowered.asm.mixin.Final
    protected StackedItemContents stackedContents;
    @Shadow
    protected CycleButton<Boolean> filterButton;

    @Unique
    private CreativeModeTab jerb$selectedCreativeTab;
    @Unique
    private int jerb$tabPageIndex = 0;
    @Unique
    private int jerb$maxPages = 0;

    @Unique
    private void jerb$rebuildCreativeTabs() {
        this.tabButtons.clear();

        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getConnection() != null) {
                FeatureFlagSet flags = mc.getConnection().enabledFeatures();
                boolean hasOperator = mc.options.operatorItemsTab().get();
                HolderLookup.Provider provider = mc.getConnection().registryAccess();
                CreativeModeTabs.tryRebuildTabContents(flags, hasOperator, provider);
            }
        } catch (Throwable ignored) {
        }

        List<CreativeModeTab> allTabs = new ArrayList<>();
        CreativeModeTab searchTab = null;
        for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
            if (tab.getType() == CreativeModeTab.Type.SEARCH) {
                searchTab = tab;
                break;
            }
        }
        if (searchTab != null) {
            allTabs.add(searchTab);
        }

        java.util.Map<Item, Boolean> entryCache = new java.util.HashMap<>();
        for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
            if (tab.getType() == CreativeModeTab.Type.INVENTORY || tab.getType() == CreativeModeTab.Type.HOTBAR
                    || tab.getType() == CreativeModeTab.Type.SEARCH) {
                continue;
            }
            boolean hasEntries = false;
            for (ItemStack stack : tab.getDisplayItems()) {
                if (stack != null && !stack.isEmpty()) {
                    Item item = stack.getItem();
                    boolean hasEntry = entryCache.computeIfAbsent(item,
                            i -> {
                                if (!RecipeBrowserIntegration.hasRecipeBrowserEntries(stack)) {
                                    return false;
                                }
                                if (!RecipeBrowserIntegration.isInventoryContext()
                                        && RecipeBrowserIntegration.getActiveRecipeBookType() != null) {
                                    return RecipeBrowserIntegration.hasCompatibleRecipe(i,
                                            RecipeBrowserIntegration.getActiveRecipeBookType());
                                }
                                return true;
                            });
                    if (hasEntry) {
                        hasEntries = true;
                        break;
                    }
                }
            }
            if (hasEntries) {
                allTabs.add(tab);
            }
        }

        if (!allTabs.isEmpty()) {
            if (this.jerb$selectedCreativeTab == null || !allTabs.contains(this.jerb$selectedCreativeTab)) {
                this.jerb$selectedCreativeTab = allTabs.get(0);
            }
        } else {
            this.jerb$selectedCreativeTab = null;
        }

        int totalTabs = allTabs.size();
        int maxTabsPerPage = 5;
        this.jerb$maxPages = (totalTabs + maxTabsPerPage - 1) / maxTabsPerPage;

        if (this.jerb$tabPageIndex >= this.jerb$maxPages) {
            this.jerb$tabPageIndex = this.jerb$maxPages - 1;
        }
        if (this.jerb$tabPageIndex < 0) {
            this.jerb$tabPageIndex = 0;
        }

        int start = this.jerb$tabPageIndex * maxTabsPerPage;
        int end = Math.min(start + maxTabsPerPage, totalTabs);

        int paneX = (this.width - 147) / 2 - this.xOffset;
        int paneY = (this.height - 166) / 2;
        int currentY = paneY + 3;

        RecipeBookComponent.TabInfo dummyTabInfo = new RecipeBookComponent.TabInfo(
                SearchRecipeBookCategory.CRAFTING);

        for (int i = start; i < end; i++) {
            CreativeModeTab tab = allTabs.get(i);
            RecipeBookTabButton tabButton = new RecipeBookTabButton(paneX - 30, currentY, dummyTabInfo, btn -> {
            });
            ((JerbTabButton) tabButton).jerb$setCreativeTab(tab);

            if (tab == this.jerb$selectedCreativeTab) {
                tabButton.select();
                this.selectedTab = tabButton;
            } else {
                tabButton.unselect();
            }

            this.tabButtons.add(tabButton);
            currentY += 27;
        }

        if (this.jerb$maxPages > 1) {
            if (this.jerb$tabPageIndex > 0) {
                RecipeBookTabButton upBtn = new RecipeBookTabButton(paneX - 19, paneY - 15, dummyTabInfo, btn -> {
                }) {
                    {
                        this.width = 12;
                        this.height = 17;
                    }

                    @Override
                    public int getWidth() {
                        return 12;
                    }

                    @Override
                    public int getHeight() {
                        return 17;
                    }

                    @Override
                    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY,
                            float partialTick) {
                        Identifier sprite = this.isHoveredOrFocused()
                                ? Identifier.withDefaultNamespace("recipe_book/page_backward_highlighted")
                                : Identifier.withDefaultNamespace("recipe_book/page_backward");
                        extractor.pose().pushMatrix();
                        extractor.pose().translate(this.getX() + 6.0F, this.getY() + 8.5F);
                        extractor.pose().rotate((float) Math.toRadians(90.0F));
                        extractor.pose().translate(-6.0F, -8.5F);
                        extractor.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 0, 0,
                                12, 17);
                        extractor.pose().popMatrix();
                    }
                };
                upBtn.setPosition(paneX - 19, paneY - 15);
                upBtn.setTooltip(Tooltip.create(Component.literal("Previous Page")));
                this.tabButtons.add(upBtn);
            }

            if (this.jerb$tabPageIndex < this.jerb$maxPages - 1) {
                RecipeBookTabButton downBtn = new RecipeBookTabButton(paneX - 19, paneY + 3 + 5 * 27, dummyTabInfo,
                        btn -> {
                        }) {
                    {
                        this.width = 12;
                        this.height = 17;
                    }

                    @Override
                    public int getWidth() {
                        return 12;
                    }

                    @Override
                    public int getHeight() {
                        return 17;
                    }

                    @Override
                    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY,
                            float partialTick) {
                        Identifier sprite = this.isHoveredOrFocused()
                                ? Identifier.withDefaultNamespace("recipe_book/page_forward_highlighted")
                                : Identifier.withDefaultNamespace("recipe_book/page_forward");
                        extractor.pose().pushMatrix();
                        extractor.pose().translate(this.getX() + 6.0F, this.getY() + 8.5F);
                        extractor.pose().rotate((float) Math.toRadians(90.0F));
                        extractor.pose().translate(-6.0F, -8.5F);
                        extractor.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 0, 0,
                                12, 17);
                        extractor.pose().popMatrix();
                    }
                };
                downBtn.setPosition(paneX - 19, paneY + 3 + 5 * 27);
                downBtn.setTooltip(Tooltip.create(Component.literal("Next Page")));
                this.tabButtons.add(downBtn);
            }
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void onInit(int width, int height, Minecraft minecraft, boolean widthTooNarrow, CallbackInfo ci) {
        if (RecipeBrowserIntegration.isActive()) {
            this.stackedContents.clear();
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.getInventory().fillStackedContents(this.stackedContents);
            }
            this.menu.fillCraftSlotsStackedContents(this.stackedContents);
            RecipeBrowserIntegration.setActiveContext(
                    this.stackedContents,
                    this.menu.getRecipeBookType(),
                    this.menu instanceof InventoryMenu);
            this.jerb$rebuildCreativeTabs();
        }
    }

    @Inject(method = "updateCollections", at = @At("HEAD"), cancellable = true)
    public void onUpdateCollections(boolean resetPage, boolean otherParam, CallbackInfo ci) {
        if (RecipeBrowserIntegration.isActive()) {
            this.stackedContents.clear();
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.getInventory().fillStackedContents(this.stackedContents);
            }
            this.menu.fillCraftSlotsStackedContents(this.stackedContents);

            RecipeBrowserIntegration.setActiveContext(
                    this.stackedContents,
                    this.menu.getRecipeBookType(),
                    this.menu instanceof InventoryMenu);

            String query = this.searchBox != null ? this.searchBox.getValue() : "";
            if (this.searchBox != null && this.searchBox.isFocused()) {
                RecipeBrowserIntegration.setSearchText(query);
            }

            List<ItemStack> filtered = RecipeBrowserIntegration.getFilteredItems(query, this.jerb$selectedCreativeTab);

            boolean filtering = this.filterButton != null && this.filterButton.getValue();
            if (filtering) {
                List<ItemStack> craftableOnly = new ArrayList<>();
                for (ItemStack stack : filtered) {
                    if (RecipeBrowserIntegration.isCraftable(stack.getItem())) {
                        craftableOnly.add(stack);
                    }
                }
                filtered = craftableOnly;
            }

            ((JerbRecipeBookPage) this.recipeBookPage).jerb$setItems(filtered, resetPage);
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        if (RecipeBrowserIntegration.isActive() && this.isVisible() && this.searchBox != null) {
            String browserQuery = RecipeBrowserIntegration.getSearchText();
            String localQuery = this.searchBox.getValue();
            if (!localQuery.equals(browserQuery)) {
                if (this.searchBox.isFocused()) {
                    RecipeBrowserIntegration.setSearchText(localQuery);
                    this.updateCollections(false, false);
                } else {
                    this.searchBox.setValue(browserQuery);
                    this.updateCollections(false, false);
                }
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void onMouseClicked(MouseButtonEvent event, boolean doubleClick,
            CallbackInfoReturnable<Boolean> cir) {
        if (RecipeBrowserIntegration.isActive() && this.isVisible()) {
            double mouseX = event.x();
            double mouseY = event.y();
            int button = event.button();
            int paneY = (this.height - 166) / 2;

            for (RecipeBookTabButton tabBtn : this.tabButtons) {
                if (tabBtn.visible) {
                    boolean isHovered = mouseX >= tabBtn.getX() && mouseX < tabBtn.getX() + tabBtn.getWidth() &&
                            mouseY >= tabBtn.getY() && mouseY < tabBtn.getY() + tabBtn.getHeight();
                    if (isHovered && (button == 0 || button == 1)) {
                        CreativeModeTab creativeTab = ((JerbTabButton) tabBtn).jerb$getCreativeTab();
                        if (creativeTab == null) {
                            SimpleSoundInstance sound = SimpleSoundInstance
                                    .forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F);
                            Minecraft.getInstance().getSoundManager().play(sound);

                            if (tabBtn.getY() < paneY) {
                                if (button == 1) {
                                    this.jerb$tabPageIndex = 0;
                                } else {
                                    this.jerb$tabPageIndex--;
                                }
                            } else {
                                if (button == 1) {
                                    this.jerb$tabPageIndex = this.jerb$maxPages - 1;
                                } else {
                                    this.jerb$tabPageIndex++;
                                }
                            }
                            this.jerb$rebuildCreativeTabs();
                            cir.setReturnValue(true);
                            return;
                        } else if (button == 0) {
                            this.jerb$selectedCreativeTab = creativeTab;

                            SimpleSoundInstance sound = SimpleSoundInstance
                                    .forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F);
                            Minecraft.getInstance().getSoundManager().play(sound);

                            this.jerb$rebuildCreativeTabs();
                            this.updateCollections(true, false);
                            cir.setReturnValue(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (RecipeBrowserIntegration.isActive() && this.isVisible()) {
            ImageButton forwardBtn = ((JerbRecipeBookPage) this.recipeBookPage).jerb$getForwardButton();
            ImageButton backBtn = ((JerbRecipeBookPage) this.recipeBookPage).jerb$getBackButton();
            int currentPage = ((JerbRecipeBookPage) this.recipeBookPage).jerb$getCurrentPage();
            int totalPages = ((JerbRecipeBookPage) this.recipeBookPage).jerb$getTotalPages();

            if (forwardBtn != null && forwardBtn.visible && mouseX >= forwardBtn.getX()
                    && mouseX < forwardBtn.getX() + forwardBtn.getWidth() && mouseY >= forwardBtn.getY()
                    && mouseY < forwardBtn.getY() + forwardBtn.getHeight()) {
                if (verticalAmount > 0 && currentPage > 0) {
                    ((JerbRecipeBookPage) this.recipeBookPage).jerb$setCurrentPage(currentPage - 1);
                    ((JerbRecipeBookPage) this.recipeBookPage).jerb$updateButtons();
                    return true;
                } else if (verticalAmount < 0 && currentPage < totalPages - 1) {
                    ((JerbRecipeBookPage) this.recipeBookPage).jerb$setCurrentPage(currentPage + 1);
                    ((JerbRecipeBookPage) this.recipeBookPage).jerb$updateButtons();
                    return true;
                }
                return false;
            }

            if (backBtn != null && backBtn.visible && mouseX >= backBtn.getX()
                    && mouseX < backBtn.getX() + backBtn.getWidth() && mouseY >= backBtn.getY()
                    && mouseY < backBtn.getY() + backBtn.getHeight()) {
                if (verticalAmount > 0 && currentPage > 0) {
                    ((JerbRecipeBookPage) this.recipeBookPage).jerb$setCurrentPage(currentPage - 1);
                    ((JerbRecipeBookPage) this.recipeBookPage).jerb$updateButtons();
                    return true;
                } else if (verticalAmount < 0 && currentPage < totalPages - 1) {
                    ((JerbRecipeBookPage) this.recipeBookPage).jerb$setCurrentPage(currentPage + 1);
                    ((JerbRecipeBookPage) this.recipeBookPage).jerb$updateButtons();
                    return true;
                }
                return false;
            }

            for (RecipeBookTabButton tabBtn : this.tabButtons) {
                if (tabBtn.visible) {
                    boolean isHovered = mouseX >= tabBtn.getX() && mouseX < tabBtn.getX() + tabBtn.getWidth() &&
                            mouseY >= tabBtn.getY() && mouseY < tabBtn.getY() + tabBtn.getHeight();
                    if (isHovered) {
                        CreativeModeTab creativeTab = ((JerbTabButton) tabBtn).jerb$getCreativeTab();
                        if (creativeTab == null) {
                            if (verticalAmount > 0) {
                                if (this.jerb$tabPageIndex > 0) {
                                    this.jerb$tabPageIndex--;
                                    this.jerb$rebuildCreativeTabs();
                                    return true;
                                }
                            } else if (verticalAmount < 0) {
                                if (this.jerb$tabPageIndex < this.jerb$maxPages - 1) {
                                    this.jerb$tabPageIndex++;
                                    this.jerb$rebuildCreativeTabs();
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Inject(method = "isMouseOver", at = @At("HEAD"), cancellable = true)
    public void onIsMouseOver(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (RecipeBrowserIntegration.isActive() && this.isVisible()) {
            ImageButton forwardBtn = ((JerbRecipeBookPage) this.recipeBookPage).jerb$getForwardButton();
            if (forwardBtn != null && forwardBtn.visible && mouseX >= forwardBtn.getX()
                    && mouseX < forwardBtn.getX() + forwardBtn.getWidth() && mouseY >= forwardBtn.getY()
                    && mouseY < forwardBtn.getY() + forwardBtn.getHeight()) {
                cir.setReturnValue(true);
                return;
            }
            ImageButton backBtn = ((JerbRecipeBookPage) this.recipeBookPage).jerb$getBackButton();
            if (backBtn != null && backBtn.visible && mouseX >= backBtn.getX()
                    && mouseX < backBtn.getX() + backBtn.getWidth() && mouseY >= backBtn.getY()
                    && mouseY < backBtn.getY() + backBtn.getHeight()) {
                cir.setReturnValue(true);
                return;
            }
            for (RecipeBookTabButton tabBtn : this.tabButtons) {
                if (tabBtn.visible && mouseX >= tabBtn.getX() && mouseX < tabBtn.getX() + tabBtn.getWidth()
                        && mouseY >= tabBtn.getY() && mouseY < tabBtn.getY() + tabBtn.getHeight()) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }
}
