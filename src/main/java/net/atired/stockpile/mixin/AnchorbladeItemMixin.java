package net.atired.stockpile.mixin;

import dev.doctor4t.arsenal.entity.AnchorbladeEntity;
import dev.doctor4t.arsenal.index.ArsenalItems;
import dev.doctor4t.arsenal.index.ArsenalSounds;
import dev.doctor4t.arsenal.item.AnchorbladeItem;
import dev.doctor4t.arsenal.util.AnchorOwner;
import net.atired.stockpile.accessor.WhirlingEntityAccessor;
import net.atired.stockpile.init.StockpileEnchantmentInit;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnchorbladeItem.class)
public class AnchorbladeItemMixin {
    @Inject(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",at = @At("HEAD"),cancellable = true)
    private void whirlingUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);
        if (user instanceof AnchorOwner owner) {
            boolean reeling = EnchantmentHelper.getLevel(StockpileEnchantmentInit.WHIRLING, stack) > 0;
            boolean unchained = EnchantmentHelper.getLevel(StockpileEnchantmentInit.UNCHAINED, stack) > 0;
            if (owner.arsenal$isAnchorActive(hand,false) && unchained){
                if(!owner.arsenal$getAnchor(hand,false).isRecalled())
                {
                    user.getItemCooldownManager().set(ArsenalItems.ANCHORBLADE,50);
                    if(world instanceof ServerWorld world1){
                        AnchorbladeEntity oldAnchor =  owner.arsenal$getAnchor(hand,false);
                        AnchorbladeEntity anchorbladeEntity = new AnchorbladeEntity(world,user,stack);
                        anchorbladeEntity.setVelocity(oldAnchor.getVelocity());
                        anchorbladeEntity.setPos(oldAnchor.getX(),oldAnchor.getY(),oldAnchor.getZ());
                        anchorbladeEntity.setRecalled(true);
                        anchorbladeEntity.setDealtDamage(false);
                        world1.spawnEntity(anchorbladeEntity);

                        if(anchorbladeEntity instanceof WhirlingEntityAccessor accessor){
                            accessor.stockpile$setUnchainedTicks(5);
                        }
                        oldAnchor.discard();
                    }


                }
                cir.cancel();
                cir.setReturnValue(TypedActionResult.fail(stack));
                return;
            }
            if (owner.arsenal$isAnchorActive(hand,false) && reeling) {
                
                if(!owner.arsenal$getAnchor(hand,false).isRecalled() && owner.arsenal$getAnchor(hand,false).getVelocity().length()>0.12f)
                    owner.arsenal$getAnchor(hand,false).addVelocity(user.getRotationVec(0).multiply(2.1));
                owner.arsenal$getAnchor(hand,false).setRecalled(true);

                if(owner.arsenal$getAnchor(hand,false) instanceof WhirlingEntityAccessor whirlingEntityAccessor){
                    whirlingEntityAccessor.stockpile$clearHitList();
                }
                cir.cancel();
                cir.setReturnValue(TypedActionResult.fail(stack));
                return;
            }
        }
    }
}
