package lumien.bloodmoon;

import lumien.bloodmoon.config.BloodmoonConfig;
import lumien.bloodmoon.proxy.ClientProxy;
import lumien.bloodmoon.proxy.CommonProxy;
import lumien.bloodmoon.server.BloodmoonHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.monster.Enemy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Bloodmoon implements ModInitializer
{
	public static Bloodmoon instance;

	@Override
	public void onInitialize() {
		instance = this;

		BloodmoonConfig.init();

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
			proxy = new ClientProxy();
		else
			proxy = new CommonProxy();

		preInit();
		init();
		postInit();

		CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
			dispatcher.register(
					Commands.literal("bloodmoon")
							.requires((source) -> source.hasPermission(2))
							.then(
									Commands.literal("force")
											.executes((ctx) -> {
												BloodmoonHandler.INSTANCE.force();
												ctx.getSource().sendSuccess(Component.translatable("text.bloodmoon.force"), true);
												return 1;
											})
							)
							.then(
									Commands.literal("stop")
											.executes((ctx) -> {
												BloodmoonHandler.INSTANCE.stop(ctx.getSource().getServer());
												ctx.getSource().sendSuccess(Component.translatable("text.bloodmoon.stop"), true);
												return 1;
											})
							)
							.then(
									Commands.literal("entitynames")
											.executes((ctx) -> {
												Entity senderEntity = ctx.getSource().getEntityOrException();

												Set<String> names = new HashSet<String>();

												List<Entity> monsterNearby = senderEntity.level.getEntities(senderEntity, senderEntity.getBoundingBox().expandTowards(10, 10, 10), EntitySelector.NO_SPECTATORS);

												for (Entity e : monsterNearby)
												{
													if (e instanceof Enemy)
													{
														names.add(BloodmoonConfig.getEntityName(e.getType()));
													}
												}

												ctx.getSource().sendSuccess(Component.translatable("text.bloodmoon.entity"), true);

												for (String s : names)
												{
													ctx.getSource().sendSuccess(Component.literal(" - " + s), true);
												}

												return 1;
											})
							)
			);
		});
	}

	public static CommonProxy proxy;

	public void preInit()
	{
		proxy.preInit();
	}

	public void init()
	{
		proxy.init();
	}

	public void postInit()
	{
		proxy.postInit();
	}

}
