package com.palm1.jerb.compat;

import net.minecraft.world.item.ItemStack;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.search.EmiSearch;
import dev.emi.emi.registry.EmiRecipes;
import java.util.ArrayList;
import java.util.List;

public class EmiCompat {
    public static List<ItemStack> getFilteredItemStacks() {
        try {
            List<? extends EmiIngredient> stacks = EmiSearch.stacks;
            if (stacks == null) return List.of();
            List<ItemStack> list = new ArrayList<>();
            for (EmiIngredient ingredient : stacks) {
                if (ingredient != null) {
                    List<EmiStack> emiStacks = ingredient.getEmiStacks();
                    if (emiStacks != null && !emiStacks.isEmpty()) {
                        ItemStack itemStack = emiStacks.get(0).getItemStack();
                        if (itemStack != null && !itemStack.isEmpty()) {
                            list.add(itemStack);
                        }
                    }
                }
            }
            return list;
        } catch (Throwable ignored) {
            return List.of();
        }
    }

    public static String getFilterText() {
        try {
            return EmiApi.getSearchText();
        } catch (Throwable ignored) {
            return "";
        }
    }

    public static void setFilterText(String text) {
        try {
            EmiApi.setSearchText(text);
        } catch (Throwable ignored) {}
    }

    public static void showRecipes(ItemStack stack) {
        try {
            EmiApi.displayRecipes(EmiStack.of(stack));
        } catch (Throwable ignored) {}
    }

    public static void showUsages(ItemStack stack) {
        try {
            EmiApi.displayUses(EmiStack.of(stack));
        } catch (Throwable ignored) {}
    }

    public static boolean hasRecipesOrUsages(ItemStack stack) {
        try {
            EmiStack emiStack = EmiStack.of(stack);
            if (!EmiApi.getRecipeManager().getRecipesByOutput(emiStack).isEmpty()) {
                return true;
            }
            if (!EmiApi.getRecipeManager().getRecipesByInput(emiStack).isEmpty()) {
                return true;
            }
            if (EmiRecipes.byWorkstation.containsKey(emiStack)) {
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
