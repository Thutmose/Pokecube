package pokecube.core.network.packets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.pokemob.PokemobAIUtilityMove;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Explode;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector3;

public class PacketPokemobAttack implements IMessage, IMessageHandler<PacketPokemobAttack, IMessage>
{
    int     entityId = -1;
    int     targetId = -1;
    boolean teleport = false;
    Vector3 targetLocation;

    public static void sendAttackUse(@Nonnull Entity attacker, @Nullable Entity target, @Nullable Vector3 location, boolean teleport)
    {
        PacketPokemobAttack packet = new PacketPokemobAttack();
        packet.entityId = attacker.getEntityId();
        if (target != null) packet.targetId = target.getEntityId();
        if (location != null) packet.targetLocation = location.copy();
        packet.teleport = teleport;
        PokecubeMod.packetPipeline.sendToServer(packet);
    }

    public PacketPokemobAttack()
    {
    }

    @Override
    public IMessage onMessage(final PacketPokemobAttack message, final MessageContext ctx)
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
        entityId = buf.readInt();
        targetId = buf.readInt();
        teleport = buf.readBoolean();
        if (buf.readBoolean()) targetLocation = Vector3.readFromBuff(buf);
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(entityId);
        buf.writeInt(targetId);
        buf.writeBoolean(teleport);
        buf.writeBoolean(targetLocation != null);
        if (targetLocation != null)
        {
            targetLocation.writeToBuff(buf);
        }
    }

    void processMessage(MessageContext ctx, PacketPokemobAttack message)
    {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        Entity user = PokecubeMod.core.getEntityProvider().getEntity(player.getEntityWorld(), message.entityId, true);
        Entity target = PokecubeMod.core.getEntityProvider().getEntity(player.getEntityWorld(), message.targetId, true);

        if (user == null || !(user instanceof IPokemob)) return;
        IPokemob pokemob = (IPokemob) user;
        Vector3 temp = Vector3.getNewVector().set(user);
        int currentMove = pokemob.getMoveIndex();

        if (currentMove == 5) { return; }

        Move_Base move = MovesUtils.getMoveFromName(pokemob.getMoves()[currentMove]);
        boolean teleport = message.teleport;

        if (teleport)
        {
            NBTTagCompound teletag = new NBTTagCompound();
            PokecubeSerializer.getInstance().writePlayerTeleports(player.getUniqueID(), teletag);

            PokecubeClientPacket packe = new PokecubeClientPacket(PokecubeClientPacket.TELEPORTLIST, teletag);
            PokecubePacketHandler.sendToClient(packe, player);
        }

        if (move instanceof Move_Explode && (user == target || target == null))
        {
            pokemob.executeMove(null, temp, 0);
        }
        else
        {
            Entity owner = pokemob.getPokemonOwner();
            if (owner != null)
            {
                Entity closest = target;
                if (closest instanceof IPokemob)
                {
                    IPokemob tempMob = (IPokemob) closest;
                    if (tempMob.getPokemonOwnerName().equals(pokemob.getPokemonOwnerName()))
                    {
                        if (tempMob == closest)
                        {
                            pokemob.executeMove(null, temp, 0);
                        }
                        return;
                    }
                }
                if (closest != null || teleport)
                {
                    if (closest instanceof EntityLivingBase)
                    {
                        ((EntityLiving) pokemob).setAttackTarget((EntityLivingBase) closest);
                        if (closest instanceof EntityLiving)
                        {
                            ((EntityLiving) closest).setAttackTarget((EntityLivingBase) pokemob);
                        }
                    }
                    else if (closest == null)
                    {
                        pokemob.executeMove(closest, temp, 0);
                    }
                    else pokemob.executeMove(closest, temp.set(closest), closest.getDistanceToEntity((Entity) pokemob));
                }
                else if (message.targetLocation != null)
                {
                    pokemob.setPokemonAIState(IMoveConstants.NEWEXECUTEMOVE, true);
                    ((PokemobAIUtilityMove) pokemob.getUtilityMoveAI()).destination = message.targetLocation;
                }
            }
        }
    }
}
