package lumien.bloodmoon.network;

import lumien.bloodmoon.lib.Reference;
import lumien.bloodmoon.network.messages.MessageBloodmoonStatus;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.minecraft.resources.ResourceLocation;

public class PacketHandler
{
	public static final SimpleChannel INSTANCE = new SimpleChannel(new ResourceLocation(Reference.MOD_ID, "channel"));
	
	public static void init()
	{
		INSTANCE.registerS2CPacket(MessageBloodmoonStatus.class, 0, MessageBloodmoonStatus::decode);
	}
}
