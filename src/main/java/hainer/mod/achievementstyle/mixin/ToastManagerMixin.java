package hainer.mod.achievementstyle.mixin;

import hainer.mod.achievementstyle.AchievementStyle;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public class ToastManagerMixin {

    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    private void interceptAdvancementToast(Toast toast, CallbackInfo ci) {
        if (toast instanceof AdvancementToast) {
            try {
                AdvancementHolder advancementEntry = ((IAdvancementToast)toast).advancement();

                if (advancementEntry != null && advancementEntry.value().display().isPresent()) {
                    System.out.println("[AchievementStyle] Intercepted advancement: " +
                            advancementEntry.value().display().get().getTitle().getString());

                    AchievementStyle.showAchievement(advancementEntry.value());

                    ci.cancel();                 }
            } catch (Exception e) {
                System.out.println("[AchievementStyle] Error intercepting advancement: " + e.getMessage());
                e.printStackTrace();

                                AchievementStyle.showCustomAchievement(
                        Component.literal("Achievement Unlocked!"),
                        Component.literal("You've made progress!"),
                        new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EXPERIENCE_BOTTLE),
                        false
                );
                ci.cancel();
            }
        }
    }
}