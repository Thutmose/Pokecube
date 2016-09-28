package pokecube.core.network.packets;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class PacketSyncTerrain implements IMessage, IMessageHandler<PacketSyncTerrain, IMessage>
{
    public static void sendTerrainEffects(Entity player, int x, int y, int z, PokemobTerrainEffects terrain)
    {
        PacketSyncTerrain packet = new PacketSyncTerrain();
        packet.type = EFFECTS;
        packet.x = x;
        packet.y = y;
        packet.z = z;
        for (int i = 0; i < 16; i++)
            packet.effects[i] = terrain.effects[i];
        PokecubeMod.packetPipeline.sendToAllAround(packet,
                new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 64));
    }

    public static void sendTerrain(Entity player, int x, int y, int z, TerrainSegment terrain)
    {
        PacketSyncTerrain packet = new PacketSyncTerrain();
        packet.type = TERRAIN;
        packet.x = x;
        packet.y = y;
        packet.z = z;
        packet.data.setInteger("dimID", player.dimension);
        
        terrain.saveToNBT(packet.data);
        PokecubeMod.packetPipeline.sendToAllAround(packet,
                new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 64));
    }

    public static final byte TERRAIN = 0;
    public static final byte EFFECTS = 1;

    public PacketSyncTerrain()
    {
    }

    int            x;
    int            y;
    int            z;
    byte           type;
    long[]         effects = new long[16];
    NBTTagCompound data    = new NBTTagCompound();

    @Override
    public IMessage onMessage(final PacketSyncTerrain message, final MessageContext ctx)
    {
        PokecubeCore.proxy.getMainThreadListener().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                processMessage(ctx, message);
            }
        });
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        type = buf.readByte();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        if (type == EFFECTS) for (int i = 0; i < 16; i++)
        {
            effects[i] = buf.readLong();
        }
        else if (type == TERRAIN)
        {
            PacketBuffer buffer = new PacketBuffer(buf);
            try
            {
                data = buffer.readNBTTagCompoundFromBuffer();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(type);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        if (type == EFFECTS) for (int i = 0; i < 16; i++)
        {
            buf.writeLong(effects[i]);
        }
        else if (type == TERRAIN)
        {
            PacketBuffer buffer = new PacketBuffer(buf);
            buffer.writeNBTTagCompoundToBuffer(data);
        }
    }

    void processMessage(MessageContext ctx, PacketSyncTerrain message)
    {
        EntityPlayer player;
        player = PokecubeCore.getPlayer(null);
        TerrainSegment t = TerrainManager.getInstance().getTerrain(player.worldObj).getTerrain(message.x, message.y,
                message.z);

        if (message.type == EFFECTS)
        {
            PokemobTerrainEffects effect = (PokemobTerrainEffects) t.geTerrainEffect("pokemobEffects");
            if (effect == null)
            {
                t.addEffect(effect = new PokemobTerrainEffects(), "pokemobEffects");
            }
            for (int i = 0; i < 16; i++)
            {
                effect.effects[i] = message.effects[i];
            }
        }
        else if (message.type == TERRAIN)
        {
            TerrainSegment.readFromNBT(t, message.data);
            
            TerrainManager.getInstance().getTerrain(message.data.getInteger("dimID")).addTerrain(t);
        }
    }
}
