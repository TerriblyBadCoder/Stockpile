package net.atired.stockpile.init;

import net.atired.stockpile.Stockpile;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class StockpileParticleInit {
    public static final DefaultParticleType BLIGHT_PARTICLE = FabricParticleTypes.simple();
    public static final DefaultParticleType BLASTED_PARTICLE = FabricParticleTypes.simple();
    public static void  registerParticles() {
        Registry.register(Registries.PARTICLE_TYPE, Stockpile.id("blight_particle"), BLIGHT_PARTICLE);
        Registry.register(Registries.PARTICLE_TYPE, Stockpile.id("blasted_particle"), BLASTED_PARTICLE);
    }
}
