package net.atired.stockpile;

import net.atired.stockpile.enchantments.WhirlingEnchantment;
import net.atired.stockpile.init.StockpileEnchantmentInit;
import net.atired.stockpile.init.StockpileParticleInit;
import net.atired.stockpile.init.StockpileStatusEffectInit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Stockpile implements ModInitializer {

    private static String MODID = "stockpile";
    public static Identifier id(String name){
        return Identifier.of(MODID,name);
    }
    @Override
    public void onInitialize() {
        StockpileEnchantmentInit.init();
        StockpileStatusEffectInit.init();
        StockpileParticleInit.registerParticles();
    }
}
