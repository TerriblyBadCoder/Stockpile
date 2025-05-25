package net.atired.stockpile.mixin;

import net.atired.stockpile.Stockpile;
import net.atired.stockpile.accessor.DebtLivingEntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class DebtInGameHudMixin {
    private static final Identifier HEALTH = Stockpile.id("textures/gui/sprites/misc/heart.png");
    private static final Identifier HEALTHL = Stockpile.id("textures/gui/sprites/misc/heartleft.png");
    private static final Identifier HEALTHR = Stockpile.id("textures/gui/sprites/misc/heartright.png");

    @Shadow private int scaledHeight;

    @Shadow private int scaledWidth;

    @Inject(method = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusBars(Lnet/minecraft/client/gui/DrawContext;)V",at=@At("HEAD"))
    private void renderHealthDebt(DrawContext context, CallbackInfo ci){
        int m = this.scaledWidth / 2 - 91;

        if(MinecraftClient.getInstance().player instanceof DebtLivingEntityAccessor accessor && accessor.stockpile$getDebt()>0){
            int debtFloor = (int) Math.floor(accessor.stockpile$getDebt()/2);
            int offset = 0;
            float debtLeft = MathHelper.fractionalPart(accessor.stockpile$getDebt()/2);
            for(int i = 0; i < debtFloor; i+=1){
                offset =(int)(Math.sin(i*1.1f+MinecraftClient.getInstance().world.getTime())*3f);
                int o = m + i*8;
                Identifier hp = HEALTH;
                if(i == 0){
                    hp = HEALTHL;
                }
                context.drawTexture(hp,o,this.scaledHeight-41+offset,0,0f,0f,9,13,9,13);
            }
            offset =(int)(Math.sin(debtFloor*1.1f+MinecraftClient.getInstance().world.getTime())*3f);
            context.drawTexture(HEALTH,m+(int)((debtFloor)*8f),this.scaledHeight-41+offset,0,0f,0f, (int) (debtLeft*9f),13,9,13);
            context.drawTexture(HEALTHR,m+(int)((debtFloor)*8f)+(int) (debtLeft*9f),this.scaledHeight-41+offset,0, (debtLeft),0f, 1,13,1,13);

        }


    }
}
