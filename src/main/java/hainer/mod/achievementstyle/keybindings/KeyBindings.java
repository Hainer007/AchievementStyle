package hainer.mod.achievementstyle.keybindings;

import hainer.mod.achievementstyle.AchievementStyle;
import hainer.mod.achievementstyle.config.AchievementConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    private static KeyBinding configKeyBinding;
    private static KeyBinding testAchievementKeyBinding;
    private static boolean wasConfigPressed = false;
    private static boolean wasTestPressed = false;

    public static void register() {
                configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.achievementstyle.config",                 InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,                 "category.achievementstyle.keybindings"         ));

                testAchievementKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.achievementstyle.test",                 InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.achievementstyle.keybindings"         ));

                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                        if (configKeyBinding.wasPressed() && !wasConfigPressed) {
                wasConfigPressed = true;
                openConfigScreen();
            } else if (!configKeyBinding.isPressed()) {
                wasConfigPressed = false;
            }

                        if (testAchievementKeyBinding.wasPressed() && !wasTestPressed) {
                wasTestPressed = true;
                showTestAchievement();
            } else if (!testAchievementKeyBinding.isPressed()) {
                wasTestPressed = false;
            }
        });
    }

    private static void openConfigScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.setScreen(AchievementConfig.createConfigScreen(client.currentScreen));
        }
    }

    public static void showTestAchievement() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
                        Text title = Text.literal("Test Achievement");
            Text description = Text.literal("This is a test achievement!");
            ItemStack icon = new ItemStack(Items.DIAMOND); // Use diamond as the icon

                        AchievementStyle.showCustomAchievement(title, description, icon, false);
        }
    }

    public static KeyBinding getConfigKeyBinding() {
        return configKeyBinding;
    }

    public static KeyBinding getTestAchievementKeyBinding() {
        return testAchievementKeyBinding;
    }
}
