package com.palm1.jerb.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;
import com.palm1.jerb.RecipeBrowserIntegration;

@JeiPlugin
public class JerbJeiPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath("jerb", "jei_plugin");

    @Override
    public Identifier getPluginUid() {
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
