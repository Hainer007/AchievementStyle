package hainer.mod.achievementstyle.mixin;

import hainer.mod.achievementstyle.AchievementStyle;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public class ToastManagerMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void interceptAdvancementToast(Toast toast, CallbackInfo ci) {
        if (toast instanceof AdvancementToast) {
            try {
                AdvancementEntry advancementEntry =((IAdvancementToast)toast).advancement();

                if (advancementEntry != null && advancementEntry.value().display().isPresent()) {
                    // Show our custom Steam-style achievement
                    AchievementStyle.showAchievement(advancementEntry.value());
                    // Cancel the vanilla toast
                    ci.cancel();
                }
            } catch (Exception e) {
                // If reflection fails, fall back to a generic achievement
                System.out.println("Reflection failed, showing generic achievement: " + e.getMessage());
                AchievementStyle.showCustomAchievement(
                        net.minecraft.text.Text.literal("Achievement Unlocked!"),
                        net.minecraft.text.Text.literal("You've made progress!"),
                        new net.minecraft.item.ItemStack(net.minecraft.item.Items.EXPERIENCE_BOTTLE)
                );
                ci.cancel();
            }
        }
    }
}