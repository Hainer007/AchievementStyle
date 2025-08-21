package hainer.mod.achievementstyle.mixin;

import hainer.mod.achievementstyle.AchievementStyle;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public class ToastManagerMixin {

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void interceptAdvancementToast(Toast toast, CallbackInfo ci) {
        if (toast instanceof AdvancementToast) {
            try {
                AdvancementEntry advancementEntry = ((IAdvancementToast)toast).advancement();

                if (advancementEntry != null && advancementEntry.value().display().isPresent()) {
                    System.out.println("[AchievementStyle] Intercepted advancement: " +
                            advancementEntry.value().display().get().getTitle().getString());

                    AchievementStyle.showAchievement(advancementEntry.value());

                    ci.cancel();                 }
            } catch (Exception e) {
                System.out.println("[AchievementStyle] Error intercepting advancement: " + e.getMessage());
                e.printStackTrace();

                                AchievementStyle.showCustomAchievement(
                        Text.literal("Achievement Unlocked!"),
                        Text.literal("You've made progress!"),
                        new net.minecraft.item.ItemStack(net.minecraft.item.Items.EXPERIENCE_BOTTLE),
                        false
                );
                ci.cancel();
            }
        }
    }
}