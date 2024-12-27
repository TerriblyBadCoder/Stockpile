package net.atired.stockpile.mixin;

import dev.doctor4t.arsenal.index.ArsenalDamageTypes;
import dev.doctor4t.arsenal.item.ScytheItem;
import net.atired.stockpile.init.StockpileParticleInit;
import net.atired.stockpile.init.StockpileStatusEffectInit;
import net.atired.stockpile.networking.StockpileNetworkingConstants;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class BlightedLivingEntityMixin extends Entity {


    public BlightedLivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow public abstract boolean hasStatusEffect(StatusEffect effect);

    @Shadow public abstract boolean removeStatusEffect(StatusEffect type);

    @ModifyVariable(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",at = @At("HEAD"),ordinal=0)
    private float blightedDamage(float amount,DamageSource source){
        if(this.hasStatusEffect(StockpileStatusEffectInit.BLIGHTED_EFFECT)&&!source.isOf(ArsenalDamageTypes.SPEWING))
        {
            if(getWorld() instanceof ServerWorld world){
                world.spawnParticles(StockpileParticleInit.BLIGHT_PARTICLE,getX(),getEyeY(),getZ(),1,0,0,0,0);
                this.removeStatusEffect(StockpileStatusEffectInit.BLIGHTED_EFFECT);
                if(!this.getWorld().isClient() && this.getWorld() instanceof ServerWorld serverWorld)
                {

                    for(ServerPlayerEntity a : PlayerLookup.tracking(this)){

                        PacketByteBuf byteBufs = PacketByteBufs.create();
                        byteBufs.writeInt(this.getId());
                        byteBufs.writeInt(-5);
                        byteBufs.writeInt(-5);
                        byteBufs.writeBoolean(false);
                        ServerPlayNetworking.send(a, StockpileNetworkingConstants.BLIGHTED_PACKET_ID, byteBufs);
                    }
                }
            }
            return amount*1.5f;
        }
        return amount;
    }
}
