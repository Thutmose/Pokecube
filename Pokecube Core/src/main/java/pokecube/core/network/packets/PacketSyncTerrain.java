package pokecube.core.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
    public static void sendTerrain(Entity player, int x, int y, int z, PokemobTerrainEffects terrain)
    {
        PacketSyncTerrain packet = new PacketSyncTerrain();
        packet.x = x;
        packet.y = y;
        packet.z = z;
        for (int i = 0; i < 16; i++)
            packet.effects[i] = terrain.effects[i];
        PokecubeMod.packetPipeline.sendToAllAround(packet,
                new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 64));
    }

    public PacketSyncTerrain()
    {
    }

    int    x;
    int    y;
    int    z;
    long[] effects = new long[16];

    @Override
    public IMessage onMessage(final PacketSyncTerrain message, final MessageContext ctx)
    {
        PokecubeCore.proxy.getMainThreadListener().addScheduledTask(new Runnable()
        {
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
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        for (int i = 0; i < 16; i++)
        {
            effects[i] = buf.readLong();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        for (int i = 0; i < 16; i++)
        {
            buf.writeLong(effects[i]);
        }
    }

    void processMessage(MessageContext ctx, PacketSyncTerrain message)
    {
        EntityPlayer player;
        player = PokecubeCore.getPlayer(null);
        TerrainSegment t = TerrainManager.getInstance().getTerrain(player.worldObj).getTerrain(message.x, message.y,
                message.z);
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
}
