package net.atired.stockpile.mixin;

import dev.doctor4t.arsenal.entity.AnchorbladeEntity;
import dev.doctor4t.arsenal.index.ArsenalDamageTypes;
import dev.doctor4t.arsenal.index.ArsenalItems;
import dev.doctor4t.arsenal.index.ArsenalParticles;
import dev.doctor4t.arsenal.index.ArsenalSounds;
import dev.doctor4t.arsenal.util.AnchorOwner;
import net.atired.stockpile.accessor.WhirlingEntityAccessor;
import net.atired.stockpile.entities.ThrownAxeEntity;
import net.atired.stockpile.init.StockpileEnchantmentInit;
import net.atired.stockpile.init.StockpileParticleInit;
import net.atired.stockpile.init.StockpileStatusEffectInit;
import net.atired.stockpile.networking.StockpileNetworkingConstants;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(AnchorbladeEntity.class)
public abstract class AnchorbladeEntityMixin extends PersistentProjectileEntity implements WhirlingEntityAccessor {
    private List<Integer> hitIds = new ArrayList<>();
    @Unique
    private static TrackedData<Integer> SPINAGE;
    @Unique
    private static TrackedData<Integer> OWNERID;
    @Unique
    private static TrackedData<Integer> UNCHAINEDTICKS;
    static {
        UNCHAINEDTICKS = DataTracker.registerData(AnchorbladeEntityMixin.class, TrackedDataHandlerRegistry.INTEGER);
        SPINAGE = DataTracker.registerData(AnchorbladeEntityMixin.class, TrackedDataHandlerRegistry.INTEGER);
        OWNERID = DataTracker.registerData(AnchorbladeEntityMixin.class, TrackedDataHandlerRegistry.INTEGER);
    }
    public AnchorbladeEntityMixin(EntityType<?> type, World world) {
        super((EntityType<? extends PersistentProjectileEntity>) type, world);
    }

    @Inject(method = "initDataTracker",at=@At("TAIL"))
    protected void initDataTracker(CallbackInfo ci) {
        this.getDataTracker().startTracking(SPINAGE, 0);
        this.getDataTracker().startTracking(UNCHAINEDTICKS, 0);
        this.getDataTracker().startTracking(OWNERID, 0);
    }



    @Override
    public void stockpile$addToEntityHit(int id) {
        hitIds.add(id);
    }

    @Override
    public List<Integer> stockpile$getEntityHitList() {
        return hitIds;
    }



    @Override
    public void stockpile$clearHitList() {
        hitIds.clear();
    }

    @Nullable
    @Override
    protected EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition) {
        boolean wasnoclip = this.isNoClip();
        this.setNoClip(false);
        EntityHitResult hitResult =super.getEntityCollision(currentPosition, nextPosition.add(getVelocity()));
        if(hasDealtDamage()){
            wasnoclip = true;
        }
        this.setNoClip(wasnoclip);
        return hitResult;
    }

    @Override
    public boolean stockpile$inGround() {
        return inGround;
    }

    @Inject(method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",at =@At("HEAD"),cancellable = true)
    private void onWhirlingEntityHit(EntityHitResult entityHitResult, CallbackInfo ci){
        if(entityHitResult.getEntity()!=null&&entityHitResult.getEntity() instanceof ThrownAxeEntity entity&&EnchantmentHelper.getLevel(StockpileEnchantmentInit.SANGUINE,entity.getStack())==0){
                if(entity.getOwner()==getOwner()){
                    if(getWorld() instanceof ServerWorld serverWorld){
                        serverWorld.spawnParticles(StockpileParticleInit.RETURNED_PARTICLE,entity.getX(),entity.getY(),entity.getZ(),1,0,0,0,0);
                    }

                    if(getOwner() instanceof PlayerEntity player && !player.getAbilities().creativeMode){

                        if(!player.giveItemStack(entity.getStack())){
                            player.dropItem(entity.getStack(),true);
                        }
                    }
                    entity.discard();
                }
            ci.cancel();
            return;
        }
        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.SPINTOWIN,getStack())>0 && stockpile$getSpinAge()<120){
            Entity hitEntity = entityHitResult.getEntity();
            if(hitEntity == getOwner()){
                ci.cancel();
                return;
            }
            if(!hasDealtDamage())
            {
                ci.cancel();
                float damage = 8.0F;

                if (hitEntity instanceof LivingEntity livingEntity) {
                    damage += EnchantmentHelper.getAttackDamage(getTrackedItem(), livingEntity.getGroup());
                }

                Entity owner = getOwner();
                SoundEvent soundEvent = this.getHitSound();
                hitEntity.timeUntilRegen = 0;
                if (hitEntity.damage(this.getWorld().getDamageSources().create(ArsenalDamageTypes.ANCHOR, this, getOwner()), damage)) {
                    if (hitEntity.getType() == EntityType.ENDERMAN) {
                        return;
                    }
                    if (hitEntity instanceof LivingEntity) {
                        LivingEntity hitLivingEntity = (LivingEntity)hitEntity;
                        if (owner instanceof LivingEntity) {

                            EnchantmentHelper.onUserDamaged(hitLivingEntity, owner);
                            EnchantmentHelper.onTargetDamaged((LivingEntity)owner, hitLivingEntity);
                        }
                        Entity entity = this.getWorld().getEntityById(this.dataTracker.get(OWNERID));
                        if(entity!=null){
                            Vec3d vel = entity.getPos().subtract(hitLivingEntity.getPos()).add(0,1.5,0).normalize().multiply(0.4);

                            if(getWorld() instanceof ServerWorld serverWorld){
                                for(ServerPlayerEntity a : PlayerLookup.tracking(this)) {
                                    PacketByteBuf byteBufs = PacketByteBufs.create();
                                    byteBufs.writeDouble(vel.x);
                                    byteBufs.writeDouble(vel.y);
                                    byteBufs.writeDouble(vel.z);
                                    byteBufs.writeInt(this.dataTracker.get(OWNERID));
                                    ServerPlayNetworking.send(a, StockpileNetworkingConstants.SPEEN_PACKET_ID, byteBufs);
                                }
                            }
                            entity.addVelocity(vel);
                        }

                        owner.setVelocityClient(owner.getVelocity().x,owner.getVelocity().y,owner.getVelocity().z);
                        hitLivingEntity.addVelocity(0,0.2,0);
                        this.onHit(hitLivingEntity);
                    }
                }
                this.playSound(soundEvent, 0.4F, 1.0F);
            }
        }
        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.WHIRLING, getStack())>0 || (EnchantmentHelper.getLevel(StockpileEnchantmentInit.UNCHAINED, getStack())>0)){
            Entity hitEntity = entityHitResult.getEntity();
            if(hitEntity == getOwner()){
                ci.cancel();
                return;
            }
            if(!hasDealtDamage())
            {
                if(getOwner() instanceof PlayerEntity user) {
                    user.getItemCooldownManager().set(ArsenalItems.ANCHORBLADE, 80);
                }
                for (int id : stockpile$getEntityHitList()){
                    if(hitEntity == getWorld().getEntityById(id)){
                        ci.cancel();
                        return;
                    }
                }
                stockpile$addToEntityHit(hitEntity.getId());
            }

            float damage = 12.0F;
            if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.WHIRLING, getStack())>0){
                damage = 6f;
            }
            if (hitEntity instanceof LivingEntity livingEntity) {
                damage += EnchantmentHelper.getAttackDamage(getTrackedItem(), livingEntity.getGroup());
            }

            Entity owner = getOwner();
            SoundEvent soundEvent = this.getHitSound();
            hitEntity.timeUntilRegen = 0;
            if (hitEntity.damage(this.getWorld().getDamageSources().create(ArsenalDamageTypes.ANCHOR, this, getOwner()), damage)) {
                if (hitEntity.getType() == EntityType.ENDERMAN) {
                    return;
                }
                if (hitEntity instanceof LivingEntity) {
                    LivingEntity hitLivingEntity = (LivingEntity)hitEntity;
                    if (owner instanceof LivingEntity) {
                        EnchantmentHelper.onUserDamaged(hitLivingEntity, owner);
                        EnchantmentHelper.onTargetDamaged((LivingEntity)owner, hitLivingEntity);
                    }

                    this.onHit(hitLivingEntity);
                }
            }
            this.playSound(soundEvent, 0.4F, 1.0F);
            ci.cancel();
        }
    }

    @Override
    public int stockpile$getUnchainedTicks() {
        return this.getDataTracker().get(UNCHAINEDTICKS);
    }

    @Override
    public void stockpile$setUnchainedTicks(int chained) {
        this.getDataTracker().set(UNCHAINEDTICKS,chained);
    }
    @Override
    public int stockpile$getSpinAge() {
        return this.getDataTracker().get(SPINAGE);
    }

    @Override
    public void stockpile$setSpinAge(int ticks) {
        this.getDataTracker().set(SPINAGE,ticks);
    }

    @Shadow public abstract ItemStack getStack();

    @Shadow protected abstract ItemStack getTrackedItem();

    @Shadow protected abstract SoundEvent getHitSound();

    @Shadow public abstract void setDealtDamage(boolean dealtDamage);

    @Shadow public abstract boolean hasDealtDamage();

    @Shadow public abstract boolean isRecalled();

    @Shadow public abstract void setPitch(float pitch);

    @Shadow public abstract void setYaw(float yaw);

    @Shadow public abstract void setRecalled(boolean recalled);

    @Shadow protected abstract ItemStack asItemStack();

    @Shadow protected abstract float getKnockbackForEntity(LivingEntity hitLivingEntity);

    @Override
    public float getYaw() {
        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.UNCHAINED, getStack())>0 && isRecalled()){
            Vec3d d = getVelocity().normalize();
            float pitch = (float) Math.asin(-d.y);
            float yaw = (float) MathHelper.atan2(d.x, d.z);
            return yaw/3.14f*180;
        }
        return super.getYaw();
    }
    @Override
    public float getPitch() {
        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.UNCHAINED, getStack())>0 && isRecalled() ){
            Vec3d d = getVelocity().normalize();
            float pitch = (float) Math.asin(-d.y);
            float yaw = (float) MathHelper.atan2(d.x, d.z);
            return pitch/3.14f*180;
        }
        return super.getPitch();
    }

    @Inject(method = "tick()V",at = @At("TAIL"))
    private void unchainedTick(CallbackInfo ci){

        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.UNCHAINED, getStack())>0 && isRecalled() ){
        }
    }

    @Override
    public void stockpile$removeground() {
        this.setOnGround(false);
        this.inGround = false;
    }

    @Inject(method = "tick()V",at = @At("HEAD"))
    private void whirlingTick(CallbackInfo ci){
        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.SPINTOWIN, getStack())>0){
            if(stockpile$getSpinAge()<120)
            {
                if(this.inGround){
                    float radius = 5.0F;
                    this.getWorld().addParticle(ArsenalParticles.SHOCKWAVE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
                    Iterator var14 = this.getWorld().getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand((double)radius), LivingEntity::isAlive).iterator();

                    while(var14.hasNext()) {
                        LivingEntity hitLivingEntity = (LivingEntity)var14.next();
                        float strength = this.getKnockbackForEntity(hitLivingEntity);
                        if (!((double)strength <= 0.0)) {
                            this.velocityDirty = true;
                            Vec3d distance = hitLivingEntity.getPos().add(0.0, (double)(hitLivingEntity.getHeight() / 2.0F), 0.0).subtract(this.getPos());
                            Vec3d footDistance = hitLivingEntity.getPos().subtract(this.getPos());
                            if (footDistance.y > distance.y) {
                                distance = footDistance;
                            }

                            float proximity = (float)MathHelper.lerp(MathHelper.clamp(distance.length() / (double)radius, 0.0, 1.0), 1.0, 0.0);
                            Vec3d direction = distance.normalize().multiply((double)(proximity * strength));
                            hitLivingEntity.addVelocity(direction.x, direction.y, direction.z);
                            hitLivingEntity.fallDistance = 0.0F;
                        }
                    }
                    this.inGround = false;
                    this.setRecalled(true);
                    this.setNoClip(true);
                    this.stockpile$setSpinAge(121);
                }

                if(!getWorld().isClient){
                    this.stockpile$setSpinAge(this.stockpile$getSpinAge()+1);
                }
                if(getOwner()!=null && !isRecalled()){
                    if(getOwner() instanceof LivingEntity livingEntity && this.getDataTracker().get(OWNERID)==0){
                        this.getDataTracker().set(OWNERID,livingEntity.getId());
                    }
                    if(getOwner().getVelocity().y<0)
                        getOwner().addVelocity(0,0.03,0);
                    getOwner().fallDistance *= 0.2f;

                    Vec3d dir = getOwner().getPos().add(new Vec3d(2.7,0.2,0).rotateY(this.stockpile$getSpinAge())).subtract(getPos());
                    this.setVelocity(dir);
                }
            }
            else if(stockpile$getSpinAge()==120&&getOwner() instanceof PlayerEntity player){
                this.stockpile$setSpinAge(121);
                setPosition(getOwner().getEyePos().add(getOwner().getRotationVec(0).multiply(2)));
                player.getItemCooldownManager().set(ArsenalItems.ANCHORBLADE,40);
                setVelocity(getOwner().getPos().add(getOwner().getRotationVec(0).multiply(64).subtract(getPos())).normalize().multiply(3));
            }


        }
        if(stockpile$getUnchainedTicks()>0){
            if(stockpile$getUnchainedTicks()==3&&getOwner()!=null){
                getWorld().playSound(getOwner().getX(), getOwner().getY(),getOwner().getZ(), SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.PLAYERS,4f,0.4f,false);
                getWorld().playSound(getOwner().getX(), getOwner().getY(),getOwner().getZ(), ArsenalSounds.ENTITY_ANCHORBLADE_LAND, SoundCategory.PLAYERS,1f,0.4f,false);
            }
            if(!getWorld().isClient)
                stockpile$setUnchainedTicks(stockpile$getUnchainedTicks()-1);
        }
        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.UNCHAINED, getStack())>0 && isRecalled() ){
            addVelocity(0,-0.16,0);
            if(this.inGround){
                this.setVelocity(getVelocity().multiply(-1));
                this.inGround=false;
                this.setNoClip(true);
            }
            else{
                this.setNoClip(false);
            }
            if(this.age>200 ){
                this.discard();
            }
        }

        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.WHIRLING, getStack())>0 ){

            for (int i : stockpile$getEntityHitList()){
                if(getWorld().getEntityById(i) instanceof LivingEntity hitLivingEntity){
                    Entity owner = getOwner();

                    Vec3d dir = hitLivingEntity.getPos().subtract(getPos()).normalize().multiply(-2).add(getVelocity().multiply(0.2f));
                    hitLivingEntity.setVelocity(dir.x, dir.y, dir.z);

                }

            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if((EnchantmentHelper.getLevel(StockpileEnchantmentInit.UNCHAINED, getStack())>0 && isRecalled())){
            float radius = 5.0F;
            this.getWorld().addParticle(ArsenalParticles.SHOCKWAVE, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            Iterator var14 = this.getWorld().getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand((double)radius), LivingEntity::isAlive).iterator();

            while(var14.hasNext()) {
                LivingEntity hitLivingEntity = (LivingEntity)var14.next();
                float strength = this.getKnockbackForEntity(hitLivingEntity);
                if (!((double)strength <= 0.0)) {
                    this.velocityDirty = true;
                    Vec3d distance = hitLivingEntity.getPos().add(0.0, (double)(hitLivingEntity.getHeight() / 2.0F), 0.0).subtract(this.getPos());
                    Vec3d footDistance = hitLivingEntity.getPos().subtract(this.getPos());
                    if (footDistance.y > distance.y) {
                        distance = footDistance;
                    }

                    float proximity = (float)MathHelper.lerp(MathHelper.clamp(distance.length() / (double)radius, 0.0, 1.0), 1.0, 0.0);
                    Vec3d direction = distance.normalize().multiply((double)(proximity * strength)).multiply(0.7);
                    hitLivingEntity.addVelocity(direction.x, direction.y, direction.z);
                    hitLivingEntity.fallDistance = 0.0F;
                }
            }
            if((EnchantmentHelper.getLevel(StockpileEnchantmentInit.UNCHAINED, getStack())>0 && isRecalled())){
                this.inGround=false;
            }

            Vec3d dir = new Vec3d(blockHitResult.getSide().getUnitVector());
            if(dir.x!=0){
                setVelocity(getVelocity().multiply(-1,1,1));
            }
            if(dir.y!=0){
                setVelocity(getVelocity().multiply(1,-1,1));
            }
            if(dir.z!=0){
                setVelocity(getVelocity().multiply(1,1,-1));
            }
            for (int i = 0; i < 12; i++) {
                getWorld().addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK,getWorld().getBlockState(blockHitResult.getBlockPos())),getX(),getY(),getZ(),(getVelocity().z+Math.random()-0.5)*3,(getVelocity().y+Math.random()-0.5)*3,(getVelocity().z+Math.random()-0.5)*3);
            }
            setVelocity(getVelocity().multiply(0.94));

        }
        else if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.UNCHAINED, getStack())>0){
            unchainedNewAnchor();
        }
        else{
            if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.WHIRLING, getStack())>0){

                if(this.getOwner()!=null){
                    this.setPosition(this.getPos().subtract(this.getPos().subtract(this.getOwner().getPos()).normalize().multiply(0.5)));
                }

            }
            super.onBlockHit(blockHitResult);
            this.setSound(SoundEvents.BLOCK_CHAIN_STEP);
        }


    }

//    @Override
//    public boolean handleAttack(Entity attacker) {
//        if(attacker == getOwner()){
//            if(attacker instanceof LivingEntity livingEntity){
//                setVelocity(livingEntity.getRotationVec(0).multiply(2));
//            }
//        }
//        return super.handleAttack(attacker);
//    }
//
//    @Override
//    public boolean isAttackable() {
//        return EnchantmentHelper.getLevel(StockpileEnchantmentInit.UNCHAINED, getStack())>0 && isRecalled();
//    }

    @Redirect(method = "tick()V",at = @At(value = "FIELD", target = "Ldev/doctor4t/arsenal/entity/AnchorbladeEntity;inGround:Z", opcode = Opcodes.GETFIELD))
    private boolean unchainedinGroundTick(AnchorbladeEntity instance){
        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.UNCHAINED, getStack())>0 ){
            return false;
        }
        return this.inGround;
    }
    @Unique
    private void unchainedNewAnchor(){
        if(!isRecalled()){
            if(getOwner() instanceof PlayerEntity user){
                if(user instanceof AnchorOwner owner){
                    user.getItemCooldownManager().set(ArsenalItems.ANCHORBLADE, 50);
                    if (getWorld() instanceof ServerWorld world1) {
                        AnchorbladeEntity oldAnchor = (AnchorbladeEntity)(PersistentProjectileEntity)this;
                        AnchorbladeEntity anchorbladeEntity = new AnchorbladeEntity(getWorld(), user, getStack());
                        anchorbladeEntity.setVelocity(oldAnchor.getVelocity());
                        anchorbladeEntity.setPos(oldAnchor.getX(), oldAnchor.getY(), oldAnchor.getZ());
                        anchorbladeEntity.setRecalled(true);
                        anchorbladeEntity.setDealtDamage(false);
                        world1.spawnEntity(anchorbladeEntity);
                        if(anchorbladeEntity instanceof WhirlingEntityAccessor accessor){
                            accessor.stockpile$setUnchainedTicks(5);
                        }

                        oldAnchor.discard();
                    }
                }
            }

        }
    }
    //Ldev/doctor4t/arsenal/entity/AnchorbladeEntity;getY()D
    @ModifyArgs(method = "tick()V",at = @At(value = "INVOKE", target = "Ldev/doctor4t/arsenal/entity/AnchorbladeEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private void whirlingReelbackTick(Args args){
        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.UNCHAINED, getStack())>0 ){
            unchainedNewAnchor();
            args.set(0,getVelocity());

        }


    }
    @Redirect(method = "tick()V",at = @At(value = "INVOKE", target = "Ldev/doctor4t/arsenal/entity/AnchorbladeEntity;getY()D"))
    private double unchainedReelbackTick(AnchorbladeEntity instance){
        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.UNCHAINED, getStack())>0 && isRecalled()){
            return lastRenderY;

        }
        return getY();
    }
}
