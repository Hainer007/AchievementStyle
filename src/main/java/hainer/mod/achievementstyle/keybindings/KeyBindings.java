package hainer.mod.achievementstyle.keybindings;

import com.mojang.blaze3d.platform.InputConstants;
import hainer.mod.achievementstyle.AchievementStyle;
import hainer.mod.achievementstyle.config.AchievementConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    private static KeyMapping configKeyBinding;
    private static KeyMapping testAchievementKeyBinding;
    private static boolean wasConfigPressed = false;
    private static boolean wasTestPressed = false;
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("achievementstyle", "keybindings"));


    public static void register() {
        configKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.achievementstyle.config", // ID для lang
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                CATEGORY
        ));

        testAchievementKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.achievementstyle.test",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY
        ));

                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                        if (configKeyBinding.consumeClick() && !wasConfigPressed) {
                wasConfigPressed = true;
                openConfigScreen();
            } else if (!configKeyBinding.isDown()) {
                wasConfigPressed = false;
            }

                        if (testAchievementKeyBinding.consumeClick() && !wasTestPressed) {
                wasTestPressed = true;
                showTestAchievement();
            } else if (!testAchievementKeyBinding.isDown()) {
                wasTestPressed = false;
            }
        });
    }

    private static void openConfigScreen() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.setScreen(AchievementConfig.createConfigScreen(client.screen));
        }
    }

    public static void showTestAchievement() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
                        Component title = Component.literal("Test Achievement");
            Component description = Component.literal("This is a test achievement!");
            ItemStack icon = new ItemStack(Items.DIAMOND); // Use diamond as the icon

                        AchievementStyle.showCustomAchievement(title, description, icon, false);
        }
    }

    public static KeyMapping getConfigKeyBinding() {
        return configKeyBinding;
    }

    public static KeyMapping getTestAchievementKeyBinding() {
        return testAchievementKeyBinding;
    }
}
