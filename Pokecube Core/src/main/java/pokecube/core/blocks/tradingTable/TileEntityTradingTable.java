package pokecube.core.blocks.tradingTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
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
import pokecube.core.items.pokecubes.RecipePokeseals;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.packets.PacketTrade;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

@Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")
public class TileEntityTradingTable extends TileEntityOwnable implements IInventory, SimpleComponent
{
    public static boolean                     theftEnabled = false;

    private ItemStack[]                       inventory    = new ItemStack[2];
    private ItemStack[]                       inventory2   = new ItemStack[1];

    public EntityPlayer                       player1;
    public EntityPlayer                       player2;

    public HashMap<String, ArrayList<String>> moves        = new HashMap<String, ArrayList<String>>();

    public int                                time         = 0;
    public boolean                            trade        = true;
    public int                                renderpass;
    boolean                                   init         = true;

    private TileEntityPC                      pc;

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
        throw new Exception("connected PC is not bound to a player");
    }

    @Override
    public void clear()
    {

    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
        if (!player.getEntityWorld().isRemote)
        {
            if (player.getCachedUniqueIdString().equals(PokecubeManager.getOwner(inventory[0])))
            {
                dropCube(inventory[0], player);
                this.setInventorySlotContents(0, null);
            }
            if (player.getCachedUniqueIdString().equals(PokecubeManager.getOwner(inventory[1])))
            {
                dropCube(inventory[1], player);
                this.setInventorySlotContents(1, null);
            }
        }
    }

    private void dropCube(ItemStack cube, EntityPlayer player)
    {
        if (cube != null)
        {
            if (player.isDead || player.getHealth() <= 0 || player.inventory.getFirstEmptyStack() == -1)
            {
                ItemTossEvent toss = new ItemTossEvent(player.entityDropItem(cube, 0F), null);
                MinecraftForge.EVENT_BUS.post(toss);
                if (!toss.isCanceled())
                {
                    player.dropItem(cube, true);
                }
            }
            else if (cube.getItem() != null && (player.isDead || !player.inventory.addItemStackToInventory(cube)))
            {
                ItemTossEvent toss = new ItemTossEvent(player.entityDropItem(cube, 0F), null);
                MinecraftForge.EVENT_BUS.post(toss);
            }
            else player.dropItem(cube, true);
            if (player instanceof EntityPlayerMP)
            {
                ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
            }
        }
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

    @Override
    public ITextComponent getDisplayName()
    {
        return null;
    }

    @Override
    public int getField(int id)
    {
        if (id == 0) return player1 == null ? -1 : player1.getEntityId();
        if (id == 1) return player2 == null ? -1 : player2.getEntityId();
        return 0;
    }

    @Override
    public int getFieldCount()
    {
        return 2;
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
        throw new Exception("connected PC is not bound to a player");
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
        return inventory2.length;
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        if (trade) return inventory[i];
        if (i < inventory2.length) return inventory2[i];
        return null;
    }

    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (worldObj.isRemote) return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
        this.writeToNBT(nbttagcompound);
        return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        return writeToNBT(nbt);
    }

    @Override
    public boolean hasCustomName()
    {
        return true;
    }

    protected boolean hasPC()
    {
        boolean pc = false;
        IBlockState state = worldObj.getBlockState(getPos());
        if (!(Boolean) state.getValue(BlockTradingTable.TMC)) { return false; }
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
        if (!player.getEntityWorld().isRemote)
        {
            boolean pc = hasPC();
            if (!pc) { return Lists.newArrayList(); }
            InventoryPC pcInv = InventoryPC.getPC(player.getCachedUniqueIdString());
            ArrayList<String> moves = getMoves(pcInv);
            Collections.sort(moves);

            PacketTrade packet = new PacketTrade(PacketTrade.SETMOVES);
            packet.data.setInteger("N", moves.size());
            for (int i = 0; i < moves.size(); i++)
            {
                packet.data.setString("M" + i, moves.get(i));
            }
            PokecubePacketHandler.sendToClient(packet, player);
            this.moves.put(player.getName(), moves);
            return moves;
        }
        return null;

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
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
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
    }

    public void openGUI(EntityPlayer player)
    {
        player.openGui(PokecubeMod.core, Config.GUITRADINGTABLE_ID, worldObj, getPos().getX(), getPos().getY(),
                getPos().getZ());
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    public void pokeseal(ItemStack a, ItemStack b, IPokemob mob)
    {
        if (b.hasTagCompound())
        {
            NBTTagCompound tag = b.getTagCompound().getCompoundTag(TagNames.POKESEAL);
            a.getTagCompound().setTag(TagNames.POKESEAL, tag.copy());
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
        if (id == 0) player1 = (EntityPlayer) worldObj.getEntityByID(value);
        if (id == 1) player2 = (EntityPlayer) worldObj.getEntityByID(value);
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

        UUID trader1 = player1.getUniqueID();
        UUID trader2 = player2.getUniqueID();

        if ((trader1.toString().equals(owner1) && trader2.toString().equals(owner2)))
        {
            mon2.setPokemonOwner(trader1);
            mon1.setPokemonOwner(trader2);
            boolean mon1everstone = PokecubeManager.getHeldItemMainhand(poke1) != null && Tools
                    .isSameStack(PokecubeManager.getHeldItemMainhand(poke1), PokecubeItems.getStack("everstone"));
            boolean mon2everstone = PokecubeManager.getHeldItemMainhand(poke2) != null && Tools
                    .isSameStack(PokecubeManager.getHeldItemMainhand(poke2), PokecubeItems.getStack("everstone"));
            if (!mon1everstone) mon1.setTraded(true);
            if (!mon2everstone) mon2.setTraded(true);
            poke1 = PokecubeManager.pokemobToItem(mon1);
            poke2 = PokecubeManager.pokemobToItem(mon2);
            ItemStack to1 = poke2;
            ItemStack to2 = poke1;
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
            boolean aFilled;
            if (((aFilled = PokecubeManager.isFilled(a)) && (PokecubeManager.isFilled(b)))) return false;
            ItemStack first = aFilled ? a : b;
            ItemStack second = aFilled ? b : a;
            ItemStack stack;
            int id = PokecubeItems.getCubeId(second);
            if (id == -2)
            {
                stack = RecipePokeseals.process(first, second);
            }
            else
            {
                PokecubeManager.setOwner(first, player1.getUniqueID());
                NBTTagCompound visualsTag = first.getTagCompound().getCompoundTag(TagNames.POKEMOB)
                        .getCompoundTag(TagNames.POKEMOBTAG).getCompoundTag(TagNames.VISUALSTAG);
                NBTTagCompound cube = new NBTTagCompound();
                stack = second.copy();
                second.writeToNBT(cube);
                visualsTag.setTag(TagNames.POKECUBE, cube);
                stack.getTagCompound().setTag(TagNames.POKEMOB,
                        first.getTagCompound().getCompoundTag(TagNames.POKEMOB).copy());
            }
            player1.inventory.addItemStackToInventory(stack);
            player1 = null;
            player2 = null;
            inventory = new ItemStack[2];
            return true;
        }

        if (!(a.getItem() == Items.EMERALD || b.getItem() == Items.EMERALD)) return false;

        int index = PokecubeManager.isFilled(a) ? 0 : 1;

        IPokemob mob = PokecubeManager.isFilled(a) ? PokecubeManager.itemToPokemob(inventory[0], worldObj)
                : PokecubeManager.itemToPokemob(inventory[1], worldObj);
        if (theftEnabled) return false;
        mob.setTraded(true);
        mob.setPokemonOwner(player1.getUniqueID());
        player1.inventory.addItemStackToInventory(inventory[index]);
        inventory = new ItemStack[2];
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
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
        return tagCompound;
    }
}
