package lumien.bloodmoon.network.messages;

import io.netty.buffer.ByteBuf;
import lumien.bloodmoon.client.ClientBloodmoonHandler;
import me.pepperbell.simplenetworking.S2CPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;

public class MessageBloodmoonStatus implements S2CPacket
{
	public boolean bloodmoonActive;

	public MessageBloodmoonStatus(boolean bloodMoon)
	{
		this.bloodmoonActive = bloodMoon;
	}

	public MessageBloodmoonStatus()
	{

	}

	public void fromBytes(ByteBuf buf)
	{
		bloodmoonActive = buf.readBoolean();
	}

	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(bloodmoonActive);
	}

	public MessageBloodmoonStatus setStatus(boolean active)
	{
		this.bloodmoonActive = active;
		return this;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void handle(Minecraft client, ClientPacketListener listener, PacketSender responseSender, SimpleChannel channel) {
		client.doRunTask(() -> {
			ClientBloodmoonHandler.INSTANCE.setBloodmoon(this.bloodmoonActive);
		});
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		toBytes(buf);
	}

	public static MessageBloodmoonStatus decode(FriendlyByteBuf buf) {
		var status = new MessageBloodmoonStatus();
		status.fromBytes(buf);
		return status;
	}
}
