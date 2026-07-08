package com.palm1.jerb.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import com.palm1.jerb.RecipeBrowserIntegration;

@JeiPlugin
public class JerbJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("jerb", "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        RecipeBrowserIntegration.setJeiActive(true);
        RecipeBrowserIntegration.setJeiRuntime(jeiRuntime);
    }

    @Override
    public void onRuntimeUnavailable() {
        RecipeBrowserIntegration.setJeiActive(false);
        RecipeBrowserIntegration.setJeiRuntime(null);
    }
}
