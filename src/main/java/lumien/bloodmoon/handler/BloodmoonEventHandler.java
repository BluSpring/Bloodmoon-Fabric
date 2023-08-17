package lumien.bloodmoon.handler;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import io.github.fabricators_of_create.porting_lib.event.client.FogEvents;
import io.github.fabricators_of_create.porting_lib.event.common.LivingEntityEvents;
import lumien.bloodmoon.Bloodmoon;
import lumien.bloodmoon.client.ClientBloodmoonHandler;
import lumien.bloodmoon.config.BloodmoonConfig;
import lumien.bloodmoon.server.BloodmoonHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public class BloodmoonEventHandler
{
	public BloodmoonEventHandler() {
		ServerWorldEvents.LOAD.register((server, world) -> {
			loadWorld(world);
		});

		LivingEntityEvents.DROPS_WITH_LEVEL.register(this::livingDrops);
		LivingEntityEvents.TICK.register(this::livingUpdate);
		EntitySleepEvents.ALLOW_SLEEPING.register(this::sleepInBed);
		PlayerEvent.PLAYER_JOIN.register(this::playerJoinedWorld);
		TickEvent.SERVER_LEVEL_POST.register(this::endWorldTick);

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			FogEvents.SET_COLOR.register((data, partialTick) -> {
				fogColor(data);
			});
		}
	}

	public void loadWorld(ServerLevel world)
	{
		if (!world.isClientSide && !world.dimensionType().hasFixedTime())
		{
			BloodmoonHandler.INSTANCE = world.getDataStorage().get(BloodmoonHandler::load, "Bloodmoon");

			if (BloodmoonHandler.INSTANCE == null)
			{
				BloodmoonHandler.INSTANCE = new BloodmoonHandler();
				BloodmoonHandler.INSTANCE.setDirty();
			}

			world.getDataStorage().set("Bloodmoon", BloodmoonHandler.INSTANCE);

			BloodmoonHandler.INSTANCE.updateClients(world.getServer());
		}
	}

	public boolean livingDrops(LivingEntity target, DamageSource source, Collection<ItemEntity> drops, int lootingLevel, boolean recentlyHit)
	{
		if (!target.level.isClientSide)
		{
			return !(source != DamageSource.OUT_OF_WORLD || !target.getExtraCustomData().getBoolean("bloodmoonSpawned"));
		}

		return false;
	}

	public void livingUpdate(LivingEntity entity)
	{
		if (BloodmoonConfig.GENERAL.VANISH.get() && BloodmoonHandler.INSTANCE != null && !entity.level.dimensionType().hasFixedTime() && !entity.level.isClientSide && !BloodmoonHandler.INSTANCE.isBloodmoonActive() && entity.level.getGameTime() % 20 == 0 && Math.random() <= 0.2f)
		{
			if (entity.getExtraCustomData().getBoolean("bloodmoonSpawned"))
			{
				entity.kill();
			}
		}
	}

	public Player.BedSleepingProblem sleepInBed(Player player, BlockPos pos)
	{
		if (BloodmoonHandler.INSTANCE != null && BloodmoonConfig.GENERAL.NO_SLEEP.get())
		{
			if (Bloodmoon.proxy.isBloodmoon())
			{
				player.displayClientMessage(Component.translatable("text.bloodmoon.nosleep").withStyle(ChatFormatting.RED), true);
				return Player.BedSleepingProblem.OTHER_PROBLEM;
			}
		}

		return null;
	}

	@Environment(EnvType.CLIENT)
	public void fogColor(FogEvents.ColorData event)
	{
		if (BloodmoonConfig.APPEARANCE.BLACK_FOG.get() && ClientBloodmoonHandler.INSTANCE.isBloodmoonActive())
		{
			event.setRed(Math.max(event.getRed() - ClientBloodmoonHandler.INSTANCE.fogRemove, 0));
			event.setGreen(Math.max(event.getGreen() - ClientBloodmoonHandler.INSTANCE.fogRemove, 0));
			event.setBlue(Math.max(event.getBlue() - ClientBloodmoonHandler.INSTANCE.fogRemove, 0));
		}
	}

	public void playerJoinedWorld(ServerPlayer player)
	{
		if (BloodmoonHandler.INSTANCE != null && !player.level.isClientSide())
		{
			BloodmoonHandler.INSTANCE.playerJoinedWorld(player);
		}
	}

	public void endWorldTick(ServerLevel level)
	{
		if (BloodmoonHandler.INSTANCE != null)
		{
			BloodmoonHandler.INSTANCE.endWorldTick(level);
		}
	}
}
