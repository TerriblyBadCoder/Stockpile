package net.atired.stockpile.client;

import net.atired.stockpile.client.events.ClutchGUIRenderEvent;
import net.atired.stockpile.init.StockpileParticleInit;
import net.atired.stockpile.init.StockpileStatusEffectInit;
import net.atired.stockpile.networking.StockpileNetworkingConstants;
import net.atired.stockpile.particles.BlastedParticle;
import net.atired.stockpile.particles.BlightedParticle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

public class StockpileClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(StockpileParticleInit.BLIGHT_PARTICLE, BlightedParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(StockpileParticleInit.BLASTED_PARTICLE, BlastedParticle.Factory::new);
        registerMessages();
        HudRenderCallback.EVENT.register(new ClutchGUIRenderEvent());
    }
    public void registerMessages(){
        ClientPlayNetworking.registerGlobalReceiver(StockpileNetworkingConstants.BLIGHTED_PACKET_ID, (client, handler, buf, responseSender) -> {
            if(client.player!=null)
            {
                int id = buf.readInt();
                int amp = buf.readInt();
                int dur = buf.readInt();
                boolean bool = buf.readBoolean();
                System.out.println("WOOOOO");
                Entity entity = client.player.getWorld().getEntityById(id);
                if(entity instanceof LivingEntity living){

                    if(bool)
                        living.addStatusEffect(new StatusEffectInstance(StockpileStatusEffectInit.BLIGHTED_EFFECT,dur,amp));
                    else
                        living.removeStatusEffect(StockpileStatusEffectInit.BLIGHTED_EFFECT);

                }
                buf.clear();
            }

        });
    }
}
