package net.atired.stockpile.items;

import com.ibm.icu.text.AlphabeticIndex;
import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.cca.ArsenalComponents;
import dev.doctor4t.arsenal.cca.WeaponOwnerComponent;
import dev.doctor4t.arsenal.index.ArsenalCosmetics;
import dev.doctor4t.arsenal.index.ArsenalDamageTypes;
import dev.doctor4t.arsenal.item.AnchorbladeItem;
import dev.doctor4t.arsenal.item.ArsenalWeaponItem;
import dev.doctor4t.arsenal.item.ScytheItem;
import dev.doctor4t.arsenal.util.SweepParticleUtil;
import dev.doctor4t.ratatouille.item.CustomHitParticleItem;
import dev.doctor4t.ratatouille.item.CustomHitSoundItem;
import net.atired.stockpile.accessor.DebtLivingEntityAccessor;
import net.atired.stockpile.entities.ThrownAxeEntity;
import net.atired.stockpile.init.StockpileEnchantmentInit;
import net.atired.stockpile.init.StockpileItemInit;
import net.atired.stockpile.init.StockpileParticleInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class ThrowingAxeItem extends MiningToolItem implements ArsenalWeaponItem, CustomHitParticleItem{
    public ThrowingAxeItem(float attackDamage, float attackSpeed, ToolMaterial material, TagKey<Block> effectiveBlocks, Settings settings) {
        super(attackDamage, attackSpeed, material, effectiveBlocks, settings);
    }
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        ItemStack itemStack = stack;
        Hand hand = Hand.MAIN_HAND;
        if(user.getStackInHand(Hand.MAIN_HAND)==itemStack){
            user.swingHand(Hand.MAIN_HAND);
        }
        else{
            user.swingHand(Hand.OFF_HAND);
            hand = Hand.OFF_HAND;
        }
        float i = this.getMaxUseTime(stack) - remainingUseTicks;
        i = Math.min(i,20);
        i = 0.33f+i/30f;
        world.playSound(
                null,
                user.getX(),
                user.getY(),
                user.getZ(),
                SoundEvents.ENTITY_FISHING_BOBBER_THROW,
                SoundCategory.NEUTRAL,
                0.5F,
                0.3F / (world.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        if(user instanceof PlayerEntity player) {
            if (!player.getAbilities().creativeMode) {
                itemStack.damage(1, player, player1 -> {
                });
            }
        }
        if (!world.isClient) {
            ThrownAxeEntity axeEntity = new ThrownAxeEntity(world, user);
            Vec3d dir = new Vec3d(-1,0,0).rotateY(-user.getYaw()/180*3.14f).multiply(0.2);
            if(hand==Hand.OFF_HAND){
                dir = dir.multiply(-1);
            }


            axeEntity.setItem(itemStack);
            axeEntity.setPosition(axeEntity.getPos().add(dir));
            if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.RECALL,itemStack)==0)
                axeEntity.setVelocity(user, user.getPitch()-30, user.getYaw(), 0.0F, i, 1.0F);
            else
                axeEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, i*1.5f, 1.0F);
            world.spawnEntity(axeEntity);
        }
        if(user instanceof PlayerEntity player){
            player.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!player.getAbilities().creativeMode&&EnchantmentHelper.getLevel(StockpileEnchantmentInit.SANGUINE,itemStack)==0) {
                itemStack.decrement(1);
            }
            else if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.SANGUINE,itemStack)!=0){
                if(player instanceof DebtLivingEntityAccessor accessor){
                    player.getItemCooldownManager().set(StockpileItemInit.THROWING_AXE,15);
                    if(!player.getAbilities().creativeMode){
                        player.damage(player.getDamageSources().create(ArsenalDamageTypes.SPEWING),0.2f);
                        accessor.stockpile$setDebt(accessor.stockpile$getDebt()+3);
                    }

                }
            }
        }

    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult blockHitResult = raycast(
                world, user,RaycastContext.FluidHandling.NONE
        );
        BlockState blockStateClicked = world.getBlockState(blockHitResult.getBlockPos());
        if (itemStack.getDamage() >= itemStack.getMaxDamage() - 1 || (blockStateClicked.isIn(BlockTags.ANVIL) || blockStateClicked.isOf(Blocks.SMITHING_TABLE))) {
            return TypedActionResult.fail(itemStack);
        }else {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(itemStack);
        }
    }

    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockState blockStateClicked = context.getWorld().getBlockState(context.getBlockPos());
        PlayerEntity user = context.getPlayer();
        if (user != null && user.isSneaking() && (blockStateClicked.isIn(BlockTags.ANVIL) || blockStateClicked.isOf(Blocks.SMITHING_TABLE)) && !context.getWorld().isClient) {
                user.stopUsingItem();
                int skinNum = context.getStack().getOrCreateNbt().getInt("stockpile_skin");
                System.out.println(skinNum);
                skinNum=(skinNum+1)%3;
                context.getStack().getOrCreateNbt().putInt("stockpile_skin",skinNum);
                context.getPlayer().playSound(SoundEvents.BLOCK_SMITHING_TABLE_USE, 0.5F, 1.0F);
                return ActionResult.SUCCESS;

        } else {
            return super.useOnBlock(context);
        }
    }
    @Override
    public void spawnHitParticles(PlayerEntity playerEntity) {
        World var3 = playerEntity.getWorld();
        if (var3 instanceof ServerWorld serverWorld) {
            Vec3d vec3d = new Vec3d(playerEntity.getX() + (double)(-MathHelper.sin((float)((double)playerEntity.getYaw() * 0.017453292519943295))), playerEntity.getBodyY(0.5), playerEntity.getZ() + (double)MathHelper.cos((float)((double)playerEntity.getYaw() * 0.017453292519943295)));
            serverWorld.spawnParticles(StockpileParticleInit.AXEHIT_PARTICLE,vec3d.x,vec3d.y,vec3d.z,1,0,0.1,0,0);
        }
    }
}
