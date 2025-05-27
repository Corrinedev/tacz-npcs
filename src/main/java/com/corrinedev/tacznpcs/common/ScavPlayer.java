package com.corrinedev.tacznpcs.common;

import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import com.corrinedev.tacznpcs.common.entity.BanditEntity;
import com.corrinedev.tacznpcs.common.entity.DutyEntity;
import com.corrinedev.tacznpcs.common.entity.behavior.TaczShootAttack;
import com.mojang.authlib.GameProfile;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.item.ModernKineticGunItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.StrafeTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.*;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScavPlayer extends ServerPlayer {
    public InternalPathfinder pathfinder = null;
    private boolean reloading;

    public ScavPlayer(ServerLevel pLevel, Vec3 pos, boolean loadFromPathfinder) {
        super(pLevel.getServer(), pLevel, new GameProfile(UUID.randomUUID(), ""));
        this.connection = new ServerGamePacketListenerImpl(pLevel.getServer(), new FakeClientConnection(PacketFlow.CLIENTBOUND), this);
        var i = Math.random();

        var gunItem = new ItemStack(ModItems.MODERN_KINETIC_GUN.get());
        gunItem.getOrCreateTag().putString("GunId", i > 0.5 ? "tacz:m4a1" : "tacz:glock_17");
        this.setItemInHand(InteractionHand.MAIN_HAND, gunItem);
        this.setGameMode(GameType.SURVIVAL);
        this.setPos(pos);
        if(!loadFromPathfinder) {
            this.pathfinder = new InternalPathfinder(pLevel, this);
            pathfinder.setPos(pos);
            pLevel.addFreshEntity(this.pathfinder);
        }
    }

    public boolean shouldShowName() {
        return false;
    }

    public static ScavPlayer addScavPlayer(ServerLevel level, Vec3 pos) {
        var scav = new ScavPlayer(level, pos, false);
        level.getServer().getPlayerList().placeNewPlayer(new FakeClientConnection(PacketFlow.SERVERBOUND), scav);
        level.addFreshEntity(scav);
        return scav;
    }

    @Override
    public void tick() {
        super.baseTick();
        var op = IGunOperator.fromLivingEntity(this);
        //this.pathfinder.tick();
        if(Math.abs(this.getDeltaMovement().x) + Math.abs(this.getDeltaMovement().z) > 0.15) {
            this.setSprinting(true);
        } else {
            this.setSprinting(false);
        }
        updateUsingItem(this.getMainHandItem());
        var entity = this.level().getNearestPlayer(this, 32);
        if(
                !op.getDataHolder().reloadStateType.isReloading()
                        && !op.getSynIsBolting()
                        && this.getMainHandItem().getItem() instanceof ModernKineticGunItem gun
                        && entity != null
        ) {
                op.aim(true);
                var result = op.shoot(() -> this.getViewXRot(1f) + (random.nextInt(-15, 15)), () -> this.getViewYRot(1f) + random.nextInt(-15, 15));
                System.out.println(result);
                if (result == ShootResult.NEED_BOLT) op.bolt();
                //if (result == ShootResult.IS_RELOADING) op.cancelReload();
                if (result == ShootResult.NOT_DRAW) op.draw(this::getMainHandItem);
        } else {
            op.aim(false);
        }
        if(this.getMainHandItem().getItem() instanceof ModernKineticGunItem gun) {
            if(!op.getDataHolder().reloadStateType.isReloading() && gun.canReload(this, this.getMainHandItem())) {
                op.reload();
            }
        }
        if (!this.level().isClientSide) {
            int i = this.getArrowCount();
            if (i > 0) {
                if (this.removeArrowTime <= 0) {
                    this.removeArrowTime = 20 * (30 - i);
                }

                --this.removeArrowTime;
                if (this.removeArrowTime <= 0) {
                    this.setArrowCount(i - 1);
                }
            }

            int j = this.getStingerCount();
            if (j > 0) {
                if (this.removeStingerTime <= 0) {
                    this.removeStingerTime = 20 * (30 - j);
                }

                --this.removeStingerTime;
                if (this.removeStingerTime <= 0) {
                    this.setStingerCount(j - 1);
                }
            }

            this.detectEquipmentUpdates();
            if (this.tickCount % 20 == 0) {
                this.getCombatTracker().recheckStatus();
            }

        }

        if (!this.isRemoved()) {
            this.aiStep();
            this.invulnerableTime--;
        } else {
            if(!this.level().isClientSide)
                this.getServer().getPlayerList().remove(this);
        }
        for(ItemEntity item : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(2.0D))) {
            if(item.getItem().getItem() instanceof ModernKineticGunItem) {
                this.setItemInHand(InteractionHand.MAIN_HAND, item.getItem());
                item.discard();
            }
        }

        double d1 = this.getX() - this.xo;
        double d0 = this.getZ() - this.zo;
        float f = (float)(d1 * d1 + d0 * d0);
        float f1 = this.yBodyRot;
        float f2 = 0.0F;
        this.oRun = this.run;
        float f3 = 0.0F;
        if (f > 0.0025000002F) {
            f3 = 1.0F;
            f2 = (float)Math.sqrt(f) * 3.0F;
            float f4 = (float) Mth.atan2(d0, d1) * (180F / (float)Math.PI) - 90.0F;
            float f5 = Mth.abs(Mth.wrapDegrees(this.getYRot()) - f4);
            if (95.0F < f5 && f5 < 265.0F) {
                f1 = f4 - 180.0F;
            } else {
                f1 = f4;
            }
        }

        if (this.attackAnim > 0.0F) {
            f1 = this.getYRot();
        }

        if (!this.onGround()) {
            f3 = 0.0F;
        }

        this.run += (f3 - this.run) * 0.3F;
        this.level().getProfiler().push("headTurn");
        f2 = this.tickHeadTurn(f1, f2);
        this.level().getProfiler().pop();
        this.level().getProfiler().push("rangeChecks");

        while(this.getYRot() - this.yRotO < -180.0F) {
            this.yRotO -= 360.0F;
        }

        while(this.getYRot() - this.yRotO >= 180.0F) {
            this.yRotO += 360.0F;
        }

        while(this.yBodyRot - this.yBodyRotO < -180.0F) {
            this.yBodyRotO -= 360.0F;
        }

        while(this.yBodyRot - this.yBodyRotO >= 180.0F) {
            this.yBodyRotO += 360.0F;
        }

        while(this.getXRot() - this.xRotO < -180.0F) {
            this.xRotO -= 360.0F;
        }

        while(this.getXRot() - this.xRotO >= 180.0F) {
            this.xRotO += 360.0F;
        }

        while(this.yHeadRot - this.yHeadRotO < -180.0F) {
            this.yHeadRotO -= 360.0F;
        }

        while(this.yHeadRot - this.yHeadRotO >= 180.0F) {
            this.yHeadRotO += 360.0F;
        }

        this.level().getProfiler().pop();
        this.animStep += f2;
        if (this.isFallFlying()) {
            ++this.fallFlyTicks;
        } else {
            this.fallFlyTicks = 0;
        }

        if (this.isSleeping()) {
            this.setXRot(0.0F);
        }

    }

    @Override
    public boolean isInvulnerable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource pSource) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (!net.minecraftforge.common.ForgeHooks.onLivingAttack(this, pSource, pAmount)) return false;
        if (this.isInvulnerableTo(pSource)) {
            return false;
        } else if (this.level().isClientSide) {
            return false;
        } else if (this.isDeadOrDying()) {
            return false;
        } else if (pSource.is(DamageTypeTags.IS_FIRE) && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        } else {
            if (this.isSleeping() && !this.level().isClientSide) {
                this.stopSleeping();
            }
            if(pSource.getDirectEntity() instanceof Player plr) this.pathfinder.attackers.add(plr);
            this.noActionTime = 0;
            float f = pAmount;
            boolean flag = false;
            float f1 = 0.0F;
            if (pAmount > 0.0F && this.isDamageSourceBlocked(pSource)) {
                net.minecraftforge.event.entity.living.ShieldBlockEvent ev = net.minecraftforge.common.ForgeHooks.onShieldBlock(this, pSource, pAmount);
                if(!ev.isCanceled()) {
                    if(ev.shieldTakesDamage()) this.hurtCurrentlyUsedShield(pAmount);
                    f1 = ev.getBlockedDamage();
                    pAmount -= ev.getBlockedDamage();
                    if (!pSource.is(DamageTypeTags.IS_PROJECTILE)) {
                        Entity entity = pSource.getDirectEntity();
                        if (entity instanceof LivingEntity livingentity) {
                            this.blockUsingShield(livingentity);
                        }
                    }

                    flag = pAmount <= 0;
                }
            }

            if (pSource.is(DamageTypeTags.IS_FREEZING) && this.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
                pAmount *= 5.0F;
            }

            this.walkAnimation.setSpeed(1.5F);
            boolean flag1 = true;
            if ((float)this.invulnerableTime > 10.0F && !pSource.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
                if (pAmount <= this.lastHurt) {
                    return false;
                }

                this.actuallyHurt(pSource, pAmount - this.lastHurt);
                this.lastHurt = pAmount;
                flag1 = false;
            } else {
                this.lastHurt = pAmount;
                this.invulnerableTime = 20;
                this.actuallyHurt(pSource, pAmount);
                this.hurtDuration = 10;
                this.hurtTime = this.hurtDuration;
            }

            if (pSource.is(DamageTypeTags.DAMAGES_HELMET) && !this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                this.hurtHelmet(pSource, pAmount);
                pAmount *= 0.75F;
            }

            Entity entity1 = pSource.getEntity();
            if (entity1 != null) {
                if (entity1 instanceof LivingEntity livingentity1) {
                    if (!pSource.is(DamageTypeTags.NO_ANGER)) {
                        this.setLastHurtByMob(livingentity1);
                    }
                }

                if (entity1 instanceof Player player1) {
                    this.lastHurtByPlayerTime = 100;
                    this.lastHurtByPlayer = player1;
                } else if (entity1 instanceof net.minecraft.world.entity.TamableAnimal tamableEntity) {
                    if (tamableEntity.isTame()) {
                        this.lastHurtByPlayerTime = 100;
                        LivingEntity livingentity2 = tamableEntity.getOwner();
                        if (livingentity2 instanceof Player player) {
                            this.lastHurtByPlayer = player;
                        } else {
                            this.lastHurtByPlayer = null;
                        }
                    }
                }
            }

            if (flag1) {
                if (flag) {
                    this.level().broadcastEntityEvent(this, (byte)29);
                } else {
                    this.level().broadcastDamageEvent(this, pSource);
                }

                if (!pSource.is(DamageTypeTags.NO_IMPACT) && (!flag || pAmount > 0.0F)) {
                    this.markHurt();
                }

                if (entity1 != null && !pSource.is(DamageTypeTags.IS_EXPLOSION)) {
                    double d0 = entity1.getX() - this.getX();

                    double d1;
                    for(d1 = entity1.getZ() - this.getZ(); d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
                        d0 = (Math.random() - Math.random()) * 0.01D;
                    }

                    this.knockback(0.4F, d0, d1);
                    if (!flag) {
                        this.indicateDamage(d0, d1);
                    }
                }
            }

            if (this.isDeadOrDying()) {
                    SoundEvent soundevent = this.getDeathSound();
                    if (flag1 && soundevent != null) {
                        this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
                    }

                    this.die(pSource);
            } else if (flag1) {
                this.playHurtSound(pSource);
            }

            boolean flag2 = !flag || pAmount > 0.0F;
            if (flag2) {
                this.lastDamageSource = pSource;
                this.lastDamageStamp = this.level().getGameTime();
            }

            if (this instanceof ServerPlayer) {
                CriteriaTriggers.ENTITY_HURT_PLAYER.trigger(this, pSource, f, pAmount, flag);
                if (f1 > 0.0F && f1 < 3.4028235E37F) {
                    this.awardStat(Stats.CUSTOM.get(Stats.DAMAGE_BLOCKED_BY_SHIELD), Math.round(f1 * 10.0F));
                }
            }

            if (entity1 instanceof ServerPlayer) {
                CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer)entity1, this, pSource, f, pAmount, flag);
            }

            return flag2;
        }
    }

    public static class InternalPathfinder extends PathfinderMob implements SmartBrainOwner<InternalPathfinder> {
        public static final EntityType<InternalPathfinder> TYPE = EntityType.Builder.of((EntityType.EntityFactory<InternalPathfinder>) InternalPathfinder::new, MobCategory.MISC).sized(0.6f, 1.8f).noSummon().build("internal_pathfinder");
        public final ScavPlayer attachedEntity;
        public final IGunOperator user;
        public boolean firing;
        public int collectiveShots;
        private LivingEntity currentAngerTarget;
        private ArrayList<Player> attackers = new ArrayList<>();
        private boolean panic = false;

        public InternalPathfinder(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
            attachedEntity = null;
            user = null;
        }
        
        public InternalPathfinder(Level pLevel, ScavPlayer pEntity) {
            super(TYPE, pLevel);
            attachedEntity = pEntity;
            user = IGunOperator.fromLivingEntity(pEntity);
            System.out.println("Creating pathfinder for " + pEntity.getName().getString());
        }

        @Override
        protected void customServerAiStep() {
            tickBrain(this);
        }

        @Override
        public boolean canBeHitByProjectile() {
            return false;
        }

        public void tick() {
            if(this.attachedEntity == null) this.discard();
            if(this.attachedEntity == null) return;
            if(this.attachedEntity.deathTime >= 20) this.discard();
            if(this.attachedEntity.isDeadOrDying()) return;
            super.tick();
            attachedEntity.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
            attachedEntity.setPose(this.getPose());
            attachedEntity.setYHeadRot(this.getYHeadRot());
            this.setHealth(attachedEntity.getHealth());
            //attachedEntity.lerpTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot(), 0, false);
        }

        protected Brain.@NotNull Provider<?> brainProvider() {
            return new SmartBrainProvider<>(this);
        }

        @Override
        public boolean isInvulnerable() {
            return true;
        }

        @Override
        public boolean hurt(DamageSource pSource, float pAmount) {
            this.attachedEntity.hurt(pSource, pAmount);
            return false;
        }

        //Prevent rendering
        public boolean shouldRender(double pX, double pY, double pZ) {return false;}
        public boolean shouldRenderAtSqrDistance(double pDistance) {return false;}

        @Override
        public List<? extends ExtendedSensor<? extends InternalPathfinder>> getSensors() {
            return ObjectArrayList.of(
                    new NearbyPlayersSensor<InternalPathfinder>().setPredicate((p, e) -> e.attackers.contains(p)),
                    new HurtBySensor<>(),
                    new NearbyLivingEntitySensor<InternalPathfinder>()
                            .setPredicate((target, entity) -> target == this.currentAngerTarget ||
                                    (target instanceof BanditEntity bandit && !bandit.deadAsContainer) ||
                                    (target instanceof Monster) ||
                                    target.getType().getCategory() == MobCategory.MONSTER));
        }

        @Override
        public BrainActivityGroup<? extends InternalPathfinder> getCoreTasks() {
            return BrainActivityGroup.coreTasks(new Behavior[]{
                    (new AvoidEntity<>()).noCloserThan(12).avoiding((entity) -> {
                        return entity == this.getTarget();
                    }),
                    new TargetOrRetaliate<InternalPathfinder>().isAllyIf((e, l) -> l instanceof InternalPathfinder).attackablePredicate(l -> l != null && this.hasLineOfSight(l)).alertAlliesWhen((m, e) -> e != null && m.hasLineOfSight(e)).runFor((e) -> 999),
                    //new SetRetaliateTarget<>().isAllyIf((e, l) -> l.getType() == e.getType()),
                    new Panic<>().setRadius(16).speedMod((e) -> 1.1f).startCondition((e) -> this.getHealth() <= 10).whenStopping((e) -> panic = false).whenStarting( (e)-> panic = true).stopIf((e) -> this.getTarget() == null || !this.getTarget().hasLineOfSight(this)).runFor((e) -> 20),
                    (new LookAtTarget<>()).runFor((entity) -> {
                        return RandomSource.create().nextInt(40, 300);
                    }), (new StrafeTarget<>()).speedMod(0.75f).strafeDistance(24).stopStrafingWhen((entity) -> {
                return this.getTarget() == null || !attachedEntity.getMainHandItem().is(ModItems.MODERN_KINETIC_GUN.get());
            }).startCondition((e) -> attachedEntity.getMainHandItem().is(ModItems.MODERN_KINETIC_GUN.get())), new MoveToWalkTarget<>()});
        }

        public BrainActivityGroup<? extends InternalPathfinder> getIdleTasks() {
            return BrainActivityGroup.idleTasks(new Behavior[]{new FirstApplicableBehaviour(new TargetOrRetaliate<>(),
                    new SetPlayerLookTarget<>(),
                    new SetRandomLookTarget<>()),
                    new OneRandomBehaviour((
                            new SetRandomWalkTarget<>()).speedModifier(1.0F),
                            (new Idle<>()).runFor((entity) -> RandomSource.create().nextInt(30, 60)))});
        }

        public BrainActivityGroup<? extends InternalPathfinder> getFightTasks() {
            return BrainActivityGroup.fightTasks(new Behavior[]{
                    new InvalidateAttackTarget<>(),
                    (new SetWalkTargetToAttackTarget<>()).startCondition((entity) -> {
                        return !attachedEntity.getMainHandItem().is(ModItems.MODERN_KINETIC_GUN.get());}),
                    new SetRetaliateTarget<>(),
                    new FirstApplicableBehaviour<>((new TaczShootAttack<>(64).stopIf((e) -> e.getTarget() instanceof AbstractScavEntity scav && scav.deadAsContainer).startCondition((x$0) -> {
                        return attachedEntity.getMainHandItem().is(ModItems.MODERN_KINETIC_GUN.get()) && !this.panic && this.collectiveShots <= 5;
                    })),
                            (new AnimatableMeleeAttack<InternalPathfinder>(0)).whenStarting((entity) -> {
                                this.setAggressive(true);
                            }).whenStopping((entity) -> {
                                this.setAggressive(false);
                            }))});
        }
    }
}
