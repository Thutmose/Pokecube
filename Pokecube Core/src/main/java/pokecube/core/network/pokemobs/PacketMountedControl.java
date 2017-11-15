package pokecube.core.network.pokemobs;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.thread.logicRunnables.LogicMountedControl;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public class PacketMountedControl implements IMessage, IMessageHandler<PacketMountedControl, IMessage>
{
    private static final byte FORWARD  = 1;
    private static final byte BACK     = 2;
    private static final byte LEFT     = 4;
    private static final byte RIGHT    = 8;
    private static final byte UP       = 16;
    private static final byte DOWN     = 32;
    private static final byte SYNCLOOK = 64;

    int                       entityId;
    byte                      message;
    float                     throttle;

    public static void sendControlPacket(Entity pokemob, LogicMountedControl controller)
    {
        PacketMountedControl packet = new PacketMountedControl();
        packet.entityId = pokemob.getEntityId();
        if (controller.backInputDown) packet.message += BACK;
        if (controller.forwardInputDown) packet.message += FORWARD;
        if (controller.leftInputDown) packet.message += LEFT;
        if (controller.rightInputDown) packet.message += RIGHT;
        if (controller.upInputDown) packet.message += UP;
        if (controller.downInputDown) packet.message += DOWN;
        if (controller.followOwnerLook) packet.message += SYNCLOOK;
        packet.throttle = (float) controller.throttle;
        PokecubeMod.packetPipeline.sendToServer(packet);
    }

    public PacketMountedControl()
    {
    }

    @Override
    public IMessage onMessage(final PacketMountedControl message, final MessageContext ctx)
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
        entityId = buf.readInt();
        message = buf.readByte();
        throttle = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(entityId);
        buf.writeByte(message);
        buf.writeFloat(throttle);
    }

    void processMessage(MessageContext ctx, PacketMountedControl message)
    {
        EntityPlayer player;
        if (ctx.side == Side.CLIENT)
        {
            player = PokecubeCore.getPlayer(null);
        }
        else
        {
            player = ctx.getServerHandler().player;
        }
        Entity mob = player.getEntityWorld().getEntityByID(message.entityId);
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob != null && pokemob.getController() != null)
        {
            if (pokemob.getOwner() != player) return;
            LogicMountedControl controller = pokemob.getController();
            controller.forwardInputDown = (message.message & FORWARD) > 0;
            controller.backInputDown = (message.message & BACK) > 0;
            controller.leftInputDown = (message.message & LEFT) > 0;
            controller.rightInputDown = (message.message & RIGHT) > 0;
            controller.upInputDown = (message.message & UP) > 0;
            controller.downInputDown = (message.message & DOWN) > 0;
            controller.followOwnerLook = (message.message & SYNCLOOK) > 0;
            controller.throttle = message.throttle;
        }
    }
}
