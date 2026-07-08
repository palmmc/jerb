package com.palm1.jerb;

import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.components.ImageButton;
import java.util.List;

public interface JerbRecipeBookPage {
    void jerb$setItems(List<ItemStack> items, boolean resetPage);
    List<ItemStack> jerb$getItems();
    ImageButton jerb$getForwardButton();
    ImageButton jerb$getBackButton();
    int jerb$getCurrentPage();
    int jerb$getTotalPages();
    void jerb$setCurrentPage(int page);
    void jerb$updateButtons();
}
