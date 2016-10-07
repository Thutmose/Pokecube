package pokecube.core.network.packets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.Village;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.PokedexEntry.SpawnData.SpawnEntry;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.PokedexInspector;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;

public class PacketPokedex implements IMessage, IMessageHandler<PacketPokedex, IMessage>
{
    public static final byte   REQUEST = -5;
    public static final byte   INSPECT = -4;
    public static final byte   VILLAGE = -3;
    public static final byte   REMOVE  = -2;
    public static final byte   RENAME  = -1;

    public static List<String> values  = Lists.newArrayList();

    public static void sendRenameTelePacket(String newName, Vector4 location)
    {
        PacketPokedex packet = new PacketPokedex();
        packet.message = RENAME;
        packet.data.setString("N", newName);
        location.writeToNBT(packet.data);
        PokecubePacketHandler.sendToServer(packet);
    }

    public static void sendRemoveTelePacket(Vector4 location)
    {
        PacketPokedex packet = new PacketPokedex();
        packet.message = REMOVE;
        location.writeToNBT(packet.data);
        PokecubePacketHandler.sendToServer(packet);
    }

    public static void sendChangePagePacket(byte page)
    {
        PacketPokedex packet = new PacketPokedex();
        packet.message = page;
        PokecubePacketHandler.sendToServer(packet);
    }

    public static void sendInspectPacket(boolean reward)
    {
        PacketPokedex packet = new PacketPokedex();
        packet.message = INSPECT;
        packet.data.setBoolean("R", reward);
        PokecubePacketHandler.sendToServer(packet);
    }

    public static void sendVillageInfoPacket(EntityPlayer player)
    {
        List<Village> villages = player.getEntityWorld().getVillageCollection().getVillageList();
        PacketPokedex packet = new PacketPokedex();
        if (villages.size() > 0)
        {
            final BlockPos pos = player.getPosition();
            Collections.sort(villages, new Comparator<Village>()
            {
                @Override
                public int compare(Village o1, Village o2)
                {
                    return (int) (pos.distanceSq(o1.getCenter()) - pos.distanceSq(o2.getCenter()));
                }
            });
            Vector3 temp = Vector3.getNewVector().set(villages.get(0).getCenter());
            temp.writeToNBT(packet.data, "village");
        }
    }

    byte                  message;
    public NBTTagCompound data = new NBTTagCompound();

    public PacketPokedex()
    {
    }

    public PacketPokedex(byte message)
    {
        this.message = message;
    }

    @Override
    public IMessage onMessage(final PacketPokedex message, final MessageContext ctx)
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

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(message);
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeNBTTagCompoundToBuffer(data);
    }

    void processMessage(MessageContext ctx, PacketPokedex message)
    {
        final EntityPlayer player;
        if (ctx.side == Side.CLIENT)
        {
            player = PokecubeCore.getPlayer(null);
        }
        else
        {
            player = ctx.getServerHandler().playerEntity;
        }
        if (message.message == REQUEST)
        {
            if (ctx.side == Side.CLIENT)
            {
                values.clear();
                int n = message.data.getKeySet().size();
                for (int i = 0; i < n; i++)
                {
                    values.add(message.data.getString("" + i));
                }
            }
            else
            {
                boolean mode = message.data.getBoolean("mode");
                PacketPokedex packet = new PacketPokedex(REQUEST);
                if (!mode)
                {
                    final Map<PokedexEntry, Float> rates = Maps.newHashMap();
                    final Vector3 pos = Vector3.getNewVector().set(player);
                    final SpawnCheck checker = new SpawnCheck(pos, player.worldObj);
                    ArrayList<PokedexEntry> names = new ArrayList<PokedexEntry>();
                    for (PokedexEntry e : Database.spawnables)
                    {
                        if (e.getSpawnData().getMatcher(checker, false) != null)
                        {
                            names.add(e);
                        }
                    }
                    Collections.sort(names, new Comparator<PokedexEntry>()
                    {
                        @Override
                        public int compare(PokedexEntry o1, PokedexEntry o2)
                        {
                            float rate1 = o1.getSpawnData().getWeight(o1.getSpawnData().getMatcher(checker, false))
                                    * 10e5f;
                            float rate2 = o2.getSpawnData().getWeight(o2.getSpawnData().getMatcher(checker, false))
                                    * 10e5f;
                            return (int) (-rate1 + rate2);
                        }
                    });
                    float total = 0;
                    for (PokedexEntry e : names)
                    {
                        SpawnBiomeMatcher matcher = e.getSpawnData().getMatcher(checker, false);
                        float val = e.getSpawnData().getWeight(matcher);
                        float min = e.getSpawnData().getMin(matcher);
                        float num = min + (e.getSpawnData().getMax(matcher) - min) / 2;
                        val *= num;
                        total += val;
                        rates.put(e, val);
                    }
                    for (PokedexEntry e : names)
                    {
                        float val = rates.get(e) * 100 / total;
                        rates.put(e, val);
                    }
                    packet.data.setString("0",
                            "" + TerrainManager.getInstance().getTerrainForEntity(player).getBiome(pos));
                    for (int i = 0; i < names.size(); i++)
                    {
                        PokedexEntry e = names.get(i);
                        packet.data.setString("" + (i + 1), e.getUnlocalizedName() + "`" + rates.get(e));
                    }
                }
                else
                {
                    List<String> biomes = Lists.newArrayList();
                    PokedexEntry entry = Database.getEntry(message.data.getString("forme"));
                    if (entry.getSpawnData() == null && entry.getChildNb() != entry.getPokedexNb())
                    {
                        PokedexEntry child;
                        if ((child = Database.getEntry(entry.getChildNb())).getSpawnData() != null)
                        {
                            entry = child;
                        }
                    }
                    SpawnData data = entry.getSpawnData();
                    if (data == null) return;
                    boolean hasBiomes = false;
                    Map<SpawnBiomeMatcher, SpawnEntry> matchers = data.matchers;
                    for (SpawnBiomeMatcher matcher : matchers.keySet())
                    {
                        String biomeString = matcher.spawnRule.values.get(SpawnBiomeMatcher.BIOMES);
                        String typeString = matcher.spawnRule.values.get(SpawnBiomeMatcher.TYPES);
                        if (biomeString != null) hasBiomes = true;
                        else if (typeString != null)
                        {
                            String[] args = typeString.split(",");
                            BiomeType subBiome = null;
                            for (String s : args)
                            {
                                for (BiomeType b : BiomeType.values())
                                {
                                    if (b.name.replaceAll(" ", "").equalsIgnoreCase(s))
                                    {
                                        subBiome = b;
                                        break;
                                    }
                                }
                                if (subBiome == null) hasBiomes = true;
                                subBiome = null;
                                if (hasBiomes) break;
                            }
                        }
                        if (hasBiomes) break;
                    }
                    if (hasBiomes) for (ResourceLocation key : Biome.REGISTRY.getKeys())
                    {
                        Biome b = Biome.REGISTRY.getObject(key);
                        if (b != null)
                        {
                            if (data.isValid(b)) biomes.add(b.getBiomeName());
                        }
                    }
                    for (BiomeType b : BiomeType.values())
                    {
                        if (data.isValid(b))
                        {
                            biomes.add(b.readableName);
                        }
                    }
                    for (int i = 0; i < biomes.size(); i++)
                    {
                        packet.data.setString("" + i, biomes.get(i));
                    }
                }
                PokecubeMod.packetPipeline.sendTo(packet, (EntityPlayerMP) player);
            }
            return;
        }
        if (message.message == REMOVE)
        {
            Vector4 location = new Vector4(message.data);
            PokecubeSerializer.getInstance().unsetTeleport(location, player.getCachedUniqueIdString());
            player.addChatMessage(new TextComponentString("Removed The location " + location.toIntString()));
            PokecubePlayerDataHandler.getInstance().save(player.getCachedUniqueIdString());
            PacketDataSync.sendInitPacket(player, "pokecube-data");
            return;
        }
        if (message.message == RENAME)
        {
            String name = message.data.getString("N");
            Vector4 location = new Vector4(message.data);
            PokecubeSerializer.getInstance().setTeleport(location, player.getCachedUniqueIdString(), name);
            player.addChatMessage(
                    new TextComponentString("Set The location " + location.toIntString() + " as " + name));
            PokecubePlayerDataHandler.getInstance().save(player.getCachedUniqueIdString());
            PacketDataSync.sendInitPacket(player, "pokecube-data");
            return;
        }
        if (message.message == VILLAGE)
        {
            Vector3 temp = Vector3.readFromNBT(message.data, "village");
            if (temp != null) pokecube.core.client.gui.GuiPokedex.closestVillage.set(temp);
            else pokecube.core.client.gui.GuiPokedex.closestVillage.clear();
            player.openGui(PokecubeCore.instance, Config.GUIPOKEDEX_ID, player.getEntityWorld(), 0, 0, 0);
            return;
        }
        if (message.message == INSPECT)
        {
            boolean reward = message.data.getBoolean("R");
            boolean inspected = PokedexInspector.inspect(player, reward);

            if (!reward)
            {
                if (inspected)
                {
                    player.addChatMessage(new TextComponentTranslation("pokedex.inspect.available"));
                }
            }
            else
            {
                if (!inspected)
                {
                    player.addChatMessage(new TextComponentTranslation("pokedex.inspect.nothing"));
                }
                player.closeScreen();
            }
            return;
        }
        player.getHeldItemMainhand().setItemDamage(message.message);
    }
}
