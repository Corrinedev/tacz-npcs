package com.corrinedev.tacznpcs.common.entity.behavior;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class Navigator<E extends LivingEntity & Navigator.NavigatingEntity<E>> {
    public final E entity;
    PathfinderMob internalPathfinder;
    public Navigator(E entity) {
        this.entity = entity;
        internalPathfinder = createPathfinder(entity);
    }

    private static
    <E extends LivingEntity & Navigator.NavigatingEntity<E>>
    PathfinderMob createPathfinder(E entity) {
        PathfinderMob mob = new Zombie(entity.level()) {
            protected void registerGoals() {
                super.registerGoals();
            }
        };
        return mob;
    }

    public void moveTo(double x, double y, double z) {
        System.out.println("Moving to: " + x + ", " + y + ", " + z);
        entity.lerpMotion(x, y, z);
    }
    public void moveTo(LivingEntity target, double speed, double stayDistance) {

    }

    /** Returns true if the entity is currently pathfinding */

    public boolean tick() {
        if(entity.getTarget() == null) return false;
        Vec3 directionToTarget = entity.getTarget().position().subtract(entity.position());
        double distanceToTarget = directionToTarget.length();
        internalPathfinder.tick();
        if (distanceToTarget > 0) {
            internalPathfinder.setTarget(entity.getTarget());
            this.entity.lerpMotion(internalPathfinder.getDeltaMovement().x,
                    internalPathfinder.getDeltaMovement().y,
                    internalPathfinder.getDeltaMovement().z);
            return true;
        }

        return false;
    }

    public interface NavigatingEntity<E extends LivingEntity & NavigatingEntity<E>> {
        Navigator<E> getNavigator();
        LivingEntity getTarget();
    }
}
