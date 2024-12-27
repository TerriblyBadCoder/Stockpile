package net.atired.stockpile.accessor;

import net.minecraft.util.math.Vec3d;

public interface ClutchPlayerAccessor {
    int stockpile$getClutchTicks();
    void stockpile$setClutchTicks(int time);
    void stockpile$addClutchTicks(int time);
    boolean stockpile$hasTrail();
    Vec3d stockpile$getTrailPosition(int pointer, float partialTick);
}
