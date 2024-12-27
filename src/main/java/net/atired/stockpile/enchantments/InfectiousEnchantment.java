package net.atired.stockpile.enchantments;

import dev.doctor4t.arsenal.index.ArsenalEnchantments;
import dev.doctor4t.arsenal.index.ArsenalItems;
import net.atired.stockpile.init.StockpileEnchantmentInit;
import net.atired.stockpile.init.StockpileStatusEffectInit;
import net.atired.stockpile.networking.StockpileNetworkingConstants;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class InfectiousEnchantment extends Enchantment {
    public InfectiousEnchantment(EquipmentSlot[] slotTypes) {
        super(Rarity.UNCOMMON, EnchantmentTarget.WEAPON, slotTypes);
    }
    public int getMinPower(int level) {
        return 15;
    }

    public int getMaxPower(int level) {
        return 40;
    }

    @Override
    public void onTargetDamaged(LivingEntity user, Entity target, int level) {
        target.setVelocity(target.getVelocity().multiply(0.7));
        if(target instanceof LivingEntity livingTarget){
            livingTarget.addStatusEffect(new StatusEffectInstance(StockpileStatusEffectInit.BLIGHTED_EFFECT,120,0));
            user.addStatusEffect(new StatusEffectInstance(StockpileStatusEffectInit.BLIGHTED_EFFECT,60,0));
        }
        super.onTargetDamaged(user, target, level);
    }

    @Override
    public float getAttackDamage(int level, EntityGroup group) {
        return super.getAttackDamage(level, group)-level-1;
    }

    public int getMaxLevel() {
        return 1;
    }
    public boolean canAccept(Enchantment other) {
        return super.canAccept(other) && other != ArsenalEnchantments.SPEWING&& other != StockpileEnchantmentInit.CLUTCH;
    }
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ArsenalItems.SCYTHE) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }
}
