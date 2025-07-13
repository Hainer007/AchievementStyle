package hainer.mod.achievementstyle;

import hainer.mod.achievementstyle.config.AchievementConfig;
import hainer.mod.achievementstyle.keybindings.KeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AchievementStyle implements ClientModInitializer {
	public static final String MOD_ID = "achievementstyle";


	public static final Identifier ACHIEVEMENT_SOUND_ID = Identifier.of(MOD_ID, "achievement_unlock");
	public static final SoundEvent ACHIEVEMENT_SOUND_EVENT = SoundEvent.of(ACHIEVEMENT_SOUND_ID);

	private static final List<SteamAchievement> activeAchievements = new ArrayList<>();
	private static final Map<Identifier, Boolean> completedAchievements = new HashMap<>();
	private static int tickCounter = 0;

	@Override
	public void onInitializeClient() {
		System.out.println("[" + MOD_ID + "] Steam Style Mod Client initializing...");


		Registry.register(Registries.SOUND_EVENT, ACHIEVEMENT_SOUND_ID, ACHIEVEMENT_SOUND_EVENT);

		AchievementConfig.init();
		// Initialize AchievementConfig
		AchievementConfig.init();

		// Register KeyBindings
		KeyBindings.register();


		HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
			renderAchievements(drawContext, 1.0f);
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.world != null) {
				tickCounter++;
				updateAchievements();
			}
		});

		System.out.println("[" + MOD_ID + "] AchievementStyle Mod initialized successfully!");
	}

	public static void showAchievement(Advancement advancement) {
		if (advancement == null) return;

		AdvancementDisplay display = advancement.display().orElse(null);
		if (display != null) {
			SteamAchievement steamAchievement = new SteamAchievement(
					display.getTitle(),
					display.getDescription(),
					display.getIcon(),
					tickCounter
			);
			activeAchievements.add(steamAchievement);


			playAchievementSound();

			System.out.println("[" + MOD_ID + "] Added Steam achievement: " + display.getTitle().getString());
		}
	}

	public static void showCustomAchievement(Text title, Text description, ItemStack icon) {
		if (title == null) title = Text.literal("Achievement");
		if (description == null) description = Text.literal("");
		if (icon == null) icon = new ItemStack(net.minecraft.item.Items.EXPERIENCE_BOTTLE);

		SteamAchievement steamAchievement = new SteamAchievement(
				title,
				description,
				icon,
				tickCounter
		);
		activeAchievements.add(steamAchievement);


		playAchievementSound();

		System.out.println("[" + MOD_ID + "] Added custom AchievementStyle achievement: " + title.getString());
	}

	private static void playAchievementSound() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null && client.world != null) {
			AchievementConfig config = AchievementConfig.get();
			if (config.soundEnabled) {
				client.player.playSound(ACHIEVEMENT_SOUND_EVENT, config.soundVolume, 1.0f);
			}
		}
	}

	private static void updateAchievements() {
		activeAchievements.removeIf(achievement -> achievement.shouldRemove(tickCounter));
	}

	private void renderAchievements(DrawContext context, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.options.hudHidden || activeAchievements.isEmpty()) return;

		AchievementConfig config = AchievementConfig.get();
		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();

		for (int i = 0; i < activeAchievements.size(); i++) {
			SteamAchievement achievement = activeAchievements.get(i);
			int yOffset = i * (config.achievementHeight + config.achievementSpacing);
			renderSteamAchievement(context, achievement, screenWidth, screenHeight, yOffset, tickCounter, tickDelta);
		}
	}

	private static void renderSteamAchievement(DrawContext context, SteamAchievement achievement,
											   int screenWidth, int screenHeight, int yOffset, int currentTick, float tickDelta) {

		AchievementConfig config = AchievementConfig.get();

		int baseY = screenHeight - config.achievementHeight - config.verticalOffset - yOffset;

		float slideProgress = achievement.getSlideProgress(currentTick, tickDelta);
		int x = (int) (screenWidth - (config.achievementWidth + 10) * slideProgress);
		int y = baseY;

		context.fill(x, y, x + config.achievementWidth, y + config.achievementHeight, config.backgroundColor);

		int borderColorWithAlpha = 0xFF000000 | (config.borderColor & 0x00FFFFFF);
		context.drawBorder(x, y, config.achievementWidth, config.achievementHeight, borderColorWithAlpha);

		for (int i = 0; i < config.achievementHeight; i++) {
			int alpha = (int) (30 * (1.0f - (float) i / config.achievementHeight));
			int red = (config.borderColor >> 16) & 0xFF;
			int green = (config.borderColor >> 8) & 0xFF;
			int blue = config.borderColor & 0xFF;
			int color = (alpha << 24) | (red << 16) | (green << 8) | blue;
			context.fill(x + 1, y + i, x + config.achievementWidth - 1, y + i + 1, color);
		}

		context.getMatrices().pushMatrix();
		context.getMatrices().translate(x + 6, y + 6);
		float iconScale = Math.min(1.5f, (config.achievementHeight - 12) / 16.0f);
		context.getMatrices().scale(iconScale, iconScale);
		context.drawItem(achievement.icon, 0, 0);
		context.getMatrices().popMatrix();

		MinecraftClient client = MinecraftClient.getInstance();

		int titleY = y + Math.max(6, (config.achievementHeight - 24) / 3);
		context.drawTextWithShadow(client.textRenderer, achievement.title,
				x + 32, titleY, 0xFFFFFFFF);

		Text description = achievement.description;
		if (description != null && config.achievementHeight > 30) {
			String descText = description.getString();
			if (client.textRenderer.getWidth(descText) > config.achievementWidth - 40) {
				descText = client.textRenderer.trimToWidth(descText, config.achievementWidth - 45) + "...";
			}
			int descY = titleY + 12;
			if (descY + 9 <= y + config.achievementHeight - 3) {
				context.drawTextWithShadow(client.textRenderer, Text.literal(descText),
						x + 32, descY, 0xFFCCCCCC);
			}
		}

		if (config.enableShineEffect) {
			long time = System.currentTimeMillis();
			double shine = Math.sin(time * 0.01) * 0.3 + 0.7;
			int shineAlpha = (int) (100 * shine);
			context.fill(x + config.achievementWidth - 15, y + 3, x + config.achievementWidth - 3, y + 5,
					(shineAlpha << 24) | 0xFFFFFF);
		}
	}

	private static class SteamAchievement {
		private final Text title;
		private final Text description;
		private final ItemStack icon;
		private final int startTick;

		public SteamAchievement(Text title, @Nullable Text description, ItemStack icon, int startTick) {
			this.title = title;
			this.description = description;
			this.icon = icon;
			this.startTick = startTick;
		}

		public boolean shouldRemove(int currentTick) {
			AchievementConfig config = AchievementConfig.get();
			int elapsed = currentTick - startTick;
			return elapsed > (config.slideDuration + config.displayDuration + config.slideDuration);
		}

		public float getSlideProgress(int currentTick, float tickDelta) {
			AchievementConfig config = AchievementConfig.get();
			float elapsed = (currentTick - startTick) + tickDelta;

			if (elapsed < config.slideDuration) {
				float progress = elapsed / config.slideDuration;
				return easeOutCubic(progress);
			} else if (elapsed < config.slideDuration + config.displayDuration) {
				return 1.0f;
			} else if (elapsed < config.slideDuration + config.displayDuration + config.slideDuration) {
				float slideOutProgress = (elapsed - config.slideDuration - config.displayDuration) / config.slideDuration;
				return 1.0f - easeInCubic(slideOutProgress);
			} else {
				return 0.0f;
			}
		}

		private float easeOutCubic(float t) {
			return 1.0f - (float) Math.pow(1.0f - t, 3.0);
		}

		private float easeInCubic(float t) {
			return (float) Math.pow(t, 3.0);
		}
	}
}