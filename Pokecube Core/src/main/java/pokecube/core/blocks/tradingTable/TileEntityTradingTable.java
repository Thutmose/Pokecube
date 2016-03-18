package pokecube.core.blocks.tradingTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import jdk.nashorn.internal.runtime.regexp.joni.constants.Arguments;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.blocks.pc.TileEntityPC;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.TradeEvent;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemTM;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.PCPacketHandler;
import pokecube.core.network.PCPacketHandler.MessageClient;
import pokecube.core.network.PokecubePacketHandler;
import thut.api.maths.Vector3;

@Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")
public class TileEntityTradingTable extends TileEntityOwnable implements IInventory, SimpleComponent
{
    private static class TMCConverter
    {
        final TileEntityTradingTable toConvert;

        public TMCConverter(TileEntityTradingTable tile)
        {
            toConvert = tile;
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void convert(WorldTickEvent event)
        {
            if (event.world.isRemote)
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                return;
            }

            if (!event.world.isAreaLoaded(toConvert.getPos(), 5)) return;

            if (event.phase == Phase.END)
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                boolean pc = false;
                toConvert.pc = null;
                for (EnumFacing side : EnumFacing.values())
                {
                    Vector3 here = Vector3.getNewVector().set(toConvert);
                    Block id = here.offset(side).getBlock(toConvert.worldObj);
                    if (id == PokecubeItems.getBlock("pc"))
                    {
                        pc = true;
                        toConvert.pc = (TileEntityPC) here.offset(side).getTileEntity(toConvert.getWorld());
                        break;
                    }
                }
                toConvert.trade = !pc;

                if (!toConvert.trade)
                {
                    IBlockState state = toConvert.worldObj.getBlockState(toConvert.getPos());
                    if (!(Boolean) state.getValue(BlockTradingTable.TMC))
                    {
                        toConvert.worldObj.setBlockState(toConvert.getPos(),
                                state.withProperty(BlockTradingTable.TMC, true));
                    }
                }
            }
        }
    }
    public static boolean theftEnabled = false;

    private ItemStack[] inventory  = new ItemStack[2];
    private ItemStack[] inventory2 = new ItemStack[1];

    public EntityPlayer player1;

    public EntityPlayer player2;

    public HashMap<String, ArrayList<String>> moves = new HashMap<String, ArrayList<String>>();

    public int time = 0;
    public boolean       trade = true;
    public int           renderpass;
    boolean              init  = true;

    private TileEntityPC pc;

    public TileEntityTradingTable()
    {
        super();
    }

    public void addMoveToTM(String move)
    {
        ItemStack tm = inventory2[0];
        if (tm != null && tm.getItem() instanceof ItemTM)
        {
            ItemTM.addMoveToStack(move, tm);
        }
    }

    public void addPlayer(EntityPlayer player)
    {
        if (player == null && !worldObj.isRemote)
        {
            player1 = null;
            player2 = null;
            String message = 9 + "," + getPos().getX() + "," + getPos().getY() + "," + getPos().getZ() + ",0,0";
            Vector3 point = Vector3.getNewVector().set(player);
            MessageClient packet = PCPacketHandler.makeClientPacket(MessageClient.TRADE, message.getBytes());
            PokecubePacketHandler.sendToAllNear(packet, point, worldObj.provider.getDimension(), 10);
            return;
        }
        if (inventory[0] != null)
        {
            if (PokecubeManager.isFilled(inventory[0])
                    && (PokecubeManager.getOwner(inventory[0]).equals(player.getUniqueID().toString())
            // || player.worldObj.isRemote
            ))
            {
                if (player1 == null) player1 = player;
                else
                {
                    player1 = null;
                    player2 = null;
                }

                if (!player.worldObj.isRemote)
                {
                    String message = 9 + "," + getPos().getX() + "," + getPos().getY() + "," + getPos().getZ() + ","
                            + player.getEntityId();
                    if (player1 == null)
                    {
                        message += "," + 0;
                    }
                    Vector3 point = Vector3.getNewVector().set(player);
                    MessageClient packet = PCPacketHandler.makeClientPacket(MessageClient.TRADE, message.getBytes());
                    PokecubePacketHandler.sendToAllNear(packet, point, player.dimension, 10);
                }
                if (player2 == null)
                {
                    player2 = player1;
                    tryChange();
                    player2 = null;
                    return;
                }
            }
        }
        if (inventory[1] != null)
        {
            if (PokecubeManager.isFilled(inventory[1])
                    && (PokecubeManager.getOwner(inventory[1]).equals(player.getUniqueID().toString())))
            {
                if (player2 == null) player2 = player;
                else
                {
                    player1 = null;
                    player2 = null;
                }

                if (!player.worldObj.isRemote)
                {
                    String message = 9 + "," + getPos().getX() + "," + getPos().getY() + "," + getPos().getZ() + ","
                            + player.getEntityId();

                    if (player2 == null)
                    {
                        message += "," + 0;
                    }

                    Vector3 point = Vector3.getNewVector().set(player);
                    MessageClient packet = PCPacketHandler.makeClientPacket(MessageClient.TRADE, message.getBytes());
                    PokecubePacketHandler.sendToAllNear(packet, point, player.dimension, 10);
                }
                if (player1 == null)
                {
                    player1 = player2;
                    tryChange();
                    player1 = null;
                    return;
                }
            }
        }
        trade();
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] applyMove(Context context, Arguments args) throws Exception
    {
        if (hasPC() && pc.isBound())
        {
            InventoryPC inv = pc.getPC();
            ArrayList<String> moves = getMoves(inv);
            String move = args.checkString(0);
            for (String s : moves)
            {
                if (s.equalsIgnoreCase(move))
                {
                    addMoveToTM(s);
                    return new Object[] {};
                }
            }
            throw new Exception("requested move not found");
        }
        if (!hasPC()) throw new Exception("no connected PC");
        else throw new Exception("connected PC is not bound to a player");
    }

    @Override
    public void clear()
    {

    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public ItemStack decrStackSize(int slot, int count)
    {
        if (trade)
        {
            if (this.inventory[slot] != null)
            {
                ItemStack itemStack;

                itemStack = inventory[slot].splitStack(count);

                if (inventory[slot].stackSize <= 0) inventory[slot] = null;

                return itemStack;
            }
        }
        else if (slot < inventory2.length && this.inventory2[slot] != null)
        {
            ItemStack itemStack;

            itemStack = inventory2[slot].splitStack(count);

            if (inventory2[slot].stackSize <= 0) inventory2[slot] = null;

            return itemStack;
        }

        return null;
    }

    @Override
    public String getComponentName()
    {
        return "tradingtable";
    }

    /** Overriden in a sign to provide the text. */
    @SuppressWarnings("rawtypes")
    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (worldObj.isRemote) return new S35PacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return null;
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    public String[] getForgottenMoves(IPokemob mob)
    {
        PokedexEntry entry = Database.getEntry(mob.getPokedexNb());
        String[] moves = null;
        List<String> list = new ArrayList<String>();

        list:
        for (String s : entry.getMovesForLevel(mob.getLevel()))
        {
            for (String s1 : mob.getMoves())
            {
                if (s1 != null && s1.equalsIgnoreCase(s)) continue list;
            }
            list.add(s);
        }
        moves = list.toArray(new String[0]);

        return moves;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared()
    {
        return 65536.0D;
    }

    public ArrayList<String> getMoves(InventoryPC pcInv)
    {
        ArrayList<String> moves = new ArrayList<String>();
        HashSet<ItemStack> stacks = pcInv.getContents();

        for (ItemStack stack : stacks)
        {
            if (PokecubeManager.isFilled(stack))
            {
                IPokemob mob = PokecubeManager.itemToPokemob(stack, worldObj);

                if (mob == null)
                {
                    System.err.println("Corrupted Pokemon in PC");
                    continue;
                }

                String[] forgotten = getForgottenMoves(mob);
                String[] current = mob.getMoves();
                for (String s : forgotten)
                {
                    if (s == null || s.contentEquals("") || !MovesUtils.isMoveImplemented(s)) continue;

                    boolean toAdd = true;
                    if (moves.size() > 0) for (String s1 : moves)
                    {
                        if (s1 == null || s1.contentEquals("") || !MovesUtils.isMoveImplemented(s1)) continue;
                        if (s1.contentEquals(s)) toAdd = false;
                    }
                    if (toAdd) moves.add(s);
                }
                for (String s : current)
                {
                    if (s == null || s.contentEquals("") || !MovesUtils.isMoveImplemented(s)) continue;

                    boolean toAdd = true;
                    if (moves.size() > 0) for (String s1 : moves)
                    {
                        if (s1 == null)
                        {
                            continue;
                        }
                        if (s1.contentEquals(s)) toAdd = false;
                    }
                    if (toAdd) moves.add(s);
                }
            }
        }
        Collections.sort(moves);
        return moves;
    }

    public ArrayList<String> getMoves(String playerName)
    {
        return moves.get(playerName);
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getMovesList(Context context, Arguments args) throws Exception
    {
        if (hasPC() && pc.isBound())
        {
            InventoryPC inv = pc.getPC();
            ArrayList<String> moves = getMoves(inv);
            return moves.toArray();
        }
        if (!hasPC()) throw new Exception("no connected PC");
        else throw new Exception("connected PC is not bound to a player");
    }

    @Override
    public String getName()
    {
        return "tradingtable";
    }

    @Override
    public int getSizeInventory()
    {
        if (trade) return inventory.length;
        else return inventory2.length;
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        if (trade) return inventory[i];
        if (i < inventory2.length) return inventory2[i];
        return null;
    }

    @Override
    public boolean hasCustomName()
    {
        return true;
    }

    protected boolean hasPC()
    {
        boolean pc = false;
        this.pc = null;
        for (EnumFacing side : EnumFacing.values())
        {
            Vector3 here = Vector3.getNewVector().set(this);
            Block id = here.offset(side).getBlock(worldObj);
            if (id == PokecubeItems.getBlock("pc"))
            {
                pc = true;
                this.pc = (TileEntityPC) here.offset(side).getTileEntity(getWorld());
                break;
            }
        }
        trade = !pc;

        if (!trade)
        {
            IBlockState state = worldObj.getBlockState(getPos());
            if (!(Boolean) state.getValue(BlockTradingTable.TMC))
            {
                worldObj.setBlockState(getPos(), state.withProperty(BlockTradingTable.TMC, true));
            }
        }
        return pc;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return true;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return worldObj.getTileEntity(getPos()) == this
                && player.getDistanceSq(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5) < 64;
    }

    public ArrayList<String> moves(EntityPlayer player)
    {
        if (!player.worldObj.isRemote)
        {
            boolean pc = hasPC();
            if (!pc) { return Lists.newArrayList(); }
            InventoryPC pcInv = InventoryPC.getPC(player.getUniqueID().toString());
            ArrayList<String> moves = getMoves(pcInv);
            Collections.sort(moves);
            String message = "" + 3 + "," + player.getUniqueID().toString();
            for (String s : moves)
                message += "," + s;

            MessageClient packet = PCPacketHandler.makeClientPacket(MessageClient.TRADE, message.getBytes());
            PokecubePacketHandler.sendToClient(packet, player);
            this.moves.put(player.getName(), moves);
            return moves;
        }
        else
        {
            return null;
        }

    }

    /** Called when you receive a TileEntityData packet for the location this
     * TileEntity is currently in. On the client, the NetworkManager will always
     * be the remote server. On the server, it will be whomever is responsible
     * for sending the packet.
     *
     * @param net
     *            The NetworkManager the packet originated from
     * @param pkt
     *            The data packet */
    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        if (worldObj.isRemote)
        {
            NBTTagCompound nbt = pkt.getNbtCompound();
            readFromNBT(nbt);
        }
    }

    @Override
    public void onLoad()
    {
        new TMCConverter(this);
    }

    public void openGUI(EntityPlayer player)
    {
        player.openGui(PokecubeMod.core, Config.GUITRADINGTABLE_ID, worldObj, getPos().getX(),
                getPos().getY(), getPos().getZ());
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    public void pokeseal(ItemStack a, ItemStack b, IPokemob mob)
    {
        if (b.hasTagCompound())
        {
            NBTTagCompound tag = b.getTagCompound().getCompoundTag("Explosion");
            NBTTagCompound mobtag = ((Entity) mob).getEntityData();
            mobtag.setTag("sealtag", tag);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
        NBTBase temp = tagCompound.getTag("Inventory");

        trade = tagCompound.getBoolean("trade");

        ItemStack[] both = new ItemStack[3];
        if (temp instanceof NBTTagList)
        {
            NBTTagList tagList = (NBTTagList) temp;
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);
                byte slot = tag.getByte("Slot");

                if (slot >= 0 && slot < both.length)
                {
                    both[slot] = ItemStack.loadItemStackFromNBT(tag);
                }
            }
        }
        inventory[0] = both[0];
        inventory[1] = both[1];
        inventory2[0] = both[2];
        init = false;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot)
    {
        if (trade)
        {
            if (inventory[slot] != null)
            {
                ItemStack stack = inventory[slot];
                inventory[slot] = null;
                return stack;
            }
        }
        else if (slot < inventory2.length && inventory2[slot] != null)
        {
            ItemStack stack = inventory2[slot];
            inventory2[slot] = null;
            return stack;
        }

        return null;
    }

    @Override
    public void setField(int id, int value)
    {

    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack)
    {
        if (trade) inventory[i] = itemstack;
        else if (i < inventory2.length) inventory2[i] = itemstack;
    }

    public void trade()
    {
        ItemStack poke1 = inventory[0];
        ItemStack poke2 = inventory[1];
        if (poke1 == null || poke2 == null || player1 == player2)
        {
            if (player1 == player2 && (poke1 != null && poke2 != null))
            {
                tryChange();
            }
            player1 = null;
            player2 = null;
            return;
        }

        if (!(PokecubeManager.isFilled(poke1) && PokecubeManager.isFilled(poke2))) { return; }

        IPokemob mon1 = PokecubeManager.itemToPokemob(poke1, worldObj);
        IPokemob mon2 = PokecubeManager.itemToPokemob(poke2, worldObj);

        String owner1 = PokecubeManager.getOwner(poke1);
        String owner2 = PokecubeManager.getOwner(poke2);

        String trader1 = player1.getUniqueID().toString();
        String trader2 = player2.getUniqueID().toString();

        if ((trader1.contentEquals(owner1) || trader1.contentEquals(owner2))
                && ((trader2.contentEquals(owner1) || trader2.contentEquals(trader2))))
        {

            boolean swap = false;
            if (!trader1.contentEquals(owner1))
            {
                mon1.setPokemonOwnerByName(trader1);
                mon2.setPokemonOwnerByName(trader2);
            }
            else
            {
                swap = true;
                mon2.setPokemonOwnerByName(trader1);
                mon1.setPokemonOwnerByName(trader2);
            }

            boolean mon1everstone = PokecubeManager.getHeldItemMainhand(poke1) != null
                    && PokecubeManager.getHeldItemMainhand(poke1).getItem() == PokecubeItems.everstone;
            boolean mon2everstone = PokecubeManager.getHeldItemMainhand(poke2) != null
                    && PokecubeManager.getHeldItemMainhand(poke2).getItem() == PokecubeItems.everstone;

            if (!mon1everstone) mon1.setTraded(true);
            if (!mon2everstone) mon2.setTraded(true);

            poke1 = PokecubeManager.pokemobToItem(mon1);
            poke2 = PokecubeManager.pokemobToItem(mon2);

            ItemStack to1 = swap ? poke2 : poke1;
            ItemStack to2 = swap ? poke1 : poke2;

            if (player1.inventory.getFirstEmptyStack() != -1) player1.inventory.addItemStackToInventory(to1);
            else InventoryPC.addPokecubeToPC(to1, worldObj);
            if (player2.inventory.getFirstEmptyStack() != -1) player2.inventory.addItemStackToInventory(to2);
            else InventoryPC.addPokecubeToPC(to2, worldObj);

            MinecraftForge.EVENT_BUS.post(new TradeEvent(worldObj, to1));
            MinecraftForge.EVENT_BUS.post(new TradeEvent(worldObj, to2));

            inventory = new ItemStack[2];
        }

        player1 = null;
        player2 = null;
    }

    public boolean tryChange()
    {
        ItemStack a = inventory[0];
        ItemStack b = inventory[1];

        if (player1 != player2 || player1 == null) return false;
        if (a == b || a == null || b == null) return false;

        if (!((PokecubeManager.isFilled(a)) || (PokecubeManager.isFilled(b)))) return false;

        if (a.getItem() instanceof IPokecube && b.getItem() instanceof IPokecube)
        {
            if (((PokecubeManager.isFilled(a)) && (PokecubeManager.isFilled(b)))) return false;

            IPokemob mob = PokecubeManager.isFilled(a) ? PokecubeManager.itemToPokemob(inventory[0], worldObj)
                    : PokecubeManager.itemToPokemob(inventory[1], worldObj);
            int id = PokecubeManager.isFilled(a) ? PokecubeItems.getCubeId(b) : PokecubeItems.getCubeId(b);
            if (id == 14)
            {
                pokeseal(a, b, mob);
            }
            else
            {
                mob.setPokemonOwnerByName(player1.getUniqueID().toString());
                mob.setPokecubeId(id);
            }
            player1.inventory.addItemStackToInventory(PokecubeManager.pokemobToItem(mob));
            player1 = null;
            player2 = null;
            inventory = new ItemStack[2];
            return true;
        }

        if (!(a.getItem() == Items.emerald || b.getItem() == Items.emerald)) return false;

        int index = PokecubeManager.isFilled(a) ? 0 : 1;

        IPokemob mob = PokecubeManager.isFilled(a) ? PokecubeManager.itemToPokemob(inventory[0], worldObj)
                : PokecubeManager.itemToPokemob(inventory[1], worldObj);
        if (!(mob.getPokemonOwnerName().equalsIgnoreCase(new UUID(1234, 4321).toString()) || theftEnabled))
            return false;

        mob.setTraded(true);
        mob.setPokemonOwnerByName(player1.getUniqueID().toString());
        player1.inventory.addItemStackToInventory(inventory[index]);

        inventory = new ItemStack[2];

        return true;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        NBTTagList itemList = new NBTTagList();
        ItemStack[] both = new ItemStack[3];
        both[0] = inventory[0];
        both[1] = inventory[1];
        both[2] = inventory2[0];

        for (int i = 0; i < both.length; i++)
        {
            ItemStack stack = both[i];

            if (stack != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        tagCompound.setBoolean("trade", trade);
        tagCompound.setTag("Inventory", itemList);
    }
}
