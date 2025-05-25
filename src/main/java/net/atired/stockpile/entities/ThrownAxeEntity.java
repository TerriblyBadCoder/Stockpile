package net.atired.stockpile.entities;

import dev.doctor4t.arsenal.entity.AnchorbladeEntity;
import dev.doctor4t.arsenal.index.ArsenalParticles;
import eu.midnightdust.lib.config.MidnightConfig;
import net.atired.stockpile.init.StockpileEnchantmentInit;
import net.atired.stockpile.init.StockpileEntityTypeInit;
import net.atired.stockpile.init.StockpileItemInit;
import net.atired.stockpile.init.StockpileParticleInit;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ThrownAxeEntity extends ThrownItemEntity {
    private static final TrackedData<Boolean> EMBEDDED = DataTracker.registerData(ThrownAxeEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Vector3f> DIR = DataTracker.registerData(ThrownAxeEntity.class, TrackedDataHandlerRegistry.VECTOR3F);

    private boolean touchedBlock = false;
    private int trailPointer = -1;
    private Vec3d lastPos;
    private Vec3d[] trailPositions = new Vec3d[64];
    public ThrownAxeEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
        this.lastPos = new Vec3d(0,0,0);
        setDir(getVelocity().toVector3f());
    }
    public ThrownAxeEntity(World world, LivingEntity owner) {
        super(StockpileEntityTypeInit.THROWN_AXE, owner, world);
    }
    public Vec3d getTrailPosition(int pointer, float partialTick) {
        if (this.isRemoved()) {
            partialTick = 1.0F;
        }
        int i = this.trailPointer - pointer & 63;
        int j = this.trailPointer - pointer - 1 & 63;
        Vec3d d0 = this.trailPositions[j];
        Vec3d d1 = this.trailPositions[i].subtract(d0);
        return d0.add(d1.multiply(partialTick));
    }
    public boolean hasTrail() {
        return trailPointer != -1;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if(entityHitResult.getEntity() instanceof LivingEntity livingEntity && livingEntity.hurtTime<=0){
            if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.RECALL,getItem())==0||livingEntity!=getOwner()) {
                livingEntity.damage(getDamageSources().mobProjectile(this, (getOwner() instanceof LivingEntity living) ? living : null), 12);
            }
            if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.RECALL,getItem())==0){
                this.setVelocity(this.getVelocity().multiply(0.3,1,0.3));
                this.addVelocity(0,0.4,0);
            }
            if(getWorld() instanceof ServerWorld world){
                world.spawnParticles(ParticleTypes.CRIT,getX(),getY(),getZ(),4,0.1,0.1,0.1,0.3);
            }
        }
        super.onEntityHit(entityHitResult);
    }
    private void dropAsItem(){
        ItemEntity item = this.dropStack(this.getStack(), 0.1F);
        if(item!=null){
            item.addVelocity(0,0.4,0);
            item.setPickupDelay(2);
        }
    }

    @Override
    public boolean canBeHitByProjectile() {
        return true;
    }

    @Override
    public void tick() {
        if(getVelocity().multiply(1,0.1,1).length()>2.2&&EnchantmentHelper.getLevel(StockpileEnchantmentInit.RECALL,getItem())!=0){
            dropAsItem();
            discard();
        }
        if (!this.getWorld().isClient&&age>10 && getOwner()!=null&&getOwner().distanceTo(this)<1.5&&getOwner() instanceof PlayerEntity player) {
            if(!player.isCreative()){
                dropAsItem();
            }

            this.discard();
        }

        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.RECALL,getItem())!=0&&(getVelocity().normalize().dotProduct(new Vec3d(getDir()).multiply(-1).normalize())<0.8f||getVelocity().length()<1.5)){
            setVelocity(getVelocity().add(new Vec3d(getDir()).multiply(-0.06)));
        }
         if(!getWorld().isClient()&&!getEmbedded()&&(EnchantmentHelper.getLevel(StockpileEnchantmentInit.RECALL,getItem())==0||age<4)){
            setDir(getVelocity().toVector3f());
        }
        if(getEmbedded()){
            setVelocity(0,0,0);
        }
        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        if(!getSteppingBlockState().isSolid()){
            this.touchedBlock=false;
        }
        super.tick();


        this.lastPos = this.getPos();
        Vec3d trailAt = this.getPos().add(0,0.4,0);
        if(!getEmbedded())
        {
            Vec3d dir = new Vec3d(new Vector3f((float) (0.7f+ Math.random()/15f),0,0).rotate(new Quaternionf().rotationXYZ(0,(float)Math.PI/2+(float)(Math.atan2(getVelocity().getX(), getVelocity().getZ())),this.age+0.4f)));
            trailAt = trailAt.add(dir);

        }


        if (trailPointer == -1) {
            Vec3d backAt = trailAt;
            for (int i = 0; i < trailPositions.length; i++) {
                trailPositions[i] = backAt;
            }
        }
        if(trailPointer == -1||(trailAt.distanceTo(this.trailPositions[this.trailPointer])>0.1||getEmbedded())){
            if (++this.trailPointer == this.trailPositions.length) {
                this.trailPointer = 0;
            }
            this.trailPositions[this.trailPointer] = trailAt;
        }


        if(!this.touchedBlock&&getEmbedded()&&!getWorld().isClient()){
            setEmbedded(false);
        }
        if(getDir().length()==0&&getOwner()==null){
            discard();
        }
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();

        this.getDataTracker().startTracking(EMBEDDED,false);
        this.getDataTracker().startTracking(DIR,new Vector3f());
    }

    @Override
    protected Box calculateBoundingBox() {
        if(!getEmbedded())
            return super.calculateBoundingBox();
        else{
            return super.calculateBoundingBox().expand(0.2);
        }
    }

    @Override
    protected float getGravity() {
        if(getEmbedded()){
            return 0.0f;
        }
        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.RECALL,getItem())!=0){
            if(getVelocity().normalize().dotProduct(new Vec3d(getDir()).multiply(-1).normalize())>=0.8f)
                return super.getGravity()*1.3f*(float)getVelocity().multiply(1,0.1,1).length();
            else
                return  0.0f;
        }

        return super.getGravity()*2f;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        this.touchedBlock = true;
        if(EnchantmentHelper.getLevel(StockpileEnchantmentInit.SANGUINE,getItem())!=0){
            setEmbedded(true);
            if(getWorld() instanceof ServerWorld serverWorld){
                serverWorld.spawnParticles(StockpileParticleInit.BLIGHT_PARTICLE,getX(),getY(),getZ(),1,0,0,0,0);
                serverWorld.spawnParticles(ArsenalParticles.BLOOD_BUBBLE,getX(),getY(),getZ(),3,0,0,0,0);

            }
            discard();
        }
        if(!getEmbedded()&&getWorld() instanceof ServerWorld world){
            move(MovementType.SELF,getVelocity().multiply(1.1));
            world.spawnParticles(ParticleTypes.CRIT,getX(),getY(),getZ(),3,0.1,0.1,0.1,0.3);
            setVelocity(0,0,0);
            setEmbedded(true);
        }
        super.onBlockHit(blockHitResult);
    }
    public void setDir(Vector3f dir) {
        this.getDataTracker().set(DIR,dir);
    }

    public Vector3f getDir() {
        return this.getDataTracker().get(DIR);
    }

    public void setEmbedded(boolean embedded) {
        this.getDataTracker().set(EMBEDDED,embedded);
    }

    public boolean getEmbedded() {
        return this.getDataTracker().get(EMBEDDED);
    }

    @Override
    protected Item getDefaultItem() {
        return StockpileItemInit.THROWING_AXE;
    }
}
