package net.atired.stockpile.mixin;

import dev.doctor4t.arsenal.index.ArsenalDamageTypes;
import dev.doctor4t.arsenal.item.ScytheItem;
import eu.midnightdust.lib.config.MidnightConfig;
import net.atired.stockpile.StockpileConfig;
import net.atired.stockpile.accessor.DebtLivingEntityAccessor;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ScytheItem.class)
public class ScytheItemMixin {
    @Redirect(method = "use",at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    private boolean redirHurttoDebt(PlayerEntity instance, DamageSource source, float amount){
        if(instance instanceof DebtLivingEntityAccessor accessor && !instance.getWorld().isClient() && !instance.isCreative()&&StockpileConfig.showInfo){
            accessor.stockpile$setDebt(accessor.stockpile$getDebt()+5);
            instance.damage(source,0.2f);
            return false;
        }
        else{
            instance.damage(source, 3.0F);
            return true;
        }

    }
}
