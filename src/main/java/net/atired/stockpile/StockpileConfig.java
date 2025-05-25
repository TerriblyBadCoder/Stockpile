package net.atired.stockpile;

import com.google.common.collect.Lists;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.util.Identifier;

import java.util.List;

public class StockpileConfig extends MidnightConfig {
    public static final String TEXT = "main";


    @Comment(category = TEXT, centered = true) public static Comment text2;      // Centered comments are the same as normal ones - just centered!
    @Comment(category = TEXT) public static Comment spacer1;                     // Comments containing the word "spacer" will just appear as a blank line
    @Entry(category = TEXT) public static boolean showInfo = true;

}
