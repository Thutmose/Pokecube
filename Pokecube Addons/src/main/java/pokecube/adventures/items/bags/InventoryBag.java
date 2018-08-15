package pokecube.adventures.items.bags;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import thut.core.common.handlers.PlayerDataHandler;
import thut.lib.CompatWrapper;

public class InventoryBag implements IInventory
{
    private static final String               FILEID    = "BagInventory";
    public static HashMap<UUID, InventoryBag> map       = new HashMap<UUID, InventoryBag>();
    public static UUID                        defaultID = new UUID(12345678910l, 12345678910l);
    public static UUID                        blankID   = new UUID(0, 0);
    public static int                         PAGECOUNT = 32;
    // blank bag for client use.
    public static InventoryBag                blank;

    public static void loadBag(UUID uuid)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;
        try
        {
            File file = PlayerDataHandler.getFileForUUID(uuid.toString(), FILEID);
            if (file != null && file.exists())
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fileinputstream);
                fileinputstream.close();
                readBagFromNBT(nbttagcompound.getCompoundTag("Data"));
            }
        }
        catch (FileNotFoundException e)
        {
        }
        catch (Exception e)
        {
        }
    }

    public static void readBagFromNBT(NBTTagCompound nbt)
    {
        // Read PC Data from NBT
        NBTBase temp = nbt.getTag("PC");
        if (temp instanceof NBTTagList)
        {
            NBTTagList tagListPC = (NBTTagList) temp;
            InventoryBag.loadFromNBT(tagListPC);
        }
    }

    public static void saveBag(String uuid)
    {

        if (FMLCommonHandler.instance().getMinecraftServerInstance() == null) return;
        try
        {
            File file = PlayerDataHandler.getFileForUUID(uuid, FILEID);
            if (file != null)
            {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                writeBagToNBT(nbttagcompound, uuid);
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setTag("Data", nbttagcompound);
                FileOutputStream fileoutputstream = new FileOutputStream(file);
                CompressedStreamTools.writeCompressed(nbttagcompound1, fileoutputstream);
                fileoutputstream.close();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void writeBagToNBT(NBTTagCompound nbt, String uuid)
    {
        NBTTagList tagsPC = InventoryBag.saveToNBT(uuid);
        nbt.setTag("PC", tagsPC);
    }

    public static void clearInventory()
    {
        map.clear();
    }

    public static InventoryBag getBag(Entity player)
    {// TODO Sync box names to blank
        if (player.getEntityWorld().isRemote) return blank == null ? blank = new InventoryBag(blankID) : blank;
        return getBag(player.getUniqueID());
    }

    public static InventoryBag getBag(UUID uuid)
    {
        if (uuid != null)
        {
            if (map.containsKey(uuid))
            {
                return map.get(uuid);
            }
            else loadBag(uuid);
            if (map.containsKey(uuid)) { return map.get(uuid); }
            return new InventoryBag(uuid);
        }
        return getBag(defaultID);
    }

    public static void loadFromNBT(NBTTagList nbt)
    {
        loadFromNBT(nbt, true);
    }

    public static void loadFromNBT(NBTTagList nbt, boolean replace)
    {
        int i;
        tags:
        for (i = 0; i < nbt.tagCount(); i++)
        {
            NBTTagCompound items = nbt.getCompoundTagAt(i);
            NBTTagCompound boxes = items.getCompoundTag("boxes");
            UUID uuid;
            try
            {
                uuid = UUID.fromString(boxes.getString("UUID"));
            }
            catch (Exception e)
            {
                continue;
            }
            if (uuid.equals(blankID)) continue;

            InventoryBag load = null;
            for (int k = 0; k < PAGECOUNT; k++)
            {
                if (k == 0)
                {
                    load = replace ? new InventoryBag(uuid) : getBag(uuid);

                    if (load == null) continue tags;
                    load.setPage(boxes.getInteger("page"));
                }
                if (boxes.getString("name" + k) != null)
                {
                    load.boxes[k] = boxes.getString("name" + k);
                }
            }
            if (load.getPage() >= PAGECOUNT) load.setPage(0);
            load.contents.clear();
            for (int k = 0; k < load.getSizeInventory(); k++)
            {
                if (!items.hasKey("item" + k)) continue;
                NBTTagCompound nbttagcompound = items.getCompoundTag("item" + k);
                int j = nbttagcompound.getShort("Slot");
                if (j >= 0 && j < load.getSizeInventory())
                {
                    if (load.contents.containsKey(j)) continue;
                    ItemStack itemstack = new ItemStack(nbttagcompound);
                    load.setInventorySlotContents(j, itemstack);
                }
            }
            map.put(uuid, load);
        }
    }

    public static NBTTagList saveToNBT(Entity owner)
    {
        return saveToNBT(owner.getCachedUniqueIdString());
    }

    public static NBTTagList saveToNBT(String uuid)
    {
        NBTTagList nbttag = new NBTTagList();
        UUID player = UUID.fromString(uuid);
        if (map.get(player) == null || blankID.equals(player)) { return nbttag; }
        NBTTagCompound items = new NBTTagCompound();
        NBTTagCompound boxes = new NBTTagCompound();
        boxes.setString("UUID", player.toString());
        boxes.setInteger("page", map.get(player).page);
        for (int i = 0; i < PAGECOUNT; i++)
        {
            boxes.setString("name" + i, map.get(player).boxes[i]);
        }
        items.setInteger("page", map.get(player).getPage());
        for (int i = 0; i < map.get(player).getSizeInventory(); i++)
        {
            ItemStack itemstack = map.get(player).getStackInSlot(i);
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            if (!itemstack.isEmpty())
            {
                nbttagcompound.setShort("Slot", (short) i);
                itemstack.writeToNBT(nbttagcompound);
                items.setTag("item" + i, nbttagcompound);
            }
        }
        items.setTag("boxes", boxes);
        nbttag.appendTag(items);
        return nbttag;
    }

    private int                              page     = 0;
    public boolean[]                         opened   = new boolean[PAGECOUNT];
    public String[]                          boxes    = new String[PAGECOUNT];
    private Int2ObjectOpenHashMap<ItemStack> contents = new Int2ObjectOpenHashMap<>();
    public final UUID                        owner;
    boolean                                  dirty    = false;

    public InventoryBag(UUID uuid)
    {
        if (uuid != null) map.put(uuid, this);
        opened = new boolean[PAGECOUNT];
        boxes = new String[PAGECOUNT];
        owner = uuid;
        for (int i = 0; i < PAGECOUNT; i++)
        {
            boxes[i] = "Box " + String.valueOf(i + 1);
        }
    }

    public void addItem(ItemStack stack)
    {
        for (int i = page * 54; i < getSizeInventory(); i++)
        {
            if (this.getStackInSlot(i).isEmpty())
            {
                this.setInventorySlotContents(i, stack);
                return;
            }
        }
        for (int i = 0; i < page * 54; i++)
        {
            if (this.getStackInSlot(i).isEmpty())
            {
                this.setInventorySlotContents(i, stack);
                return;
            }
        }
    }

    @Override
    public void clear()
    {
        this.contents.clear();
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
        saveBag(player.getCachedUniqueIdString());
    }

    @Override
    public ItemStack decrStackSize(int i, int j)
    {
        if (CompatWrapper.isValid(contents.get(i)))
        {
            ItemStack itemstack = contents.get(i).splitStack(j);
            if (!CompatWrapper.isValid(contents.get(i)))
            {
                contents.remove(i);
            }
            return itemstack;
        }
        return ItemStack.EMPTY;
    }

    public HashSet<ItemStack> getContents()
    {
        HashSet<ItemStack> ret = new HashSet<ItemStack>();
        for (int i : contents.keySet())
        {
            if (CompatWrapper.isValid(contents.get(i))) ret.add(contents.get(i));
        }
        return ret;
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

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public String getName()
    {
        return null;
    }

    public int getPage()
    {
        return page;
    }

    @Override
    public int getSizeInventory()
    {
        return PAGECOUNT * 54;
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        ItemStack stack = contents.get(i);
        if (stack == null) stack = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    /** Returns true if automation is allowed to insert the given stack
     * (ignoring stack size) into the given slot. */
    @Override
    public boolean isItemValidForSlot(int par1, ItemStack stack)
    {
        return ContainerBag.isItemValid(stack);
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityplayer)
    {
        return true;
    }

    @Override
    public void markDirty()
    {
        dirty = true;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public ItemStack removeStackFromSlot(int i)
    {
        ItemStack stack = contents.remove(i);
        if (stack == null) stack = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public void setField(int id, int value)
    {

    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack)
    {
        if (CompatWrapper.isValid(itemstack)) contents.put(i, itemstack);
        else contents.remove(i);
    }

    public void setPage(int page)
    {
        this.page = page;
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }

}
