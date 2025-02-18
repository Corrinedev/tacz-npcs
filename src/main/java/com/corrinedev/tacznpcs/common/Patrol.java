package com.corrinedev.tacznpcs.common;


import com.corrinedev.tacznpcs.Config;
import com.corrinedev.tacznpcs.common.entity.AbstractScavEntity;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;

import java.util.HashSet;
import java.util.Set;

public class Patrol<T extends AbstractScavEntity> {
    public static String DUTY_NBT = "[banner_patterns=[{\"pattern\":\"border\",\"color\":\"black\"},{\"pattern\":\"circle\",\"color\":\"blue\"},{\"pattern\":\"flower\",\"color\":\"yellow\"}]]";
    public HashSet<T> patrolMembers = new HashSet<>();
    public T leader;
    public Level level;
    RandomSource ran = RandomSource.create();



    public Patrol(Level level, EntityType<T> type, boolean random) {
        if(random) {
            for (int i = 0; i < ran.nextInt(2, 8); i++) {
                this.patrolMembers.add(type.create(level));

            }
        } else {
            for (int i = 0; i < 5; i++) {
                this.patrolMembers.add(type.create(level));
            }
        }
        this.leader = patrolMembers.iterator().next();
        this.level = level;
    }

    public Patrol(Level level, EntityType<? extends T> type) {
        for (int i = 0; i < ran.nextInt(Config.PATROLMIN.get(), Config.PATROLMAX.get()); i++) {
            this.patrolMembers.add(type.create(level));
        }
        this.leader = patrolMembers.iterator().next();
        this.leader.isPatrolLeader = true;
        this.level = level;
    }

    public Patrol(Level level, T typeOfEntity) {
        for (int i = 0; i < ran.nextInt(Config.PATROLMIN.get(), Config.PATROLMAX.get()); i++) {
            this.patrolMembers.add((T) typeOfEntity.getType().create(level));
        }
        this.leader = patrolMembers.iterator().next();
        this.leader.isPatrolLeader = true;
        this.level = level;

        this.patrolMembers.forEach((member) -> {
            member.patrolLeader = this.leader;
        });
        BannerPattern.Builder pattern = new BannerPattern.Builder().addPattern(BannerPatterns.BORDER, DyeColor.BLACK).addPattern(BannerPatterns.CIRCLE_MIDDLE, DyeColor.BLUE).addPattern(BannerPatterns.FLOWER, DyeColor.YELLOW);
        ItemStack banner = Items.RED_BANNER.getDefaultInstance();
        System.out.println("BANNER - " + pattern.toListTag());
        banner.getOrCreateTag().put("banner_patterns", pattern.toListTag());
        this.leader.patrolLeaderBanner = banner;
    }

    public Patrol(Level level, EntityType<? extends T> type, boolean random, int from, int to) {
        if(random) {
            for (int i = 0; i < ran.nextInt(from, to); i++) {
                this.patrolMembers.add(type.create(level));
            }
        } else {
            for (int i = 0; i < 5; i++) {
                this.patrolMembers.add(type.create(level));
            }
        }
        this.leader = patrolMembers.iterator().next();
        this.level = level;
    }

    public Patrol (HashSet<T> patrolMembers) {
        this.patrolMembers = patrolMembers;
        this.leader = patrolMembers.iterator().next();
        this.leader.isPatrolLeader = true;
        this.level = leader.level();
    }

    public Patrol (HashSet<T> patrolMembers, T leader) {
        this.patrolMembers = patrolMembers;
        this.leader = leader;
        this.leader.isPatrolLeader = true;
        this.level = leader.level();
    }

}
