package hainer.mod.achievementstyle;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AchievementStyle implements ClientModInitializer {
	public static final String MOD_ID = "achievementstyle";
	private static final List<SteamAchievement> activeAchievements = new ArrayList<>();
	private static final Map<Identifier, Boolean> completedAchievements = new HashMap<>();
	private static final int ACHIEVEMENT_WIDTH = 170;
	private static final int ACHIEVEMENT_HEIGHT = 40;
	private static final int SLIDE_DURATION = 40;
	private static final int DISPLAY_DURATION = 120;
	private static final int VERTICAL_OFFSET = 25; // Відступ від низу екрану
	private static int tickCounter = 0;

	@Override
	public void onInitializeClient() {
		System.out.println("[" + MOD_ID + "] Steam Style Mod Client initializing...");

		// Register HUD rendering
		HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
			renderAchievements(drawContext, 1.0f); // Use fixed tick delta for simplicity
		});

		// Register tick handler
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.world != null) {
				tickCounter++;
				updateAchievements();
			}
		});

		System.out.println("[" + MOD_ID + "] Steam Style Mod Client initialized successfully!");
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
		System.out.println("[" + MOD_ID + "] Added custom Steam achievement: " + title.getString());
	}

	private static void updateAchievements() {
		activeAchievements.removeIf(achievement -> achievement.shouldRemove(tickCounter));
	}

	private void renderAchievements(DrawContext context, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.options.hudHidden || activeAchievements.isEmpty()) return;

		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();

		for (int i = 0; i < activeAchievements.size(); i++) {
			SteamAchievement achievement = activeAchievements.get(i);
			int yOffset = i * (ACHIEVEMENT_HEIGHT + 5);
			renderSteamAchievement(context, achievement, screenWidth, screenHeight, yOffset, tickCounter, tickDelta);
		}
	}

	private static void renderSteamAchievement(DrawContext context, SteamAchievement achievement,
											   int screenWidth, int screenHeight, int yOffset, int currentTick, float tickDelta) {

		// Тепер досягнення з'являються вище - використовуємо VERTICAL_OFFSET
		int baseY = screenHeight - ACHIEVEMENT_HEIGHT - VERTICAL_OFFSET - yOffset;

		// Animation
		float slideProgress = achievement.getSlideProgress(currentTick, tickDelta);
		int x = (int) (screenWidth - (ACHIEVEMENT_WIDTH + 10) * slideProgress);
		int y = baseY;

		// Background (dark rectangle with Steam-style)
		context.fill(x, y, x + ACHIEVEMENT_WIDTH, y + ACHIEVEMENT_HEIGHT, 0xE0000000);

		// Border
		context.drawBorder(x, y, ACHIEVEMENT_WIDTH, ACHIEVEMENT_HEIGHT, 0xFF4A90E2);

		// Gradient for more Steam-like appearance
		for (int i = 0; i < ACHIEVEMENT_HEIGHT; i++) {
			int alpha = (int) (30 * (1.0f - (float) i / ACHIEVEMENT_HEIGHT));
			int color = (alpha << 24) | 0x4A90E2;
			context.fill(x + 1, y + i, x + ACHIEVEMENT_WIDTH - 1, y + i + 1, color);
		}

		// Item/block icon
		context.getMatrices().push();
		context.getMatrices().translate(x + 6, y + 6, 0);
		context.getMatrices().scale(1.5f, 1.5f, 1.0f);
		context.drawItem(achievement.icon, 0, 0);
		context.getMatrices().pop();

		// Achievement text
		MinecraftClient client = MinecraftClient.getInstance();

		// Title
		context.drawTextWithShadow(client.textRenderer, achievement.title,
				x + 32, y + 6, 0xFFFFFF);

		// Description
		Text description = achievement.description;
		if (description != null) {
			// Trim text if too long
			String descText = description.getString();
			if (client.textRenderer.getWidth(descText) > ACHIEVEMENT_WIDTH - 40) {
				descText = client.textRenderer.trimToWidth(descText, ACHIEVEMENT_WIDTH - 45) + "...";
			}
			context.drawTextWithShadow(client.textRenderer, Text.literal(descText),
					x + 32, y + 18, 0xCCCCCC);
		}

		// Extra decoration (small shine effect)
		long time = System.currentTimeMillis();
		double shine = Math.sin(time * 0.01) * 0.3 + 0.7;
		int shineAlpha = (int) (100 * shine);
		context.fill(x + ACHIEVEMENT_WIDTH - 15, y + 3, x + ACHIEVEMENT_WIDTH - 3, y + 5,
				(shineAlpha << 24) | 0xFFFFFF);
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
			int elapsed = currentTick - startTick;
			return elapsed > (SLIDE_DURATION + DISPLAY_DURATION + SLIDE_DURATION);
		}

		public float getSlideProgress(int currentTick, float tickDelta) {
			float elapsed = (currentTick - startTick) + tickDelta;

			if (elapsed < SLIDE_DURATION) {
				// Smooth appearance animation with easing
				float progress = elapsed / SLIDE_DURATION;
				return easeOutCubic(progress);
			} else if (elapsed < SLIDE_DURATION + DISPLAY_DURATION) {
				// Full display
				return 1.0f;
			} else if (elapsed < SLIDE_DURATION + DISPLAY_DURATION + SLIDE_DURATION) {
				// Smooth disappearance animation with easing
				float slideOutProgress = (elapsed - SLIDE_DURATION - DISPLAY_DURATION) / SLIDE_DURATION;
				return 1.0f - easeInCubic(slideOutProgress);
			} else {
				return 0.0f;
			}
		}

		// Smooth animation functions
		private float easeOutCubic(float t) {
			return 1.0f - (float) Math.pow(1.0f - t, 3.0);
		}

		private float easeInCubic(float t) {
			return (float) Math.pow(t, 3.0);
		}
	}
}