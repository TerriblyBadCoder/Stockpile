package net.atired.stockpile.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import eu.midnightdust.lib.config.MidnightConfig;
import net.atired.stockpile.accessor.ClutchPlayerAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class ClutchPlayerMixin extends LivingEntity implements ClutchPlayerAccessor {

    private Vec3d[] trailPositions = new Vec3d[64];
    private int trailPointer = -1;

    @Unique
    private static TrackedData<Integer> TRAILTICKS;

    protected ClutchPlayerMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void takeKnockback(double strength, double x, double z) {
        if(stockpile$getClutchTicks()==0)
            super.takeKnockback(strength, x, z);
    }

    @Override
    public Vec3d stockpile$getTrailPosition(int pointer, float partialTick) {

        int i = this.trailPointer - pointer & 63;
        int j = this.trailPointer - pointer - 1 & 63;
        Vec3d d0 = this.trailPositions[j];
        Vec3d d1 = this.trailPositions[i].subtract(d0);
        return d0.add(d1.multiply(partialTick));
    }

    @ModifyReturnValue(method = "getMovementSpeed",at=@At("RETURN"))
    public float getMovementSpeed(float original) {
        if(stockpile$getClutchTicks()>0) {
            return original * (1f + stockpile$getClutchTicks() / 15f);
        }
        else
        {
            return  original;
        }
    }

    @Inject(method = "tick",at=@At("TAIL"))
    private void clutchTick(CallbackInfo ci){
       if(stockpile$getClutchTicks()>0){
           if(!getWorld().isClient())
            stockpile$setClutchTicks(stockpile$getClutchTicks()-1);
           Vec3d trailAt = this.getPos().add(0, 0.2, 0);
           if (trailPointer == -1) {
               Vec3d backAt = trailAt;
               for (int i = 0; i < trailPositions.length; i++) {
                   trailPositions[i] = backAt;
               }
           }
           if (++this.trailPointer == this.trailPositions.length) {
               this.trailPointer = 0;
           }
           this.trailPositions[this.trailPointer] = trailAt;
       } else if (this.trailPositions[0]!=null) {
           trailPointer=-1;
           this.trailPositions= new Vec3d[64];
       }

    }
    @Override
    public boolean stockpile$hasTrail() {
        return trailPointer != -1;
    }

    @Override
    public int stockpile$getClutchTicks() {
        return this.dataTracker.get(TRAILTICKS);
    }

    @Override
    public void stockpile$setClutchTicks(int time) {
        this.dataTracker.set(TRAILTICKS,time);
    }
    static {
        TRAILTICKS = DataTracker.registerData(ClutchPlayerMixin.class, TrackedDataHandlerRegistry.INTEGER);
    }
    @Inject(method = "initDataTracker",at=@At("TAIL"))
    protected void initDataTracker(CallbackInfo ci) {
        this.getDataTracker().startTracking(TRAILTICKS, 0);
    }
}
