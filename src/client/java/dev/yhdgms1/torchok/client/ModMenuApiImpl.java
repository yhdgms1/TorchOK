package dev.yhdgms1.torchok.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.yhdgms1.torchok.TorchOK;
import eu.midnightdust.lib.config.MidnightConfig;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> MidnightConfig.getScreen(parent, TorchOK.MOD_ID);
    }
}
