package net.atired.stockpile.enchantments;

import dev.doctor4t.arsenal.index.ArsenalEnchantments;
import dev.doctor4t.arsenal.index.ArsenalItems;
import net.atired.stockpile.init.StockpileEnchantmentInit;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class SpinToWinEnchantment extends Enchantment {
    public SpinToWinEnchantment(EquipmentSlot[] slotTypes) {
        super(Rarity.RARE, EnchantmentTarget.WEAPON, slotTypes);
    }
    public int getMinPower(int level) {
        return 20;
    }

    public int getMaxPower(int level) {
        return 50;
    }

    public int getMaxLevel() {
        return 1;
    }

    @Override
    public float getAttackDamage(int level, EntityGroup group) {
        return super.getAttackDamage(level, group);
    }
    public boolean canAccept(Enchantment other) {
        return super.canAccept(other) && other != ArsenalEnchantments.REELING&& other != StockpileEnchantmentInit.UNCHAINED&& other != StockpileEnchantmentInit.WHIRLING;
    }
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ArsenalItems.ANCHORBLADE) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }
}
