package net.atired.stockpile.init;

import net.atired.stockpile.Stockpile;
import net.atired.stockpile.enchantments.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class StockpileEnchantmentInit {
    public static Enchantment WHIRLING = new WhirlingEnchantment(new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    public static Enchantment INFECTIOUS = new InfectiousEnchantment(new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    public static Enchantment UNCHAINED = new UnchainedEnchantment(new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    public static Enchantment CLUTCH = new ClutchEnchantment(new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    public static Enchantment SPINTOWIN = new SpinToWinEnchantment(new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    public static Enchantment SANGUINE = new SanguineEnchantment(new EquipmentSlot[]{EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND});
    public static Enchantment RECALL = new RecallEnchantment(new EquipmentSlot[]{EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND});

    public static void init(){
        Registry.register(Registries.ENCHANTMENT, Stockpile.id("whirling"), WHIRLING);
        Registry.register(Registries.ENCHANTMENT, Stockpile.id("spin_to_win"), SPINTOWIN);
        Registry.register(Registries.ENCHANTMENT, Stockpile.id("infectious"), INFECTIOUS);
        Registry.register(Registries.ENCHANTMENT, Stockpile.id("unchained"), UNCHAINED);
        Registry.register(Registries.ENCHANTMENT, Stockpile.id("clutch"), CLUTCH);
        Registry.register(Registries.ENCHANTMENT, Stockpile.id("sanguine"), SANGUINE);
        Registry.register(Registries.ENCHANTMENT, Stockpile.id("recall"), RECALL);
    }
}
