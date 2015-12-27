/**
 *
 */
package pokecube.core.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.PokecubeItems;
import pokecube.core.mod_Pokecube;
import pokecube.core.ai.utils.AISaveHandler;
import pokecube.core.blocks.healtable.ContainerHealTable;
import pokecube.core.client.gui.GuiMoveMessages;
import pokecube.core.client.gui.GuiScrollableLists;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.events.StarterEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.megastuff.ItemMegastone;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.animations.MoveAnimationHelper;
import pokecube.core.moves.animations.MoveAnimationHelper.MoveAnimation;
import pokecube.core.moves.templates.Move_Explode;
import pokecube.core.moves.templates.Move_Utility;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import pokecube.core.utils.Tools;
import pokecube.core.utils.Vector4;
import thut.api.entity.Transporter;
import thut.api.entity.Transporter.TelDestination;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

/** @author Manchou */
public class PokecubePacketHandler
{
    public final static byte CHANNEL_ID_ChooseFirstPokemob = 0;
    public final static byte CHANNEL_ID_PokemobMove        = 1;
    public final static byte CHANNEL_ID_EntityPokemob      = 2;
    public final static byte CHANNEL_ID_HealTable          = 3;
    public final static byte CHANNEL_ID_PokemobSpawner     = 4;
    public final static byte CHANNEL_ID_STATS              = 6;

    public static boolean giveHealer    = true;
    public static boolean serverOffline = false;

    private static void handlePacketGuiChooseFirstPokemobServer(byte[] packet, EntityPlayer player)
    {
        try
        {
            DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet));
            int pokedexNb = inputStream.readInt();
            boolean fixed = inputStream.readBoolean();

            String username = player.getName();

            if (PokecubeSerializer.getInstance().hasStarter(player)) { return; }
            PokecubeSerializer.getInstance().setHasStarter(player);
            List<ItemStack> items = new ArrayList<ItemStack>();
            ItemStack pokecubesItemStack = new ItemStack(PokecubeItems.getEmptyCube(0), 10);
            items.add(pokecubesItemStack);

            if (giveHealer && !fixed)
            {
                ItemStack pokecenterItemStack = new ItemStack(PokecubeItems.pokecenter);
                items.add(pokecenterItemStack);
            }
            ItemStack pokedexItemStack = new ItemStack(PokecubeItems.pokedex);
            items.add(pokedexItemStack);

            username = username.toLowerCase();

            if (!specialStarters.containsKey(username) || fixed)
            {
                ItemStack pokemobItemstack = PokecubeSerializer.getInstance().starter(pokedexNb, player);
                items.add(pokemobItemstack);
            }
            else
            {
                boolean starterGiven = false;
                StarterInfo[] starter = specialStarters.get(username);

                player.addStat(PokecubeMod.get1stPokemob, 1);
                for (StarterInfo i : starter)
                {
                    if (i == null)
                    {
                        if (!starterGiven)
                        {
                            starterGiven = true;
                            ItemStack pokemobItemstack = PokecubeSerializer.getInstance().starter(pokedexNb, player);
                            items.add(pokemobItemstack);
                        }
                    }
                    else
                    {
                        ItemStack start = i.makeStack(player);
                        items.add(start);
                    }

                }
            }
            ItemStack[] itemArr = items.toArray(new ItemStack[0]);
            if (!fixed)
            {
                StarterEvent evt = new StarterEvent(player, itemArr.clone(), pokedexNb);
                MinecraftForge.EVENT_BUS.post(evt);
                itemArr = evt.starterPack.clone();
            }
            player.addStat(PokecubeMod.get1stPokemob, 1);
            for (ItemStack e : itemArr)
            {
                if (e == null) continue;

                player.inventory.addItemStackToInventory(e);
                pokedexNb = PokecubeManager.getPokedexNb(e);
                if (pokedexNb > 0)
                {
                    StatsCollector.addCapture(PokecubeManager.itemToPokemob(e, player.worldObj));
                }
            }

            PokecubeSerializer.getInstance().save();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static HashMap<String, StarterInfo[]> specialStarters = new HashMap<String, StarterInfo[]>();

    private static void handlePacketGuiChooseFirstPokemobClient(byte[] packet, EntityPlayer player)
    {
        if (player == null)
        {
            new NullPointerException("Null Player while recieving starter packet");
            return;
        }

        String username = player.getName().toLowerCase();
        ByteBuf buf = Unpooled.buffer().writeBytes(packet);

        boolean bool = buf.readBoolean();
        if (!bool)
        {
            boolean bool2 = buf.readBoolean();
            if (bool2)
            {
                PokecubeSerializer.getInstance().setHasStarter(player, false);
                return;
            }
        }

        if (bool)
        {
            ArrayList<Integer> starters = new ArrayList<Integer>();
            int i = -1;
            while ((i = buf.readInt()) != 0)
            {
                starters.add(i);
            }
            pokecube.core.client.gui.GuiNewChooseFirstPokemob.starters = starters.toArray(new Integer[0]);

            player.openGui(mod_Pokecube.instance, Mod_Pokecube_Helper.GUICHOOSEFIRSTPOKEMOB_ID, player.worldObj, 0, 1,
                    0);
            return;
        }

        StarterEvent.Pre evt = new StarterEvent.Pre(player);
        MinecraftForge.EVENT_BUS.post(evt);
        boolean special = specialStarters.containsKey(username);
        if (!special || (evt.isCanceled() && evt.getResult() != Result.DENY))
        {
            player.openGui(mod_Pokecube.instance, Mod_Pokecube_Helper.GUICHOOSEFIRSTPOKEMOB_ID, player.worldObj, 0, 0,
                    0);
        }
        else
        {
            StarterInfo[] starter = specialStarters.get(username);
            for (StarterInfo i : starter)
            {
                if (i == null)
                {
                    player.openGui(mod_Pokecube.instance, Mod_Pokecube_Helper.GUICHOOSEFIRSTPOKEMOB_ID, player.worldObj,
                            0, 0, 0);
                    return;
                }
            }
            // ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
            // DataOutputStream outputStream = new DataOutputStream(bos);
            //// TODO see what this was doing
            // try
            // {
            // outputStream.writeInt(-1);
            // PokecubeServerPacket pack = PokecubePacketHandler
            // .makeServerPacket(PokecubeServerPacket.EDITPOKEMOB,
            // bos.toByteArray());
            // PokecubePacketHandler.sendToServer(pack);
            // }
            // catch (Exception ex)
            // {
            // ex.printStackTrace();
            // }
        }
    }

    public static class StarterInfo
    {
        public final String name;
        public final String data;
        public byte         red   = 127;
        public byte         green = 127;
        public byte         blue  = 127;
        public boolean      shiny = false;

        private List<String> moves = Lists.newArrayList();

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
            }
        }

        public int getNumber()
        {
            PokedexEntry entry = Database.getEntry(name);
            return entry == null ? 0 : entry.getPokedexNb();
        }

        public ItemStack makeStack(EntityPlayer owner)
        {
            ItemStack ret = null;
            PokedexEntry entry = Database.getEntry(name);
            if (entry != null)
            {
                World worldObj = owner.worldObj;
                IPokemob entity = (IPokemob) PokecubeMod.core.createEntityByPokedexNb(entry.getPokedexNb(), worldObj);
                if (entity != null)
                {
                    ((EntityLivingBase) entity).setHealth(((EntityLivingBase) entity).getMaxHealth());
                    entity.setPokemonOwnerByName(owner.getUniqueID().toString());
                    entity.setPokecubeId(0);
                    entity.setExp(Tools.levelToXp(entity.getExperienceMode(), 5), false, false);
                    if (shiny) entity.setShiny(true);
                    entity.setColours(new byte[] { red, green, blue });

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

        public static void processStarterInfo(String[] infos)
        {
            for (String s : infos)
            {
                String[] data = s.split(":");
                if (data.length < 2)
                {
                    continue;
                }
                String username = data[0].toLowerCase();
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
                        info[i] = null;
                    }
                }
                specialStarters.put(username, info);
            }
        }

        @Override
        public String toString()
        {
            return name + " " + data;
        }
    }

    public static void handlePokecenterPacket(byte[] packet, EntityPlayerMP sender)
    {
        if (sender.openContainer instanceof ContainerHealTable)
        {
            ContainerHealTable containerHealTable = (ContainerHealTable) sender.openContainer;
            containerHealTable.heal();
        }
    }

    public static void handlePokemobSpawnerPacket(byte[] packet, EntityPlayerMP sender)
    {

    }

    public static void handlePokemobMoveClientAnimation(byte[] packet)
    {
        try
        {
            String message = ChatAllowedCharacters.filterAllowedCharacters(new String(packet));

            String[] args = message.split("`");

            String moveName = args[0];
            int attackerId = Integer.valueOf(args[1]);
            int attackedId = Integer.valueOf(args[5]);
            Vector3 target = Vector3.getNewVectorFromPool().set(Double.valueOf(args[2]), Double.valueOf(args[3]),
                    Double.valueOf(args[4]));

            Move_Base move = MovesUtils.getMoveFromName(moveName);

            Entity attacker = FMLClientHandler.instance().getClient().theWorld.getEntityByID(attackerId);
            Entity attacked = FMLClientHandler.instance().getClient().theWorld.getEntityByID(attackedId);

            if (target.isEmpty() && attacked != null)
            {
                target.set(attacked);
            }

            if (move.animation != null && attacker != null)
            {
                MoveAnimation anim = new MoveAnimation(attacker, attacked, target, move, move.animation.getDuration());
                MoveAnimationHelper.Instance().addMove(attacker, anim);
            }
            else target.freeVectorFromPool();
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
                GuiMoveMessages.clear();
                new GuiScrollableLists();

                PokecubeSerializer.getInstance().readFromNBT(nbt);
            }
            else if (nbt.getBoolean("hasTerrain"))
            {
                NBTTagCompound tag = nbt.getCompoundTag("terrain");
                TerrainManager.getInstance().getTerrain(tag.getInteger("dimID"))
                        .addTerrain(TerrainSegment.readFromNBT(tag));
                Vector3 temp = Vector3.readFromNBT(tag, "village");
                if (temp != null) pokecube.core.client.gui.GuiPokedex.closestVillage.set(temp);
                else pokecube.core.client.gui.GuiPokedex.closestVillage.clear();

                if (temp != null) temp.freeVectorFromPool();
                player.openGui(mod_Pokecube.instance, Mod_Pokecube_Helper.GUIPOKEDEX_ID, player.worldObj, 0, 0, 0);
                System.out.println(player);
            }
            else if (nbt.getBoolean("toLoadTerrain"))
            {
                NBTTagCompound tag = nbt.getCompoundTag("terrain");
                TerrainManager.getInstance().getTerrain(tag.getInteger("dimID")).loadTerrain(nbt);
            }
            if (nbt.hasKey("serveroffline"))
            {
                serverOffline = nbt.getBoolean("serveroffline");
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Bad thing happened reading stats packet again");
        }
    }

    private static void handleStatsPacketServer(byte[] packet, EntityPlayer player)
    {
        byte message = 0;
        message = packet[0];
        EntityPlayerMP sender = (EntityPlayerMP) player;
        Vector3 v = Vector3.getNewVectorFromPool();
        if (message == 21)
        {
            byte[] arr = new byte[packet.length - 2];
            for (int i = 2; i < packet.length; i++)
            {
                arr[i - 2] = packet[i];
            }
            PacketBuffer dat = new PacketBuffer(Unpooled.buffer());
            dat.writeBytes(arr);
            int id = dat.readInt();
            int id1 = dat.readInt();

            if (!(player.worldObj.getEntityByID(id1) instanceof IPokemob)) return;

            IPokemob pokemob = (IPokemob) player.worldObj.getEntityByID(id1);
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

                    PokecubeClientPacket packe = new PokecubeClientPacket(PokecubeClientPacket.TELEPORTLIST, teletag);
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
                    }
                }
            }
        }
        if (message == 22)
        {
            boolean shift = sender.isSneaking();
            List<Entity> pokemobs = new ArrayList<Entity>(sender.worldObj.loadedEntityList);
            if (!shift)
            {
                for (Entity e : pokemobs)
                {
                    if (e instanceof IPokemob) if (((IPokemob) e).getPokemonAIState(IPokemob.TAMED)
                            && ((IPokemob) e).getPokemonOwner() == sender
                            && !((IPokemob) e).getPokemonAIState(IPokemob.STAYING)
                            && !((IPokemob) e).getPokemonAIState(IPokemob.GUARDING))
                    {

                        ((IPokemob) e).setPokemonAIState(IPokemob.SITTING,
                                !((IPokemob) e).getPokemonAIState(IPokemob.SITTING));
                    }
                }
            }
        }
    }

    public static void sendToServer(IMessage toSend)
    {
        PokecubeMod.packetPipeline.sendToServer(toSend);
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

    public static void sendToAll(IMessage toSend)
    {
        PokecubeMod.packetPipeline.sendToAll(toSend);
    }

    public static void sendToAllNear(IMessage toSend, Vector3 point, int dimID, double distance)
    {
        PokecubeMod.packetPipeline.sendToAllAround(toSend, new TargetPoint(dimID, point.x, point.y, point.z, distance));
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

    public static class PokecubeClientPacket implements IMessage
    {

        public static final byte CHOOSE1ST      = 0;
        public static final byte MOVEANIMATION  = 1;
        public static final byte TERRAIN        = 5;
        public static final byte STATS          = 6;
        public static final byte TERRAINEFFECTS = 7;
        public static final byte TELEPORTLIST   = 8;
        public static final byte MOVEMESSAGE    = 10;
        public static final byte KILLENTITY     = 11;
        public static final byte MOVEENTITY     = 12;
        public static final byte TELEPORTINDEX  = 13;
        public static final byte CHANGEFORME    = 14;

        public static final byte WIKIWRITE = 15;

        PacketBuffer buffer;

        public PokecubeClientPacket()
        {
        };

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

        public static class PokecubeMessageHandlerClient
                implements IMessageHandler<PokecubeClientPacket, PokecubeServerPacket>
        {

            public void handleClientSide(EntityPlayer player, PacketBuffer buffer)
            {
                byte channel = buffer.readByte();
                byte[] message = new byte[buffer.array().length - 1];
                for (int i = 0; i < message.length; i++)
                {
                    message[i] = buffer.array()[i + 1];
                }
                if (channel == CHOOSE1ST)
                {
                    handlePacketGuiChooseFirstPokemobClient(message, player);
                }
                else if (channel == MOVEANIMATION)
                {
                    handlePokemobMoveClientAnimation(message);
                }
                else if (channel == TERRAIN)
                {
                    try
                    {
                        NBTTagCompound nbt = buffer.readNBTTagCompoundFromBuffer();
                        TerrainSegment t = TerrainSegment.readFromNBT(nbt);
                        TerrainManager.getInstance().getTerrain(player.worldObj).addTerrain(t);

                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                else if (channel == STATS)
                {
                    handleStatsPacketClient(buffer, player);
                }
                else if (channel == TERRAINEFFECTS)
                {
                    TerrainSegment t = TerrainManager.getInstance().getTerrain(player.worldObj)
                            .getTerrain(buffer.readInt(), buffer.readInt(), buffer.readInt());

                    PokemobTerrainEffects effect = (PokemobTerrainEffects) t.geTerrainEffect("pokemobEffects");
                    if (effect == null)
                    {
                        t.addEffect(effect = new PokemobTerrainEffects(), "pokemobEffects");
                    }
                    for (int i = 0; i < 16; i++)
                    {
                        effect.effects[i] = buffer.readLong();
                    }
                }
                else if (channel == TELEPORTLIST)
                {
                    try
                    {
                        NBTTagCompound nbt = buffer.readNBTTagCompoundFromBuffer();
                        PokecubeSerializer.getInstance().readPlayerTeleports(nbt);
                        GuiScrollableLists.instance().refresh();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                else if (channel == MOVEMESSAGE)
                {
                    try
                    {
                        NBTTagCompound nbt = buffer.readNBTTagCompoundFromBuffer();
                        int id = nbt.getInteger("id");
                        String mess = nbt.getString("message");
                        Entity e = player.worldObj.getEntityByID(id);
                        if (e != null && e instanceof IPokemob)
                        {
                            ((IPokemob) e).displayMessageToOwner(mess);
                        }
                        else if (e instanceof EntityPlayer)
                        {
                            pokecube.core.client.gui.GuiMoveMessages.addMessage(mess);
                        }

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
                        if (player.worldObj.getEntityByID(id) != null) player.worldObj.getEntityByID(id).setDead();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                else if (channel == MOVEENTITY)
                {
                    int id = buffer.readInt();
                    Entity e = player.worldObj.getEntityByID(id);
                    Vector3 v = Vector3.readFromBuff(buffer);

                    if (e != null)
                    {
                        v.moveEntity(e);
                    }
                }
                else if (channel == TELEPORTINDEX)
                {
                    PokecubeServerPacket packet = new PokecubeServerPacket(new byte[] { PokecubeServerPacket.TELEPORT,
                            (byte) GuiScrollableLists.instance().indexLocation });
                    PokecubePacketHandler.sendToServer(packet);
                }
                else if (channel == CHANGEFORME)
                {
                    try
                    {
                        NBTTagCompound nbt = buffer.readNBTTagCompoundFromBuffer();
                        int id = nbt.getInteger("id");
                        String forme = nbt.getString("forme");
                        if (player.worldObj.getEntityByID(id) != null)
                        {
                            PokedexEntry entry = ((IPokemob) player.worldObj.getEntityByID(id)).getPokedexEntry()
                                    .getForm(forme);
                            ((IPokemob) player.worldObj.getEntityByID(id)).setPokedexEntry(entry);
                        }

                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                else if (channel == WIKIWRITE)
                {
                    int number = buffer.readInt();
                    System.out.println(number);
                    // WikiWriter.setCaptureTarget(number);
                    // WikiWriter.beginGifCapture();//TODO re-instate this

                }
            }

            @Override
            public PokecubeServerPacket onMessage(PokecubeClientPacket message, MessageContext ctx)
            {
                EntityPlayer player = mod_Pokecube.getPlayer(null);
                handleClientSide(player, message.buffer);

                return null;
            }
        }
    }

    public static class PokecubeServerPacket implements IMessage
    {

        public static final byte CHOOSE1ST      = 0;
        public static final byte POKECENTER     = 3;
        public static final byte POKEMOBSPAWNER = 4;
        public static final byte POKEDEX        = 5;
        public static final byte STATS          = 6;
        public static final byte POKECUBEUSE    = 7;
        public static final byte TELEPORT       = 9;
        public static final byte MEGAEVOLVE     = 17;

        PacketBuffer buffer;

        public PokecubeServerPacket()
        {
        };

        public PokecubeServerPacket(byte[] data)
        {
            this.buffer = new PacketBuffer(Unpooled.copiedBuffer(data));
        }

        public PokecubeServerPacket(ByteBuf buffer)
        {
            this.buffer = (PacketBuffer) buffer;
        }

        public PokecubeServerPacket(byte channel, NBTTagCompound nbt)
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

        public static class PokecubeMessageHandlerServer implements IMessageHandler<PokecubeServerPacket, IMessage>
        {

            public void handleServerSide(EntityPlayer player, PacketBuffer buffer)
            {
                byte channel = buffer.readByte();
                byte[] message = new byte[buffer.array().length - 1];

                for (int i = 0; i < message.length; i++)
                {
                    message[i] = buffer.array()[i + 1];
                }
                if (channel == CHOOSE1ST)
                {
                    handlePacketGuiChooseFirstPokemobServer(message, player);
                }
                else if (channel == 1)
                {
                    new Exception().printStackTrace();
                    // handlePokemobMoveClientAnimation(message);
                }
                else if (channel == POKECENTER)
                {
                    handlePokecenterPacket(message, (EntityPlayerMP) player);
                }
                else if (channel == POKEMOBSPAWNER)
                {
                    handlePokemobSpawnerPacket(message, (EntityPlayerMP) player);
                }
                else if (channel == POKEDEX)
                {
                    byte index = buffer.readByte();
                    if (player.getHeldItem() != null && player.getHeldItem().getItem() == PokecubeItems.pokedex
                            && index >= 0)
                    {
                        player.getHeldItem().setItemDamage(index);
                    }
                    else
                    {
                        int w = buffer.readInt();
                        float x = buffer.readFloat();
                        float y = buffer.readFloat();
                        float z = buffer.readFloat();

                        if (index == -1)
                        {
                            String name = buffer.readStringFromBuffer(20);
                            Vector4 vec = null;
                            vec = new Vector4(x, y, z, w);
                            if (vec != null)
                            {
                                PokecubeSerializer.getInstance().setTeleport(vec, player.getUniqueID().toString(),
                                        name);
                                player.addChatMessage(
                                        new ChatComponentText("Set The location " + vec.toIntString() + " as " + name));
                                PokecubeSerializer.getInstance().save();

                                NBTTagCompound teletag = new NBTTagCompound();
                                PokecubeSerializer.getInstance().writePlayerTeleports(player.getUniqueID(), teletag);

                                PokecubeClientPacket packet = new PokecubeClientPacket(
                                        PokecubeClientPacket.TELEPORTLIST, teletag);
                                PokecubePacketHandler.sendToClient(packet, player);
                            }
                        }
                        else if (index == -2)
                        {
                            Vector4 vec = null;
                            vec = new Vector4(x, y, z, w);
                            if (vec != null)
                            {
                                player.addChatMessage(
                                        new ChatComponentText("Removed The location " + vec.toIntString()));
                                PokecubeSerializer.getInstance().unsetTeleport(vec, player.getUniqueID().toString());
                                PokecubeSerializer.getInstance().save();

                                NBTTagCompound teletag = new NBTTagCompound();
                                PokecubeSerializer.getInstance().writePlayerTeleports(player.getUniqueID(), teletag);

                                PokecubeClientPacket packet = new PokecubeClientPacket((byte) 8, teletag);
                                PokecubePacketHandler.sendToClient(packet, player);
                            }
                        }
                    }
                }
                else if (channel == STATS)
                {
                    handleStatsPacketServer(message, player);
                }
                else if (channel == POKECUBEUSE)
                {
                    if (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof IPokecube)
                    {
                        Vector3 targetLocation = Vector3.getNewVectorFromPool();

                        long time = player.getEntityData().getLong("lastThrow");
                        if (time == player.worldObj.getTotalWorldTime()) return;
                        player.getEntityData().setLong("lastThrow", player.worldObj.getTotalWorldTime());

                        int id = buffer.readInt();
                        Entity target = player.worldObj.getEntityByID(id);
                        if (target != null && target instanceof IPokemob) targetLocation.set(target);
                        boolean used = ((IPokecube) player.getHeldItem().getItem()).throwPokecube(player.worldObj,
                                player, player.getHeldItem(), targetLocation, target);
                        targetLocation.freeVectorFromPool();
                        if (player.getHeldItem() != null && !(!PokecubeManager.isFilled(player.getHeldItem())
                                && player.capabilities.isCreativeMode) && used)
                        {
                            player.getHeldItem().stackSize--;
                            if (player.getHeldItem().stackSize == 0)
                            {
                                int current = player.inventory.currentItem;
                                player.inventory.mainInventory[current] = null;
                                player.inventory.markDirty();
                            }
                        }
                    }
                }
                else if (channel == TELEPORT)
                {
                    int index = message[0];
                    PokecubeSerializer.getInstance().setTeleIndex(player.getUniqueID().toString(), index);
                    TeleDest d = PokecubeSerializer.getInstance().getTeleport(player.getUniqueID().toString());
                    if (d == null) return;

                    Vector3 loc = d.getLoc();
                    int dim = d.getDim();

                    World dest = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dim);

                    TelDestination link = new TelDestination(dest, loc.getAABB(), loc.x, loc.y, loc.z, loc.intX(),
                            loc.intY(), loc.intZ());
                    Transporter.teleportEntity(player, link);
                    Transporter.teleportEntity(player, loc, dim, false);
                }
                else if (channel == MEGAEVOLVE)
                {
                    int id = buffer.readInt();
                    System.out.println("test");
                    EntityPokemob pokemob = (EntityPokemob) PokecubeSerializer.getInstance().getPokemob(id);

                    if (pokemob == null)
                    {
                        System.err.println(id + " " + player.worldObj);
                        new Exception().printStackTrace();
                        return;
                    }
                    if (pokemob.getPokemonAIState(IMoveConstants.EVOLVING)) return;

                    int happiness = pokemob.getHappiness();
                    ItemStack held = pokemob.getHeldItem();
                    if (held == null || !(held.getItem() instanceof ItemMegastone)
                            || (happiness < 255 && !player.capabilities.isCreativeMode))
                    {
                        player.addChatMessage(new ChatComponentText(
                                pokemob.getPokemonDisplayName() + " is not currently able to Mega evolve"));
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

                        if (pokemob.getPokedexEntry() == Database.getEntry(forme))
                        {
                            pokemob.megaEvolve(pokemob.getPokedexEntry().getBaseName());
                        }
                        else
                        {
                            pokemob.megaEvolve(forme);
                        }
                    }

                }

            }

            @Override
            public PokecubeServerPacket onMessage(PokecubeServerPacket message, MessageContext ctx)
            {
                EntityPlayer player = ctx.getServerHandler().playerEntity;
                handleServerSide(player, message.buffer);

                return null;
            }
        }
    }

}
