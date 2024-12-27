package net.atired.stockpile.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.doctor4t.arsenal.client.render.entity.AnchorbladeEntityRenderer;
import dev.doctor4t.arsenal.entity.AnchorbladeEntity;
import dev.doctor4t.arsenal.index.ArsenalCosmetics;
import dev.doctor4t.arsenal.item.AnchorbladeItem;
import net.atired.stockpile.Stockpile;
import net.atired.stockpile.accessor.WhirlingEntityAccessor;
import net.atired.stockpile.init.StockpileEnchantmentInit;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.Consumer;

@Mixin(AnchorbladeEntityRenderer.class)
public abstract class AnchorbladeEntityRendererMixin extends EntityRenderer<AnchorbladeEntity> {
    @Shadow @Final private BakedModelManager bakedModelManager;

    @Shadow @Final private ItemRenderer itemRenderer;
    @Unique
    private static final Identifier TEXTURE = Stockpile.id("textures/entity/anchorspeen.png");

    protected AnchorbladeEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Shadow protected abstract void vertex(Vec3d vec, VertexConsumer vertexConsumer, float u, float v, Matrix4f modelMatrix, Matrix3f normal, int light);

    @ModifyArgs(method = "render(Ldev/doctor4t/arsenal/entity/AnchorbladeEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE",ordinal = 1, target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private void whirlingRender(Args args, AnchorbladeEntity anchorbladeEntity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light){
        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.WHIRLING, anchorbladeEntity.getStack())>0 && anchorbladeEntity instanceof WhirlingEntityAccessor accessor && !accessor.stockpile$inGround()){
            float bonusrot = (anchorbladeEntity.age+tickDelta)*35;
            float arg0 = args.get(2);
            float arg1 = args.get(1);
            args.set(2,arg0+bonusrot);
            args.set(1,arg1+bonusrot);
        }
    }
    @Unique
    private Consumer<Quaternionf> rot(float yRot, float xRot, float zRot)
    {
        return (p_253347_) -> {
            p_253347_.mul(((new Quaternionf()).rotationYXZ(yRot,zRot,xRot)));
        };
    }
    @ModifyArgs(method = "render(Ldev/doctor4t/arsenal/entity/AnchorbladeEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE",ordinal = 0, target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private void whirlingRenderYaw(Args args, AnchorbladeEntity anchorbladeEntity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light){
        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.WHIRLING, anchorbladeEntity.getStack())>0 && anchorbladeEntity instanceof WhirlingEntityAccessor accessor && !accessor.stockpile$inGround()){
            float bonusrot = (float) (Math.cos((anchorbladeEntity.age+tickDelta)*3+0.5f)*5f);
            float arg0 = args.get(2);
            float arg1 = args.get(1);
            args.set(2,arg0+bonusrot);
            args.set(1,arg1+bonusrot);
        }
    }
    @Inject(method = "render(Ldev/doctor4t/arsenal/entity/AnchorbladeEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",at=@At("HEAD"),cancellable = true)
    private void unchainedUnRender(AnchorbladeEntity anchorbladeEntity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci){
        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.WHIRLING, anchorbladeEntity.getStack())>0 && anchorbladeEntity.getVelocity().length()>0.4 && !anchorbladeEntity.hasDealtDamage()){
            float yawAngle = MathHelper.lerp(tickDelta, anchorbladeEntity.prevYaw, anchorbladeEntity.getYaw())-(float) (Math.cos((anchorbladeEntity.age+tickDelta)*3+0.5f)*5f);
            float pitchAngle = MathHelper.lerp(tickDelta, anchorbladeEntity.prevPitch, anchorbladeEntity.getPitch())+(anchorbladeEntity.age+tickDelta)*120;
            matrices.push();
            matrices.translate(0.0, 0.6, 0.0);
            MatrixStack.Entry matrixEntry = matrices.peek();
            Matrix4f modelMatrix = matrixEntry.getPositionMatrix();
            Matrix3f normal = matrixEntry.getNormalMatrix();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yawAngle + 90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-pitchAngle + 45.0F));
            VertexConsumer vertexconsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
            Vec3d[] vec3ds = new Vec3d[]{new Vec3d(-1,-1,0),new Vec3d(-1,1,0),new Vec3d(1,1,0),new Vec3d(1,-1,0)};
            float velscale = (float) MathHelper.clamp(anchorbladeEntity.getVelocity().length(),0f,4f);
            for(int i=0; i<vec3ds.length;i++){
                vec3ds[i] = vec3ds[i].multiply(velscale);
            }
            this.vertexMine(vec3ds[0], vertexconsumer, 0, 0, modelMatrix, normal, 0xF00000,velscale/4f);
            this.vertexMine(vec3ds[1], vertexconsumer, 0, 1, modelMatrix, normal, 0xF00000,velscale/4f);
            this.vertexMine(vec3ds[2], vertexconsumer, 1, 1, modelMatrix, normal, 0xF00000,velscale/4f);
            this.vertexMine(vec3ds[3], vertexconsumer, 1, 0, modelMatrix, normal, 0xF00000,velscale/4f);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(pitchAngle - 45.0F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yawAngle - 90.0F));
            matrices.translate(0.0,-0.6,0.0);
            matrices.pop();
        }
        else if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.UNCHAINED, anchorbladeEntity.getStack())>0&& anchorbladeEntity.isRecalled()){
            float yawAngle = MathHelper.lerp(tickDelta, anchorbladeEntity.prevYaw, anchorbladeEntity.getYaw());
            float pitchAngle = MathHelper.lerp(tickDelta, anchorbladeEntity.prevPitch, anchorbladeEntity.getPitch());
            matrices.push();
            matrices.translate(0.0, 0.6, 0.0);
            float scale = 1.6F;
            matrices.scale(scale, scale, scale);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(anchorbladeEntity.getYaw()+90));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(anchorbladeEntity.getPitch()+45));
            BakedModel model = this.bakedModelManager.getModel(AnchorbladeItem.Skin.DEFAULT.anchorbladeEntityModel);
            RenderLayer chainLayer = RenderLayer.getEntitySmoothCutout(AnchorbladeItem.Skin.DEFAULT.chainTexture);
            ItemStack stack = anchorbladeEntity.getStack();
            AnchorbladeItem.Skin skin = AnchorbladeItem.Skin.fromString(ArsenalCosmetics.getSkin(stack));
            if (skin != null) {
                model = this.bakedModelManager.getModel(skin.anchorbladeEntityModel);
                chainLayer = RenderLayer.getEntityTranslucent(skin.chainTexture);
            }

            this.itemRenderer.renderItem(stack, ModelTransformationMode.FIXED, false, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV, model);
            matrices.pop();
            Entity var15 = anchorbladeEntity.getOwner();
            if (var15 instanceof LivingEntity livingOwner && anchorbladeEntity instanceof WhirlingEntityAccessor accessor && accessor.stockpile$getUnchainedTicks()>0) {
                matrices.push();
                Vec3d pos = anchorbladeEntity.getLerpedPos(tickDelta);
                Vec3d ringPos = (new Vec3d((double)(skin == AnchorbladeItem.Skin.AMBESSA ? 0.0F : 1.0F), 0.0, 0.0)).rotateZ(pitchAngle * 0.017453292F).rotateY((yawAngle + 90.0F) * 0.017453292F).add(0.0, (double)(anchorbladeEntity.getHeight() / 2.0F), 0.0);
                Vec3d leashPos = livingOwner.getLeashPos(tickDelta);
                Vec3d ownerPos = leashPos.subtract(pos);
                float length = (float)ringPos.distanceTo(ownerPos);
                MatrixStack.Entry matrixEntry = matrices.peek();
                Matrix4f modelMatrix = matrixEntry.getPositionMatrix();
                Matrix3f normal = matrixEntry.getNormalMatrix();
                float minU = 0.0F;
                float maxU = 1.0F;
                float minV = 0.0F;
                float maxV = length / 8.0F;
                VertexConsumer vertexConsumer = vertexConsumers.getBuffer(chainLayer);
                Vec3d offset = ownerPos.subtract(ringPos).normalize().multiply(0.25, 0.0, 0.25).rotateY(1.5707964F);
                Vec3d offset2 = offset.multiply((5-accessor.stockpile$getUnchainedTicks()-tickDelta)/20f+1);
                offset = offset.multiply(6-accessor.stockpile$getUnchainedTicks()-tickDelta);
                Vec3d vert1 = ringPos.add(offset);
                Vec3d vert2 = ownerPos.add(offset2);
                Vec3d vert3 = ownerPos.subtract(offset2);
                Vec3d vert4 = ringPos.subtract(offset);
                float alpha = (accessor.stockpile$getUnchainedTicks()+tickDelta)/8f;

                int chainLight = LightmapTextureManager.pack(this.getBlockLight(anchorbladeEntity, livingOwner.getBlockPos()), this.getSkyLight(anchorbladeEntity, livingOwner.getBlockPos()));
                this.vertexMine(vert1, vertexConsumer, minU, minV, modelMatrix, normal, light,alpha);
                this.vertexMine(vert2, vertexConsumer, minU, maxV, modelMatrix, normal, chainLight,alpha);
                this.vertexMine(vert3, vertexConsumer, maxU, maxV, modelMatrix, normal, chainLight,alpha);
                this.vertexMine(vert4, vertexConsumer, maxU, minV, modelMatrix, normal, light,alpha);
                matrices.pop();
            }
            ci.cancel();
        }

    }
    private void vertexMine(Vec3d vec, VertexConsumer vertexConsumer, float u, float v, Matrix4f modelMatrix, Matrix3f normal, int light,float alpha) {
        vertexConsumer.vertex(modelMatrix, (float)vec.x, (float)vec.y, (float)vec.z).color(1f, 1f, 1f, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normal, 0.0F, 1.0F, 0.0F).next();
    }
}
