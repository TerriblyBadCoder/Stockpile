package net.atired.stockpile.init;

import net.atired.stockpile.Stockpile;
import net.atired.stockpile.entities.ThrownAxeEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class StockpileEntityTypeInit {
    public static final EntityType<ThrownAxeEntity> THROWN_AXE = Registry.register(
            Registries.ENTITY_TYPE,
            Stockpile.id("thrown_axe"),
            EntityType.Builder.<ThrownAxeEntity>create(ThrownAxeEntity::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f).build("thrown_axe"));
    public static void init(){

    }
}
