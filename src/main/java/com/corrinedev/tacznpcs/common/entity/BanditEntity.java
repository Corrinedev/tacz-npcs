package com.corrinedev.tacznpcs.common.entity;

import com.corrinedev.tacznpcs.Config;
import com.tacz.guns.item.ModernKineticGunItem;
import dev.kosmx.playerAnim.api.layered.AnimationStack;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraftforge.fml.ModList;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.corrinedev.tacznpcs.NPCS.MODID;

public class BanditEntity extends AbstractScavEntity {
    public static final EntityType<BanditEntity> BANDIT;
    static {
        BANDIT = EntityType.Builder.of(BanditEntity::new, MobCategory.MONSTER).sized(0.65f, 1.95f).build("bandit");
    }

    protected BanditEntity(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
        if(this.getServer() != null) {
            ObjectArrayList<ItemStack> stacks = this.getServer().getLootData().getLootTable(new ResourceLocation(MODID, "bandit")).getRandomItems(new LootParams.Builder(this.getServer().overworld()).create(LootContextParamSet.builder().build()));
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
                inventory.getItem(i).getOrCreateTag().putString("type","bandit");
                inventory.getItem(i).setHoverName(Component.literal( r.rank.toString() + " Bandit Patch"));
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
    public boolean allowInventory(Player player) {
        return this.deadAsContainer;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if(pSource.getEntity() instanceof BanditEntity) {
            return false;
        }
        panic = true;
        paniccooldown = 60;
        return super.hurt(pSource, pAmount);
    }
    @Override
    public List<? extends ExtendedSensor<? extends AbstractScavEntity>> getSensors() {
        return ObjectArrayList.of(
                new NearbyPlayersSensor<>(),
                new HurtBySensor<>(),
                new NearbyLivingEntitySensor<AbstractScavEntity>()
                        .setPredicate((target, entity) ->
                                target instanceof Player ||
                                        target instanceof IronGolem ||
                                        (target instanceof DutyEntity duty && !duty.deadAsContainer)||
                                        target instanceof Wolf ||
                                        (target instanceof AbstractVillager)));
    }
}
