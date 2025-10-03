package hainer.mod.achievementstyle.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import hainer.mod.achievementstyle.config.AchievementConfig;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return AchievementConfig::createConfigScreen;
    }
}