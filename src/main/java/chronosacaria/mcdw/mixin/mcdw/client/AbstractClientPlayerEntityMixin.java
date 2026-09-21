/*
Timefall Development License 1.2
Copyright (c) 2020-2024. Chronosacaria, Kluzzio, Timefall Development. All Rights Reserved.

This software's content is licensed under the Timefall Development License 1.2. You can find this license information here: https://github.com/Timefall-Development/Timefall-Development-Licence/blob/main/TimefallDevelopmentLicense1.2.txt
*/
package chronosacaria.mcdw.mixin.mcdw.client;

import chronosacaria.mcdw.api.util.RangedAttackHelper;
import chronosacaria.mcdw.bases.McdwBow;
import chronosacaria.mcdw.bases.McdwLongbow;
import chronosacaria.mcdw.bases.McdwShortbow;
import chronosacaria.mcdw.enums.EnchantmentsID;
import chronosacaria.mcdw.registries.EnchantsRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {

    @Inject(method = "getFovMultiplier", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    public void mcdw$customBowsZoom(CallbackInfoReturnable<Float> cir, float f) {

        AbstractClientPlayerEntity abPlayer = MinecraftClient.getInstance().player;

        if (abPlayer == null)
            return;
        if (abPlayer.getActiveItem() == null)
            return;
        ItemStack itemStack = abPlayer.getActiveItem();
        if (abPlayer.isUsingItem()) {
            if (itemStack.getItem() instanceof McdwBow ||
                    itemStack.getItem() instanceof McdwShortbow ||
                    itemStack.getItem() instanceof McdwLongbow) {
                int i = abPlayer.getItemUseTime();
                float pullTime = RangedAttackHelper.getBowPullTime(itemStack);
                int overchargeLevel = EnchantmentHelper.getLevel(EnchantsRegistry.enchantments.get(EnchantmentsID.OVERCHARGE), itemStack);
                if (overchargeLevel > 0) {
                    int overcharge = (int) Math.min((i / pullTime) - 1, overchargeLevel);
                    i = overcharge == overchargeLevel ? i : (int) (i % pullTime);
                }
                float g = (float)i / pullTime;
                if (g > 1.0F) {
                    g = 1.0F;
                } else {
                    g *= g;
                }

                f *= 1.0F - g * 0.15F;

                cir.setReturnValue(MathHelper.lerp(MinecraftClient.getInstance().options.getFovEffectScale().getValue().floatValue(), 1.0F, f));
            }
        }
    }
}
