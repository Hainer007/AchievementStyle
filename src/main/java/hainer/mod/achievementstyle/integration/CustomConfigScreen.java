package hainer.mod.achievementstyle.integration;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.gui.ConfigScreenProvider;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import hainer.mod.achievementstyle.config.AchievementConfig;

public class CustomConfigScreen {

    public static Screen createConfigScreen(Screen parent) {
        AchievementConfig config = AchievementConfig.get();
        AchievementConfig defaultConfig = new AchievementConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("text.autoconfig.achievementstyle.title"))
                .setSavingRunnable(() -> {
                    AutoConfig.getConfigHolder(AchievementConfig.class).save();
                });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        
        ConfigCategory appearance = builder.getOrCreateCategory(
                Text.translatable("text.autoconfig.achievementstyle.category.appearance"));

        appearance.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("text.autoconfig.achievementstyle.option.achievementWidth"),
                        config.achievementWidth, 100, 300)
                .setDefaultValue(defaultConfig.achievementWidth)
                .setTooltip(Text.translatable("text.autoconfig.achievementstyle.option.achievementWidth.@Tooltip"))
                .setSaveConsumer(value -> config.achievementWidth = value)
                .build());

        appearance.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("text.autoconfig.achievementstyle.option.achievementHeight"),
                        config.achievementHeight, 20, 80)
                .setDefaultValue(defaultConfig.achievementHeight)
                .setTooltip(Text.translatable("text.autoconfig.achievementstyle.option.achievementHeight.@Tooltip"))
                .setSaveConsumer(value -> config.achievementHeight = value)
                .build());

        appearance.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("text.autoconfig.achievementstyle.option.verticalOffset"),
                        config.verticalOffset, 0, 100)
                .setDefaultValue(defaultConfig.verticalOffset)
                .setTooltip(Text.translatable("text.autoconfig.achievementstyle.option.verticalOffset.@Tooltip"))
                .setSaveConsumer(value -> config.verticalOffset = value)
                .build());

        
        ConfigCategory animation = builder.getOrCreateCategory(
                Text.translatable("text.autoconfig.achievementstyle.category.animation"));

        animation.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("text.autoconfig.achievementstyle.option.slideDuration"),
                        config.slideDuration, 10, 100)
                .setDefaultValue(defaultConfig.slideDuration)
                .setTooltip(Text.translatable("text.autoconfig.achievementstyle.option.slideDuration.@Tooltip"))
                .setSaveConsumer(value -> config.slideDuration = value)
                .build());

        animation.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("text.autoconfig.achievementstyle.option.displayDuration"),
                        config.displayDuration, 60, 300)
                .setDefaultValue(defaultConfig.displayDuration)
                .setTooltip(Text.translatable("text.autoconfig.achievementstyle.option.displayDuration.@Tooltip"))
                .setSaveConsumer(value -> config.displayDuration = value)
                .build());

        
        ConfigCategory style = builder.getOrCreateCategory(
                Text.translatable("text.autoconfig.achievementstyle.category.style"));

        style.addEntry(entryBuilder.startAlphaColorField(
                        Text.translatable("text.autoconfig.achievementstyle.option.backgroundColor"),
                        config.backgroundColor)
                .setDefaultValue(defaultConfig.backgroundColor)
                .setTooltip(Text.translatable("text.autoconfig.achievementstyle.option.backgroundColor.@Tooltip"))
                .setSaveConsumer(value -> config.backgroundColor = value)
                .build());

        style.addEntry(entryBuilder.startStrField(
                        Text.translatable("text.autoconfig.achievementstyle.option.borderColor"),
                        String.format("#%06X", config.borderColor & 0xFFFFFF))
                .setDefaultValue(String.format("#%06X", defaultConfig.borderColor & 0xFFFFFF))
                .setTooltip(Text.translatable("text.autoconfig.achievementstyle.option.borderColor.@Tooltip"))
                .setSaveConsumer(value -> {
                    try {
                        
                        String hex = value.startsWith("#") ? value.substring(1) : value;
                        config.borderColor = Integer.parseInt(hex, 16) & 0xFFFFFF;
                    } catch (NumberFormatException e) {
                        config.borderColor = 0x4A90E2;
                    }
                })
                .build());

        style.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("text.autoconfig.achievementstyle.option.enableShineEffect"),
                        config.enableShineEffect)
                .setDefaultValue(defaultConfig.enableShineEffect)
                .setTooltip(Text.translatable("text.autoconfig.achievementstyle.option.enableShineEffect.@Tooltip"))
                .setSaveConsumer(value -> config.enableShineEffect = value)
                .build());

        
        ConfigCategory position = builder.getOrCreateCategory(
                Text.translatable("text.autoconfig.achievementstyle.category.position"));

        position.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("text.autoconfig.achievementstyle.option.achievementSpacing"),
                        config.achievementSpacing, 0, 10)
                .setDefaultValue(defaultConfig.achievementSpacing)
                .setTooltip(Text.translatable("text.autoconfig.achievementstyle.option.achievementSpacing.@Tooltip"))
                .setSaveConsumer(value -> config.achievementSpacing = value)
                .build());

        
        ConfigCategory sound = builder.getOrCreateCategory(
                Text.translatable("text.autoconfig.achievementstyle.category.sound"));

        sound.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("text.autoconfig.achievementstyle.option.soundEnabled"),
                        config.soundEnabled)
                .setDefaultValue(defaultConfig.soundEnabled)
                .setTooltip(Text.translatable("text.autoconfig.achievementstyle.option.soundEnabled.@Tooltip"))
                .setSaveConsumer(value -> config.soundEnabled = value)
                .build());

        sound.addEntry(entryBuilder.startFloatField(
                        Text.translatable("text.autoconfig.achievementstyle.option.soundVolume"),
                        config.soundVolume)
                .setDefaultValue(defaultConfig.soundVolume)
                .setMin(0.0f)
                .setMax(1.0f)
                .setTooltip(Text.translatable("text.autoconfig.achievementstyle.option.soundVolume.@Tooltip"))
                .setSaveConsumer(value -> config.soundVolume = Math.max(0.0f, Math.min(1.0f, value)))
                .build());

        return builder.build();
    }
}