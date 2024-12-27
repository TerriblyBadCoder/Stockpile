package net.atired.stockpile.statuseffects;

import net.atired.stockpile.init.StockpileStatusEffectInit;
import net.atired.stockpile.networking.StockpileNetworkingConstants;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class BlightedEffect extends StatusEffect {
    public BlightedEffect() {
        super(StatusEffectCategory.HARMFUL, 0xd9386e);
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if(!entity.getWorld().isClient() && entity.getWorld() instanceof ServerWorld serverWorld)
        {
            for(ServerPlayerEntity a : serverWorld.getPlayers()){
                PacketByteBuf byteBufs = PacketByteBufs.create();
                byteBufs.writeInt(entity.getId());
                byteBufs.writeInt(amplifier);
                byteBufs.writeInt(entity.getStatusEffect(StockpileStatusEffectInit.BLIGHTED_EFFECT).getDuration());
                byteBufs.writeBoolean(true);

                ServerPlayNetworking.send(a, StockpileNetworkingConstants.BLIGHTED_PACKET_ID, byteBufs);
            }

        }
        super.onApplied(entity, attributes, amplifier);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        if(!entity.getWorld().isClient() && entity.getWorld() instanceof ServerWorld serverWorld)
        {

            for(ServerPlayerEntity a : serverWorld.getPlayers()){

                PacketByteBuf byteBufs = PacketByteBufs.create();
                byteBufs.writeInt(entity.getId());
                byteBufs.writeInt(-5);
                byteBufs.writeInt(-5);
                byteBufs.writeBoolean(false);
                ServerPlayNetworking.send(a, StockpileNetworkingConstants.BLIGHTED_PACKET_ID, byteBufs);
            }
        }
        super.onRemoved(entity, attributes, amplifier);
    }
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration%15 == 0;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        super.applyUpdateEffect(entity,amplifier);
    }
}