package com.palm1.jerb.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.api.distmarker.Dist;
import com.palm1.jerb.RecipeBrowserIntegration;

@Mod("jerb")
public class JerbNeoForge {
    public JerbNeoForge() {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            if (FMLLoader.getLoadingModList().getModFileById("roughlyenoughitems") != null) {
                RecipeBrowserIntegration.setReiActive(true);
            }
            if (FMLLoader.getLoadingModList().getModFileById("emi") != null) {
                RecipeBrowserIntegration.setEmiActive(true);
            }
        }
    }
}
