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
        // Create the key binding for opening the configuration
        configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.achievementstyle.config", // Localization key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I, // Key I
                "category.achievementstyle.keybindings" // Category in settings
        ));

        // Create the key binding for showing a test achievement
        testAchievementKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.achievementstyle.test", // Localization key
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.achievementstyle.keybindings" // Category in settings
        ));

        // Register the key event handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Handle config key press
            if (configKeyBinding.wasPressed() && !wasConfigPressed) {
                wasConfigPressed = true;
                openConfigScreen();
            } else if (!configKeyBinding.isPressed()) {
                wasConfigPressed = false;
            }

            // Handle test achievement key press
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

    private static void showTestAchievement() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            // Create a test achievement
            Text title = Text.literal("Test Achievement");
            Text description = Text.literal("This is a test achievement!");
            ItemStack icon = new ItemStack(Items.DIAMOND); // Use diamond as the icon

            // Call the method to show a custom achievement
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
