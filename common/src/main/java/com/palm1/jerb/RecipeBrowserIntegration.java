package com.palm1.jerb;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookType;
import com.palm1.jerb.compat.JeiCompat;
import com.palm1.jerb.compat.ReiCompat;
import com.palm1.jerb.compat.EmiCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class RecipeBrowserIntegration {
    private static boolean jeiActive = false;
    private static boolean reiActive = false;
    private static boolean emiActive = false;
    private static Object jeiRuntime = null;
    private static Set<Item> craftableItems = null;
    private static StackedContents activeStackedContents = null;
    private static RecipeBookType activeRecipeBookType = null;
    private static boolean activeIsInventory = false;
    private static Map<Item, List<RecipeHolder<?>>> recipesByOutput = null;
    private static net.minecraft.world.item.crafting.RecipeManager cachedRecipeManager = null;

    private static void checkRecipeManager() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getConnection() != null) {
                net.minecraft.world.item.crafting.RecipeManager currentManager = mc.getConnection().getRecipeManager();
                if (currentManager != cachedRecipeManager) {
                    craftableItems = null;
                    recipesByOutput = null;
                    cachedRecipeManager = currentManager;
                    if (reiActive) {
                        ReiCompat.clearCache();
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public static void clearCraftableItemsCache() {
        craftableItems = null;
        recipesByOutput = null;
        cachedRecipeManager = null;
        if (reiActive) {
            ReiCompat.clearCache();
        }
    }

    public static void setActiveContext(StackedContents stackedContents, RecipeBookType bookType, boolean isInventory) {
        activeStackedContents = stackedContents;
        activeRecipeBookType = bookType;
        activeIsInventory = isInventory;
    }

    public static StackedContents getActiveStackedContents() {
        return activeStackedContents;
    }

    public static RecipeBookType getActiveRecipeBookType() {
        return activeRecipeBookType;
    }

    public static boolean isInventoryContext() {
        return activeIsInventory;
    }

    private static Map<Item, List<RecipeHolder<?>>> getRecipesByOutput() {
        checkRecipeManager();
        if (recipesByOutput == null) {
            recipesByOutput = new HashMap<>();
            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc != null && mc.getConnection() != null) {
                    for (RecipeHolder<?> holder : mc.getConnection().getRecipeManager().getRecipes()) {
                        try {
                            ItemStack result = holder.value().getResultItem(mc.getConnection().registryAccess());
                            if (result != null && !result.isEmpty()) {
                                recipesByOutput.computeIfAbsent(result.getItem(), k -> new ArrayList<>()).add(holder);
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return recipesByOutput;
    }

    public static boolean isRecipeCompatible(RecipeHolder<?> recipe, RecipeBookType bookType) {
        if (recipe == null)
            return false;
        net.minecraft.world.item.crafting.RecipeType<?> recipeType = recipe.value().getType();
        if (bookType == RecipeBookType.CRAFTING) {
            return recipeType == net.minecraft.world.item.crafting.RecipeType.CRAFTING;
        } else if (bookType == RecipeBookType.FURNACE) {
            return recipeType == net.minecraft.world.item.crafting.RecipeType.SMELTING;
        } else if (bookType == RecipeBookType.BLAST_FURNACE) {
            return recipeType == net.minecraft.world.item.crafting.RecipeType.BLASTING;
        } else if (bookType == RecipeBookType.SMOKER) {
            return recipeType == net.minecraft.world.item.crafting.RecipeType.SMOKING;
        }
        try {
            String bookTypeName = bookType.name();
            if (bookTypeName != null) {
                int firstUnderscore = bookTypeName.indexOf('_');
                if (firstUnderscore > 0) {
                    String namespace = bookTypeName.substring(0, firstUnderscore).toLowerCase(Locale.ROOT);
                    String path = bookTypeName.substring(firstUnderscore + 1).toLowerCase(Locale.ROOT);
                    ResourceLocation typeKey = BuiltInRegistries.RECIPE_TYPE.getKey(recipeType);
                    if (typeKey != null && typeKey.getNamespace().equals(namespace) && typeKey.getPath().equals(path)) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static boolean hasCompatibleRecipe(Item item, RecipeBookType bookType) {
        Map<Item, List<RecipeHolder<?>>> recipes = getRecipesByOutput();
        List<RecipeHolder<?>> recipeList = recipes.get(item);
        if (recipeList == null || recipeList.isEmpty()) {
            return false;
        }
        for (RecipeHolder<?> holder : recipeList) {
            if (isRecipeCompatible(holder, bookType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCraftable(Item item, StackedContents stackedContents, RecipeBookType bookType) {
        Map<Item, List<RecipeHolder<?>>> recipes = getRecipesByOutput();
        List<RecipeHolder<?>> recipeList = recipes.get(item);
        if (recipeList == null || recipeList.isEmpty()) {
            return false;
        }
        for (RecipeHolder<?> holder : recipeList) {
            if (isRecipeCompatible(holder, bookType)) {
                if (holder.value().isSpecial()) {
                    continue;
                }
                if (stackedContents.canCraft(holder.value(), null)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isCraftable(Item item) {
        if (activeStackedContents == null || activeRecipeBookType == null) {
            return false;
        }
        return isCraftable(item, activeStackedContents, activeRecipeBookType);
    }

    public static Set<Item> getCraftableItems() {
        checkRecipeManager();
        if (craftableItems == null) {
            craftableItems = new HashSet<>();
            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc != null && mc.getConnection() != null) {
                    for (RecipeHolder<?> holder : mc.getConnection().getRecipeManager().getRecipes()) {
                        try {
                            ItemStack result = holder.value().getResultItem(mc.getConnection().registryAccess());
                            if (result != null && !result.isEmpty()) {
                                craftableItems.add(result.getItem());
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return craftableItems;
    }

    public static void setJeiActive(boolean active) {
        jeiActive = active;
    }

    public static void setReiActive(boolean active) {
        reiActive = active;
    }

    public static void setEmiActive(boolean active) {
        emiActive = active;
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
        return emiActive;
    }

    public static boolean isActive() {
        return isJeiActive() || isReiActive() || isEmiActive();
    }

    public static boolean isRecipeBookVisible() {
        try {
            Minecraft mc = Minecraft.getInstance();
            return mc.screen instanceof RecipeUpdateListener;
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
        if (isEmiActive()) {
            return EmiCompat.getFilterText();
        }
        return "";
    }

    public static void setSearchText(String text) {
        if (isJeiActive()) {
            JeiCompat.setFilterText(jeiRuntime, text);
        } else if (isReiActive()) {
            ReiCompat.setFilterText(text);
        } else if (isEmiActive()) {
            EmiCompat.setFilterText(text);
        }
    }

    public static List<ItemStack> getFilteredItems(String query, CreativeModeTab tab) {
        List<ItemStack> rawItems = new ArrayList<>();
        if (isJeiActive()) {
            rawItems.addAll(JeiCompat.getFilteredItemStacks(jeiRuntime));
        } else if (isReiActive()) {
            rawItems.addAll(ReiCompat.getFilteredItemStacks());
        } else if (isEmiActive()) {
            rawItems.addAll(EmiCompat.getFilteredItemStacks());
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
        if (isEmiActive()) {
            return EmiCompat.hasRecipesOrUsages(stack);
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
        } else if (isEmiActive()) {
            EmiCompat.showRecipes(stack);
        }
    }

    public static void showUsages(ItemStack stack) {
        if (isJeiActive()) {
            JeiCompat.showUsages(jeiRuntime, stack);
        } else if (isReiActive()) {
            ReiCompat.showUsages(stack);
        } else if (isEmiActive()) {
            EmiCompat.showUsages(stack);
        }
    }
}
