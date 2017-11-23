package pokecube.core.network.pokemobs;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;

@Deprecated
public class PacketPokemobGui implements IMessage, IMessageHandler<PacketPokemobGui, IMessage>
{
    public static final byte BUTTONTOGGLESTAY  = 0;
    public static final byte BUTTONTOGGLEGUARD = 1;
    public static final byte BUTTONTOGGLESIT   = 2;

    byte                     message;
    int                      id;

    public PacketPokemobGui()
    {
    }

    public PacketPokemobGui(byte message, int id)
    {
        this.message = message;
        this.id = id;
    }

    @Override
    public IMessage onMessage(final PacketPokemobGui message, final MessageContext ctx)
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
        message = buf.readByte();
        id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(message);
        buf.writeInt(id);
    }

    void processMessage(MessageContext ctx, PacketPokemobGui message)
    {
        Entity entity = ctx.getServerHandler().player.getEntityWorld().getEntityByID(message.id);
        IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
        if (mob != null)
        {
            if (message.message == BUTTONTOGGLEGUARD)
            {
                mob.setPokemonAIState(IMoveConstants.GUARDING, !mob.getPokemonAIState(IMoveConstants.GUARDING));
            }
            else if (message.message == BUTTONTOGGLESTAY)
            {
                boolean stay;
                mob.setPokemonAIState(IMoveConstants.STAYING, stay = !mob.getPokemonAIState(IMoveConstants.STAYING));
                if (stay)
                {
                    Vector3 mid = Vector3.getNewVector().set(entity);
                    mob.setHome(mid.intX(), mid.intY(), mid.intZ(), 16);
                    if (mob.getGuardAI() != null)
                    {
                        ((GuardAI) mob.getGuardAI()).setTimePeriod(TimePeriod.fullDay);
                        ((GuardAI) mob.getGuardAI()).setPos(mid.getPos());
                    }
                }
                else
                {
                    if (mob.getGuardAI() != null) ((GuardAI) mob.getGuardAI()).setTimePeriod(new TimePeriod(0, 0));
                }
            }
            else if (message.message == BUTTONTOGGLESIT)
            {
                mob.setPokemonAIState(IMoveConstants.SITTING, !mob.getPokemonAIState(IMoveConstants.SITTING));
            }
        }
    }
}
