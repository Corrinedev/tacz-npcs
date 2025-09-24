package com.corrinedev.tacznpcs.common.entity;

import com.corrinedev.tacznpcs.Config;
import com.corrinedev.tacznpcs.common.entity.behavior.TaczShootAttack;
import com.corrinedev.tacznpcs.common.entity.inventory.ScavInventory;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.shooter.*;
import com.tacz.guns.entity.sync.ModSyncedEntityData;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.item.AmmoItem;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Panic;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.AvoidEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.StrafeTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.*;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public abstract class AbstractScavEntity extends PathfinderMob implements SmartBrainOwner<AbstractScavEntity>, IGunOperator, InventoryCarrier, HasCustomInventoryScreen, MenuProvider {
    public int rangedCooldown = 0;
    public AbstractScavEntity patrolLeader = null;
    public List<AbstractScavEntity> team = new ArrayList<>();
    public boolean isPatrolLeader = false;
    public ItemStack patrolLeaderBanner = ItemStack.EMPTY;
    public boolean firing = true;
    public int collectiveShots = 0;
    public boolean panic = false;
    public int paniccooldown = 0;
    public boolean isReloading = false;
    public boolean deadAsContainer = false;
    public int deadAsContainerTime = 0;
    public int randomDeathNumber = RandomSource.create().nextInt(1,4);
    public long shootTimestamp = 0L;
    public SimpleContainer inventory;
    public List<LivingEntity> attackers = new ArrayList<>();

    public AbstractScavEntity generateNew() {
        return (AbstractScavEntity) this.getType().create(this.level());
    }

    protected AbstractScavEntity(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        setPathfindingMalus(BlockPathTypes.WATER, 1.0f);
        setPathfindingMalus(BlockPathTypes.WATER_BORDER, 1.0f);
        initialData();
        setMaxUpStep(1f);
        inventory = new SimpleContainer(27);
        this.tacz$draw = new LivingEntityDrawGun(this.tacz$shooter, this.tacz$data);
        this.tacz$aim = new LivingEntityAim(this.tacz$shooter, this.tacz$data);
        this.tacz$crawl = new LivingEntityCrawl(this.tacz$shooter, this.tacz$data);
        this.tacz$ammoCheck = new LivingEntityAmmoCheck(this.tacz$shooter);
        this.tacz$fireSelect = new LivingEntityFireSelect(this.tacz$shooter, this.tacz$data);
        this.tacz$melee = new LivingEntityMelee(this.tacz$shooter, this.tacz$data, this.tacz$draw);
        this.tacz$shoot = new LivingEntityShoot(this.tacz$shooter, this.tacz$data, this.tacz$draw);
        this.tacz$bolt = new LivingEntityBolt(this.tacz$data, this.tacz$shooter, this.tacz$draw, this.tacz$shoot);
        this.tacz$reload = new LivingEntityReload(this.tacz$shooter, this.tacz$data, this.tacz$draw, this.tacz$shoot);
        this.tacz$speed = new LivingEntitySpeedModifier(this.tacz$shooter, this.tacz$data);
        this.tacz$sprint = new LivingEntitySprint(this.tacz$shooter, this.tacz$data);

        this.inventory.addItem(patrolLeaderBanner);

    }

    public static AttributeSupplier.@NotNull Builder createLivingAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    public void openCustomInventoryScreen(@NotNull Player pPlayer) {
        createMenu(999, pPlayer.getInventory(), pPlayer);
        pPlayer.openMenu(this);
    }

    @Override
    public @org.jetbrains.annotations.Nullable AbstractContainerMenu createMenu(int pContainerId, @NotNull Inventory pPlayerInventory, @NotNull Player pPlayer) {
        return ScavInventory.generate(pContainerId, pPlayerInventory, inventory, this);
    }

    @Override
    public @NotNull SimpleContainer getInventory() {
        return inventory;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if(pSource.getEntity() instanceof LivingEntity living) {
            attackers.add(living);
        }
        if(this.deadAsContainer) {
            return false;
        }
        return super.hurt(pSource, pAmount);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        this.writeInventoryToTag(pCompound);
        pCompound.putBoolean("dead", deadAsContainer);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        resetSlots();
        this.readInventoryFromTag(pCompound);
        if(pCompound.contains("dead")) {
            this.deadAsContainer = pCompound.getBoolean("dead");
        }
    }
    public void resetSlots() {
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            this.setItemSlot(slot, ItemStack.EMPTY);
        }
    }

    public boolean isSlim() {
        return true;
    }

    @Override
    public boolean isDeadOrDying() {
        return (this.getHealth() <= 0.0F) || this.deadAsContainer;
    }

    @Override
    public float getHealth() {
        //if(this.deadAsContainer) {
        //    return 1.0f;
        //}
        return super.getHealth();
    }

    @Override
    public void onEquipItem(@NotNull EquipmentSlot pSlot, @NotNull ItemStack pOldItem, ItemStack pNewItem) {
        boolean flag = pNewItem.isEmpty() && pOldItem.isEmpty();
        if (!flag && !ItemStack.isSameItemSameTags(pOldItem, pNewItem) && !this.firstTick) {
            Equipable equipable = Equipable.get(pNewItem);
            if (equipable != null && !this.isSpectator() && equipable.getEquipmentSlot() == pSlot) {
                if (this.doesEmitEquipEvent(pSlot)) {
                    this.gameEvent(GameEvent.EQUIP);
                }
            }
        }
    }

    public abstract boolean allowInventory(Player player);

    public BrainActivityGroup<? extends AbstractScavEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(new Behavior[]{
                (new AvoidEntity<>()).noCloserThan(12).avoiding((entity) -> {
            return entity == this.getTarget();
        }),
                new FollowEntity<AbstractScavEntity, AbstractScavEntity>().following((entity) -> entity.patrolLeader).stopFollowingWithin(8).speedMod(1.1f),
                new TargetOrRetaliate<AbstractScavEntity>().isAllyIf((e, l) -> l.getType() == e.getType()).attackablePredicate(l -> l != null && this.hasLineOfSight(l)).alertAlliesWhen((m, e) -> e != null && m.hasLineOfSight(e)).runFor((e) -> 999).stopIf((e) -> e.getTarget() instanceof AbstractScavEntity scav && scav.deadAsContainer),
                //new SetRetaliateTarget<>().isAllyIf((e, l) -> l.getType() == e.getType()),
                new Panic<>().setRadius(16).speedMod((e) -> 1.1f).startCondition((e) -> this.getHealth() <= 10).whenStopping((e) -> panic = false).whenStarting( (e)-> panic = true).stopIf((e) -> this.getTarget() == null && !this.getTarget().hasLineOfSight(this)).runFor((e) -> 20),
                (new LookAtTarget<AbstractScavEntity>()).runFor((entity) -> {
            return RandomSource.create().nextInt(40, 300);
        }).stopIf((e) -> e.getTarget() instanceof AbstractScavEntity scav && scav.deadAsContainer),
                new OneRandomBehaviour<>(new ExtendedBehaviour[]{
                new StrafeTarget<>().speedMod(0.75f).strafeDistance(24).stopStrafingWhen((entity) -> {
                    return this.getTarget() == null || !this.getMainHandItem().is(ModItems.MODERN_KINETIC_GUN.get());
                }).startCondition((e) -> this.getMainHandItem().is(ModItems.MODERN_KINETIC_GUN.get())),
                new MoveToWalkTarget<>()
         }),
        });
    }

    @Override
    public boolean isAlive() {
        return (!this.isRemoved() && this.getHealth() > 0.0F) || this.deadAsContainer;
    }

    @Override
    protected void tickDeath() {
        if(this.inventory.isEmpty()) {
            super.tickDeath();
        }
    }

    @Override
    public void die(@NotNull DamageSource pDamageSource) {
        if(this.inventory.isEmpty()) {
            super.die(pDamageSource);
        } else {
            this.deadAsContainer = true;
            if (net.minecraftforge.common.ForgeHooks.onLivingDeath(this, pDamageSource)) return;
            if (!this.isRemoved() && !this.dead) {
                Entity entity = pDamageSource.getEntity();
                LivingEntity livingentity = this.getKillCredit();
                if (this.deathScore >= 0 && livingentity != null) {
                    livingentity.awardKillScore(this, this.deathScore, pDamageSource);
                }

                if (this.isSleeping()) {
                    this.stopSleeping();
                }

                this.dead = false;
                this.getCombatTracker().recheckStatus();
                Level level = this.level();
               //if (level instanceof ServerLevel serverlevel) {
               //    if (entity == null || entity.killedEntity(serverlevel, this)) {
               //        this.gameEvent(GameEvent.ENTITY_DIE);
               //        this.dropAllDeathLoot(pDamageSource);
               //        this.createWitherRose(livingentity);
               //    }
               //    this.level().broadcastEntityEvent(this, (byte)3);
               //}

                //this.setPose(Pose.DYING);
            }
        }
    }
    @Override
    public void kill() {
        if(!this.deadAsContainer) {
            super.kill();
        } else {
            this.remove(Entity.RemovalReason.KILLED);
            this.gameEvent(GameEvent.ENTITY_DIE);
        }
    }

    @Override
    protected void dropAllDeathLoot(DamageSource pDamageSource) {
        if(Config.DROPITEMS.get()) {
            dropCustomDeathLoot(pDamageSource, 0, true);
        }
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource pSource, int pLooting, boolean pRecentlyHit) {
        for (int i = 0; i < inventory.getContainerSize() - 1; i++) {
            this.spawnAtLocation(inventory.removeItem(i, inventory.getItem(i).getCount()));
        }
        for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            ItemStack itemstack = this.getItemBySlot(equipmentslot);
            if (!itemstack.isEmpty()) {
                if (itemstack.isDamageableItem()) {
                    itemstack.setDamageValue(itemstack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemstack.getMaxDamage() - 3, 1))));
                }

                this.spawnAtLocation(itemstack);
                this.setItemSlot(equipmentslot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public @org.jetbrains.annotations.Nullable ItemEntity spawnAtLocation(ItemStack pStack, float pOffsetY) {
        if (pStack.isEmpty()) {
            return null;
        } else if (this.level().isClientSide) {
            return null;
        } else {
            ItemEntity itementity = new ItemEntity(this.level(), this.getX(), this.getY() + (double)pOffsetY, this.getZ(), pStack);
            itementity.setDefaultPickUpDelay();
            itementity.lifespan = 1000;
            if (this.captureDrops() != null) {
                this.captureDrops().add(itementity);
            } else {
                this.level().addFreshEntity(itementity);
            }

            return itementity;
        }
    }

    public BrainActivityGroup<? extends AbstractScavEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(new Behavior[]{new FirstApplicableBehaviour(new ExtendedBehaviour[]{
                new TargetOrRetaliate<>(),
                new SetPlayerLookTarget<>(),
                new SetRandomLookTarget<>()}),
                new OneRandomBehaviour(new ExtendedBehaviour[]{(
                        new SetRandomWalkTarget<>()).speedModifier(1.0F),
                        (new Idle<>()).runFor((entity) -> RandomSource.create().nextInt(30, 60))})});
    }

    public BrainActivityGroup<? extends AbstractScavEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(new Behavior[]{
                new InvalidateAttackTarget<>(),
                (new SetWalkTargetToAttackTarget<>()).startCondition((entity) -> {
            return !this.getMainHandItem().is(ModItems.MODERN_KINETIC_GUN.get());}),
            new SetRetaliateTarget<>(),
            new FirstApplicableBehaviour<>(new ExtendedBehaviour[]{(new TaczShootAttack<>(64).stopIf((e) -> e.getTarget() instanceof AbstractScavEntity scav && scav.deadAsContainer).startCondition((x$0) -> {
            return this.getMainHandItem().is(ModItems.MODERN_KINETIC_GUN.get()) && !this.panic && this.collectiveShots <= this.getStateBurst();
        })),
                    (new AnimatableMeleeAttack<>(0)).whenStarting((entity) -> {
            this.setAggressive(true);
        }).whenStopping((entity) -> {
            this.setAggressive(false);
        })})});
    }
    public boolean isUsingGun() {
        return this.getMainHandItem().getItem() instanceof ModernKineticGunItem;
    }
    @Override
    public abstract List<? extends ExtendedSensor<? extends AbstractScavEntity>> getSensors();

    public GunTabType heldGunType() {
        if(this.getMainHandItem().getItem() instanceof ModernKineticGunItem gun) {
            if(TimelessAPI.getCommonGunIndex(gun.getGunId(this.getMainHandItem())).isPresent()) {
                return switch (TimelessAPI.getCommonGunIndex(gun.getGunId(this.getMainHandItem())).get().getType()) {
                    case "pistol" -> GunTabType.PISTOL;
                    case "rifle" -> GunTabType.RIFLE;
                    case "sniper" -> GunTabType.SNIPER;
                    case "smg" -> GunTabType.SMG;
                    case "rpg" -> GunTabType.RPG;
                    case "shotgun" -> GunTabType.SHOTGUN;
                    case "mg" -> GunTabType.MG;
                    default ->
                            throw new IllegalStateException("Unexpected value: " + TimelessAPI.getCommonGunIndex(gun.getGunId(this.getMainHandItem())).get().getType());
                };
            }
        }
            return GunTabType.PISTOL;
    }
    public static @Nullable GunTabType heldGunType(ItemStack gunStack) {
        if(gunStack.getItem() instanceof ModernKineticGunItem gun) {
            switch (TimelessAPI.getCommonGunIndex(gun.getGunId(gunStack)).get().getType()) {
                case "pistol" : return GunTabType.PISTOL;
                case "rifle" : return GunTabType.RIFLE;
                case "sniper" : return GunTabType.SNIPER;
                case "smg" : return GunTabType.SMG;
                case "rpg" : return GunTabType.RPG;
                case "shotgun" : return GunTabType.SHOTGUN;
                case "mg" : return GunTabType.MG;
            }
        }
        return null;
    }
    public int getStateRangedCooldown() {
        if(heldGunType() != null) {
            return switch (heldGunType()) {
                case RIFLE -> 10;
                case PISTOL -> 8;
                case SNIPER -> 30;
                case SHOTGUN -> 20;
                case SMG, MG -> 5;
                case RPG -> 100;
                default -> 60;
            };
        }
        return 60;
    }
    int getStateBurst() {
        if(heldGunType() != null) {
            return switch (heldGunType()) {
                case RIFLE -> 2;
                case PISTOL -> 3;
                case SNIPER -> 1;
                case SHOTGUN -> 1;
                case SMG, MG -> 4;
                case RPG -> 1;
            };
        }
        return 1;
    }
    protected Brain.@NotNull Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        if(!this.deadAsContainer) {
            this.tickBrain(this);
        }
    }

    public void pickUpItem(ItemEntity pItemEntity) {
        if(!this.isDeadOrDying()) {
            ItemStack itemstack = pItemEntity.getItem();
            if (this.wantsToPickUp(itemstack)) {
                SimpleContainer simplecontainer = inventory;
                boolean flag = simplecontainer.canAddItem(itemstack);
                if (!flag) {
                    return;
                }

                this.onItemPickup(pItemEntity);
                int i = itemstack.getCount();
                ItemStack itemstack1 = simplecontainer.addItem(itemstack);
                this.take(pItemEntity, i - itemstack1.getCount());
                if (itemstack1.isEmpty()) {
                    pItemEntity.discard();
                } else {
                    itemstack.setCount(itemstack1.getCount());
                }
            }
        }
    }

    @Override
    public void onAddedToWorld() {
        if(this.deadAsContainer) {
            if(this.getLastDamageSource() != null) {
                dropCustomDeathLoot(this.getLastDamageSource(), 0, true);
            } else {
                dropCustomDeathLoot(this.damageSources().generic(), 0, true);
            }
            this.discard();
        }
        super.onAddedToWorld();
        if(this.isPatrolLeader) this.setCustomName(Component.literal(this.getCustomName().getString() + " | Patrol Leader"));
    }

    @Override
    public void tick() {
        if(this.getTarget() != null && this.getTarget() instanceof AbstractScavEntity scav && scav.deadAsContainer) {
            this.setTarget(null);
            this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, (LivingEntity) null);
        }
        if(this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) {
            if(this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get() instanceof AbstractScavEntity scav && scav.deadAsContainer) {
                this.setTarget(null);
                this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, (LivingEntity) null);
            }
        }

        if(this.deadAsContainer) {
            tickDeath();
            //super.aiStep();
            this.detectEquipmentUpdates();
            super.baseTick();
            setBoundingBox(AABB.ofSize(this.position(), 0.8, 0.5, 0.8));
            deadAsContainerTime++;
            if(this.deadAsContainerTime > Config.TICKITEMS.get()) {
                this.discard();
            }
            return;
        }

        onTickServerSide();
        if(ModList.get().isLoaded("gundurability")) {
            if(this.getMainHandItem().getItem() instanceof ModernKineticGunItem) {
                if(this.getMainHandItem().getOrCreateTag().getBoolean("Jammed")) {
                    if(RandomSource.create().nextInt(0, 60) == 1) {
                        this.getMainHandItem().getOrCreateTag().putBoolean("Jammed", false);
                    }
                }
            }
        }
        if(inventory.hasAnyMatching((i) -> i.getItem() instanceof ArmorItem)) {
            for (int i = 0; i < inventory.getContainerSize() - 1; i++) {
                if (inventory.getItem(i).getItem() instanceof ArmorItem item) {
                    if(this.getItemBySlot(item.getEquipmentSlot()).isEmpty()) {
                        this.setItemSlotAndDropWhenKilled(item.getEquipmentSlot(), inventory.getItem(i));
                    }
                }
            }
        }
        if(inventory.hasAnyOf(Set.of(ModItems.MODERN_KINETIC_GUN.get())) && !(this.getMainHandItem().getItem() instanceof ModernKineticGunItem)) {
            for (int i = 0; i < inventory.getContainerSize() - 1; i++) {
                if (inventory.getItem(i).is(ModItems.MODERN_KINETIC_GUN.get())) {
                    this.setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, inventory.getItem(i));
                }
            }
        } else if(!(this.getMainHandItem().getItem() instanceof ModernKineticGunItem)) {
            for (int i = 0; i < inventory.getContainerSize() - 1; i++) {
                if (inventory.getItem(i).getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE)) {
                    this.setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, inventory.getItem(i));
                    //inventory.removeItem(i, 1);
                }
            }
        }

        if(getMainHandItem().getItem() instanceof ModernKineticGunItem gun) {
            if(this.getMainHandItem().getOrCreateTag().getInt("GunCurrentAmmoCount") == 0 && !this.getMainHandItem().getOrCreateTag().getBoolean("HasBulletInBarrel")) {
                this.reload();
            }
        }
        if (getMainHandItem().getItem() instanceof ModernKineticGunItem gun) {
            if(gun.getCurrentAmmoCount(getMainHandItem()) > 0) {
                this.isReloading = false;
            } else {
                if(!this.isReloading) {
                    this.reload();
                }
                this.isReloading = true;

            }
        }


        if (firing && shootTimestamp != 0) {
            if ((System.currentTimeMillis() - shootTimestamp) / 100 > getStateRangedCooldown()) {
                collectiveShots = 0;
                shootTimestamp = 0;
                firing = false;
                aim(false);
            }
        }
        if(rangedCooldown != 0) {
            rangedCooldown--;
        }
        if(paniccooldown != 0) {

            paniccooldown--;
            if(paniccooldown == 1) {
                panic = false;
            }
        }
        List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(1.1));
            if (!items.isEmpty()) {
                for (ItemEntity item : items) {
                    if (item.getItem().getItem() instanceof ModernKineticGunItem || item.getItem().is(ItemTags.AXES) || item.getItem().is(ItemTags.SWORDS) || item.getItem().getItem() instanceof ArmorItem || item.getItem().getItem() instanceof AmmoItem) {

                        pickUpItem(item);
                    }
                }
        }
        super.tick();
    }

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if(allowInventory(pPlayer)) {
           openCustomInventoryScreen(pPlayer);
        }
        return super.mobInteract(pPlayer, pHand);
    }

    protected void reduceAmmo(ItemStack currentGunItem, ModernKineticGunItem gunitem) {
        Bolt boltType = (Bolt)TimelessAPI.getCommonGunIndex(gunitem.getGunId(currentGunItem)).map((index) -> index.getGunData().getBolt()).orElse((Bolt) null);
        if (boltType != null) {
            if (boltType == Bolt.MANUAL_ACTION) {
                gunitem.setBulletInBarrel(currentGunItem, false);
            } else if (boltType == Bolt.CLOSED_BOLT) {
                if (gunitem.getCurrentAmmoCount(currentGunItem) > 0) {
                    gunitem.reduceCurrentAmmoCount(currentGunItem);
                } else {
                    gunitem.setBulletInBarrel(currentGunItem, false);
                }
            } else {
                gunitem.reduceCurrentAmmoCount(currentGunItem);
            }

        }
    }

    
    private final LivingEntity tacz$shooter = (LivingEntity)this;
    
    public final ShooterDataHolder tacz$data = new ShooterDataHolder();
    
    private final LivingEntityDrawGun tacz$draw;
    
    private final LivingEntityAim tacz$aim;
    
    private final LivingEntityCrawl tacz$crawl;
    
    private final LivingEntityAmmoCheck tacz$ammoCheck;
    
    private final LivingEntityFireSelect tacz$fireSelect;
    
    private final LivingEntityMelee tacz$melee;
    
    private final LivingEntityShoot tacz$shoot;
    
    private final LivingEntityBolt tacz$bolt;
    
    private final LivingEntityReload tacz$reload;
    
    private final LivingEntitySpeedModifier tacz$speed;

    private final LivingEntitySprint tacz$sprint;

    private boolean drawn = false;
    
    public void draw(Supplier<ItemStack> gunItemSupplier) {
        this.tacz$draw.draw(gunItemSupplier);
        this.drawn = true;
    }
    
    public void reload() {
        this.tacz$reload.reload();
        this.isReloading = true;
    }

    public void aim(boolean isAim) {
        this.tacz$aim.aim(isAim);
    }

    public void crawl(boolean isCrawl) {
        this.tacz$crawl.crawl(isCrawl);
    }

    public void updateCacheProperty(AttachmentCacheProperty cacheProperty) {
        this.tacz$data.cacheProperty = cacheProperty;
    }

    @Nullable
    public AttachmentCacheProperty getCacheProperty() {
        return this.tacz$data.cacheProperty;
    }

    
    public void fireSelect() {
        this.tacz$fireSelect.fireSelect();
    }

    
    public void zoom() {
        this.tacz$aim.zoom();
    }

    public long getSynShootCoolDown() {
        return (Long)ModSyncedEntityData.SHOOT_COOL_DOWN_KEY.getValue(this.tacz$shooter);
    }

    public long getSynMeleeCoolDown() {
        return (Long)ModSyncedEntityData.MELEE_COOL_DOWN_KEY.getValue(this.tacz$shooter);
    }

    public long getSynDrawCoolDown() {
        return (Long)ModSyncedEntityData.DRAW_COOL_DOWN_KEY.getValue(this.tacz$shooter);
    }

    public boolean getSynIsBolting() {
        return (Boolean)ModSyncedEntityData.IS_BOLTING_KEY.getValue(this.tacz$shooter);
    }

    public ReloadState getSynReloadState() {
        return (ReloadState)ModSyncedEntityData.RELOAD_STATE_KEY.getValue(this.tacz$shooter);
    }

    public float getSynAimingProgress() {
        return (Float)ModSyncedEntityData.AIMING_PROGRESS_KEY.getValue(this.tacz$shooter);
    }

    public float getSynSprintTime() {
        return (Float)ModSyncedEntityData.SPRINT_TIME_KEY.getValue(this.tacz$shooter);
    }

    public boolean getSynIsAiming() {
        return (Boolean)ModSyncedEntityData.IS_AIMING_KEY.getValue(this.tacz$shooter);
    }

    public void initialData() {
        this.tacz$data.initialData();
        AttachmentPropertyManager.postChangeEvent(this.tacz$shooter, this.tacz$shooter.getMainHandItem());
    }

    public void bolt() {
        this.tacz$bolt.bolt();
    }

    public void cancelReload() {
        this.tacz$reload.cancelReload();
    }

    public void melee() {
        this.tacz$melee.melee();
    }

    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw) {
        return this.shoot(pitch, yaw, System.currentTimeMillis() - this.tacz$data.baseTimestamp);
    }

    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp) {
        this.shootTimestamp = System.currentTimeMillis();
        return this.tacz$shoot.shoot(pitch, yaw, timestamp);
    }

    public boolean needCheckAmmo() {
        return false;
    }

    public boolean consumesAmmoOrNot() {
        return this.tacz$ammoCheck.consumesAmmoOrNot();
    }

    @Unique
    public boolean getProcessedSprintStatus(boolean sprint) {
        return this.tacz$sprint.getProcessedSprintStatus(sprint);
    }

    public ShooterDataHolder getDataHolder() {
        return this.tacz$data;
    }

    public boolean nextBulletIsTracer(int tracerCountInterval) {
        ++this.tacz$data.shootCount;
        if (tracerCountInterval == -1) {
            return false;
        } else {
            return this.tacz$data.shootCount % (tracerCountInterval + 1) == 0;
        }
    }
    private void onTickServerSide() {
        if (!this.level().isClientSide()) {
            if(this.getMainHandItem().getItem() instanceof ModernKineticGunItem gun) {
                if(!drawn) {
                    this.draw(this::getMainHandItem);
                }
                ItemStack gunItem = this.getMainHandItem();
                ResourceLocation gunId = gun.getGunId(gunItem);
                IGun iGun = IGun.getIGunOrNull(gunItem);
                LivingEntity shooter = this;
                if (iGun != null) {
                    Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
                    if (!gunIndexOptional.isEmpty()) {
                        CommonGunIndex gunIndex = (CommonGunIndex) gunIndexOptional.get();
                        BulletData bulletData = gunIndex.getBulletData();
                        GunData gunData = gunIndex.getGunData();
                        AttachmentCacheProperty property = new AttachmentCacheProperty();
                        property.eval(this.getMainHandItem(), gunData);
                        updateCacheProperty(property);
                    }
                }

            }
            this.bolt();
            ReloadState reloadState = this.tacz$reload.tickReloadState();
            this.tacz$aim.tickAimingProgress();
            this.tacz$aim.tickSprint();
            this.tacz$crawl.tickCrawling();
            this.tacz$bolt.tickBolt();
            this.tacz$melee.scheduleTickMelee();
            this.tacz$speed.updateSpeedModifier();
            this.tacz$shooter.setSprinting(this.getProcessedSprintStatus(this.tacz$shooter.isSprinting()));
            ModSyncedEntityData.SHOOT_COOL_DOWN_KEY.setValue(this.tacz$shooter, this.tacz$shoot.getShootCoolDown());
            ModSyncedEntityData.MELEE_COOL_DOWN_KEY.setValue(this.tacz$shooter, this.tacz$melee.getMeleeCoolDown());
            ModSyncedEntityData.DRAW_COOL_DOWN_KEY.setValue(this.tacz$shooter, this.tacz$draw.getDrawCoolDown());
            ModSyncedEntityData.IS_BOLTING_KEY.setValue(this.tacz$shooter, this.tacz$data.isBolting);
            ModSyncedEntityData.RELOAD_STATE_KEY.setValue(this.tacz$shooter, reloadState);
            ModSyncedEntityData.AIMING_PROGRESS_KEY.setValue(this.tacz$shooter, this.tacz$data.aimingProgress);
            ModSyncedEntityData.IS_AIMING_KEY.setValue(this.tacz$shooter, this.tacz$data.isAiming);
            ModSyncedEntityData.SPRINT_TIME_KEY.setValue(this.tacz$shooter, this.tacz$data.sprintTimeS);
        }
    }
}
