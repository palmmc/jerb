package com.palm1.jerb;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.RecipeBookType;
import com.palm1.jerb.compat.JeiCompat;
import com.palm1.jerb.compat.ReiCompat;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class RecipeBrowserIntegration {
    private static boolean jeiActive = false;
    private static boolean reiActive = false;
    private static Object jeiRuntime = null;
    private static StackedItemContents activeStackedContents = null;
    private static RecipeBookType activeRecipeBookType = null;
    private static boolean activeIsInventory = false;

    private static Set<Item> activeCompatibleItems = null;
    private static RecipeBookType cachedCompatibleBookType = null;
    private static Set<Item> activeCraftableItems = null;

    public static void clearCompatibleItemsCache() {
        activeCompatibleItems = null;
        cachedCompatibleBookType = null;
    }

    public static void clearCraftableItemsCache() {
        activeCraftableItems = null;
        if (reiActive) {
            ReiCompat.clearCache();
        }
    }

    public static void setActiveContext(StackedItemContents stackedContents, RecipeBookType bookType,
            boolean isInventory) {
        activeStackedContents = stackedContents;
        activeRecipeBookType = bookType;
        activeIsInventory = isInventory;
        clearCompatibleItemsCache();
        clearCraftableItemsCache();
    }

    public static StackedItemContents getActiveStackedContents() {
        return activeStackedContents;
    }

    public static RecipeBookType getActiveRecipeBookType() {
        return activeRecipeBookType;
    }

    public static boolean isInventoryContext() {
        return activeIsInventory;
    }

    public static boolean isCategoryCompatible(RecipeBookCategory entryCategory, RecipeBookType bookType) {
        SearchRecipeBookCategory searchCategory = null;
        if (bookType == RecipeBookType.CRAFTING) {
            searchCategory = SearchRecipeBookCategory.CRAFTING;
        } else if (bookType == RecipeBookType.FURNACE) {
            searchCategory = SearchRecipeBookCategory.FURNACE;
        } else if (bookType == RecipeBookType.BLAST_FURNACE) {
            searchCategory = SearchRecipeBookCategory.BLAST_FURNACE;
        } else if (bookType == RecipeBookType.SMOKER) {
            searchCategory = SearchRecipeBookCategory.SMOKER;
        }
        if (searchCategory != null) {
            return searchCategory.includedCategories().contains(entryCategory);
        }
        return false;
    }

    public static boolean hasCompatibleRecipe(Item item, RecipeBookType bookType) {
        if (activeCompatibleItems != null && cachedCompatibleBookType == bookType) {
            return activeCompatibleItems.contains(item);
        }

        Set<Item> compatible = new HashSet<>();
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                ContextMap contextMap = SlotDisplayContext.fromLevel(mc.level);
                ClientRecipeBook book = mc.player.getRecipeBook();
                for (RecipeCollection collection : book.getCollections()) {
                    for (RecipeDisplayEntry entry : collection.getRecipes()) {
                        if (isCategoryCompatible(entry.category(), bookType)) {
                            for (ItemStack output : entry.resultItems(contextMap)) {
                                if (output != null && !output.isEmpty()) {
                                    compatible.add(output.getItem());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        activeCompatibleItems = compatible;
        cachedCompatibleBookType = bookType;
        return activeCompatibleItems.contains(item);
    }

    public static boolean isCraftable(Item item) {
        if (activeStackedContents == null || activeRecipeBookType == null) {
            return false;
        }
        if (activeCraftableItems != null) {
            return activeCraftableItems.contains(item);
        }

        Set<Item> craftable = new HashSet<>();
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                ContextMap contextMap = SlotDisplayContext.fromLevel(mc.level);
                ClientRecipeBook book = mc.player.getRecipeBook();
                for (RecipeCollection collection : book.getCollections()) {
                    for (RecipeDisplayEntry entry : collection.getRecipes()) {
                        if (entry.canCraft(activeStackedContents)) {
                            if (isCategoryCompatible(entry.category(), activeRecipeBookType)) {
                                for (ItemStack output : entry.resultItems(contextMap)) {
                                    if (output != null && !output.isEmpty()) {
                                        craftable.add(output.getItem());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        activeCraftableItems = craftable;
        return activeCraftableItems.contains(item);
    }

    public static Set<Item> getCraftableItems() {
        if (activeCraftableItems != null) {
            return activeCraftableItems;
        }
        isCraftable(null);
        return activeCraftableItems != null ? activeCraftableItems : new HashSet<>();
    }

    public static void setJeiActive(boolean active) {
        jeiActive = active;
    }

    public static void setReiActive(boolean active) {
        reiActive = active;
    }

    public static void setEmiActive(boolean active) {
    }

    public static void setJeiRuntime(Object runtime) {
        jeiRuntime = runtime;
    }

    public static boolean isJeiActive() {
        return jeiActive && jeiRuntime != null;
    }

    public static boolean isReiActive() {
        return reiActive;
    }

    public static boolean isEmiActive() {
        return false;
    }

    public static boolean isActive() {
        return isJeiActive() || isReiActive() || isEmiActive();
    }

    public static boolean isRecipeBookVisible() {
        try {
            Minecraft mc = Minecraft.getInstance();
            return mc.gui.screen() instanceof RecipeUpdateListener;
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static boolean shouldDisableBrowserList() {
        if (isRecipeBookVisible()) {
            return true;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && !mc.player.isCreative()) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static String getSearchText() {
        if (isJeiActive()) {
            return JeiCompat.getFilterText(jeiRuntime);
        }
        if (isReiActive()) {
            return ReiCompat.getFilterText();
        }
        return "";
    }

    public static void setSearchText(String text) {
        if (isJeiActive()) {
            JeiCompat.setFilterText(jeiRuntime, text);
        } else if (isReiActive()) {
            ReiCompat.setFilterText(text);
        }
    }

    public static List<ItemStack> getFilteredItems(String query, CreativeModeTab tab) {
        List<ItemStack> rawItems = new ArrayList<>();
        if (isJeiActive()) {
            rawItems.addAll(JeiCompat.getFilteredItemStacks(jeiRuntime));
        } else if (isReiActive()) {
            rawItems.addAll(ReiCompat.getFilteredItemStacks());
        }

        if (rawItems.isEmpty()) {
            return rawItems;
        }

        List<ItemStack> filtered = new ArrayList<>();
        for (ItemStack stack : rawItems) {
            if (stack == null || stack.isEmpty())
                continue;
            if (hasRecipeBrowserEntries(stack) && matchesTab(stack, tab)) {
                if (!activeIsInventory && activeRecipeBookType != null) {
                    if (!hasCompatibleRecipe(stack.getItem(), activeRecipeBookType)) {
                        continue;
                    }
                }
                filtered.add(stack);
            }
        }
        return filtered;
    }

    public static boolean hasRecipeBrowserEntries(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        if (isJeiActive()) {
            return JeiCompat.hasRecipesOrUsages(jeiRuntime, stack);
        }
        if (isReiActive()) {
            return ReiCompat.hasRecipesOrUsages(stack);
        }
        return false;
    }

    public static boolean matchesTab(ItemStack stack, CreativeModeTab tab) {
        if (tab == null)
            return true;
        if (tab.getType() == CreativeModeTab.Type.SEARCH) {
            return true;
        }
        net.minecraft.world.item.Item item = stack.getItem();
        for (ItemStack tabStack : tab.getDisplayItems()) {
            if (tabStack.getItem() == item) {
                return true;
            }
        }
        return false;
    }

    public static void showRecipes(ItemStack stack) {
        if (isJeiActive()) {
            JeiCompat.showRecipes(jeiRuntime, stack);
        } else if (isReiActive()) {
            ReiCompat.showRecipes(stack);
        }
    }

    public static void showUsages(ItemStack stack) {
        if (isJeiActive()) {
            JeiCompat.showUsages(jeiRuntime, stack);
        } else if (isReiActive()) {
            ReiCompat.showUsages(stack);
        }
    }
}
