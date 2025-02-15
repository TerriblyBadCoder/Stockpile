package net.atired.stockpile.mixin;

import dev.doctor4t.arsenal.index.ArsenalDamageTypes;
import net.atired.stockpile.Stockpile;
import net.atired.stockpile.accessor.ClutchPlayerAccessor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(PlayerEntityRenderer.class)
public class ClutchPlayerRendererMixin {
    private static final Vector3f ROTATION_VECTOR = (new Vector3f(0.5F, 0.5F, 0.5F)).normalize();
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
    private static final Identifier TEXTURE = Stockpile.id("textures/entity/trail_clutch.png");
    @Inject(method = "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",at=@At("HEAD"))
    private void clutchRender(AbstractClientPlayerEntity abstractClientPlayerEntity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci){
        if(( abstractClientPlayerEntity.getVelocity().length()>0.12 || abstractClientPlayerEntity.isOnGround() )&& abstractClientPlayerEntity instanceof ClutchPlayerAccessor accessor && accessor.stockpile$getClutchTicks()>0){
            if (accessor.stockpile$hasTrail()) {
                float amult = abstractClientPlayerEntity.isOnGround() ? 1.0f: (float) MathHelper.clamp(abstractClientPlayerEntity.getVelocity().length()*2,0,1);
                double x = MathHelper.lerp(tickDelta, abstractClientPlayerEntity.lastRenderX, abstractClientPlayerEntity.getX());
                double y = MathHelper.lerp(tickDelta, abstractClientPlayerEntity.lastRenderY, abstractClientPlayerEntity.getY());
                double z = MathHelper.lerp(tickDelta, abstractClientPlayerEntity.lastRenderZ, abstractClientPlayerEntity.getZ());
                matrices.push();
                matrices.translate(-x, -y, -z);
                Vec3d delta = abstractClientPlayerEntity.getVelocity().normalize().multiply(0.6);
                matrices.translate(delta.x, delta.y, delta.z);
                renderTrail(accessor,abstractClientPlayerEntity,tickDelta,matrices,vertexConsumers,1f,1,1,1f,light,0,amult);
                matrices.pop();
            }
        }
    }
    private Consumer<Quaternionf> rot(float yRot, float xRot,float zRot)
    {
        return (p_253347_) -> {
            p_253347_.mul(((new Quaternionf()).rotationYXZ(yRot,zRot,xRot)));
        };
    }
    private void renderTrail(ClutchPlayerAccessor entityIn,AbstractClientPlayerEntity abstractClientPlayerEntity, float partialTicks, MatrixStack poseStack, VertexConsumerProvider bufferIn, float trailR, float trailG, float trailB, float trailA, int packedLightIn, float addedrot,float alphamult) {

        int sampleSize = 6;
        float trailHeight = 1.3F;
        trailA*=alphamult;
        float removealpha = 0.16f*alphamult;
        Vec3d drawFrom = entityIn.stockpile$getTrailPosition(0, partialTicks);
        VertexConsumer vertexconsumer = bufferIn.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
        Vec3d dir =abstractClientPlayerEntity.getVelocity().normalize();
        float yRot = (float) Math.atan2(dir.x, dir.z);
        Consumer<Quaternionf> OLDquaternionfConsumer = rot(yRot,0,0);
        for(int samples = 0; samples < sampleSize; samples++) {
            Vec3d sample = entityIn.stockpile$getTrailPosition(samples + 2, partialTicks);
            dir = drawFrom.subtract(sample);
            yRot = (float) Math.atan2(dir.x, dir.z);
            Consumer<Quaternionf> quaternionfConsumer = rot(yRot,0,0);
            Quaternionf $$8 = (new Quaternionf()).setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());
            quaternionfConsumer.accept($$8);
            $$8.transform(TRANSFORM_VECTOR);
            Quaternionf $$9 = (new Quaternionf()).setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());
            OLDquaternionfConsumer.accept($$9);
            $$9.transform(TRANSFORM_VECTOR);
            Vec3d topAngleVec = new Vec3d(new Vector3f(trailHeight, 0, 0).rotate($$8));
            Vec3d bottomAngleVec = new Vec3d(new Vector3f(-trailHeight, 0, 0).rotate($$8));
            Vec3d OLDtopAngleVec = new Vec3d(new Vector3f(trailHeight, 0, 0).rotate($$9));
            Vec3d OLDbottomAngleVec = new Vec3d(new Vector3f(-trailHeight, 0, 0).rotate($$9));

            float u1 = samples / (float) sampleSize;
            float u2 = u1 + 1 / (float) sampleSize;
            Vec3d draw1 = drawFrom;
            Vec3d draw2 = sample;

            MatrixStack.Entry posestack$pose = poseStack.peek();
            Matrix4f matrix4f = posestack$pose.getPositionMatrix();
            Matrix3f matrix3f = posestack$pose.getNormalMatrix();
            vertexconsumer.vertex(matrix4f, (float) draw1.x + (float) OLDbottomAngleVec.x, (float) draw1.y + (float) OLDbottomAngleVec.y, (float) draw1.z + (float) OLDbottomAngleVec.z).color(trailR, trailG, trailB, trailA).texture(u1, 1F).overlay(OverlayTexture.DEFAULT_UV).light(packedLightIn).normal(matrix3f, 0F, 1.0f, 0.0F).next();
            vertexconsumer.vertex(matrix4f, (float) draw2.x + (float) bottomAngleVec.x, (float) draw2.y + (float) bottomAngleVec.y, (float) draw2.z + (float) bottomAngleVec.z).color(trailR-0.1f, trailG, trailB,  trailA-removealpha).texture(u2, 1F).overlay(OverlayTexture.DEFAULT_UV).light(packedLightIn).normal(matrix3f, .0F, 1.0f, 0.0F).next();
            vertexconsumer.vertex(matrix4f, (float) draw2.x + (float) topAngleVec.x, (float) draw2.y + (float) topAngleVec.y, (float) draw2.z + (float) topAngleVec.z).color(trailR-0.1f, trailG, trailB,  trailA-removealpha).texture(u2, 0).overlay(OverlayTexture.DEFAULT_UV).light(packedLightIn).normal(matrix3f, .0F, 1.0f, 0.0F).next();
            vertexconsumer.vertex(matrix4f, (float) draw1.x + (float) OLDtopAngleVec.x, (float) draw1.y + (float) OLDtopAngleVec.y, (float) draw1.z + (float) OLDtopAngleVec.z).color(trailR, trailG, trailB,  trailA).texture(u1, 0).overlay(OverlayTexture.DEFAULT_UV).light(packedLightIn).normal(matrix3f, 0F, 1.0f, 0.0F).next();
            drawFrom = sample;
            trailA-=removealpha;
            trailR-=0.1f;
            OLDquaternionfConsumer = quaternionfConsumer;
        }
    }
}
