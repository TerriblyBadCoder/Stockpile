package net.atired.stockpile.enchantments;

import dev.doctor4t.arsenal.index.ArsenalEnchantments;
import dev.doctor4t.arsenal.index.ArsenalItems;
import net.atired.stockpile.init.StockpileEnchantmentInit;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class WhirlingEnchantment extends Enchantment {
    public WhirlingEnchantment(EquipmentSlot[] slotTypes) {
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
    public boolean canAccept(Enchantment other) {
        return super.canAccept(other) && other != ArsenalEnchantments.REELING&& other != StockpileEnchantmentInit.UNCHAINED;
    }
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(ArsenalItems.ANCHORBLADE) || stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }
}
