package lumien.bloodmoon.client;

import com.mojang.blaze3d.systems.RenderSystem;
import lumien.bloodmoon.config.BloodmoonConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class ClientBloodmoonHandler implements ClientModInitializer
{
	public static ClientBloodmoonHandler INSTANCE = new ClientBloodmoonHandler();

	public void onInitializeClient() {}

	boolean bloodmoonActive;

	final float sinMax = (float) (Math.PI / 12000d);

	float lightSub;
	public float fogRemove;
	float skyColorAdd;
	float moonColorRed;

	float d = 1f / 15000f;
	int difTime = 0;

	double sin;

	public ClientBloodmoonHandler()
	{
		bloodmoonActive = false;
		ClientTickEvents.END_CLIENT_TICK.register((client) -> {
			clientTick();
		});
		ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> {
			// force reset the colours when leaving
			setBloodmoon(false);
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		}));
	}

	public boolean isBloodmoonActive()
	{
		return bloodmoonActive;
	}

	public void setBloodmoon(boolean active)
	{
		this.bloodmoonActive = active;
	}

	public void moonColorHook()
	{
		if (isBloodmoonActive() && BloodmoonConfig.APPEARANCE.RED_MOON.get())
		{
			RenderSystem.setShaderColor(0.8f, 0, 0, 1);
		}
	}

	public Vec3 skyColorHook(Vec3 color)
	{
		if (isBloodmoonActive() && BloodmoonConfig.APPEARANCE.RED_SKY.get())
		{
			color.add(INSTANCE.skyColorAdd, 0, 0);
		}
		
		return color;
	}

	public int manipulateRed(int position, int originalValue)
	{
		return originalValue;
	}

	public int manipulateGreen(int position, int originalValue)
	{
		if (isBloodmoonActive() && BloodmoonConfig.APPEARANCE.RED_LIGHT.get())
		{
			int height = position / 16;

			if (height < 16)
			{
				float mod = 1F / 16F * height;
				originalValue -= mod * lightSub * (sin / 2f + 1);
				return Math.max(originalValue, 0);
			}
		}
		return originalValue;
	}

	public int manipulateBlue(int height, int originalValue)
	{
		if (isBloodmoonActive() && BloodmoonConfig.APPEARANCE.RED_LIGHT.get())
		{
			if (height < 16)
			{
				float mod = 1F / 16F * height;
				originalValue -= mod * lightSub * 2.3f;
				return Math.max(originalValue, 0);
			}
		}
		return originalValue;
	}

	public void clientTick()
	{
		if (isBloodmoonActive())
		{
			ClientLevel world = Minecraft.getInstance().level;
			LocalPlayer player = Minecraft.getInstance().player;
			if (world != null && player != null)
			{
				float difTime = (int) (world.getDayTime() % 24000) - 12000;
				sin = Math.sin(difTime * sinMax);
				lightSub = (float) (sin * 150f);
				skyColorAdd = (float) (sin * 0.1f);
				moonColorRed = (float) (sin * 0.7f);

				fogRemove = (float) (sin * d * 6000f);

				if (world.dimensionType().hasFixedTime())
				{
					bloodmoonActive = false;
				}
			}
			else if (bloodmoonActive)
			{
				bloodmoonActive = false;
			}
		}
	}
}
