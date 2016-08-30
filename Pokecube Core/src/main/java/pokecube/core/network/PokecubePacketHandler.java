/**
 *
 */
package pokecube.core.network;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.utils.AISaveHandler;
import pokecube.core.client.gui.GuiInfoMessages;
import pokecube.core.client.gui.GuiTeleport;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IHealer;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.packets.PacketChoose;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.core.network.packets.PacketPC;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.network.packets.PacketSound;
import pokecube.core.network.packets.PacketSyncTerrain;
import pokecube.core.network.packets.PacketTrade;
import pokecube.core.network.pokemobs.PacketChangeForme;
import pokecube.core.network.pokemobs.PacketNickname;
import pokecube.core.network.pokemobs.PacketPokemobAttack;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.network.pokemobs.PacketPokemobMessage;
import pokecube.core.network.pokemobs.PacketPosSync;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import pokecube.core.utils.Tools;
import thut.api.entity.Transporter;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

/** @author Manchou */
public class PokecubePacketHandler
{

    public static class PokecubeClientPacket implements IMessage
    {

        public static class PokecubeMessageHandlerClient
                implements IMessageHandler<PokecubeClientPacket, PokecubeServerPacket>
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
                            if (channel == STATS)
                            {
                                handleStatsPacketClient(buffer, player);
                            }
                            else if (channel == TELEPORTLIST)
                            {
                                try
                                {
                                    NBTTagCompound nbt = buffer.readNBTTagCompoundFromBuffer();
                                    PokecubeSerializer.getInstance().readPlayerTeleports(nbt);
                                    GuiTeleport.instance().refresh();
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            else if (channel == KILLENTITY)
                            {
                                try
                                {
                                    NBTTagCompound nbt = buffer.readNBTTagCompoundFromBuffer();
                                    int id = nbt.getInteger("id");
                                    if (player.getEntityWorld().getEntityByID(id) != null)
                                        player.getEntityWorld().getEntityByID(id).setDead();
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            else if (channel == MOVEENTITY)
                            {
                                int id = buffer.readInt();
                                Entity e = player.getEntityWorld().getEntityByID(id);
                                Vector3 v = Vector3.readFromBuff(buffer);

                                if (e != null)
                                {
                                    v.moveEntity(e);
                                }
                            }
                            else if (channel == TELEPORTINDEX)
                            {
                                PokecubeServerPacket packet = new PokecubeServerPacket(new byte[] {
                                        PokecubeServerPacket.TELEPORT, (byte) GuiTeleport.instance().indexLocation });
                                PokecubePacketHandler.sendToServer(packet);
                            }
                            else if (channel == CHANGEFORME)
                            {
                                try
                                {
                                    NBTTagCompound nbt = buffer.readNBTTagCompoundFromBuffer();
                                    int id = nbt.getInteger("id");
                                    String forme = nbt.getString("forme");
                                    if (player.getEntityWorld().getEntityByID(id) != null)
                                    {
                                        PokedexEntry entry = ((IPokemob) player.getEntityWorld().getEntityByID(id))
                                                .getPokedexEntry().getForm(forme);
                                        ((IPokemob) player.getEntityWorld().getEntityByID(id)).setPokedexEntry(entry);
                                    }

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
            public PokecubeServerPacket onMessage(PokecubeClientPacket message, MessageContext ctx)
            {
                EntityPlayer player = PokecubeCore.getPlayer(null);
                if (player == null)
                {
                    System.err.println(FMLClientHandler.instance().getClientPlayerEntity());
                    // Thread.dumpStack();
                    return null;
                }
                new PacketHandler(player, message.buffer);
                return null;
            }
        }

        public static final byte STATS         = 6;
        public static final byte TELEPORTLIST  = 8;
        public static final byte KILLENTITY    = 11;
        public static final byte MOVEENTITY    = 12;
        public static final byte TELEPORTINDEX = 13;

        public static final byte CHANGEFORME   = 14;

        PacketBuffer             buffer;;

        public PokecubeClientPacket()
        {
        }

        public PokecubeClientPacket(byte[] data)
        {
            this.buffer = new PacketBuffer(Unpooled.copiedBuffer(data));
        }

        public PokecubeClientPacket(ByteBuf buffer)
        {
            if (buffer instanceof PacketBuffer) this.buffer = (PacketBuffer) buffer;
            else this.buffer = new PacketBuffer(buffer);
        }

        public PokecubeClientPacket(int channel, NBTTagCompound nbt)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeByte((byte) channel);
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
    }

    public static class PokecubeServerPacket implements IMessage
    {

        public static class PokecubeMessageHandlerServer implements IMessageHandler<PokecubeServerPacket, IMessage>
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
                            if (channel == 1)
                            {
                                new Exception().printStackTrace();
                            }
                            else if (channel == POKECENTER)
                            {
                                byte[] message = new byte[buffer.array().length - 1];

                                for (int i = 0; i < message.length; i++)
                                {
                                    message[i] = buffer.array()[i + 1];
                                }
                                handlePokecenterPacket(message, (EntityPlayerMP) player);
                            }
                            else if (channel == TELEPORT)
                            {
                                int index = buffer.readByte();
                                PokecubeSerializer.getInstance().setTeleIndex(player.getCachedUniqueIdString(), index);
                                TeleDest d = PokecubeSerializer.getInstance()
                                        .getTeleport(player.getCachedUniqueIdString());
                                if (d == null) return;

                                Vector3 loc = d.getLoc();
                                int dim = d.getDim();
                                Transporter.teleportEntity(player, loc, dim, false);
                            }
                        }
                    };
                    PokecubeCore.proxy.getMainThreadListener().addScheduledTask(toRun);
                }
            }

            @Override
            public PokecubeServerPacket onMessage(PokecubeServerPacket message, MessageContext ctx)
            {
                EntityPlayer player = ctx.getServerHandler().playerEntity;
                new PacketHandler(player, message.buffer);
                return null;
            }
        }

        // public static final byte CHOOSE1ST = 0;
        public static final byte POKECENTER     = 3;
        public static final byte POKEMOBSPAWNER = 4;
        public static final byte TELEPORT       = 5;

        PacketBuffer             buffer;;

        public PokecubeServerPacket()
        {
        }

        public PokecubeServerPacket(byte channel, NBTTagCompound nbt)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeByte(channel);
            buffer.writeNBTTagCompoundToBuffer(nbt);
        }

        public PokecubeServerPacket(byte[] data)
        {
            this.buffer = new PacketBuffer(Unpooled.copiedBuffer(data));
        }

        public PokecubeServerPacket(ByteBuf buffer)
        {
            this.buffer = (PacketBuffer) buffer;
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            if (buffer == null)
            {
                buffer = new PacketBuffer(Unpooled.buffer(buf.capacity()));
            }
            buffer.writeBytes(buf);
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            if (buffer == null)
            {
                buffer = new PacketBuffer(Unpooled.buffer(buf.capacity()));
            }
            buf.writeBytes(buffer);
        }
    }

    public static class StarterInfo
    {
        public static void processStarterInfo(String[] infos)
        {
            specialStarters.clear();
            for (String s : infos)
            {
                String[] data = s.split(":");
                if (data.length < 2)
                {
                    continue;
                }
                String username = data[0].toLowerCase(java.util.Locale.ENGLISH);
                if (specialStarters.containsKey(username)) continue;
                String[] pokemonData = new String[data.length - 1];
                for (int i = 1; i < data.length; i++)
                {
                    pokemonData[i - 1] = data[i];
                }
                StarterInfo[] info = new StarterInfo[pokemonData.length];
                for (int i = 0; i < info.length; i++)
                {
                    String s1 = pokemonData[i];
                    String[] dat = s1.split(";");
                    String name = dat[0];
                    if (Database.getEntry(name) != null)
                    {
                        String s2 = dat.length > 1 ? dat[1] : "";
                        info[i] = new StarterInfo(name, s2);
                    }
                    else
                    {
                        String s2 = dat.length > 1 ? dat[1] : "";
                        info[i] = new StarterInfo(null, s2);
                    }
                }
                StarterInfoContainer cont = new StarterInfoContainer(info);
                specialStarters.put(username, cont);
            }
        }

        public final String  name;
        public final String  data;
        public int           red     = 255;
        public int           green   = 255;
        public int           blue    = 255;
        public boolean       shiny   = false;

        public String        ability = null;

        private List<String> moves   = Lists.newArrayList();

        public StarterInfo(String name, String data)
        {
            this.name = name;
            this.data = data;
            String[] stuff = data.split("`");

            if (stuff.length > 0) for (String s : stuff)
            {
                if (s.isEmpty()) continue;

                String arg1 = s.substring(0, 1);
                String arg2 = s.substring(1);
                if (arg1.equals("S"))
                {
                    shiny = true;
                }
                if (arg1.equals("R"))
                {
                    red = 0;
                }
                if (arg1.equals("G"))
                {
                    green = 0;
                }
                if (arg1.equals("B"))
                {
                    blue = 0;
                }
                if (arg1.equals("M"))
                {
                    moves.add(arg2);
                }
                if (arg1.equals("A"))
                {
                    ability = arg2;
                }
            }
        }

        public int getNumber()
        {
            PokedexEntry entry = Database.getEntry(name);
            return entry == null ? 0 : entry.getPokedexNb();
        }

        public ItemStack makeStack(EntityPlayer owner)
        {
            return makeStack(owner, 0);
        }

        public ItemStack makeStack(EntityPlayer owner, int number)
        {
            ItemStack ret = null;
            PokedexEntry entry = (name != null) ? Database.getEntry(name) : Database.getEntry(number);
            if (entry != null)
            {
                World worldObj = owner.getEntityWorld();
                IPokemob entity = (IPokemob) PokecubeMod.core.createEntityByPokedexNb(entry.getPokedexNb(), worldObj);
                if (entity != null)
                {
                    ((EntityLivingBase) entity).setHealth(((EntityLivingBase) entity).getMaxHealth());
                    entity.setPokemonOwnerByName(owner.getCachedUniqueIdString());
                    entity.setPokecubeId(0);
                    entity.setExp(Tools.levelToXp(entity.getExperienceMode(), 5), false, false);
                    if (shiny) entity.setShiny(true);
                    if (red == 0 && Database.getEntry(entry.getName() + "R") != null)
                        entity.changeForme(entry.getName() + "R");
                    if (green == 0 && Database.getEntry(entry.getName() + "G") != null)
                        entity.changeForme(entry.getName() + "G");
                    if (blue == 0 && Database.getEntry(entry.getName() + "B") != null)
                        entity.changeForme(entry.getName() + "B");
                    if (ability != null && AbilityManager.abilityExists(ability))
                    {
                        entity.setAbility(AbilityManager.getAbility(ability));
                    }
                    if (moves.size() > 4)
                    {
                        Collections.shuffle(moves);
                    }
                    for (int i = 0; i < Math.min(4, moves.size()); i++)
                    {
                        String move = moves.get(i);
                        if (MovesUtils.isMoveImplemented(move)) entity.setMove(i, move);
                    }

                    ItemStack item = PokecubeManager.pokemobToItem(entity);
                    ((Entity) entity).isDead = true;
                    return item;
                }

                return PokecubeSerializer.getInstance().starter(entry.getPokedexNb(), owner);
            }
            return ret;
        }

        @Override
        public String toString()
        {
            return name + " " + data;
        }
    }

    public static class StarterInfoContainer
    {
        public final StarterInfo[] info;

        public StarterInfoContainer(StarterInfo[] info)
        {
            this.info = info;
        }
    }

    public final static byte                            CHANNEL_ID_ChooseFirstPokemob = 0;
    public final static byte                            CHANNEL_ID_PokemobMove        = 1;

    public final static byte                            CHANNEL_ID_EntityPokemob      = 2;
    public final static byte                            CHANNEL_ID_HealTable          = 3;

    public final static byte                            CHANNEL_ID_PokemobSpawner     = 4;

    public final static byte                            CHANNEL_ID_STATS              = 6;

    public static boolean                               giveHealer                    = true;

    public static HashMap<String, StarterInfoContainer> specialStarters               = Maps.newHashMap();

    public static void init()
    {
        PokecubeMod.packetPipeline.registerMessage(PacketPC.class, PacketPC.class, PokecubeCore.getMessageID(),
                Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(PacketPC.class, PacketPC.class, PokecubeCore.getMessageID(),
                Side.SERVER);

        PokecubeMod.packetPipeline.registerMessage(PacketTrade.class, PacketTrade.class, PokecubeCore.getMessageID(),
                Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(PacketTrade.class, PacketTrade.class, PokecubeCore.getMessageID(),
                Side.SERVER);

        PokecubeMod.packetPipeline.registerMessage(PacketChoose.class, PacketChoose.class, PokecubeCore.getMessageID(),
                Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(PacketChoose.class, PacketChoose.class, PokecubeCore.getMessageID(),
                Side.SERVER);

        PokecubeMod.packetPipeline.registerMessage(PacketPokemobGui.class, PacketPokemobGui.class,
                PokecubeCore.getMessageID(), Side.SERVER);
        PokecubeMod.packetPipeline.registerMessage(PacketPokemobAttack.class, PacketPokemobAttack.class,
                PokecubeCore.getMessageID(), Side.SERVER);
        PokecubeMod.packetPipeline.registerMessage(PacketNickname.class, PacketNickname.class,
                PokecubeCore.getMessageID(), Side.SERVER);
        PokecubeMod.packetPipeline.registerMessage(PacketPokedex.class, PacketPokedex.class,
                PokecubeCore.getMessageID(), Side.SERVER);

        PokecubeMod.packetPipeline.registerMessage(PacketPokemobMessage.class, PacketPokemobMessage.class,
                PokecubeCore.getMessageID(), Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(PacketSound.class, PacketSound.class, PokecubeCore.getMessageID(),
                Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(PacketSyncTerrain.class, PacketSyncTerrain.class,
                PokecubeCore.getMessageID(), Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(PacketDataSync.class, PacketDataSync.class,
                PokecubeCore.getMessageID(), Side.CLIENT);

        PokecubeMod.packetPipeline.registerMessage(PacketPosSync.class, PacketPosSync.class,
                PokecubeCore.getMessageID(), Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(PacketPosSync.class, PacketPosSync.class,
                PokecubeCore.getMessageID(), Side.SERVER);

        PokecubeMod.packetPipeline.registerMessage(PacketChangeForme.class, PacketChangeForme.class,
                PokecubeCore.getMessageID(), Side.CLIENT);
        PokecubeMod.packetPipeline.registerMessage(PacketChangeForme.class, PacketChangeForme.class,
                PokecubeCore.getMessageID(), Side.SERVER);

    }

    public static void handlePokecenterPacket(byte[] packet, EntityPlayerMP sender)
    {
        if (sender.openContainer instanceof IHealer)
        {
            IHealer healer = (IHealer) sender.openContainer;
            healer.heal();
        }
    }

    private static void handleStatsPacketClient(PacketBuffer buffer, EntityPlayer player)
    {
        try
        {
            NBTTagCompound nbt = buffer.readNBTTagCompoundFromBuffer();
            if (nbt == null)
            {
                System.err.println("Error with the stats update packet");
                return;
            }
            StatsCollector.readFromNBT(nbt);
            if (nbt.getBoolean("hasSerializer"))
            {
                PokecubeSerializer.getInstance().clearInstance();
                AISaveHandler.clearInstance();
                GuiInfoMessages.clear();
                new GuiTeleport();
                PokecubeCore.registerSpawns();
            }
            else if (nbt.getBoolean("hasTerrain"))
            {
                NBTTagCompound tag = nbt.getCompoundTag("terrain");
                TerrainManager.getInstance().getTerrain(tag.getInteger("dimID"))
                        .addTerrain(TerrainSegment.readFromNBT(tag));
                Vector3 temp = Vector3.readFromNBT(tag, "village");
                if (temp != null) pokecube.core.client.gui.GuiPokedex.closestVillage.set(temp);
                else pokecube.core.client.gui.GuiPokedex.closestVillage.clear();
                player.openGui(PokecubeCore.instance, Config.GUIPOKEDEX_ID, player.getEntityWorld(), 0, 0, 0);
            }
            else if (nbt.getBoolean("toLoadTerrain"))
            {
                NBTTagCompound tag = nbt.getCompoundTag("terrain");
                TerrainManager.getInstance().getTerrain(tag.getInteger("dimID")).loadTerrain(nbt);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Bad thing happened reading stats packet again");
        }
    }

    public static PokecubeClientPacket makeClientPacket(byte channel, byte[] data)
    {
        byte[] packetData = new byte[data.length + 1];
        packetData[0] = channel;

        for (int i = 1; i < packetData.length; i++)
        {
            packetData[i] = data[i - 1];
        }
        return new PokecubeClientPacket(packetData);
    }

    public static PokecubeClientPacket makeClientPacket(byte channel, NBTTagCompound nbt)
    {
        PacketBuffer packetData = new PacketBuffer(Unpooled.buffer());
        packetData.writeByte(channel);
        packetData.writeNBTTagCompoundToBuffer(nbt);

        return new PokecubeClientPacket(packetData);
    }

    public static PokecubeServerPacket makeServerPacket(byte channel, byte[] data)
    {
        byte[] packetData = new byte[data.length + 1];
        packetData[0] = channel;

        for (int i = 1; i < packetData.length; i++)
        {
            packetData[i] = data[i - 1];
        }
        return new PokecubeServerPacket(packetData);
    }

    public static void sendToAll(IMessage toSend)
    {
        PokecubeMod.packetPipeline.sendToAll(toSend);
    }

    public static void sendToAllNear(IMessage toSend, Vector3 point, int dimID, double distance)
    {
        PokecubeMod.packetPipeline.sendToAllAround(toSend, new TargetPoint(dimID, point.x, point.y, point.z, distance));
    }

    public static void sendToClient(IMessage toSend, EntityPlayer player)
    {
        if (player == null)
        {
            System.out.println("null player");
            return;
        }
        if (!(player instanceof EntityPlayerMP))
        {
            new ClassCastException("Cannot cast " + player + " to EntityPlayerMP").printStackTrace();
            return;
        }
        PokecubeMod.packetPipeline.sendTo(toSend, (EntityPlayerMP) player);
    }

    public static void sendToServer(IMessage toSend)
    {
        PokecubeMod.packetPipeline.sendToServer(toSend);
    }
}
