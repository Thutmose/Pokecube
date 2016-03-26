package pokecube.core.network.pokemobs;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.helper.EntityMountablePokemob;
import pokecube.core.entity.pokemobs.helper.EntityMountablePokemob.MountState;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Explode;
import pokecube.core.moves.templates.Move_Utility;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

/** This class handles the packets sent for the IPokemob Entities.
 * 
 * @author Thutmose */
public class PokemobPacketHandler
{
    public static class MessageClient implements IMessage
    {
        public static class MessageHandlerClient implements IMessageHandler<MessageClient, MessageServer>
        {
            static class PacketHandler
            {
                final EntityPlayer player;
                final PacketBuffer buffer;

                public PacketHandler(EntityPlayer p, PacketBuffer b)
                {
                    this.player = p;
                    this.buffer = b;
                    Runnable toRun = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            byte channel = buffer.readByte();
                            int id = buffer.readInt();
                            IPokemob pokemob;
                            World world = player.getEntityWorld();
                            pokemob = (IPokemob) PokecubeMod.core.getEntityProvider().getEntity(world, id, true);
                            if (pokemob == null) { return; }

                            if (channel == CHANGEFORME)
                            {
                                try
                                {
                                    NBTTagCompound tag = buffer.readNBTTagCompoundFromBuffer();
                                    String forme = tag.getString("f");
                                    pokemob.changeForme(forme);
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }

                            }
                        }
                    };
                    PokecubeCore.proxy.getMainThreadListener().addScheduledTask(toRun);
                }
            }

            @Override
            public MessageServer onMessage(MessageClient message, MessageContext ctx)
            {
                new PacketHandler(PokecubeCore.getPlayer(null), message.buffer);
                return null;
            }
        }

        public static final byte CHANGEFORME = 0;

        public PacketBuffer      buffer;;

        public MessageClient()
        {
        }

        public MessageClient(byte messageid, int entityId)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer(9));
            buffer.writeByte(messageid);
            buffer.writeInt(entityId);
        }

        public MessageClient(byte channel, NBTTagCompound nbt)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeByte(channel);
            buffer.writeNBTTagCompoundToBuffer(nbt);
        }

        public MessageClient(byte[] data)
        {
            this.buffer = new PacketBuffer(Unpooled.copiedBuffer(data));
        }

        public MessageClient(PacketBuffer buffer)
        {
            this.buffer = buffer;
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
    }

    public static class MessageServer implements IMessage
    {
        public static class MessageHandlerServer implements IMessageHandler<MessageServer, IMessage>
        {
            static class PacketHandler
            {
                final EntityPlayer player;
                final PacketBuffer buffer;

                public PacketHandler(EntityPlayer p, PacketBuffer b)
                {
                    this.player = p;
                    this.buffer = b;
                    Runnable toRun = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            byte channel = buffer.readByte();
                            int id = buffer.readInt();
                            IPokemob pokemob;
                            WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance()
                                    .worldServerForDimension(player.dimension);
                            Entity entity = PokecubeMod.core.getEntityProvider().getEntity(world, id, true);
                            if (entity == null || !(entity instanceof IPokemob)) { return; }
                            pokemob = (IPokemob) entity;
                            if (channel == RETURN)
                            {
                                Entity mob = (Entity) pokemob;
                                ((IPokemob) mob).returnToPokecube();
                            }
                            else if (channel == MOUNTDIR)
                            {
                                EntityMountablePokemob mob = (EntityMountablePokemob) pokemob;
                                byte mess = buffer.readByte();
                                MountState state = MountState.values()[mess];
                                mob.state = state;
                            }
                            else if (channel == SYNCPOS)
                            {
                                Vector3 v = Vector3.getNewVector().set(buffer.readFloat(), buffer.readFloat(),
                                        buffer.readFloat());
                                v.moveEntity((Entity) pokemob);
                            }
                            else if (channel == MOVEUSE)
                            {
                                handleMoveUse(pokemob, id);
                            }
                            else if (channel == MOVEINDEX)
                            {
                                byte moveIndex = buffer.readByte();
                                pokemob.setMoveIndex(moveIndex);
                            }
                            else if (channel == COME)
                            {
                                ((EntityLiving) pokemob).getNavigator().tryMoveToEntityLiving(player, 0.4);
                                ((EntityLiving) pokemob).setAttackTarget(null);
                                return;
                            }
                            else if (channel == CHANGEFORM)
                            {
                                if (pokemob.getPokemonAIState(IMoveConstants.EVOLVING)) return;
                                PokedexEntry megaEntry = pokemob.getPokedexEntry().getEvo(pokemob);

                                if (megaEntry != null
                                        && megaEntry.getBaseName().equals(pokemob.getPokedexEntry().getBaseName()))
                                {
                                    String old = pokemob.getPokemonDisplayName();
                                    if (pokemob.getPokedexEntry() == megaEntry)
                                    {
                                        pokemob.megaEvolve(pokemob.getPokedexEntry().getBaseName());
                                        megaEntry = pokemob.getPokedexEntry().baseForme;
                                        String mess = I18n.translateToLocalFormatted("pokemob.megaevolve.revert", old,
                                                megaEntry.getTranslatedName());
                                        player.addChatMessage(new TextComponentString(mess));
                                    }
                                    else
                                    {
                                        pokemob.setPokemonAIState(IMoveConstants.MEGAFORME, true);
                                        pokemob.megaEvolve(megaEntry.getName());
                                        String mess = I18n.translateToLocalFormatted("pokemob.megaevolve.success", old,
                                                megaEntry.getTranslatedName());
                                        player.addChatMessage(new TextComponentString(mess));
                                    }
                                }
                                else
                                {
                                    if (pokemob.getPokemonAIState(IMoveConstants.MEGAFORME))
                                    {
                                        String old = pokemob.getPokemonDisplayName();
                                        pokemob.megaEvolve(pokemob.getPokedexEntry().getBaseName());
                                        pokemob.setPokemonAIState(IMoveConstants.MEGAFORME, false);
                                        megaEntry = pokemob.getPokedexEntry().baseForme;
                                        String mess = I18n.translateToLocalFormatted("pokemob.megaevolve.revert", old,
                                                megaEntry.getTranslatedName());
                                        player.addChatMessage(new TextComponentString(mess));
                                    }
                                    else
                                    {
                                        String mess = I18n.translateToLocalFormatted("pokemob.megaevolve.failed",
                                                pokemob.getPokemonDisplayName());
                                        player.addChatMessage(new TextComponentString(mess));
                                    }
                                }

                            }
                            else if (channel == MOVESWAP)
                            {
                                byte moveIndex0 = buffer.readByte();
                                byte moveIndex1 = buffer.readByte();
                                int num = buffer.readInt();
                                pokemob.setLeaningMoveIndex(num);
                                pokemob.exchangeMoves(moveIndex0, moveIndex1);
                            }
                            else if (channel == NICKNAME)
                            {

                                boolean OT = pokemob.getPokemonOwnerName() == null
                                        || (PokecubeMod.fakeUUID.equals(pokemob.getOriginalOwnerUUID()))
                                        || (pokemob.getPokemonOwnerName()
                                                .equals(pokemob.getOriginalOwnerUUID().toString()));

                                if (!OT && pokemob.getPokemonOwner() != null)
                                {
                                    OT = pokemob.getPokemonOwner().getUniqueID().equals(pokemob.getOriginalOwnerUUID());
                                }
                                if (!OT)
                                {
                                    if (pokemob.getPokemonOwner() != null)
                                    {
                                        String mess = I18n.translateToLocal("pokemob.rename.deny");
                                        pokemob.getPokemonOwner().addChatMessage(new TextComponentString(mess));
                                    }
                                }
                                else
                                {
                                    byte[] string = new byte[buffer.readByte() + 1];
                                    for (int i = 0; i < string.length; i++)
                                    {
                                        string[i] = buffer.readByte();
                                    }
                                    String name = ChatAllowedCharacters.filterAllowedCharacters(new String(string));
                                    pokemob.setPokemonNickname(name);
                                }
                            }
                            else if (channel == STANCE)
                            {
                                byte dir = buffer.readByte();
                                byte type = 0;
                                if (dir > 0)
                                {
                                    if (pokemob.getPokemonAIState(IMoveConstants.STAYING))
                                    {
                                        type = 3;
                                    }
                                    else if (pokemob.getPokemonAIState(IMoveConstants.GUARDING))
                                    {
                                        type = 2;
                                    }
                                    else
                                    {
                                        type = 1;
                                    }
                                }
                                else
                                {
                                    if (pokemob.getPokemonAIState(IMoveConstants.STAYING))
                                    {
                                        type = 1;
                                    }
                                    else if (pokemob.getPokemonAIState(IMoveConstants.GUARDING))
                                    {
                                        type = 3;
                                    }
                                    else
                                    {
                                        type = 2;
                                    }
                                }
                                if (dir == 4) type = 4;
                                if (type == 1)
                                {
                                    pokemob.setPokemonAIState(IMoveConstants.GUARDING, true);
                                    TerrainSegment terrain = TerrainManager.getInstance()
                                            .getTerrainForEntity((Entity) pokemob);
                                    Vector3 mid = terrain.getCentre();
                                    pokemob.setHome(mid.intX(), mid.intY(), mid.intZ(), 16);
                                    pokemob.setPokemonAIState(IMoveConstants.STAYING, false);
                                    if (pokemob.getGuardAI() != null)
                                    {
                                        ((GuardAI) pokemob.getGuardAI()).setTimePeriod(TimePeriod.fullDay);
                                        ((GuardAI) pokemob.getGuardAI()).setPos(mid.getPos());
                                    }
                                }
                                else if (type == 2)
                                {
                                    pokemob.setPokemonAIState(IMoveConstants.GUARDING, false);
                                    if (pokemob.getGuardAI() != null)
                                        ((GuardAI) pokemob.getGuardAI()).setTimePeriod(TimePeriod.fullDay);
                                    pokemob.setPokemonAIState(IMoveConstants.STAYING, true);
                                }
                                else if (type == 3)
                                {
                                    pokemob.setPokemonAIState(IMoveConstants.STAYING, false);
                                    if (pokemob.getGuardAI() != null)
                                        ((GuardAI) pokemob.getGuardAI()).setTimePeriod(new TimePeriod(0, 0));
                                    pokemob.setPokemonAIState(IMoveConstants.GUARDING, false);
                                }
                                else if (dir == 4)
                                {
                                    pokemob.setPokemonAIState(IMoveConstants.SITTING,
                                            !pokemob.getPokemonAIState(IMoveConstants.SITTING));
                                }
                            }
                        }
                    };
                    PokecubeCore.proxy.getMainThreadListener().addScheduledTask(toRun);
                }

                private void handleMoveUse(IPokemob pokemob, int id1)
                {
                    PacketBuffer dat = buffer;
                    int id = dat.readInt();
                    Vector3 v = Vector3.getNewVector();

                    if (pokemob != null)
                    {
                        int currentMove = pokemob.getMoveIndex();

                        if (currentMove == 5) { return; }

                        Move_Base move = MovesUtils.getMoveFromName(pokemob.getMoves()[currentMove]);
                        boolean teleport = dat.readBoolean();

                        if (teleport)
                        {
                            NBTTagCompound teletag = new NBTTagCompound();
                            PokecubeSerializer.getInstance().writePlayerTeleports(player.getUniqueID(), teletag);

                            PokecubeClientPacket packe = new PokecubeClientPacket(PokecubeClientPacket.TELEPORTLIST,
                                    teletag);
                            PokecubePacketHandler.sendToClient(packe, player);
                        }

                        if (move instanceof Move_Explode && (id1 == id || id == 0))
                        {
                            pokemob.executeMove(null, v.set(pokemob), 0);
                        }
                        else if (Move_Utility.isUtilityMove(move.name) && (id1 == id || id == 0))
                        {
                            pokemob.setPokemonAIState(IMoveConstants.NEWEXECUTEMOVE, true);
                        }
                        else
                        {
                            Entity owner = pokemob.getPokemonOwner();
                            if (owner != null)
                            {
                                Entity closest = PokecubeMod.core.getEntityProvider().getEntity(owner.worldObj, id,
                                        false);
                                if (closest instanceof IPokemob)
                                {
                                    IPokemob target = (IPokemob) closest;
                                    if (target.getPokemonOwnerName().equals(pokemob.getPokemonOwnerName())) { return; }
                                }

                                if (closest != null)
                                {
                                    if (closest instanceof EntityLivingBase)
                                    {
                                        ((EntityLiving) pokemob).setAttackTarget((EntityLivingBase) closest);
                                        if (closest instanceof EntityLiving)
                                        {
                                            ((EntityLiving) closest).setAttackTarget((EntityLivingBase) pokemob);
                                        }
                                    }
                                    else pokemob.executeMove(closest, v.set(closest),
                                            closest.getDistanceToEntity((Entity) pokemob));
                                }
                                else if (buffer.isReadable(24))
                                {
                                    v = Vector3.readFromBuff(buffer);
                                    pokemob.setPokemonAIState(IMoveConstants.NEWEXECUTEMOVE, true);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public IMessage onMessage(MessageServer message, MessageContext ctx)
            {
                EntityPlayer player = ctx.getServerHandler().playerEntity;
                new PacketHandler(player, message.buffer);
                return null;
            }
        }

        public static final byte RETURN      = 0;
        public static final byte NICKNAME    = 1;
        public static final byte MOVEUSE     = 2;
        public static final byte MOVEMESSAGE = 3;
        public static final byte MOVESWAP    = 4;
        public static final byte MOVEINDEX   = 5;
        public static final byte CHANGEFORM  = 6;
        public static final byte ALIVECHECK  = 7;
        public static final byte SYNCPOS     = 8;
        public static final byte STANCE      = 9;
        public static final byte COME        = 10;
        public static final byte MOUNTDIR    = 11;

        PacketBuffer             buffer;;

        public MessageServer()
        {
        }

        public MessageServer(byte messageid, int entityId)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer(9));
            buffer.writeByte(messageid);
            buffer.writeInt(entityId);
        }

        public MessageServer(byte channel, int id, NBTTagCompound nbt)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer(9));
            buffer.writeByte(channel);
            buffer.writeInt(id);
            buffer.writeNBTTagCompoundToBuffer(nbt);
        }

        public MessageServer(byte[] data)
        {
            this.buffer = new PacketBuffer(Unpooled.copiedBuffer(data));
        }

        public MessageServer(PacketBuffer buffer)
        {
            this.buffer = buffer;
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
}
