package com.corrinedev.tacznpcs.common.entity;

import com.corrinedev.tacznpcs.Config;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.item.ModernKineticGunItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Panic;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.AvoidEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.StrafeTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static com.corrinedev.tacznpcs.NPCS.MODID;

public class DutyEntity extends AbstractScavEntity {
    public static final EntityType<DutyEntity> DUTY;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private int remainingPersistentAngerTime;
    @javax.annotation.Nullable
    private UUID persistentAngerTarget;
    private LivingEntity currentAngerTarget;
    private static final int ALERT_RANGE_Y = 10;
    private static final UniformInt ALERT_INTERVAL = TimeUtil.rangeOfSeconds(4, 6);
    private boolean angry = false;
    static {
        DUTY = EntityType.Builder.of(DutyEntity::new, MobCategory.MISC).sized(0.65f, 1.95f).build("duty");
    }
    protected DutyEntity(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        if(this.getServer() != null) {
            ObjectArrayList<ItemStack> stacks = this.getServer().getLootData().getLootTable(new ResourceLocation(MODID, "duty")).getRandomItems(new LootParams.Builder(this.getServer().overworld()).create(LootContextParamSet.builder().build()));
            stacks.forEach((stack) -> {
                if(stack.getItem() instanceof ModernKineticGunItem) {
                    if(ModList.get().isLoaded("gundurability")) {
                        stack.getOrCreateTag().putInt("Durability", RandomSource.create().nextInt(Config.DURABILITYFROM.get(), Config.DURABILITYTO.get()));
                    }
                }
                if(stack.getMaxDamage() != 0) {
                    stack.setDamageValue(RandomSource.create().nextInt((int)stack.getMaxDamage() / 2, stack.getMaxDamage()));
                }
                inventory.addItem(stack);
            });
        }
        for(int i = 0; i < this.inventory.getContainerSize() - 1; i++) {
            if(inventory.getItem(i).getItem() instanceof PatchItem r) {
                this.setCustomName(Component.literal(r.rank.toString() + " " + this.getName().getString()));
                inventory.getItem(i).getOrCreateTag().putString("type","duty");
                inventory.getItem(i).setHoverName(Component.literal( r.rank.toString() + " Duty Patch"));
                switch (r.rank) {
                    case ROOKIE -> {

                    }
                    case EXPERIENCED -> {

                    }
                    case EXPERT -> {

                    }
                    case VETERAN -> {

                    }
                }
            }
        }
    }

    @Override
    public boolean isSlim() {
        return false;
    }

    @Override
    public boolean allowInventory(Player player) {
        if(this.deadAsContainer) {
            return true;
        }
        return lastHurtByPlayer != player;
    }
    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if(pSource.getEntity() instanceof DutyEntity) {
            return false;
        }
        this.panic = true;
        paniccooldown = 60;
        if(pSource.getEntity() instanceof LivingEntity entity) {
            this.currentAngerTarget = entity;
            List<DutyEntity> entities = this.level().getEntitiesOfClass(DutyEntity.class, AABB.ofSize(this.position(), 64, 16, 64));
            List<DutyEntity> filter1 = entities.stream().filter((e) -> e.hasLineOfSight(currentAngerTarget) || BehaviorUtils.entityIsVisible(e.getBrain(), currentAngerTarget)).toList();
            for (DutyEntity duty: filter1) {
                duty.setTarget(currentAngerTarget);
                duty.currentAngerTarget = entity;
                duty.brain.setMemory(MemoryModuleType.ATTACK_TARGET, currentAngerTarget);
            }
        }

        return super.hurt(pSource, pAmount);
    }
    @Override
    public List<? extends ExtendedSensor<? extends AbstractScavEntity>> getSensors() {
        return ObjectArrayList.of(
                new NearbyPlayersSensor<AbstractScavEntity>().setPredicate((p, e) -> e.attackers.contains(p)),
                new HurtBySensor<>(),
                new NearbyLivingEntitySensor<AbstractScavEntity>()
                        .setPredicate((target, entity) -> target == this.currentAngerTarget ||
                                (target instanceof BanditEntity bandit && !bandit.deadAsContainer) ||
                                        (target instanceof Monster && !(target instanceof BanditEntity)) ||
                                        target.getType().getCategory() == MobCategory.MONSTER));
    }

    @Override
    public void tick() {
        if(this.getTarget() == null && this.angry) {
            angry = false;
        }
        if(this.currentAngerTarget != null && this.currentAngerTarget instanceof AbstractScavEntity scav && scav.deadAsContainer) {
            this.setTarget(null);
            this.currentAngerTarget = null;
        }
        if(this.currentAngerTarget != null && !this.currentAngerTarget.isAlive()) {
            this.currentAngerTarget = null;
        }
        super.tick();
    }

    public void setTarget(@Nullable LivingEntity pLivingEntity) {
        if (this.getTarget() == null && pLivingEntity != null) {
            ALERT_INTERVAL.sample(this.random);
        }

        if (pLivingEntity instanceof Player) {
            this.setLastHurtByPlayer((Player)pLivingEntity);
        }

        super.setTarget(pLivingEntity);
    }

    @Override
    public BrainActivityGroup<? extends AbstractScavEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(new Behavior[]{
                (new AvoidEntity<>()).noCloserThan(12).avoiding((entity) -> {
                    return entity == this.getTarget();
                }),
                new TargetOrRetaliate<DutyEntity>().isAllyIf((e, l) -> l instanceof DutyEntity).attackablePredicate(l -> l != null && this.hasLineOfSight(l)).alertAlliesWhen((m, e) -> e != null && m.hasLineOfSight(e)).runFor((e) -> 999),
                //new SetRetaliateTarget<>().isAllyIf((e, l) -> l.getType() == e.getType()),
                new Panic<>().setRadius(16).speedMod((e) -> 1.1f).startCondition((e) -> this.getHealth() <= 10).whenStopping((e) -> panic = false).whenStarting( (e)-> panic = true).stopIf((e) -> (this.getTarget() != null && !this.getTarget().hasLineOfSight(this)) || this.getTarget() == null).runFor((e) -> 20),
                (new LookAtTarget<>()).runFor((entity) -> {
                    return RandomSource.create().nextInt(40, 300);
                }), (new StrafeTarget<>()).speedMod(0.75f).strafeDistance(24).stopStrafingWhen((entity) -> {
            return this.getTarget() == null || !this.getMainHandItem().is(ModItems.MODERN_KINETIC_GUN.get());
        }).startCondition((e) -> this.getMainHandItem().is(ModItems.MODERN_KINETIC_GUN.get())), new MoveToWalkTarget<>()});
    }
}
