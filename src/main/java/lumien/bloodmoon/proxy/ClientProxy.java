package lumien.bloodmoon.proxy;

import lumien.bloodmoon.client.ClientBloodmoonHandler;
import lumien.bloodmoon.network.PacketHandler;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		super.preInit();
	}

	@Override
	public void init() {
		super.init();
		PacketHandler.INSTANCE.initClientListener();
	}

	@Override
	public boolean isBloodmoon()
	{
		return ClientBloodmoonHandler.INSTANCE.isBloodmoonActive();
	}
}
