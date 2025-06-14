package hainer.mod.achievementstyle.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = "achievementstyle")
public class AchievementConfig implements ConfigData {


    @Override
    public void validatePostLoad() throws ValidationException {

        achievementWidth = Math.max(100, Math.min(300, achievementWidth));
        achievementHeight = Math.max(20, Math.min(80, achievementHeight));
        verticalOffset = Math.max(0, Math.min(100, verticalOffset));
        slideDuration = Math.max(10, Math.min(100, slideDuration));
        displayDuration = Math.max(60, Math.min(300, displayDuration));
        achievementSpacing = Math.max(0, Math.min(10, achievementSpacing));


        soundVolume = Math.max(0.0f, Math.min(1.0f, soundVolume));


        if ((backgroundColor & 0xFF000000) == 0) {
            backgroundColor |= 0xFF000000;
        }


        borderColor &= 0x00FFFFFF;
    }

    @ConfigEntry.Category("appearance")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 100, max = 300)
    public int achievementWidth = 170;

    @ConfigEntry.Category("appearance")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 20, max = 80)
    public int achievementHeight = 40;

    @ConfigEntry.Category("appearance")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int verticalOffset = 25;

    @ConfigEntry.Category("animation")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 10, max = 100)
    public int slideDuration = 40;

    @ConfigEntry.Category("animation")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 60, max = 300)
    public int displayDuration = 120;

    @ConfigEntry.Category("style")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.ColorPicker(allowAlpha = true)
    public int backgroundColor = 0xE0000000;

    @ConfigEntry.Category("style")
    @ConfigEntry.Gui.Tooltip
    public int borderColor = 0x4A90E2;

    @ConfigEntry.Category("style")
    @ConfigEntry.Gui.Tooltip
    public boolean enableShineEffect = true;

    @ConfigEntry.Category("position")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
    public int achievementSpacing = 5;


    @ConfigEntry.Category("sound")
    @ConfigEntry.Gui.Tooltip
    public boolean soundEnabled = true;

    @ConfigEntry.Category("sound")
    @ConfigEntry.Gui.Tooltip
    public float soundVolume = 0.8f;

    public static AchievementConfig get() {
        return AutoConfig.getConfigHolder(AchievementConfig.class).getConfig();
    }

    public static void init() {
        AutoConfig.register(AchievementConfig.class, GsonConfigSerializer::new);
    }
}