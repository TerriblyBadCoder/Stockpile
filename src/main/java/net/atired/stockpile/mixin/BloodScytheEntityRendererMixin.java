package net.atired.stockpile.mixin;

import dev.doctor4t.arsenal.client.render.entity.AnchorbladeEntityRenderer;
import dev.doctor4t.arsenal.client.render.entity.BloodScytheEntityRenderer;
import dev.doctor4t.arsenal.entity.BloodScytheEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(BloodScytheEntityRenderer.class)
public class BloodScytheEntityRendererMixin {
    private static final Identifier TEXTURE = new Identifier("stockpile", "textures/entity/trail.png");

    @Inject(method = "render(Ldev/doctor4t/arsenal/entity/BloodScytheEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",at = @At("TAIL"))
    private void fancyRender(BloodScytheEntity bloodScythe, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci){
        matrixStack.push();
        Vec3d delta = bloodScythe.getVelocity().normalize().multiply(0.6);
        matrixStack.translate(delta.x, delta.y, delta.z);
        renderTrail(bloodScythe, g,matrixStack,vertexConsumerProvider,1f,0.1f,0.1f,1f*(bloodScythe.ticksUntilRemove)/5f,0xF00000,0.4f-(5-bloodScythe.ticksUntilRemove)/5f);
        renderTrail(bloodScythe, g,matrixStack,vertexConsumerProvider,1f,0.1f,0.1f,1f*(bloodScythe.ticksUntilRemove)/5f,0xF00000,-0.4f+(5-bloodScythe.ticksUntilRemove)/5f);
        matrixStack.pop();
    }
    @Unique
    private void renderTrail(BloodScytheEntity entityIn, float partialTicks, MatrixStack poseStack, VertexConsumerProvider bufferIn, float trailR, float trailG, float trailB, float trailA, int packedLightIn, float addedrot) {

        int sampleSize = 8;
        float trailHeight =8F;
        Vec3d drawFrom = new Vec3d(0,0,0);
        VertexConsumer vertexconsumer = bufferIn.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
        Vec3d dir =entityIn.getVelocity().normalize();
        float yRot = (float) Math.atan2(dir.x,dir.z);
        float pitch = (float)Math.asin(-dir.y);
        Consumer<Quaternionf> OLDquaternionfConsumer = rot(yRot,addedrot,pitch);
        for(int samples = 0; samples < sampleSize; samples++) {
            Consumer<Quaternionf> quaternionfConsumer = rot(yRot, samples*0.03f+(5-entityIn.ticksUntilRemove)/7f*addedrot*samples+addedrot,pitch);
            Quaternionf $$8 = (new Quaternionf()).setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());
            quaternionfConsumer.accept($$8);
            $$8.transform(TRANSFORM_VECTOR);
            Quaternionf $$9 = (new Quaternionf()).setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());
            OLDquaternionfConsumer.accept($$9);
            $$9.transform(TRANSFORM_VECTOR);
            float cosx=(float)Math.cos(samples*4+entityIn.age)/1.5f;
            float cosx2=(float)Math.cos((samples-1)*4+entityIn.age)/1.5f;
            Vec3d topAngleVec = new Vec3d(new Vector3f(trailHeight+cosx, 0, 0).rotate($$8));
            Vec3d bottomAngleVec = new Vec3d(new Vector3f(-trailHeight+cosx, 0, 0).rotate($$8));
            Vec3d OLDtopAngleVec = new Vec3d(new Vector3f(trailHeight+cosx2, 0, 0).rotate($$9));
            Vec3d OLDbottomAngleVec = new Vec3d(new Vector3f(-trailHeight+cosx2, 0, 0).rotate($$9));
            Vec3d sample = dir.multiply(-samples*1.2f).add(0,Math.sin(samples*4+entityIn.age/12f)/1.5f,0);
            float u1 = samples / (float) sampleSize;
            float u2 = u1 + 1 / (float) sampleSize;
            Vec3d draw1 = drawFrom;
            Vec3d draw2 = sample;
            float trailAc= MathHelper.clamp(trailA-.12f,0,1f);
            MatrixStack.Entry posestack$pose = poseStack.peek();
            Matrix4f matrix4f = posestack$pose.getPositionMatrix();
            Matrix3f matrix3f = posestack$pose.getNormalMatrix();
            vertexconsumer.vertex(matrix4f, (float) draw1.x + (float) OLDbottomAngleVec.x, (float) draw1.y + (float) OLDbottomAngleVec.y, (float) draw1.z + (float) OLDbottomAngleVec.z).color(trailR, trailG, trailB, trailA).texture(u1, 1F).overlay(OverlayTexture.DEFAULT_UV).light(packedLightIn).normal(matrix3f, 0F, 1.0f, 0.0F).next();
            vertexconsumer.vertex(matrix4f, (float) draw2.x + (float) bottomAngleVec.x, (float) draw2.y + (float) bottomAngleVec.y, (float) draw2.z + (float) bottomAngleVec.z).color(trailR, trailG+=0.1f, trailB+=0.1f,  trailAc).texture(u2, 1F).overlay(OverlayTexture.DEFAULT_UV).light(packedLightIn).normal(matrix3f, .0F, 1.0f, 0.0F).next();
            vertexconsumer.vertex(matrix4f, (float) draw2.x + (float) topAngleVec.x, (float) draw2.y + (float) topAngleVec.y, (float) draw2.z + (float) topAngleVec.z).color(trailR, trailG+=0.1f, trailB+=0.1f,  trailAc).texture(u2, 0).overlay(OverlayTexture.DEFAULT_UV).light(packedLightIn).normal(matrix3f, .0F, 1.0f, 0.0F).next();
            vertexconsumer.vertex(matrix4f, (float) draw1.x + (float) OLDtopAngleVec.x, (float) draw1.y + (float) OLDtopAngleVec.y, (float) draw1.z + (float) OLDtopAngleVec.z).color(trailR, trailG, trailB,  trailA).texture(u1, 0).overlay(OverlayTexture.DEFAULT_UV).light(packedLightIn).normal(matrix3f, 0F, 1.0f, 0.0F).next();
            drawFrom = sample;
            trailA= MathHelper.clamp(trailA-.12f,0,1f);
            trailB+=0.1f;
            trailG+=0.1f;
            OLDquaternionfConsumer = quaternionfConsumer;
        }
    }
    private static final Vector3f ROTATION_VECTOR = (new Vector3f(0.5F, 0.5F, 0.5F)).normalize();
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
    private Consumer<Quaternionf> rot(float yRot, float xRot,float zRot)
    {
        return (p_253347_) -> {
            p_253347_.mul(((new Quaternionf()).rotationYXZ(yRot,zRot,xRot)));
        };
    }
}
