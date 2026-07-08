package com.palm1.jerb.compat;

import net.minecraft.world.item.ItemStack;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.item.Item;

public class ReiCompat {
    private static final Map<Item, Boolean> cache = new ConcurrentHashMap<>();

    public static void clearCache() {
        cache.clear();
    }

    public static List<ItemStack> getFilteredItemStacks() {
        try {
            List<EntryStack<?>> entries = EntryRegistry.getInstance().getPreFilteredList();
            if (entries == null) return List.of();
            List<ItemStack> list = new ArrayList<>();
            for (EntryStack<?> stack : entries) {
                if (stack != null && stack.getValue() instanceof ItemStack itemStack) {
                    list.add(itemStack);
                }
            }
            return list;
        } catch (Throwable ignored) {
            return List.of();
        }
    }

    public static String getFilterText() {
        try {
            TextField widget = REIRuntime.getInstance().getSearchTextField();
            return widget != null ? widget.getText() : "";
        } catch (Throwable ignored) {
            return "";
        }
    }

    public static void setFilterText(String text) {
        try {
            TextField widget = REIRuntime.getInstance().getSearchTextField();
            if (widget != null) {
                widget.setText(text);
            }
        } catch (Throwable ignored) {}
    }

    public static void showRecipes(ItemStack stack) {
        try {
            ViewSearchBuilder.builder()
                    .addRecipesFor(EntryStacks.of(stack))
                    .open();
        } catch (Throwable ignored) {}
    }

    public static void showUsages(ItemStack stack) {
        try {
            ViewSearchBuilder.builder()
                    .addUsagesFor(EntryStacks.of(stack))
                    .open();
        } catch (Throwable ignored) {}
    }

    public static boolean hasRecipesOrUsages(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return cache.computeIfAbsent(item, i -> {
            try {
                if (ViewSearchBuilder.builder().addRecipesFor(EntryStacks.of(stack)).streamDisplays().findAny().isPresent()) {
                    return true;
                }
                if (ViewSearchBuilder.builder().addUsagesFor(EntryStacks.of(stack)).streamDisplays().findAny().isPresent()) {
                    return true;
                }
            } catch (Throwable ignored) {}
            return false;
        });
    }
}
