package hainer.mod.achievementstyle;

import hainer.mod.achievementstyle.config.AchievementConfig;
import hainer.mod.achievementstyle.keybindings.KeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AchievementStyle implements ClientModInitializer {
	public static final String MOD_ID = "achievementstyle";

	public static final Identifier ACHIEVEMENT_SOUND_ID =
			Identifier.fromNamespaceAndPath(MOD_ID, "achievement_unlock");
	public static final SoundEvent ACHIEVEMENT_SOUND_EVENT =
			SoundEvent.createVariableRangeEvent(ACHIEVEMENT_SOUND_ID);
	private static final Identifier HUD_ID =
			Identifier.fromNamespaceAndPath(MOD_ID, "achievement_style");

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
		System.out.println("[" + MOD_ID + "] Steam Style Mod Client initializing.");

		Registry.register(BuiltInRegistries.SOUND_EVENT, ACHIEVEMENT_SOUND_ID, ACHIEVEMENT_SOUND_EVENT);

		AchievementConfig.init();
		KeyBindings.register();

		HudElementRegistry.attachElementBefore(
				VanillaHudElements.CHAT,
				HUD_ID,
				this::renderAchievements
		);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.level != null) {
				tickCounter++;
				updateAchievements();
			}
		});

		System.out.println("[" + MOD_ID + "] AchievementStyle Mod initialized successfully!");
	}

	public static void showAchievement(Advancement advancement) {
		if (advancement == null) return;

		AchievementConfig config = AchievementConfig.get();
		DisplayInfo display = advancement.display().orElse(null);

		if (display != null) {
			boolean isRare = display.getType() == AdvancementType.CHALLENGE;

			SteamAchievement steamAchievement = new SteamAchievement(
					display.getTitle(),
					display.getDescription(),
					display.getIcon().create(),
					-1,
					isRare
			);

			if (activeAchievements.size() >= config.achievementLimit) {
				pendingAchievements.add(steamAchievement);
			} else {
				addActiveAchievement(steamAchievement);
				System.out.println("[" + MOD_ID + "] Added Steam achievement: " + display.getTitle().getString());
			}
		}
	}

	public static void showCustomAchievement(Component title, Component description, ItemStack icon, boolean isRare) {
		AchievementConfig config = AchievementConfig.get();

		if (title == null) title = Component.literal("Achievement");
		if (description == null) description = Component.literal("");
		if (icon == null) icon = new ItemStack(Items.EXPERIENCE_BOTTLE);

		SteamAchievement steamAchievement = new SteamAchievement(
				title,
				description,
				icon,
				-1,
				isRare
		);

		if (activeAchievements.size() >= config.achievementLimit) {
			pendingAchievements.add(steamAchievement);
		} else {
			addActiveAchievement(steamAchievement);
			System.out.println("[" + MOD_ID + "] Added custom AchievementStyle achievement: " + title.getString());
		}
	}

	private static void addActiveAchievement(SteamAchievement achievement) {
		achievement.activate(tickCounter);
		activeAchievements.add(achievement);
		playAchievementSound();
	}

	private static void playAchievementSound() {
		Minecraft client = Minecraft.getInstance();
		if (client.player != null && client.level != null) {
			AchievementConfig config = AchievementConfig.get();
			if (config.soundEnabled) {
				client.player.playSound(ACHIEVEMENT_SOUND_EVENT, config.soundVolume, 1.0f);
			}
		}
	}

	private static void updateAchievements() {
		activeAchievements.removeIf(achievement -> achievement.shouldRemove(tickCounter));

		AchievementConfig config = AchievementConfig.get();
		while (activeAchievements.size() < config.achievementLimit && !pendingAchievements.isEmpty()) {
			SteamAchievement next = pendingAchievements.remove(0);
			addActiveAchievement(next);
		}
	}

	private void renderAchievements(GuiGraphicsExtractor context, DeltaTracker tickDelta) {
		Minecraft client = Minecraft.getInstance();
		if (client.options.hideGui || activeAchievements.isEmpty()) return;

		AchievementConfig config = AchievementConfig.get();
		int screenWidth = client.getWindow().getGuiScaledWidth();
		int screenHeight = client.getWindow().getGuiScaledHeight();

		int renderLimit = config.achievementLimit > 0 && config.achievementLimit <= 10
				? config.achievementLimit
				: activeAchievements.size();

		for (int i = 0; i < Math.min(renderLimit, activeAchievements.size()); i++) {
			SteamAchievement achievement = activeAchievements.get(i);
			int stackOffset = i * (config.achievementHeight + config.achievementSpacing);

			renderSteamAchievement(
					context,
					achievement,
					screenWidth,
					screenHeight,
					stackOffset,
					tickCounter,
					1.0f
			);
		}
	}

	private static void renderSteamAchievement(
			GuiGraphicsExtractor context,
			SteamAchievement achievement,
			int screenWidth,
			int screenHeight,
			int stackOffset,
			int currentTick,
			float tickDelta
	) {
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
		return switch (position) {
			case BOTTOM_RIGHT, TOP_RIGHT -> screenWidth - achievementWidth - 10;
			case BOTTOM_LEFT, TOP_LEFT -> 10;
			case TOP_CENTER, BOTTOM_CENTER -> (screenWidth - achievementWidth) / 2;
		};
	}

	private static int calculateBaseY(int screenHeight, int achievementHeight, int verticalOffset, AchievementPosition position) {
		return switch (position) {
			case BOTTOM_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER -> screenHeight - achievementHeight - verticalOffset;
			case TOP_RIGHT, TOP_LEFT, TOP_CENTER -> verticalOffset;
		};
	}

	private static int addStackOffset(int baseY, int stackOffset, AchievementPosition position) {
		return switch (position) {
			case BOTTOM_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER -> baseY - stackOffset;
			case TOP_RIGHT, TOP_LEFT, TOP_CENTER -> baseY + stackOffset;
		};
	}

	private static int calculateAnimatedX(int baseX, int screenWidth, int achievementWidth, float slideProgress, AchievementPosition position) {
		return switch (position) {
			case BOTTOM_RIGHT, TOP_RIGHT -> (int) (screenWidth - (achievementWidth + 10) * slideProgress);
			case BOTTOM_LEFT, TOP_LEFT -> (int) (baseX - achievementWidth + achievementWidth * slideProgress);
			case TOP_CENTER, BOTTOM_CENTER -> baseX;
		};
	}

	private static int calculateAnimatedY(int stackedY, int screenHeight, int achievementHeight, float slideProgress, AchievementPosition position) {
		return switch (position) {
			case TOP_CENTER -> (int) (stackedY - achievementHeight * (1.0f - slideProgress));
			case BOTTOM_CENTER -> (int) (stackedY + achievementHeight * (1.0f - slideProgress));
			default -> stackedY;
		};
	}

	private static void drawThickBorder(GuiGraphicsExtractor context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + 1, color);
		context.fill(x, y + height - 1, x + width, y + height, color);
		context.fill(x, y, x + 1, y + height, color);
		context.fill(x + width - 1, y, x + width, y + height, color);
	}

	private static void drawAchievementBox(GuiGraphicsExtractor context, AchievementConfig config, SteamAchievement achievement, int x, int y) {
		context.fill(x, y, x + config.achievementWidth, y + config.achievementHeight, config.backgroundColor);

		int borderColorToUse = achievement.isRare ? config.rareBorderColor : config.borderColor;
		int borderColorWithAlpha = ((borderColorToUse & 0xFF000000) == 0)
				? 0xFF000000 | (borderColorToUse & 0x00FFFFFF)
				: borderColorToUse;

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

			context.fill(
					x + config.achievementWidth - 15,
					y + 3,
					x + config.achievementWidth - 3,
					y + 5,
					(shineAlpha << 24) | 0xFFFFFF
			);
		}
	}

	private static void drawAchievementContent(GuiGraphicsExtractor context, AchievementConfig config, SteamAchievement achievement, int x, int y) {
		Minecraft client = Minecraft.getInstance();

		context.pose().pushMatrix();

		float iconScale = Math.min(1.5f, (config.achievementHeight - 12) / 16.0f);
		context.pose().translate((float) (x + 6), (float) (y + 6));
		context.pose().scale(iconScale, iconScale);

		context.item(achievement.icon, 0, 0);

		context.pose().popMatrix();

		int titleY = y + Math.max(6, (config.achievementHeight - 24) / 3);
		context.text(client.font, achievement.title, x + 33, titleY, 0xFFFFFFFF, false);

		Component description = achievement.description;
		if (description != null && config.achievementHeight > 30) {
			String descText = description.getString();

			if (client.font.width(descText) > config.achievementWidth - 40) {
				descText = client.font.plainSubstrByWidth(descText, config.achievementWidth - 45) + "...";
			}

			int descY = titleY + 12;
			if (descY + 9 <= y + config.achievementHeight - 3) {
				context.text(client.font, Component.literal(descText), x + 33, descY, 0xFFCCCCCC, false);
			}
		}
	}

	private static class SteamAchievement {
		private final Component title;
		private final Component description;
		private final ItemStack icon;
		private int startTick;
		private final boolean isRare;

		public SteamAchievement(Component title, Component description, ItemStack icon, int startTick, boolean isRare) {
			this.title = title;
			this.description = description;
			this.icon = icon;
			this.startTick = startTick;
			this.isRare = isRare;
		}

		public void activate(int currentTick) {
			this.startTick = currentTick;
		}

		public boolean shouldRemove(int currentTick) {
			AchievementConfig config = AchievementConfig.get();
			if (startTick < 0) return false;

			int elapsed = currentTick - startTick;
			return elapsed > (config.slideDuration + config.displayDuration + config.slideDuration);
		}

		public float getSlideProgress(int currentTick, float tickDelta) {
			AchievementConfig config = AchievementConfig.get();
			if (startTick < 0) return 0.0f;

			float elapsed = (currentTick - startTick) + tickDelta;

			if (elapsed < config.slideDuration) {
				float progress = elapsed / config.slideDuration;
				return easeOutCubic(progress);
			} else if (elapsed < config.slideDuration + config.displayDuration) {
				return 1.0f;
			} else if (elapsed < config.slideDuration + config.displayDuration + config.slideDuration) {
				float slideOutProgress =
						(elapsed - config.slideDuration - config.displayDuration) / config.slideDuration;
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