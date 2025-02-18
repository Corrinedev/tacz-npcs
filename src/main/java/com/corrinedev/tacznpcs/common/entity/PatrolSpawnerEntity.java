package com.corrinedev.tacznpcs.common.entity;

import com.corrinedev.tacznpcs.NPCS;
import com.corrinedev.tacznpcs.common.Patrol;
import com.mojang.realmsclient.util.WorldGenerationInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public abstract class PatrolSpawnerEntity<E extends AbstractScavEntity> extends Entity {
    public PatrolSpawnerEntity(EntityType<?> pEntityType, Level pLevel, E entity) {
        super(pEntityType, pLevel);
        this.entity = entity;
    }

    public E entity;

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {

    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

        Patrol<E> patrol = new Patrol<>(this.level(), entity);

        patrol.patrolMembers.forEach((member) -> {
            BlockPos offsetPos = this.blockPosition().offset( random.nextInt(-6, 6), 0 , random.nextInt(-6, 6) );
            // Ensure the entity is spawned on top of a solid block
                while(level().getBlockState(offsetPos).isCollisionShapeFullBlock(level(), offsetPos)) {
                    offsetPos = offsetPos.above();
                    if(offsetPos.getY() > 255) {
                        offsetPos = this.blockPosition();
                        NPCS.LOGGER.warn("COULD NOT SET BLOCKPOS");
                        break;
                    }
                }
            member.setPos(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ());
            this.level().addFreshEntity(member);
        });
        BlockPos newPos = this.blockPosition();
        while(level().getBlockState(this.blockPosition()).isCollisionShapeFullBlock(level(), this.blockPosition())) {
            newPos = this.blockPosition().above();
            if(newPos.getY() > 255) {
                newPos = this.blockPosition();
                NPCS.LOGGER.warn("COULD NOT SET BLOCKPOS");
                break;
            }
        }
        patrol.leader.setPos(this.blockPosition().getX(), newPos.getY(), this.blockPosition().getZ());
        this.level().addFreshEntity(patrol.leader);

        this.discard();
    }
}
