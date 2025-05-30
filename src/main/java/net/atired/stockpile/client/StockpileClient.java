package net.atired.stockpile.client;

import net.atired.stockpile.Stockpile;
import net.atired.stockpile.accessor.ClutchPlayerAccessor;
import net.atired.stockpile.client.events.ClutchGUIRenderEvent;
import net.atired.stockpile.client.renderer.ThrownAxeRenderer;
import net.atired.stockpile.entities.ThrownAxeEntity;
import net.atired.stockpile.init.StockpileEntityTypeInit;
import net.atired.stockpile.init.StockpileItemInit;
import net.atired.stockpile.init.StockpileParticleInit;
import net.atired.stockpile.init.StockpileStatusEffectInit;
import net.atired.stockpile.mixin.ClutchPlayerMixin;
import net.atired.stockpile.networking.StockpileNetworkingConstants;
import net.atired.stockpile.particles.AxeHitParticle;
import net.atired.stockpile.particles.BlastedParticle;
import net.atired.stockpile.particles.BlightedParticle;
import net.atired.stockpile.particles.ReturnedParticle;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class StockpileClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(StockpileParticleInit.BLIGHT_PARTICLE, BlightedParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(StockpileParticleInit.BLASTED_PARTICLE, BlastedParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(StockpileParticleInit.RETURNED_PARTICLE, ReturnedParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(StockpileParticleInit.AXEHIT_PARTICLE, AxeHitParticle.Factory::new);
        EntityRendererRegistry.register(StockpileEntityTypeInit.THROWN_AXE, context->{
            return new ThrownAxeRenderer(context,0,false);
        });
        ModelPredicateProviderRegistry.register(StockpileItemInit.THROWING_AXE, new Identifier("skin"),(stack, clientWorld, living, seed)-> {
            return stack.getOrCreateNbt().getInt("stockpile_skin")/5.0f;
        });
        registerMessages();
        HudRenderCallback.EVENT.register(new ClutchGUIRenderEvent());
    }
    public void registerMessages(){
        ClientPlayNetworking.registerGlobalReceiver(StockpileNetworkingConstants.SPEEN_PACKET_ID, (client, handler, buf, responseSender) -> {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            int id = buf.readInt();
            Vec3d dir = new Vec3d(x,y,z);
            Entity entity = client.player.getWorld().getEntityById(id);
            if(entity instanceof LivingEntity living){
                entity.addVelocity(dir);
            }

                });
        ClientPlayNetworking.registerGlobalReceiver(StockpileNetworkingConstants.CLUTCH_PACKET_ID, (client, handler, buf, responseSender) -> {
            int val = buf.readInt();
            int id = buf.readInt();

            Entity entity = client.player.getWorld().getEntityById(id);
            if(entity instanceof LivingEntity living && living instanceof ClutchPlayerAccessor accessor){
                accessor.stockpile$setClutchTicks(val);
            }

        });
        ClientPlayNetworking.registerGlobalReceiver(StockpileNetworkingConstants.BLIGHTED_PACKET_ID, (client, handler, buf, responseSender) -> {
            if(client.player!=null)
            {
                int id = buf.readInt();
                int amp = buf.readInt();
                int dur = buf.readInt();
                boolean bool = buf.readBoolean();
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
