package net.atired.stockpile.init;

import net.atired.stockpile.Stockpile;
import net.atired.stockpile.items.ThrowingAxeItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BlockTags;

public class StockpileItemInit {
    public static final Item THROWING_AXE = new ThrowingAxeItem(3,-2.8f, ToolMaterials.IRON, BlockTags.AXE_MINEABLE,new FabricItemSettings().maxCount(1).maxDamage(256));
    public static void init() {
        Registry.register(Registries.ITEM, Stockpile.id("throwing_axe"), THROWING_AXE);
    }
}
