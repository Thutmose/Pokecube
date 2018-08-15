package pokecube.core.blocks.tradingTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import com.google.common.collect.Lists;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.blocks.pc.BlockPC;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.blocks.pc.TileEntityPC;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemTM;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.packets.PacketTrade;
import thut.api.maths.Vector3;
import thut.core.common.blocks.DefaultInventory;
import thut.lib.CompatWrapper;

@Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class TileEntityTMMachine extends TileEntityOwnable implements DefaultInventory, SimpleComponent
{
    private List<ItemStack>                   inventory = NonNullList.<ItemStack> withSize(1, ItemStack.EMPTY);

    public HashMap<String, ArrayList<String>> moves     = new HashMap<String, ArrayList<String>>();

    boolean                                   init      = true;

    private TileEntityPC                      pc;

    public TileEntityTMMachine()
    {
        super();
    }

    public void addMoveToTM(String move)
    {
        ItemStack tm = inventory.get(0);
        if (tm != null && tm.getItem() instanceof ItemTM)
        {
            ItemStack newTM = ItemTM.getTM(move);
            newTM.setCount(tm.getCount());
            this.setInventorySlotContents(0, newTM);
        }
    }

    @Callback
    @Optional.Method(modid = "opencomputers")
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

    @Callback
    @Optional.Method(modid = "opencomputers")
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
    public void closeInventory(EntityPlayer player)
    {

    }

    @Override
    public String getComponentName()
    {
        return "tm_machine";
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
        PokedexEntry entry = mob.getPokedexEntry();
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
                IPokemob mob = PokecubeManager.itemToPokemob(stack, world);

                if (mob == null)
                {
                    PokecubeMod.log(Level.WARNING, "Corrupted Pokemon in PC. " + stack.getDisplayName());
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

    @Override
    public String getName()
    {
        return "tm_machine";
    }

    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (world.isRemote) return new SPacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
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
        this.pc = null;
        for (EnumFacing side : EnumFacing.values())
        {
            Vector3 here = Vector3.getNewVector().set(this);
            Block id = here.offset(side).getBlock(world);
            if (id instanceof BlockPC)
            {
                pc = true;
                this.pc = (TileEntityPC) here.offset(side).getTileEntity(getWorld());
                break;
            }
        }
        return pc;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return itemstack.getItem() instanceof ItemTM;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        return world.getTileEntity(getPos()) == this
                && player.getDistanceSq(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5) < 64;
    }

    public ArrayList<String> moves(EntityPlayer player)
    {
        if (!player.getEntityWorld().isRemote)
        {
            boolean pc = hasPC();
            if (!pc) { return Lists.newArrayList(); }
            InventoryPC pcInv = InventoryPC.getPC(player.getUniqueID());
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
        if (world.isRemote)
        {
            NBTTagCompound nbt = pkt.getNbtCompound();
            readFromNBT(nbt);
        }
    }

    public void openGUI(EntityPlayer player)
    {
        player.openGui(PokecubeMod.core, Config.GUITMTABLE_ID, world, getPos().getX(), getPos().getY(),
                getPos().getZ());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
        NBTBase temp = tagCompound.getTag("Inventory");
        inventory = NonNullList.<ItemStack> withSize(1, ItemStack.EMPTY);
        if (temp instanceof NBTTagList)
        {
            NBTTagList tagList = (NBTTagList) temp;
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);
                byte slot = tag.getByte("Slot");

                if (slot >= 0 && slot < inventory.size())
                {
                    inventory.set(slot, new ItemStack(tag));
                }
            }
        }
        init = false;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inventory.size(); i++)
        {
            ItemStack stack;
            if (CompatWrapper.isValid(stack = inventory.get(i)))
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        tagCompound.setTag("Inventory", itemList);
        return tagCompound;
    }

    @Override
    public List<ItemStack> getInventory()
    {
        return inventory;
    }
}
