package net.atired.stockpile.client.events;

import com.mojang.blaze3d.systems.RenderSystem;

import net.atired.stockpile.Stockpile;
import net.atired.stockpile.accessor.ClutchPlayerAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ClutchGUIRenderEvent implements HudRenderCallback {
    private static final Identifier TEXTURE = Stockpile.id("textures/gui/sprites/misc/warn_blast.png");


    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        if(MinecraftClient.getInstance().player instanceof ClutchPlayerAccessor accessor && accessor.stockpile$getClutchTicks()>0 && accessor.stockpile$getClutchTicks()<14){
            int x = drawContext.getScaledWindowWidth() / 2 - 30, y = drawContext.getScaledWindowHeight() / 2-8;
            RenderSystem.enableBlend();
            drawContext.setShaderColor(1F,1,1, 1);
            drawContext.drawTexture(TEXTURE, x, y,0,0, 16, 16,16,16);
            drawContext.drawTexture(TEXTURE, x+42, y,0,0, 16, 16,16,16);
            drawContext.setShaderColor(1F,1f,1f,1f);
            RenderSystem.disableBlend();
        }

    }
}
