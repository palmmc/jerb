package com.palm1.jerb.compat;

import net.minecraft.world.item.ItemStack;
import java.util.List;

public class EmiCompat {
    public static List<ItemStack> getFilteredItemStacks() {
        return List.of();
    }

    public static String getFilterText() {
        return "";
    }

    public static void setFilterText(String text) {
    }

    public static void showRecipes(ItemStack stack) {
    }

    public static void showUsages(ItemStack stack) {
    }

    public static boolean hasRecipesOrUsages(ItemStack stack) {
        return false;
    }
}
