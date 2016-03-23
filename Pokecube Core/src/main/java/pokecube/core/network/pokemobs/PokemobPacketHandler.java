package pokecube.core.network.pokemobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.megastuff.ItemMegastone;
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
        PacketBuffer buffer;

        public MessageClient()
        {
        };

        public MessageClient(byte[] data)
        {
            this.buffer = new PacketBuffer(Unpooled.copiedBuffer(data));
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
            @Override
            public MessageServer onMessage(MessageClient message, MessageContext ctx)
            {
                return null;
            }
        }
    }

    public static class MessageServer implements IMessage
    {
        public static final byte RETURN      = 0;
        public static final byte NICKNAME    = 1;
        public static final byte MOVEUSE     = 2;
        public static final byte MOVEMESSAGE = 3;
        public static final byte MOVESWAP    = 4;
        public static final byte MOVEINDEX   = 5;
        public static final byte CHANGEFORM  = 6;
        public static final byte ALIVECHECK  = 7;
        public static final byte JUMP        = 8;
        public static final byte STANCE      = 9;
        PacketBuffer             buffer;

        public MessageServer()
        {
        };

        public MessageServer(byte messageid, int entityId)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer(9));
            buffer.writeByte(messageid);
            buffer.writeInt(entityId);
        }

        public MessageServer(byte[] data)
        {
            this.buffer = new PacketBuffer(Unpooled.copiedBuffer(data));
        }

        public MessageServer(PacketBuffer buffer)
        {
            this.buffer = buffer;
        }

        public MessageServer(byte channel, int id, NBTTagCompound nbt)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer(9));
            buffer.writeByte(channel);
            buffer.writeInt(id);
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
            @Override
            public IMessage onMessage(MessageServer message, MessageContext ctx)
            {
                EntityPlayer player = ctx.getServerHandler().playerEntity;
                new PacketHandler(player, message.buffer);
                return null;
            }

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
                            pokemob = (IPokemob) world.getEntityByID(id);
                            if (pokemob == null) { return; }

                            if (channel == RETURN)
                            {
                                Entity mob = (Entity) pokemob;
                                ((IPokemob) mob).returnToPokecube();
                            }
                            else if (channel == JUMP)
                            {
                                EntityLiving mob = (EntityLiving) pokemob;
                                mob.getJumpHelper().setJumping();
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
                            else if (channel == CHANGEFORM)
                            {
                                if (pokemob.getPokemonAIState(IMoveConstants.EVOLVING)) return;

                                int happiness = pokemob.getHappiness();
                                ItemStack held = ((EntityLiving) pokemob).getHeldItem();
                                if (held == null || !(held.getItem() instanceof ItemMegastone)
                                        || (happiness < 255 && !player.capabilities.isCreativeMode))
                                {
                                    String mess = "";
                                    if (held == null || !(held.getItem() instanceof ItemMegastone))
                                        mess = StatCollector.translateToLocalFormatted("pokemob.megaevolve.nostone",
                                                pokemob.getPokemonDisplayName());
                                    else StatCollector.translateToLocalFormatted("pokemob.megaevolve.nothappy",
                                            pokemob.getPokemonDisplayName());
                                    player.addChatMessage(new ChatComponentText(mess));
                                    return;
                                }
                                NBTTagCompound tag = held.getTagCompound();
                                if (tag == null)
                                {
                                    held.setTagCompound(tag = new NBTTagCompound());
                                }
                                String stackname = tag.getString("pokemon");

                                String forme = null;

                                if (!(stackname == null || stackname.isEmpty()))
                                {
                                    forme = stackname;
                                }
                                if (forme == null)
                                {
                                    List<String> keys = new ArrayList<String>(pokemob.getPokedexEntry().forms.keySet());
                                    Collections.shuffle(keys);
                                    for (String s : keys)
                                    {
                                        String name = pokemob.getPokedexEntry().forms.get(s).getName();
                                        String[] args = name.split(" ");
                                        if (args.length > 1)
                                        {
                                            String mega = args[1];
                                            if (mega.toLowerCase().contains("mega"))
                                            {
                                                forme = s;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (forme != null)
                                {
                                    if (stackname == null || stackname.isEmpty())
                                    {
                                        tag.setString("pokemon", forme);
                                        held.setTagCompound(tag);
                                    }

                                    PokedexEntry megaEntry = Database.getEntry(forme);
                                    if (megaEntry != null
                                            && megaEntry.getBaseName().equals(pokemob.getPokedexEntry().getBaseName()))
                                    {
                                        String old = pokemob.getPokemonDisplayName();
                                        if (pokemob.getPokedexEntry() == megaEntry)
                                        {
                                            pokemob.megaEvolve(pokemob.getPokedexEntry().getBaseName());
                                            megaEntry = pokemob.getPokedexEntry().baseForme;
                                            String mess = StatCollector.translateToLocalFormatted(
                                                    "pokemob.megaevolve.revert", old, megaEntry.getTranslatedName());
                                            player.addChatMessage(new ChatComponentText(mess));
                                        }
                                        else
                                        {
                                            pokemob.setPokemonAIState(IPokemob.MEGAFORME, true);
                                            pokemob.megaEvolve(forme);
                                            String mess = StatCollector.translateToLocalFormatted(
                                                    "pokemob.megaevolve.success", old, megaEntry.getTranslatedName());
                                            player.addChatMessage(new ChatComponentText(mess));
                                        }
                                    }
                                    else
                                    {
                                        String mess = StatCollector.translateToLocalFormatted(
                                                "pokemob.megaevolve.wrongstone", pokemob.getPokemonDisplayName());
                                        player.addChatMessage(new ChatComponentText(mess));
                                    }
                                }
                                else
                                {
                                    String mess = StatCollector.translateToLocalFormatted("pokemob.megaevolve.nomega",
                                            pokemob.getPokemonDisplayName());
                                    player.addChatMessage(new ChatComponentText(mess));
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
                                        pokemob.getPokemonOwner().addChatMessage(
                                                new ChatComponentText("Cannot rename a traded pokemob"));
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
                                    if (pokemob.getPokemonAIState(IPokemob.STAYING))
                                    {
                                        type = 3;
                                    }
                                    else if (pokemob.getPokemonAIState(IPokemob.GUARDING))
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
                                    if (pokemob.getPokemonAIState(IPokemob.STAYING))
                                    {
                                        type = 1;
                                    }
                                    else if (pokemob.getPokemonAIState(IPokemob.GUARDING))
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
                                    pokemob.setPokemonAIState(IPokemob.GUARDING, true);
                                    TerrainSegment terrain = TerrainManager.getInstance()
                                            .getTerrainForEntity((Entity) pokemob);
                                    Vector3 mid = terrain.getCentre();
                                    pokemob.setHome(mid.intX(), mid.intY(), mid.intZ(), 16);
                                    pokemob.setPokemonAIState(IPokemob.STAYING, false);
                                    if (pokemob.getGuardAI() != null)
                                    {
                                        ((GuardAI) pokemob.getGuardAI()).setTimePeriod(TimePeriod.fullDay);
                                        ((GuardAI) pokemob.getGuardAI()).setPos(mid.getPos());
                                    }
                                }
                                else if (type == 2)
                                {
                                    pokemob.setPokemonAIState(IPokemob.GUARDING, false);
                                    if (pokemob.getGuardAI() != null)
                                        ((GuardAI) pokemob.getGuardAI()).setTimePeriod(TimePeriod.fullDay);
                                    pokemob.setPokemonAIState(IPokemob.STAYING, true);
                                }
                                else if (type == 3)
                                {
                                    pokemob.setPokemonAIState(IPokemob.STAYING, false);
                                    if (pokemob.getGuardAI() != null)
                                        ((GuardAI) pokemob.getGuardAI()).setTimePeriod(new TimePeriod(0, 0));
                                    pokemob.setPokemonAIState(IPokemob.GUARDING, false);
                                }
                                else if (dir == 4)
                                {
                                    pokemob.setPokemonAIState(IPokemob.SITTING,
                                            !pokemob.getPokemonAIState(IPokemob.SITTING));
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

                        if (player.isSneaking())
                        {
                            ((EntityLiving) pokemob).getNavigator().tryMoveToEntityLiving(player, 0.4);
                            ((EntityLiving) pokemob).setAttackTarget(null);
                            return;
                        }

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
                            pokemob.setPokemonAIState(IPokemob.NEWEXECUTEMOVE, true);
                        }
                        else
                        {
                            Entity owner = pokemob.getPokemonOwner();
                            if (owner != null)
                            {
                                Entity closest = owner.worldObj.getEntityByID(id);
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
                                    pokemob.setPokemonAIState(IPokemob.NEWEXECUTEMOVE, true);
                                }
                            }
                        }
                    }
                }
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
