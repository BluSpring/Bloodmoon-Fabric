package lumien.bloodmoon.proxy;

import lumien.bloodmoon.handler.BloodmoonEventHandler;
import lumien.bloodmoon.network.PacketHandler;
import lumien.bloodmoon.server.BloodmoonHandler;

public class CommonProxy
{
	public void preInit()
	{
		BloodmoonEventHandler handler = new BloodmoonEventHandler();

		PacketHandler.init();
	}

	public void init()
	{

	}

	public void postInit()
	{

	}

	public boolean isBloodmoon()
	{
		if (BloodmoonHandler.INSTANCE == null)
		{
			return false;
		}
		else
		{
			return BloodmoonHandler.INSTANCE.isBloodmoonActive();
		}
	}
}
