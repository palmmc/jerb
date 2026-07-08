package com.palm1.jerb.compat;

import net.minecraft.world.item.ItemStack;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import java.util.List;

public class JeiCompat {
    public static List<ItemStack> getFilteredItemStacks(Object jeiRuntime) {
        if (jeiRuntime == null) return List.of();
        try {
            IJeiRuntime runtime = (IJeiRuntime) jeiRuntime;
            return runtime.getIngredientFilter().getFilteredIngredients(VanillaTypes.ITEM_STACK);
        } catch (Throwable ignored) {
            return List.of();
        }
    }

    public static String getFilterText(Object jeiRuntime) {
        if (jeiRuntime == null) return "";
        try {
            return ((IJeiRuntime) jeiRuntime).getIngredientFilter().getFilterText();
        } catch (Throwable ignored) {
            return "";
        }
    }

    public static void setFilterText(Object jeiRuntime, String text) {
        if (jeiRuntime == null) return;
        try {
            ((IJeiRuntime) jeiRuntime).getIngredientFilter().setFilterText(text);
        } catch (Throwable ignored) {}
    }

    public static void showRecipes(Object jeiRuntime, ItemStack stack) {
        if (jeiRuntime == null) return;
        try {
            IJeiRuntime runtime = (IJeiRuntime) jeiRuntime;
            runtime.getRecipesGui().show(runtime.getJeiHelpers().getFocusFactory().createFocus(
                    RecipeIngredientRole.OUTPUT,
                    VanillaTypes.ITEM_STACK,
                    stack
            ));
        } catch (Throwable ignored) {}
    }

    public static void showUsages(Object jeiRuntime, ItemStack stack) {
        if (jeiRuntime == null) return;
        try {
            IJeiRuntime runtime = (IJeiRuntime) jeiRuntime;
            runtime.getRecipesGui().show(runtime.getJeiHelpers().getFocusFactory().createFocus(
                    RecipeIngredientRole.INPUT,
                    VanillaTypes.ITEM_STACK,
                    stack
            ));
        } catch (Throwable ignored) {}
    }

    public static boolean hasRecipesOrUsages(Object jeiRuntime, ItemStack stack) {
        if (jeiRuntime == null) return false;
        try {
            IJeiRuntime runtime = (IJeiRuntime) jeiRuntime;
            IFocusFactory ff = runtime.getJeiHelpers().getFocusFactory();
            IFocus<ItemStack> outputFocus = ff.createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack);
            if (runtime.getRecipeManager().createRecipeCategoryLookup()
                    .limitFocus(List.of(outputFocus))
                    .get()
                    .findAny()
                    .isPresent()) {
                return true;
            }
            IFocus<ItemStack> inputFocus = ff.createFocus(RecipeIngredientRole.INPUT, VanillaTypes.ITEM_STACK, stack);
            if (runtime.getRecipeManager().createRecipeCategoryLookup()
                    .limitFocus(List.of(inputFocus))
                    .get()
                    .findAny()
                    .isPresent()) {
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
