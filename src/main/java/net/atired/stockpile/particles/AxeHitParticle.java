package net.atired.stockpile.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class AxeHitParticle extends SpriteBillboardParticle {
    private SpriteProvider spriteProvider;
    protected AxeHitParticle(ClientWorld clientWorld, double d, double e, double f, SpriteProvider spriteProvider, double xd, double yd, double zd) {
        super(clientWorld, d, e, f,xd,yd,zd);
        this.spriteProvider = spriteProvider;
        this.x = d;
        this.y = e;
        this.z = f;
        this.velocityX = xd;
        this.velocityY = yd;
        this.velocityZ = zd;
        this.scale = 0.7f;
        this.maxAge = 6;
        this.red = 1;
        this.green = 1f;
        this.blue = 1f;
        this.angle = (float) ((Math.random()-0.5f)*3.14f/4);
        this.prevAngle = this.angle;

    }

    @Override
    public void tick() {
        super.tick();
        this.prevAngle = this.angle;


        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }
    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider sprites;

        public Factory(SpriteProvider spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(DefaultParticleType particleType, ClientWorld level, double x, double y, double z,
                                       double dx, double dy, double dz) {
            AxeHitParticle blightedParticle = new AxeHitParticle(level, x, y, z, this.sprites, dx, dy, dz);
            blightedParticle.setSprite(this.sprites);
            return blightedParticle;
        }
    }
}
