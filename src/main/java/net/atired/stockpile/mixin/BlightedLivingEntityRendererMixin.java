package net.atired.stockpile.mixin;

import net.atired.stockpile.Stockpile;
import net.atired.stockpile.init.StockpileStatusEffectInit;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class BlightedLivingEntityRendererMixin {
    private static final Identifier BLIGHT_TEXTURE = Stockpile.id("textures/mob_effect/blighted_render.png");

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    private void renderFadingMixin(LivingEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci){
        if(livingEntity.hasStatusEffect(StockpileStatusEffectInit.BLIGHTED_EFFECT) && livingEntity.isAlive())
        {

            Matrix4f pose = matrixStack.peek().getPositionMatrix();
            Matrix3f normal = matrixStack.peek().getNormalMatrix();
            Quaternionf quaternionf = new Quaternionf().rotateYXZ(-livingEntity.getHeadYaw()/180*3.14f,livingEntity.getPitch()/180*3.14f,0);
            VertexConsumer consumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(BLIGHT_TEXTURE));
            Vector3f[] $$9 = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
            matrixStack.push();
            for(int $$11 = 0; $$11 < 4; ++$$11) {
                Vector3f $$12 = $$9[$$11];
                $$12.add((float) (Math.sin(livingEntity.age/2f+$$11*4)/4f),0,1.4f);
                $$12.rotate(quaternionf);
                $$12.mul((float) (0.3f*livingEntity.getBoundingBox().getAverageSideLength()));
                $$12.add(livingEntity.getEyePos().toVector3f().add(0,0.05f,0)).sub(livingEntity.getPos().toVector3f());
            }
            this.vertex(pose, normal, consumer, $$9[0].x(), $$9[0].y(), $$9[0].z(), 0.0F, 0, -1, 0, 0, 16,livingEntity.age+2,190);
            this.vertex(pose, normal, consumer,  $$9[1].x(), $$9[1].y(), $$9[1].z(), 1, 0F, -1, 0, 0, 16,livingEntity.age+4,190);
            this.vertex(pose, normal, consumer,  $$9[2].x(), $$9[2].y(), $$9[2].z(), 1, 1F, -1, 0, 0, 16,livingEntity.age,190);
            this.vertex(pose, normal, consumer, $$9[3].x(), $$9[3].y(), $$9[3].z(), 0.0F, 1, -1, 0, 0, 16, livingEntity.age+6,190);

            matrixStack.pop();
        }

    }
    @Unique
    public void vertex(Matrix4f p_254392_, Matrix3f p_254011_, VertexConsumer p_253902_, float p_254058_, float p_254338_, float p_254196_, float p_254003_, float p_254165_, int p_253982_, int p_254037_, int p_254038_, int p_254271_, int ticks, int a) {
        p_253902_.vertex(p_254392_, (float)p_254058_, (float)p_254338_, (float)p_254196_).color(255, (int) (230-(Math.cos(ticks)/2*16)), (int) (230-(Math.cos(ticks)/2*16)), a).texture(p_254003_, p_254165_).overlay(OverlayTexture.DEFAULT_UV).light(0xF00000).normal(p_254011_, (float)p_253982_, (float)p_254038_, (float)p_254037_).next();
    }
}
