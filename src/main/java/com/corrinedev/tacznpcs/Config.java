package com.corrinedev.tacznpcs;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NPCS.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue NPCDAMAGE = BUILDER.comment("Damage multiplier for tacz npcs (doesn't affect player damage)").defineInRange("base multiplier", 1.0, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.DoubleValue NPCDAMAGEPLAYER = BUILDER.comment("Seperate damage multiplier for tacz npcs that affects only players").defineInRange("base player multiplier", 0.50, 0, Double.MAX_VALUE);
    public static final ForgeConfigSpec.BooleanValue DROPITEMS = BUILDER.comment("NPCs drop items?").define("dropitems", true);
    public static final ForgeConfigSpec.IntValue TICKITEMS = BUILDER.comment("Time (in ticks) before NPC bodies dissapear, default is 1.6 minutes or 2000 ticks").defineInRange("despawntime", 2000, 1, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue PATROLMIN = BUILDER.comment("Minimum patrol members to spawn").defineInRange("min", 3, 1, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue PATROLMAX = BUILDER.comment("Maximum patrol members to spawn").defineInRange("max", 8, 1, Integer.MAX_VALUE);
    //TACZ DURABILITY
    public static final ForgeConfigSpec.IntValue DURABILITYFROM = BUILDER.comment("The lowest durability value for guns to spawn with from TACZ Durability").defineInRange("from", 200, 0, Integer.MAX_VALUE);
    public static final ForgeConfigSpec.IntValue DURABILITYTO = BUILDER.comment("The highest durability value for guns to spawn with from TACZ Durability").defineInRange("to", 800, 0, Integer.MAX_VALUE);

    static final ForgeConfigSpec SPEC = BUILDER.build();
}
