package net.atired.stockpile.mixin;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//



import net.atired.stockpile.init.StockpileItemInit;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ZombieEntity.class})
public abstract class ZombieEntityMixin extends MobEntity{


    protected ZombieEntityMixin(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = {"initEquipment"},
            at = {@At("TAIL")}
    )
    protected void giveEmAxes(Random random, LocalDifficulty localDifficulty, CallbackInfo ci) {
        if ((double)random.nextFloat() > 0.93 && ((MobEntity)this) instanceof HuskEntity huskEntity) {
            this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(StockpileItemInit.THROWING_AXE));
            this.updateDropChances(EquipmentSlot.MAINHAND);

        }

    }
}
