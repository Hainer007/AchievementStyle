package hainer.mod.achievementstyle;

import hainer.mod.achievementstyle.config.AchievementConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;

@Environment(EnvType.CLIENT)
public class AchievementStyle implements ClientModInitializer {
	public static final String MOD_ID = "achievementstyle";

	public static final Identifier ACHIEVEMENT_SOUND_ID = new Identifier(MOD_ID, "achievement_unlock");
	public static final SoundEvent ACHIEVEMENT_SOUND_EVENT = SoundEvent.of(ACHIEVEMENT_SOUND_ID);

	private static final List<SteamAchievement> activeAchievements = new ArrayList<>();
	private static final Map<Identifier, Boolean> achievementStates = new HashMap<>();
	private static int tickCounter = 0;
	private static boolean initialized = false;
	private static int initializationDelay = 0;

	@Override
	public void onInitializeClient() {
		System.out.println("[" + MOD_ID + "] Steam Style Achievements initializing...");

		try {
			
			Registry.register(Registries.SOUND_EVENT, ACHIEVEMENT_SOUND_ID, ACHIEVEMENT_SOUND_EVENT);

			
			AchievementConfig.init();

			
			ClientTickEvents.END_CLIENT_TICK.register(client -> {
				if (client.player != null && client.world != null) {
					tick();

					
					if (!initialized) {
						initializationDelay++;
						if (initializationDelay > 100) { 
							initializeAchievementStates(client);
						}
					}

					
					totalToastAnnihilation(client);

					
					if (initialized && tickCounter % 5 == 0) {
						checkAchievements(client);
					}
				}
			});

			
			HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
				MinecraftClient mc = MinecraftClient.getInstance();
				if (mc.player != null && mc.world != null) {
					renderAchievements(drawContext, tickDelta);
				}
			});

			
			ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
				activeAchievements.clear();
				achievementStates.clear();
				initialized = false;
				initializationDelay = 0;
			});

			System.out.println("[" + MOD_ID + "] Steam Style Achievements initialized successfully!");
		} catch (Exception e) {
			System.err.println("[" + MOD_ID + "] Error during initialization: " + e.getMessage());
			e.printStackTrace();
		}
	}

	
	private static void totalToastAnnihilation(MinecraftClient client) {
		try {
			ToastManager toastManager = client.getToastManager();

			clearAllFieldsRecursively(toastManager);
			clearViaAllMethods(toastManager);
			replaceAdvancementToasts(toastManager);

		} catch (Exception e) {
			
		}
	}

	private static void clearAllFieldsRecursively(Object obj) {
		if (obj == null) return;

		try {
			Class<?> currentClass = obj.getClass();

			while (currentClass != null && currentClass != Object.class) {
				for (Field field : currentClass.getDeclaredFields()) {
					try {
						field.setAccessible(true);
						Object value = field.get(obj);

						if (value instanceof Collection) {
							Collection<?> collection = (Collection<?>) value;
							collection.removeIf(item -> item instanceof AdvancementToast);

						} else if (value instanceof Map) {
							Map<?, ?> map = (Map<?, ?>) value;
							map.entrySet().removeIf(entry ->
									entry.getKey() instanceof AdvancementToast ||
											entry.getValue() instanceof AdvancementToast);

						} else if (value instanceof AdvancementToast) {
							field.set(obj, null);

						} else if (value != null && value.getClass().isArray()) {
							if (value instanceof Object[]) {
								Object[] array = (Object[]) value;
								for (int i = 0; i < array.length; i++) {
									if (array[i] instanceof AdvancementToast) {
										array[i] = null;
									}
								}
							}
						}
					} catch (Exception e) {
						
					}
				}
				currentClass = currentClass.getSuperclass();
			}
		} catch (Exception e) {
			
		}
	}

	private static void clearViaAllMethods(Object obj) {
		try {
			Method[] methods = obj.getClass().getDeclaredMethods();

			for (Method method : methods) {
				try {
					String methodName = method.getName().toLowerCase();
					if ((methodName.contains("clear") ||
							methodName.contains("remove") ||
							methodName.contains("reset")) &&
							method.getParameterCount() == 0) {

						method.setAccessible(true);
						method.invoke(obj);
					}

					else if (methodName.startsWith("get") &&
							method.getParameterCount() == 0) {

						method.setAccessible(true);
						Object result = method.invoke(obj);

						if (result instanceof Collection) {
							Collection<?> collection = (Collection<?>) result;
							collection.removeIf(item -> item instanceof AdvancementToast);
						}
					}
				} catch (Exception e) {
					
				}
			}
		} catch (Exception e) {
			
		}
	}

	private static void replaceAdvancementToasts(ToastManager toastManager) {
		try {
			Field[] fields = toastManager.getClass().getDeclaredFields();

			for (Field field : fields) {
				try {
					field.setAccessible(true);
					Object value = field.get(toastManager);

					if (value != null && value.getClass().isArray()) {
						Object[] array = (Object[]) value;
						for (int i = 0; i < array.length; i++) {
							if (array[i] != null) {
								try {
									Method getInstanceMethod = array[i].getClass().getMethod("getInstance");
									Object instance = getInstanceMethod.invoke(array[i]);
									if (instance instanceof AdvancementToast) {
										array[i] = null;
									}
								} catch (Exception e) {
									array[i] = null;
								}
							}
						}
					}
				} catch (Exception e) {
					
				}
			}
		} catch (Exception e) {
			
		}
	}

	private static void initializeAchievementStates(MinecraftClient client) {
		try {
			if (client.player == null || client.player.networkHandler == null) {
				return;
			}

			ClientAdvancementManager manager = client.player.networkHandler.getAdvancementHandler();
			if (manager == null) return;

			achievementStates.clear();
			int displayableAdvancements = 0;

			for (Advancement advancement : manager.getManager().getAdvancements()) {
				if (advancement == null) continue;

				AdvancementDisplay display = advancement.getDisplay();
				if (display == null) continue;

				displayableAdvancements++;
				Identifier id = advancement.getId();
				AdvancementProgress progress = getAdvancementProgress(manager, advancement);
				boolean isCompleted = progress != null && progress.isDone();

				achievementStates.put(id, isCompleted);
			}

			initialized = true;
			long completedCount = achievementStates.values().stream().mapToLong(b -> b ? 1 : 0).sum();
			System.out.println("[" + MOD_ID + "] Initialized " + displayableAdvancements +
					" achievements (" + completedCount + " completed)");
		} catch (Exception e) {
			System.err.println("[" + MOD_ID + "] Error initializing: " + e.getMessage());
		}
	}

	private static void checkAchievements(MinecraftClient client) {
		if (!initialized) return;

		try {
			if (client.player == null || client.player.networkHandler == null) return;

			ClientAdvancementManager manager = client.player.networkHandler.getAdvancementHandler();
			if (manager == null) return;

			for (Advancement advancement : manager.getManager().getAdvancements()) {
				if (advancement == null) continue;

				AdvancementDisplay display = advancement.getDisplay();
				if (display == null) continue;

				Identifier id = advancement.getId();
				boolean wasCompleted = achievementStates.getOrDefault(id, false);

				AdvancementProgress progress = getAdvancementProgress(manager, advancement);
				boolean isNowCompleted = progress != null && progress.isDone();

				if (!wasCompleted && isNowCompleted) {
					showAchievement(advancement);
					achievementStates.put(id, true);
				}
			}
		} catch (Exception e) {
			System.err.println("[" + MOD_ID + "] Error checking achievements: " + e.getMessage());
		}
	}

	private static AdvancementProgress getAdvancementProgress(ClientAdvancementManager manager, Advancement advancement) {
		try {
			Method[] methods = manager.getClass().getDeclaredMethods();
			for (Method method : methods) {
				if (method.getParameterCount() == 1 &&
						method.getParameterTypes()[0] == Advancement.class &&
						method.getReturnType() == AdvancementProgress.class) {

					method.setAccessible(true);
					return (AdvancementProgress) method.invoke(manager, advancement);
				}
			}

			for (Field field : manager.getClass().getDeclaredFields()) {
				if (field.getType() == Map.class) {
					field.setAccessible(true);
					@SuppressWarnings("unchecked")
					Map<Advancement, AdvancementProgress> progressMap = (Map<Advancement, AdvancementProgress>) field.get(manager);
					if (progressMap.containsKey(advancement)) {
						return progressMap.get(advancement);
					}
				}
			}
		} catch (Exception e) {
			
		}
		return null;
	}

	public static void tick() {
		tickCounter++;
		updateAchievements();
	}

	public static void showAchievement(Advancement advancement) {
		if (advancement == null) return;

		AdvancementDisplay display = advancement.getDisplay();
		if (display != null) {
			try {
				SteamAchievement steamAchievement = new SteamAchievement(
						display.getTitle(),
						display.getDescription(),
						display.getIcon(),
						tickCounter
				);
				activeAchievements.add(steamAchievement);

				playAchievementSound();
			} catch (Exception e) {
				System.err.println("[" + MOD_ID + "] Error showing achievement: " + e.getMessage());
			}
		}
	}

	public static void showCustomAchievement(Text title, Text description, ItemStack icon) {
		try {
			if (title == null) title = Text.literal("Achievement");
			if (description == null) description = Text.literal("");
			if (icon == null) icon = new ItemStack(Items.EXPERIENCE_BOTTLE);

			SteamAchievement steamAchievement = new SteamAchievement(
					title,
					description,
					icon,
					tickCounter
			);
			activeAchievements.add(steamAchievement);

			playAchievementSound();
		} catch (Exception e) {
			System.err.println("[" + MOD_ID + "] Error showing custom achievement: " + e.getMessage());
		}
	}

	private static void playAchievementSound() {
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player != null && client.world != null) {
				AchievementConfig config = AchievementConfig.get();
				if (config.soundEnabled) {
					client.getSoundManager().play(PositionedSoundInstance.master(
							ACHIEVEMENT_SOUND_EVENT, 1.0f, config.soundVolume));
				}
			}
		} catch (Exception e) {
			System.err.println("[" + MOD_ID + "] Error playing sound: " + e.getMessage());
		}
	}

	private static void updateAchievements() {
		try {
			activeAchievements.removeIf(achievement -> achievement.shouldRemove(tickCounter));
		} catch (Exception e) {
			System.err.println("[" + MOD_ID + "] Error updating achievements: " + e.getMessage());
		}
	}

	private static void renderAchievements(DrawContext context, float tickDelta) {
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.options.hudHidden || activeAchievements.isEmpty()) return;
			if (client.player == null || client.world == null) return;

			AchievementConfig config = AchievementConfig.get();
			int screenWidth = client.getWindow().getScaledWidth();
			int screenHeight = client.getWindow().getScaledHeight();

			for (int i = 0; i < activeAchievements.size(); i++) {
				SteamAchievement achievement = activeAchievements.get(i);
				int yOffset = i * (config.achievementHeight + config.achievementSpacing);
				renderSteamAchievement(context, achievement, screenWidth, screenHeight, yOffset, tickCounter, tickDelta);
			}
		} catch (Exception e) {
			System.err.println("[" + MOD_ID + "] Error rendering: " + e.getMessage());
		}
	}

	private static void renderSteamAchievement(DrawContext context, SteamAchievement achievement,
											   int screenWidth, int screenHeight, int yOffset, int currentTick, float tickDelta) {
		try {
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

			
			context.getMatrices().push();
			context.getMatrices().translate(x + 6, y + 6, 0);
			float iconScale = Math.min(1.5f, (config.achievementHeight - 12) / 16.0f);
			context.getMatrices().scale(iconScale, iconScale, 1.0f);
			context.drawItem(achievement.icon, 0, 0);
			context.getMatrices().pop();

			MinecraftClient client = MinecraftClient.getInstance();

			
			int titleY = y + Math.max(6, (config.achievementHeight - 24) / 3);
			context.drawTextWithShadow(client.textRenderer, achievement.title,
					x + 32, titleY, 0xFFFFFF);

			
			Text description = achievement.description;
			if (description != null && config.achievementHeight > 30) {
				String descText = description.getString();
				if (client.textRenderer.getWidth(descText) > config.achievementWidth - 40) {
					descText = client.textRenderer.trimToWidth(descText, config.achievementWidth - 45) + "...";
				}
				int descY = titleY + 12;
				if (descY + 9 <= y + config.achievementHeight - 3) {
					context.drawTextWithShadow(client.textRenderer, Text.literal(descText),
							x + 32, descY, 0xCCCCCC);
				}
			}

			
			if (config.enableShineEffect) {
				long time = System.currentTimeMillis();
				double shine = Math.sin(time * 0.01) * 0.3 + 0.7;
				int shineAlpha = (int) (100 * shine);
				context.fill(x + config.achievementWidth - 15, y + 3, x + config.achievementWidth - 3, y + 5,
						(shineAlpha << 24) | 0xFFFFFF);
			}
		} catch (Exception e) {
			System.err.println("[" + MOD_ID + "] Error rendering achievement: " + e.getMessage());
		}
	}

	@Environment(EnvType.CLIENT)
	public static class SteamAchievement {
		public final Text title;
		public final Text description;
		public final ItemStack icon;
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