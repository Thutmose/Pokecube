package pokecube.core.network.pokemobs;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.IMobGenetics;

public class PacketSyncGene implements IMessage, IMessageHandler<PacketSyncGene, IMessage>
{
    public static void syncGene(Entity mob, Alleles gene)
    {
        if (mob.getEntityWorld() == null || mob.getEntityWorld().isRemote || gene == null) return;
        PacketSyncGene packet = new PacketSyncGene();
        packet.genes = gene;
        packet.entityId = mob.getEntityId();
        PokecubeMod.packetPipeline.sendToDimension(packet, mob.dimension);
    }

    public PacketSyncGene()
    {
    }

    Alleles genes = new Alleles();
    int     entityId;

    @Override
    public IMessage onMessage(PacketSyncGene message, MessageContext ctx)
    {
        PokecubeCore.proxy.getMainThreadListener().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                processMessage(ctx, message);
            }

            void processMessage(MessageContext ctx, PacketSyncGene message)
            {
                EntityPlayer player = PokecubeCore.getPlayer(null);
                int id = message.entityId;
                Alleles alleles = message.genes;
                Entity mob = PokecubeMod.core.getEntityProvider().getEntity(player.getEntityWorld(), id, true);
                if (mob == null) return;
                IMobGenetics genes = mob.getCapability(IMobGenetics.GENETICS_CAP, null);
                IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
                if (genes != null && alleles != null && alleles.getExpressed() != null)
                {
                    genes.getAlleles().put(alleles.getExpressed().getKey(), alleles);
                }
                if (pokemob != null) pokemob.onGenesChanged();
            }
        });
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        PacketBuffer buffer = new PacketBuffer(buf);
        entityId = buffer.readInt();
        try
        {
            genes.load(buffer.readCompoundTag());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeInt(entityId);
        buffer.writeCompoundTag(genes.save());
    }
}
