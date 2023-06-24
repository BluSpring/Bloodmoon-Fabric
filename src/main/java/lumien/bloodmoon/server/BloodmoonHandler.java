package lumien.bloodmoon.server;

import lumien.bloodmoon.config.BloodmoonConfig;
import lumien.bloodmoon.network.PacketHandler;
import lumien.bloodmoon.network.messages.MessageBloodmoonStatus;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.saveddata.SavedData;

public class BloodmoonHandler extends SavedData
{
	public static BloodmoonHandler INSTANCE;

	private BloodmoonSpawner bloodMoonSpawner;

	boolean bloodMoon;
	boolean forceBloodMoon;

	int nightCounter;

	public BloodmoonHandler()
	{
		super();
		bloodMoonSpawner = new BloodmoonSpawner();
		bloodMoon = false;
		forceBloodMoon = false;
	}

	public BloodmoonHandler(String name)
	{
		super();
		bloodMoonSpawner = new BloodmoonSpawner();
		bloodMoon = false;
		forceBloodMoon = false;
	}

	public void playerJoinedWorld(ServerPlayer player)
	{
		if (!player.level.isClientSide())
		{
			if (bloodMoon)
			{
				PacketHandler.INSTANCE.sendToClient(new MessageBloodmoonStatus(bloodMoon), player);
			}
		}
	}

	public void endWorldTick(ServerLevel world)
	{
		{
			if (!world.dimensionType().hasFixedTime())
			{
				int time = (int) (world.getDayTime() % 24000);
				if (isBloodmoonActive())
				{
					if (!BloodmoonConfig.GENERAL.RESPECT_GAMERULE.get() || world.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING))
					{
						for (int i = 0; i < BloodmoonConfig.SPAWNING.SPAWN_SPEED.get(); i++)
						{
							bloodMoonSpawner.findChunksForSpawning(world, world.getDifficulty() != Difficulty.PEACEFUL, false, false);
						}
					}

					if (time >= 0 && time < 12000)
					{
						setBloodmoon(false, world.getServer());
					}
				}
				else
				{
					if (time == 12000)
					{
						if (BloodmoonConfig.SCHEDULE.NTH_NIGHT.get() != 0)
						{
							nightCounter--;

							if (nightCounter < 0)
							{
								nightCounter = BloodmoonConfig.SCHEDULE.NTH_NIGHT.get();
							}

							this.setDirty();
						}

						if (forceBloodMoon || Math.random() < BloodmoonConfig.SCHEDULE.CHANCE.get() || (BloodmoonConfig.SCHEDULE.FULLMOON.get() && world.getMoonBrightness() == 1.0F) || (BloodmoonConfig.SCHEDULE.NTH_NIGHT.get() != 0 && nightCounter == 0))
						{
							forceBloodMoon = false;
							setBloodmoon(true, world.getServer());

							if (BloodmoonConfig.GENERAL.SEND_MESSAGE.get())
							{
								for (ServerPlayer player : world.players())
								{
									player.sendSystemMessage(Component.translatable("text.bloodmoon.notify", new Object[0]).withStyle(ChatFormatting.RED), true);
								}
							}

							if (nightCounter == 0 && BloodmoonConfig.SCHEDULE.NTH_NIGHT.get() != 0)
							{
								nightCounter = BloodmoonConfig.SCHEDULE.NTH_NIGHT.get();
								this.setDirty();
							}
						}
					}
				}
			}
		}
	}

	private void setBloodmoon(boolean bloodMoon, MinecraftServer server)
	{
		if (this.bloodMoon != bloodMoon)
		{
			PacketHandler.INSTANCE.sendToClientsInWorld(new MessageBloodmoonStatus(bloodMoon), server.overworld());
			this.setDirty();
		}
		this.bloodMoon = bloodMoon;
	}

	public void updateClients(MinecraftServer server)
	{
		PacketHandler.INSTANCE.sendToClientsInWorld(new MessageBloodmoonStatus(bloodMoon), server.overworld());
	}

	public void force()
	{
		forceBloodMoon = true;
		this.setDirty();
	}

	public boolean isBloodmoonActive()
	{
		return bloodMoon;
	}

	public static BloodmoonHandler load(CompoundTag nbt)
	{
		var handler = new BloodmoonHandler();
		handler.bloodMoon = nbt.getBoolean("bloodMoon");
		handler.forceBloodMoon = nbt.getBoolean("forceBloodMoon");
		handler.nightCounter = nbt.getInt("nightCounter");
		return handler;
	}

	@Override
	public CompoundTag save(CompoundTag nbt)
	{
		nbt.putBoolean("bloodMoon", bloodMoon);
		nbt.putBoolean("forceBloodMoon", forceBloodMoon);
		nbt.putInt("nightCounter", nightCounter);

		return nbt;
	}

	public boolean isBloodmoonScheduled()
	{
		return forceBloodMoon;
	}

	public void stop(MinecraftServer server)
	{
		setBloodmoon(false, server);
	}
}