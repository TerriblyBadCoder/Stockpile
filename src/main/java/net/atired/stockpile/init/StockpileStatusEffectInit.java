package net.atired.stockpile.init;

import net.atired.stockpile.Stockpile;
import net.atired.stockpile.statuseffects.BlightedEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class StockpileStatusEffectInit {
    public static final StatusEffect BLIGHTED_EFFECT;

    static {
        BLIGHTED_EFFECT = Registry.register(Registries.STATUS_EFFECT, Stockpile.id("blighted"), new BlightedEffect());
    }
    public static void init(){

    }
}
