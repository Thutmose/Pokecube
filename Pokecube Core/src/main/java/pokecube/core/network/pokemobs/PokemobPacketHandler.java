package pokecube.core.network.pokemobs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.mod_Pokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector3;

public class PokemobPacketHandler
{
	public static final byte MESSAGERETURN = 0;
	public static final byte MESSAGERENAME = 1;
	
	public static final byte MESSAGEMOVEUSE = 2;
	public static final byte MESSAGEMOVEMESSAGE = 3;
	public static final byte MESSAGEMOVESWAP = 4;
	public static final byte MESSAGEMOVEINDEX = 5;
	
	public static final byte MESSAGECHANGEFORM = 6;
	
	public static final byte MESSAGEALIVECHECK = 7;
	
	public static class MessageClient implements IMessage
	{
		PacketBuffer buffer;

		public MessageClient()
		{
		};

		public MessageClient(byte[] data)
		{
			this.buffer = new PacketBuffer(Unpooled.buffer());
			buffer.writeBytes(data);
		}

		public MessageClient(PacketBuffer buffer)
		{
			this.buffer = buffer;
		}

		public MessageClient(byte channel, NBTTagCompound nbt)
		{
			this.buffer = new PacketBuffer(Unpooled.buffer());
			buffer.writeByte(channel);
			buffer.writeNBTTagCompoundToBuffer(nbt);
			// System.out.println(buffer.array().length);
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			if (buffer == null)
			{
				buffer = new PacketBuffer(Unpooled.buffer());
			}
			buffer.writeBytes(buf);
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			if (buffer == null)
			{
				buffer = new PacketBuffer(Unpooled.buffer());
			}
			buf.writeBytes(buffer);
		}

		public static class MessageHandlerClient implements IMessageHandler<MessageClient, MessageServer>
		{
			public void handleClientSide(EntityPlayer player, PacketBuffer buffer)
			{
				byte channel = buffer.readByte();

				if(player==null)
				{
//					new NullPointerException("Packet recieved by null player").printStackTrace();
					System.out.println(Minecraft.getMinecraft().thePlayer+" "+channel);
					return;
				}
				
				if(channel == MESSAGEALIVECHECK)
				{
					int id = buffer.readInt();
					boolean alive = buffer.readBoolean();
					Entity e;
					if(!alive && (e = (Entity) PokecubeSerializer.getInstance().getPokemob(id))!=null)
					{
						e.setDead();
					}
					else if((e = (Entity) PokecubeSerializer.getInstance().getPokemob(id))!=null)
					{
                        Vector3 v = Vector3.readFromBuff(buffer);
						Vector3 v2 = Vector3.getNewVectorFromPool().set(e);
						if(v.distanceTo(v2)>e.width)
						{
							v.moveEntity(e);
						}
					}
				}
			}

			@Override
			public MessageServer onMessage(MessageClient message, MessageContext ctx)
			{
				handleClientSide(mod_Pokecube.getPlayer(null), message.buffer);
				return null;
			}
		}
	}

	public static class MessageServer implements IMessage
	{
		PacketBuffer buffer;

		public MessageServer()
		{
		};

		public MessageServer(byte[] data)
		{
			this.buffer = new PacketBuffer(Unpooled.buffer());
			buffer.writeBytes(data);
		}

		public MessageServer(PacketBuffer buffer)
		{
			this.buffer = buffer;
		}

		public MessageServer(byte channel, NBTTagCompound nbt)
		{
			this.buffer = new PacketBuffer(Unpooled.buffer());
			buffer.writeByte(channel);
			buffer.writeNBTTagCompoundToBuffer(nbt);
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			if (buffer == null)
			{
				buffer = new PacketBuffer(Unpooled.buffer());
			}
			buffer.writeBytes(buf);
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			if (buffer == null)
			{
				buffer = new PacketBuffer(Unpooled.buffer());
			}
			buf.writeBytes(buffer);
		}

		public static class MessageHandlerServer implements IMessageHandler<MessageServer, IMessage>
		{
			public void handleServerSide(EntityPlayer player, PacketBuffer buffer)
			{
				byte channel = buffer.readByte();
				
				if(channel == MESSAGEALIVECHECK)
				{
					int id = buffer.readInt();
					
					Vector3 v = Vector3.getNewVectorFromPool();
					
					Entity e;
					if((e = (Entity) PokecubeSerializer.getInstance().getPokemob(id))==null || e.isDead)
					{
						PacketBuffer ret = new PacketBuffer(Unpooled.buffer());
						ret.writeByte(MESSAGEALIVECHECK);
						ret.writeInt(id);
						ret.writeBoolean(false);
						MessageClient message = new MessageClient(ret);
						PokecubePacketHandler.sendToClient(message, player);
					}
					else
					{
						PacketBuffer ret = new PacketBuffer(Unpooled.buffer());
						ret.writeByte(MESSAGEALIVECHECK);
						ret.writeInt(id);
						ret.writeBoolean(true);
						v.set(e).writeToBuff(ret);
						MessageClient message = new MessageClient(ret);
						PokecubePacketHandler.sendToClient(message, player);
					}
					
					v.freeVectorFromPool();
				}

				if(channel == MESSAGERETURN)
				{
					int id = buffer.readInt();
					Entity mob = player.worldObj.getEntityByID(id);
					if(mob!=null && !mob.isDead && mob instanceof IPokemob)
					{
						((IPokemob)mob).returnToPokecube();
					}
				}
			}

			@Override
			public IMessage onMessage(MessageServer message, MessageContext ctx)
			{
				EntityPlayer player = ctx.getServerHandler().playerEntity;
				handleServerSide(player, message.buffer);
				return null;
			}

		}

	}

	public static MessageServer makeServerPacket(byte channel, byte[] data)
	{
		byte[] packetData = new byte[data.length + 1];
		packetData[0] = channel;

		for (int i = 1; i < packetData.length; i++)
		{
			packetData[i] = data[i - 1];
		}
		return new MessageServer(packetData);
	}

	public static MessageClient makeClientPacket(byte channel, byte[] data)
	{
		byte[] packetData = new byte[data.length + 1];
		packetData[0] = channel;

		for (int i = 1; i < packetData.length; i++)
		{
			packetData[i] = data[i - 1];
		}
		return new MessageClient(packetData);
	}
}
