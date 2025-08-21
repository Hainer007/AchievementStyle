package hainer.mod.achievementstyle.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;

public class AchievementConfig {

    public static final ConfigClassHandler<AchievementConfig> HANDLER = ConfigClassHandler.createBuilder(AchievementConfig.class)
            .id(Identifier.of("achievementstyle", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("achievementstyle.json5"))
                    .build())
            .build();

    // Налаштування зовнішнього вигляду
    @SerialEntry
    public int achievementWidth = 170;

    @SerialEntry
    public int achievementHeight = 40;

    @SerialEntry
    public int verticalOffset = 25;

    // Налаштування анімації
    @SerialEntry
    public int slideDuration = 40;

    @SerialEntry
    public int displayDuration = 120;

    // Налаштування стилю
    @SerialEntry
    public int backgroundColor = 0xE0000000;

    @SerialEntry
    public int borderColor = 0x4A90E2;

    @SerialEntry
    public int rareBorderColor = 0x9B30FF; // Фіолетовий колір для рідкісних досягнень

    @SerialEntry
    public boolean enableShineEffect = true;

    // Налаштування позиції
    @SerialEntry
    public int achievementSpacing = 5;

    @SerialEntry
    public int achievementPosition = 0; // 0 = BOTTOM_RIGHT за замовчуванням

    // Налаштування звуку
    @SerialEntry
    public boolean soundEnabled = true;

    @SerialEntry
    public float soundVolume = 0.8f;

    public static AchievementConfig get() {
        return HANDLER.instance();
    }

    public static void init() {
        HANDLER.load();
    }

    public static Screen createConfigScreen(Screen parent) {
        AchievementConfig config = get();
        AchievementConfig defaults = new AchievementConfig();

        return YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("text.autoconfig.achievementstyle.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("text.autoconfig.achievementstyle.category.appearance"))
                        .tooltip(Text.translatable("text.autoconfig.achievementstyle.category.appearance.tooltip"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("text.autoconfig.achievementstyle.category.appearance"))
                                .collapsed(false)
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("text.autoconfig.achievementstyle.option.achievementWidth"))
                                        .description(OptionDescription.of(Text.translatable("text.autoconfig.achievementstyle.option.achievementWidth.@Tooltip")))
                                        .binding(defaults.achievementWidth, () -> config.achievementWidth, value -> config.achievementWidth = value)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(100, 300)
                                                .step(1))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("text.autoconfig.achievementstyle.option.achievementHeight"))
                                        .description(OptionDescription.of(Text.translatable("text.autoconfig.achievementstyle.option.achievementHeight.@Tooltip")))
                                        .binding(defaults.achievementHeight, () -> config.achievementHeight, value -> config.achievementHeight = value)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(20, 80)
                                                .step(1))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("text.autoconfig.achievementstyle.option.verticalOffset"))
                                        .description(OptionDescription.of(Text.translatable("text.autoconfig.achievementstyle.option.verticalOffset.@Tooltip")))
                                        .binding(defaults.verticalOffset, () -> config.verticalOffset, value -> config.verticalOffset = value)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 100)
                                                .step(1))
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("text.autoconfig.achievementstyle.category.style"))
                        .tooltip(Text.translatable("text.autoconfig.achievementstyle.category.style.tooltip"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("text.autoconfig.achievementstyle.category.style"))
                                .collapsed(false)
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("text.autoconfig.achievementstyle.option.backgroundColor"))
                                        .description(OptionDescription.of(Text.translatable("text.autoconfig.achievementstyle.option.backgroundColor.@Tooltip")))
                                        .binding(new Color(defaults.backgroundColor, true),
                                                () -> new Color(config.backgroundColor, true),
                                                value -> config.backgroundColor = value.getRGB())
                                        .controller(opt -> ColorControllerBuilder.create(opt)
                                                .allowAlpha(true))
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("text.autoconfig.achievementstyle.option.borderColor"))
                                        .description(OptionDescription.of(Text.translatable("text.autoconfig.achievementstyle.option.borderColor.@Tooltip")))
                                        .binding(new Color(defaults.borderColor),
                                                () -> new Color(config.borderColor),
                                                value -> config.borderColor = value.getRGB() & 0xFFFFFF)
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.<Color>createBuilder()
                                        .name(Text.translatable("text.autoconfig.achievementstyle.option.rareBorderColor"))
                                        .description(OptionDescription.of(Text.translatable("text.autoconfig.achievementstyle.option.rareBorderColor.@Tooltip")))
                                        .binding(new Color(defaults.rareBorderColor),
                                                () -> new Color(config.rareBorderColor),
                                                value -> config.rareBorderColor = value.getRGB() & 0xFFFFFF)
                                        .controller(ColorControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("text.autoconfig.achievementstyle.option.enableShineEffect"))
                                        .description(OptionDescription.of(Text.translatable("text.autoconfig.achievementstyle.option.enableShineEffect.@Tooltip")))
                                        .binding(defaults.enableShineEffect, () -> config.enableShineEffect, value -> config.enableShineEffect = value)
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .yesNoFormatter()
                                                .coloured(true))
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("text.autoconfig.achievementstyle.category.position"))
                        .tooltip(Text.translatable("text.autoconfig.achievementstyle.category.position.tooltip"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("text.autoconfig.achievementstyle.category.position"))
                                .collapsed(false)
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("text.autoconfig.achievementstyle.option.achievementPosition"))
                                        .description(OptionDescription.of(Text.translatable("text.autoconfig.achievementstyle.option.achievementPosition.@Tooltip")))
                                        .binding(defaults.achievementPosition, () -> config.achievementPosition, value -> config.achievementPosition = value)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 5)
                                                .step(1)
                                                .formatValue(value -> {
                                                    switch (value) {
                                                        case 0: return Text.translatable("text.autoconfig.achievementstyle.position.bottom_right");
                                                        case 1: return Text.translatable("text.autoconfig.achievementstyle.position.bottom_left");
                                                        case 2: return Text.translatable("text.autoconfig.achievementstyle.position.top_right");
                                                        case 3: return Text.translatable("text.autoconfig.achievementstyle.position.top_left");
                                                        case 4: return Text.translatable("text.autoconfig.achievementstyle.position.top_center");
                                                        case 5: return Text.translatable("text.autoconfig.achievementstyle.position.bottom_center");
                                                        default: return Text.literal("Unknown");
                                                    }
                                                }))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("text.autoconfig.achievementstyle.option.achievementSpacing"))
                                        .description(OptionDescription.of(Text.translatable("text.autoconfig.achievementstyle.option.achievementSpacing.@Tooltip")))
                                        .binding(defaults.achievementSpacing, () -> config.achievementSpacing, value -> config.achievementSpacing = value)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 20)
                                                .step(1))
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("text.autoconfig.achievementstyle.category.animation"))
                        .tooltip(Text.translatable("text.autoconfig.achievementstyle.category.animation.tooltip"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("text.autoconfig.achievementstyle.category.animation"))
                                .collapsed(false)
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("text.autoconfig.achievementstyle.option.slideDuration"))
                                        .description(OptionDescription.of(Text.translatable("text.autoconfig.achievementstyle.option.slideDuration.@Tooltip")))
                                        .binding(defaults.slideDuration, () -> config.slideDuration, value -> config.slideDuration = value)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(10, 100)
                                                .step(1))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("text.autoconfig.achievementstyle.option.displayDuration"))
                                        .description(OptionDescription.of(Text.translatable("text.autoconfig.achievementstyle.option.displayDuration.@Tooltip")))
                                        .binding(defaults.displayDuration, () -> config.displayDuration, value -> config.displayDuration = value)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(60, 300)
                                                .step(1))
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Text.translatable("text.autoconfig.achievementstyle.category.sound"))
                        .tooltip(Text.translatable("text.autoconfig.achievementstyle.category.sound.tooltip"))
                        .group(OptionGroup.createBuilder()
                                .name(Text.translatable("text.autoconfig.achievementstyle.category.sound"))
                                .collapsed(false)
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("text.autoconfig.achievementstyle.option.soundEnabled"))
                                        .description(OptionDescription.of(Text.translatable("text.autoconfig.achievementstyle.option.soundEnabled.@Tooltip")))
                                        .binding(defaults.soundEnabled, () -> config.soundEnabled, value -> config.soundEnabled = value)
                                        .controller(opt -> BooleanControllerBuilder.create(opt)
                                                .yesNoFormatter()
                                                .coloured(true))
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Text.translatable("text.autoconfig.achievementstyle.option.soundVolume"))
                                        .description(OptionDescription.of(Text.translatable("text.autoconfig.achievementstyle.option.soundVolume.@Tooltip")))
                                        .binding(defaults.soundVolume, () -> config.soundVolume, value -> config.soundVolume = value)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0.0f, 1.0f)
                                                .step(0.01f))
                                        .build())
                                .build())
                        .build())
                .save(HANDLER::save)
                .build()
                .generateScreen(parent);
    }
}