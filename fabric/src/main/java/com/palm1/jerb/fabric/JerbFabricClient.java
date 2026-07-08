package com.palm1.jerb.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import com.palm1.jerb.RecipeBrowserIntegration;

public class JerbFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
            RecipeBrowserIntegration.setReiActive(true);
        }
        if (FabricLoader.getInstance().isModLoaded("emi")) {
            RecipeBrowserIntegration.setEmiActive(true);
        }
    }
}
