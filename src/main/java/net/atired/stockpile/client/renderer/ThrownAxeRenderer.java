package net.atired.stockpile.client.renderer;

import net.atired.stockpile.Stockpile;
import net.atired.stockpile.accessor.ClutchPlayerAccessor;
import net.atired.stockpile.entities.ThrownAxeEntity;
import net.atired.stockpile.init.StockpileEnchantmentInit;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Consumer;

public class ThrownAxeRenderer extends FlyingItemEntityRenderer<ThrownAxeEntity> {
    private static final Vector3f ROTATION_VECTOR = (new Vector3f(0.5F, 0.5F, 0.5F)).normalize();
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
    private static final Identifier TEXTURE = Stockpile.id("textures/entity/trailaxe.png");
    private static final Identifier TEXTURE_SPIN = Stockpile.id("textures/entity/anchorspeen_side.png");
    private final ItemRenderer itemRenderer;
    public ThrownAxeRenderer(EntityRendererFactory.Context ctx, float scale, boolean lit) {
        super(ctx, scale, lit);
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(ThrownAxeEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (entity.hasTrail()) {

            double x = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
            double y = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
            double z = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());
            matrices.push();
            matrices.translate(-x, -y, -z);

            renderTrail(entity,0,matrices,vertexConsumers,1f,1,1,1f,light,0,0.6f);
            matrices.pop();
        }
        if (entity.age >= 2 || !(this.dispatcher.camera.getFocusedEntity().squaredDistanceTo(entity) < 64)) {
            matrices.push();
            matrices.scale(1.5f, 1.5f, 1.5f);
            Vec3d dir = new Vec3d(entity.getDir());

            float rot = (entity.age+tickDelta);
            if(entity.getEmbedded()){
                rot = MathHelper.clamp((entity.getId()%20)*(float)Math.PI/40,0,(float)Math.PI/2f)+(float)Math.PI/2;
            }
            matrices.translate(0,0.2f,0);
            matrices.multiply(new Quaternionf().rotationXYZ(0,(float)Math.PI/2+(float)(Math.atan2(dir.getX(), dir.getZ())),rot));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));

            float yRot = (float) Math.atan2(dir.x, dir.z)/3.14f*180f;
            float yawAngle = yRot-(float) (Math.cos((entity.age+tickDelta)*3+0.5f)*5f);
            float pitchAngle = (entity.age+tickDelta)*120;
            this.itemRenderer
                    .renderItem(
                            entity.getStack(), ModelTransformationMode.GROUND, light, EnchantmentHelper.getLevel(StockpileEnchantmentInit.SANGUINE,entity.getStack())!=0?OverlayTexture.getUv(0,true):OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), entity.getId()
                    );
            matrices.pop();
            if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.SANGUINE,entity.getStack())!=0){
                matrices.push();
                matrices.translate(0.0, 0.3, 0.0);
                MatrixStack.Entry matrixEntry = matrices.peek();
                Matrix4f modelMatrix = matrixEntry.getPositionMatrix();
                Matrix3f normal = matrixEntry.getNormalMatrix();
                matrices.multiply(new Quaternionf().rotationXYZ(0,(float)Math.PI/2+(float)(Math.atan2(dir.getX(), dir.getZ())),rot));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
                VertexConsumer vertexconsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE_SPIN));
                Vec3d[] vec3ds = new Vec3d[]{new Vec3d(-1,-1,0),new Vec3d(-1,1,0),new Vec3d(1,1,0),new Vec3d(1,-1,0)};
                float velscale = (float) MathHelper.clamp(entity.getVelocity().length(),0f,2f);
                for(int i=0; i<vec3ds.length;i++){
                    vec3ds[i] = vec3ds[i].multiply(0.9f);
                }
                this.vertexMine(vec3ds[0], vertexconsumer, 0, 0, modelMatrix, normal, 0xF00000,0.8f);
                this.vertexMine(vec3ds[1], vertexconsumer, 0, 1, modelMatrix, normal, 0xF00000,0.8f);
                this.vertexMine(vec3ds[2], vertexconsumer, 1, 1, modelMatrix, normal, 0xF00000,0.8f);
                this.vertexMine(vec3ds[3], vertexconsumer, 1, 0, modelMatrix, normal, 0xF00000,0.8f);

                matrices.pop();
            }

            super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        }
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
    private Quaternionf rot(float yRot, float xRot, float zRot)
    {
        return new Quaternionf().rotationXYZ(xRot,yRot,zRot);
    }
    private void vertexMine(Vec3d vec, VertexConsumer vertexConsumer, float u, float v, Matrix4f modelMatrix, Matrix3f normal, int light, float alpha) {
        vertexConsumer.vertex(modelMatrix, (float)vec.x, (float)vec.y, (float)vec.z).color(1f, 0.2f, 0.2f, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normal, 0.0F, 1.0F, 0.0F).next();
    }
    private void renderTrail(ThrownAxeEntity entityIn, float partialTicks, MatrixStack poseStack, VertexConsumerProvider bufferIn, float trailR, float trailG, float trailB, float trailA, int packedLightIn, float addedrot, float alphamult) {
        boolean sanguine = EnchantmentHelper.getLevel(StockpileEnchantmentInit.SANGUINE,entityIn.getStack())!=0;
        boolean recall = EnchantmentHelper.getLevel(StockpileEnchantmentInit.RECALL,entityIn.getStack())!=0;
        int sampleSize = 8;
        float redminus = 0.f;
        float trailHeight = 0.2F;
        float removealpha = 0.125f*alphamult;
        if(sanguine){

            sampleSize = 12;
            removealpha/=1.5f;
            redminus = 0.04f;
            trailB-=0.7f;
            trailG-=0.7f;
        }


        trailA*=alphamult;

        Vec3d drawFrom = entityIn.getTrailPosition(0, partialTicks);
        VertexConsumer vertexconsumer = bufferIn.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
        Vec3d dir =entityIn.getVelocity().normalize();
        float yRot = (float) Math.atan2(dir.x, dir.z);
        float xRot = (float) Math.asin(-dir.y);
        Quaternionf OLDquaternionfConsumer = rot(yRot,0,0);
        for(int samples = 0; samples < sampleSize; samples++) {
            Vec3d sample = entityIn.getTrailPosition(samples + 2, partialTicks);
            dir = drawFrom.subtract(sample);
            xRot = (float) Math.asin(-dir.y);
            yRot = (float) Math.atan2(dir.x, dir.z);
            Quaternionf quaternionfConsumer = rot(yRot,0,0);

            Vec3d topAngleVec = new Vec3d(new Vector3f(trailHeight,0 , 0).rotate(quaternionfConsumer));
            Vec3d bottomAngleVec = new Vec3d(new Vector3f(-trailHeight, 0, 0).rotate(quaternionfConsumer));
            Vec3d OLDtopAngleVec = new Vec3d(new Vector3f(trailHeight, 0, 0).rotate(OLDquaternionfConsumer));
            Vec3d OLDbottomAngleVec = new Vec3d(new Vector3f(-trailHeight, 0, 0).rotate(OLDquaternionfConsumer));

            float u1 = samples / (float) sampleSize;
            float u2 = u1 + 1 / (float) sampleSize;
            Vec3d draw1 = drawFrom;
            Vec3d draw2 = sample;

            MatrixStack.Entry posestack$pose = poseStack.peek();
            Matrix4f matrix4f = posestack$pose.getPositionMatrix();
            Matrix3f matrix3f = posestack$pose.getNormalMatrix();
            vertexconsumer.vertex(matrix4f, (float) draw1.x + (float) OLDbottomAngleVec.x, (float) draw1.y + (float) OLDbottomAngleVec.y, (float) draw1.z + (float) OLDbottomAngleVec.z).color(trailR, trailG, trailB, trailA).texture(u1, 1F).overlay(OverlayTexture.DEFAULT_UV).light(packedLightIn).normal(matrix3f, 0F, 1.0f, 0.0F).next();
            vertexconsumer.vertex(matrix4f, (float) draw2.x + (float) bottomAngleVec.x, (float) draw2.y + (float) bottomAngleVec.y, (float) draw2.z + (float) bottomAngleVec.z).color(trailR-0.08f, trailG-redminus, trailB-redminus,  trailA-removealpha).texture(u2, 1F).overlay(OverlayTexture.DEFAULT_UV).light(packedLightIn).normal(matrix3f, .0F, 1.0f, 0.0F).next();
            vertexconsumer.vertex(matrix4f, (float) draw2.x + (float) topAngleVec.x, (float) draw2.y + (float) topAngleVec.y, (float) draw2.z + (float) topAngleVec.z).color(trailR-0.08f, trailG-redminus, trailB-redminus,  trailA-removealpha).texture(u2, 0).overlay(OverlayTexture.DEFAULT_UV).light(packedLightIn).normal(matrix3f, .0F, 1.0f, 0.0F).next();
            vertexconsumer.vertex(matrix4f, (float) draw1.x + (float) OLDtopAngleVec.x, (float) draw1.y + (float) OLDtopAngleVec.y, (float) draw1.z + (float) OLDtopAngleVec.z).color(trailR, trailG, trailB,  trailA).texture(u1, 0).overlay(OverlayTexture.DEFAULT_UV).light(packedLightIn).normal(matrix3f, 0F, 1.0f, 0.0F).next();
            drawFrom = sample;
            trailA-=removealpha;
            trailB-=redminus;
            trailG-=redminus;
            if(recall){
                trailR-=0.08f;
            }
            OLDquaternionfConsumer = quaternionfConsumer;
        }
    }
}
