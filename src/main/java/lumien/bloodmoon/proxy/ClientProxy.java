package lumien.bloodmoon.proxy;

import lumien.bloodmoon.client.ClientBloodmoonHandler;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		super.preInit();
	}

	@Override
	public boolean isBloodmoon()
	{
		return ClientBloodmoonHandler.INSTANCE.isBloodmoonActive();
	}
}
