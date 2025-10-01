package hainer.mod.achievementstyle;

import hainer.mod.achievementstyle.config.AchievementConfig;
import hainer.mod.achievementstyle.keybindings.KeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
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
	private static final List<SteamAchievement> pendingAchievements = new ArrayList<>();
	private static final Map<Identifier, Boolean> completedAchievements = new HashMap<>();
	private static int tickCounter = 0;

	public enum AchievementPosition {
		BOTTOM_RIGHT("bottom_right"),
		BOTTOM_LEFT("bottom_left"),
		TOP_RIGHT("top_right"),
		TOP_LEFT("top_left"),
		TOP_CENTER("top_center"),
		BOTTOM_CENTER("bottom_center");

		private final String key;

		AchievementPosition(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}

		public static AchievementPosition fromOrdinal(int ordinal) {
			AchievementPosition[] values = values();
			if (ordinal >= 0 && ordinal < values.length) {
				return values[ordinal];
			}
			return BOTTOM_RIGHT;
		}
	}

	@Override
	public void onInitializeClient() {
		System.out.println("[" + MOD_ID + "] Steam Style Mod Client initializing...");

		Registry.register(Registries.SOUND_EVENT, ACHIEVEMENT_SOUND_ID, ACHIEVEMENT_SOUND_EVENT);

		AchievementConfig.init();
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

		AchievementConfig config = AchievementConfig.get();

		AdvancementDisplay display = advancement.display().orElse(null);
		if (display != null) {
			boolean isRare = display.getFrame() == AdvancementFrame.CHALLENGE;

			SteamAchievement steamAchievement = new SteamAchievement(
					display.getTitle(),
					display.getDescription(),
					display.getIcon(),
					tickCounter,
					isRare
			);
			if (activeAchievements.size() >= config.achievementLimit) {
				pendingAchievements.add(steamAchievement);
			} else {
				activeAchievements.add(steamAchievement);
				playAchievementSound();
				System.out.println("[" + MOD_ID + "] Added Steam achievement: " + display.getTitle().getString());
			}
		}
	}

	public static void showCustomAchievement(Text title, Text description, ItemStack icon, boolean isRare) {
		AchievementConfig config = AchievementConfig.get();

		if (title == null) title = Text.literal("Achievement");
		if (description == null) description = Text.literal("");
		if (icon == null) icon = new ItemStack(net.minecraft.item.Items.EXPERIENCE_BOTTLE);

		SteamAchievement steamAchievement = new SteamAchievement(
				title,
				description,
				icon,
				tickCounter,
				isRare
		);
		if (activeAchievements.size() >= config.achievementLimit) {
			pendingAchievements.add(steamAchievement);
		} else {
			activeAchievements.add(steamAchievement);
			playAchievementSound();
			System.out.println("[" + MOD_ID + "] Added custom AchievementStyle achievement: " + title.getString());
		}
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
		boolean removed = activeAchievements.removeIf(
				achievement -> achievement.shouldRemove(tickCounter)
		);

		AchievementConfig config = AchievementConfig.get();
		while (activeAchievements.size() < config.achievementLimit && !pendingAchievements.isEmpty()) {
			SteamAchievement next = pendingAchievements.remove(0);
			activeAchievements.add(next);
			playAchievementSound();
		}
	}

	private void renderAchievements(DrawContext context, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.options.hudHidden || activeAchievements.isEmpty()) return;

		AchievementConfig config = AchievementConfig.get();
		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();

		int renderLimit = config.achievementLimit > 0 && config.achievementLimit <= 10 ? config.achievementLimit : activeAchievements.size();

		for (int i = 0; i < Math.min(renderLimit, activeAchievements.size()); i++) {
			SteamAchievement achievement = activeAchievements.get(i);
			int stackOffset = i * (config.achievementHeight + config.achievementSpacing);
			renderSteamAchievement(context, achievement, screenWidth, screenHeight, stackOffset, tickCounter, tickDelta);
		}
	}

	private static void renderSteamAchievement(DrawContext context, SteamAchievement achievement,
											   int screenWidth, int screenHeight, int stackOffset, int currentTick, float tickDelta) {
		AchievementConfig config = AchievementConfig.get();
		AchievementPosition position = AchievementPosition.fromOrdinal(config.achievementPosition);

		int baseX = calculateBaseX(screenWidth, config.achievementWidth, position);
		int baseY = calculateBaseY(screenHeight, config.achievementHeight, config.verticalOffset, position);

		int stackedY = addStackOffset(baseY, stackOffset, position);

		float slideProgress = achievement.getSlideProgress(currentTick, tickDelta);
		int animatedX = calculateAnimatedX(baseX, screenWidth, config.achievementWidth, slideProgress, position);
		int animatedY = calculateAnimatedY(stackedY, screenHeight, config.achievementHeight, slideProgress, position);

		drawAchievementBox(context, config, achievement, animatedX, animatedY);
		drawAchievementContent(context, config, achievement, animatedX, animatedY);
	}

	private static int calculateBaseX(int screenWidth, int achievementWidth, AchievementPosition position) {
		switch (position) {
			case BOTTOM_RIGHT:
			case TOP_RIGHT:
				return screenWidth - achievementWidth - 10;
			case BOTTOM_LEFT:
			case TOP_LEFT:
				return 10;
			case TOP_CENTER:
			case BOTTOM_CENTER:
				return (screenWidth - achievementWidth) / 2;
			default:
				return screenWidth - achievementWidth - 10;
		}
	}

	private static int calculateBaseY(int screenHeight, int achievementHeight, int verticalOffset, AchievementPosition position) {
		switch (position) {
			case BOTTOM_RIGHT:
			case BOTTOM_LEFT:
			case BOTTOM_CENTER:
				return screenHeight - achievementHeight - verticalOffset;
			case TOP_RIGHT:
			case TOP_LEFT:
			case TOP_CENTER:
				return verticalOffset;
			default:
				return screenHeight - achievementHeight - verticalOffset;
		}
	}

	private static int addStackOffset(int baseY, int stackOffset, AchievementPosition position) {
		switch (position) {
			case BOTTOM_RIGHT:
			case BOTTOM_LEFT:
			case BOTTOM_CENTER:
				return baseY - stackOffset;
			case TOP_RIGHT:
			case TOP_LEFT:
			case TOP_CENTER:
				return baseY + stackOffset;
			default:
				return baseY - stackOffset;
		}
	}

	private static int calculateAnimatedX(int baseX, int screenWidth, int achievementWidth, float slideProgress, AchievementPosition position) {
		switch (position) {
			case BOTTOM_RIGHT:
			case TOP_RIGHT:
				return (int) (screenWidth - (achievementWidth + 10) * slideProgress);
			case BOTTOM_LEFT:
			case TOP_LEFT:
				return (int) (baseX - achievementWidth + achievementWidth * slideProgress);
			case TOP_CENTER:
			case BOTTOM_CENTER:
				return baseX;
			default:
				return (int) (screenWidth - (achievementWidth + 10) * slideProgress);
		}
	}

	private static int calculateAnimatedY(int stackedY, int screenHeight, int achievementHeight, float slideProgress, AchievementPosition position) {
		switch (position) {
			case TOP_CENTER:
				return (int) (stackedY - achievementHeight * (1.0f - slideProgress));
			case BOTTOM_CENTER:
				return (int) (stackedY + achievementHeight * (1.0f - slideProgress));
			default:
				return stackedY;
		}
	}

	/**
	 * Малює товсту рамку (2 пікселі)
	 */
	private static void drawThickBorder(DrawContext context, int x, int y, int width, int height, int color) {
		// Up
		context.fill(x, y, x + width, y + 1, color);
		// Down
		context.fill(x, y + height - 1, x + width, y + height, color);
		// Left
		context.fill(x, y, x + 1, y + height, color);
		// Right
		context.fill(x + width - 1, y, x + width, y + height, color);
	}

	private static void drawAchievementBox(DrawContext context, AchievementConfig config, SteamAchievement achievement, int x, int y) {
		context.fill(x, y, x + config.achievementWidth, y + config.achievementHeight, config.backgroundColor);
		int borderColorToUse = achievement.isRare ? config.rareBorderColor : config.borderColor;
		int borderColorWithAlpha;
		if ((borderColorToUse & 0xFF000000) == 0) {
			borderColorWithAlpha = 0xFF000000 | (borderColorToUse & 0x00FFFFFF);
		} else {
			borderColorWithAlpha = borderColorToUse;
		}

		drawThickBorder(context, x, y, config.achievementWidth, config.achievementHeight, borderColorWithAlpha);

		for (int i = 2; i < config.achievementHeight - 2; i++) {
			int alpha = (int) (30 * (1.0f - (float) (i - 2) / (config.achievementHeight - 4)));
			int red = (borderColorWithAlpha >> 16) & 0xFF;
			int green = (borderColorWithAlpha >> 8) & 0xFF;
			int blue = borderColorWithAlpha & 0xFF;
			int color = (alpha << 24) | (red << 16) | (green << 8) | blue;
			context.fill(x + 2, y + i, x + config.achievementWidth - 2, y + i + 1, color);
		}

		if (config.enableShineEffect) {
			long time = System.currentTimeMillis();
			double shine = Math.sin(time * 0.005) * 0.3 + 0.7;
			int shineAlpha = (int) (100 * shine);
			context.fill(x + config.achievementWidth - 15, y + 3, x + config.achievementWidth - 3, y + 5,
					(shineAlpha << 24) | 0xFFFFFF);
		}
	}

	private static void drawAchievementContent(DrawContext context, AchievementConfig config, SteamAchievement achievement, int x, int y) {
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
	}

	private static class SteamAchievement {
		private final Text title;
		private final Text description;
		private final ItemStack icon;
		private final int startTick;
		private final boolean isRare;

		public SteamAchievement(Text title, @Nullable Text description, ItemStack icon, int startTick, boolean isRare) {
			this.title = title;
			this.description = description;
			this.icon = icon;
			this.startTick = startTick;
			this.isRare = isRare;
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