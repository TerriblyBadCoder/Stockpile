package net.atired.stockpile.mixin;

import dev.doctor4t.arsenal.index.ArsenalSounds;
import dev.doctor4t.arsenal.item.ScytheItem;
import net.atired.stockpile.accessor.ClutchPlayerAccessor;
import net.atired.stockpile.init.StockpileEnchantmentInit;
import net.atired.stockpile.init.StockpileParticleInit;
import net.atired.stockpile.networking.StockpileNetworkingConstants;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScytheItem.class)
public abstract class ClutchScytheMixin extends ToolItem {
    public ClutchScytheMixin(ToolMaterial material, Item.Settings settings) {
        super(material,settings);
    }

    @Inject(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",at=@At("HEAD"),cancellable = true)
    private void clutchUse(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir){
        if (EnchantmentHelper.getEquipmentLevel(StockpileEnchantmentInit.CLUTCH, player) > 0) {

            if(player instanceof ClutchPlayerAccessor accessor){
                if(accessor.stockpile$getClutchTicks()<14){

                    if(accessor.stockpile$getClutchTicks()>0){
                        player.getItemCooldownManager().set(this, 10);
                        player.addVelocity(player.getRotationVec(0).multiply(1,0,1).normalize().multiply(1.5));
                        accessor.stockpile$setClutchTicks(31);

                    }
                    else{
                        player.getItemCooldownManager().set(this, 10);
                        player.addVelocity(player.getRotationVec(0).multiply(1,0,1).normalize().multiply(0.5));
                        accessor.stockpile$setClutchTicks(28);

                    }

                    Vec3d lookdir = player.getRotationVec(0).multiply(1,0,1).normalize().multiply(0.2);
                    player.swingHand(hand);
                    player.getWorld().addParticle(StockpileParticleInit.BLASTED_PARTICLE,player.getX(),player.getBodyY(0.5),player.getZ(),lookdir.x,0,lookdir.z);
                    cir.cancel();
                    cir.setReturnValue(TypedActionResult.success(player.getStackInHand(hand)));

                    world.playSound((PlayerEntity)null, player.getX(), player.getY(), player.getZ(), ArsenalSounds.ITEM_SCYTHE_SPEWING, SoundCategory.PLAYERS, 0.9F, 2.0F);
                }
                else{
                    player.getItemCooldownManager().set(this, 30);
                    accessor.stockpile$setClutchTicks(0);
                }

            }

        }
    }


}
