package lumien.bloodmoon.config;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BloodmoonConfig
{
	private static ModConfig config;
	private static ForgeConfigSpec.Builder builder;
	public static General GENERAL;
	
	public static class General
	{
		public ForgeConfigSpec.BooleanValue NO_SLEEP = builder
				.comment("Whether players are not able to sleep on a bloodmoon")
				.define("NoSleep", true);

		public ForgeConfigSpec.BooleanValue VANISH = builder
				.comment("Whether monsters spawned by a bloodmoon should die at dawn")
				.define("Vanish", false);

		public ForgeConfigSpec.BooleanValue RESPECT_GAMERULE = builder
				.comment("Whether bloodmoons should respect the doMobSpawning gamerule")
				.define("RespectGamerule", true);

		public ForgeConfigSpec.BooleanValue SEND_MESSAGE = builder
				.comment("Whether all players in the overworld should receive a message when the bloodmoon starts")
				.define("SendMessage", true);
	}

	public static Appearance APPEARANCE;

	public static class Appearance
	{
		public ForgeConfigSpec.BooleanValue RED_MOON = builder
				.comment()
				.define("RedMoon", true);

		public ForgeConfigSpec.BooleanValue RED_SKY = builder
				.define("RedSky", true);

		public ForgeConfigSpec.BooleanValue RED_LIGHT = builder
				.define("RedLight", true);

		// this is a joke
		private boolean GREEN_LIGHT = false;

		public ForgeConfigSpec.BooleanValue BLACK_FOG = builder
				.define("BlackFog", true);
	}

	public static Schedule SCHEDULE;

	public static class Schedule
	{
		public ForgeConfigSpec.DoubleValue CHANCE = builder
				.comment("The chance of a bloodmoon occuring at the beginning of a night (0=Never;1=Every night;0.05=5% of all nights)")
				.defineInRange("Chance", 0.05, 0.0, 1.0);

		public ForgeConfigSpec.BooleanValue FULLMOON = builder
				.comment("Whether there should be a bloodmoon whenever there is a full moon")
				.define("Fullmoon", false);

		public ForgeConfigSpec.IntValue NTH_NIGHT = builder
				.comment("Every nth night there will be a bloodmoon (0 disables this, 1 would be every night, 2 every second night)")
				.defineInRange("NthNight", 0, 0, Integer.MAX_VALUE);
	}

	public static Spawning SPAWNING;

	public static class Spawning
	{
		public ForgeConfigSpec.IntValue SPAWN_SPEED = builder
				.comment("How much faster enemys spawn on a bloodmoon (0=Vanilla)")
				.defineInRange("SpawnSpeed", 4, 0, Integer.MAX_VALUE);

		public ForgeConfigSpec.IntValue SPAWN_LIMIT_MULT = builder
				.comment("With which number should the default entity limit be multiplicated on a blood moon")
				.defineInRange("SpawnLimitMultiplier", 4, 0, Integer.MAX_VALUE);

		public ForgeConfigSpec.IntValue SPAWN_RANGE = builder
				.comment("How close can enemys spawn next to the player on a bloodmoon in blocks? (Vanilla=24)")
				.defineInRange("SpawnRange", 2, 0, Integer.MAX_VALUE);

		public ForgeConfigSpec.IntValue SPAWN_DISTANCE = builder
				.comment("How close can enemys spawn next to the World Spawn (Vanilla=24)")
				.defineInRange("WorldSpawnDistance", 24, 0, Integer.MAX_VALUE);

		public ForgeConfigSpec.ConfigValue<List<? extends String>> SPAWN_WHITELIST = builder
				.comment("If this isn't empty only monsters which names are in this list will get spawned by the bloodmoon. (Example: \"minecraft:skeleton,minecraft:spider\")")
				.defineList("SpawnWhitelist", new ArrayList<>(), (a) -> a instanceof String && ResourceLocation.isValidResourceLocation(a.toString()));

		public ForgeConfigSpec.ConfigValue<List<? extends String>> SPAWN_BLACKLIST = builder
				.comment("Monsters which names are on this list won't get spawned by the bloodmoon (Has no effect when a whitelist is active). (Example: \"minecraft:skeleton,minecraft:spider\")")
				.defineList("SpawnBlacklist", new ArrayList<>(), (a) -> a instanceof String && ResourceLocation.isValidResourceLocation(a.toString()));
	}

	// Cache
	static HashMap<String, String> classToEntityNameMap = new HashMap<String, String>();

	public static void init() {}

	static {
		builder = new ForgeConfigSpec.Builder();

		builder.push("general");
		GENERAL = new General();
		builder.pop();

		builder.push("appearance");
		APPEARANCE = new Appearance();
		builder.pop();

		builder.push("schedule");
		SCHEDULE = new Schedule();
		builder.pop();

		builder.push("spawning");
		SPAWNING = new Spawning();
		builder.pop();

		config = ModLoadingContext.registerConfig("bloodmoon", ModConfig.Type.COMMON, builder.build());
	}

	public static boolean canSpawn(EntityType<?> entityType)
	{
		if (SPAWNING.SPAWN_WHITELIST.get().size() == 0)
		{
			if (SPAWNING.SPAWN_BLACKLIST.get().size() == 0)
			{
				return true;
			}
			else
			{
				String className = Registry.ENTITY_TYPE.getKey(entityType).toString();
				String entityName;
				
				if (classToEntityNameMap.containsKey(className))
				{
					entityName = classToEntityNameMap.get(className);
				}
				else
				{
					entityName = getEntityName(entityType);
					classToEntityNameMap.put(className, entityName);
				}

				for (int i = 0; i < SPAWNING.SPAWN_BLACKLIST.get().size(); i++)
				{
					if (SPAWNING.SPAWN_BLACKLIST.get().get(i).equals(entityName))
					{
						return false;
					}
				}

				return true;
			}
		}
		else
		{
			String className = Registry.ENTITY_TYPE.getKey(entityType).toString();
			String entityName;
			
			if (classToEntityNameMap.containsKey(className))
			{
				entityName = classToEntityNameMap.get(className);
			}
			else
			{
				entityName = getEntityName(entityType);
				classToEntityNameMap.put(className, entityName);
			}

			for (int i = 0; i < SPAWNING.SPAWN_WHITELIST.get().size(); i++)
			{
				if (SPAWNING.SPAWN_WHITELIST.get().get(i).equals(entityName))
				{
					return true;
				}
			}

			return false;
		}
	}

	public static String getEntityName(EntityType<?> entityType)
	{
		return Registry.ENTITY_TYPE.getKey(entityType).toString();
	}
}
