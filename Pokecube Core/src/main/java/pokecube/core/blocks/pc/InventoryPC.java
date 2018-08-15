package pokecube.core.blocks.pc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.PCSaveHandler;
import thut.lib.CompatWrapper;

public class InventoryPC implements IInventory
{
    public static HashMap<UUID, InventoryPC> map       = new HashMap<UUID, InventoryPC>();
    // blank PC for client use.
    public static InventoryPC                blank;
    public static UUID                       defaultId = new UUID(1234, 4321);
    public static int                        PAGECOUNT = 32;

    public static void addPokecubeToPC(ItemStack mob, World world)
    {
        if (!(PokecubeManager.isFilled(mob))) return;
        String player = PokecubeManager.getOwner(mob);
        UUID id;
        try
        {
            id = UUID.fromString(player);
            addStackToPC(id, mob);
        }
        catch (Exception e)
        {

        }
    }

    public static void addStackToPC(UUID uuid, ItemStack mob)
    {
        if (uuid == null || !CompatWrapper.isValid(mob))
        {
            System.err.println("Could not find the owner of this item " + mob + " " + uuid);
            return;
        }
        InventoryPC pc = getPC(uuid);

        if (pc == null) { return; }

        if (PokecubeManager.isFilled(mob))
        {
            ItemStack stack = mob;
            PokecubeManager.heal(stack);
            if (PokecubeCore.proxy.getPlayer(uuid) != null) PokecubeCore.proxy.getPlayer(uuid)
                    .sendMessage(new TextComponentTranslation("tile.pc.sentto", mob.getDisplayName()));
        }
        pc.addItem(mob.copy());
        PCSaveHandler.getInstance().savePC(uuid);
    }

    public static void clearPC()
    {
        map.clear();
    }

    public static InventoryPC getPC(Entity player)
    {// TODO Sync box names/numbers to blank
        if (player == null || player.getEntityWorld().isRemote)
            return blank == null ? blank = new InventoryPC(defaultId) : blank;
        return getPC(player.getUniqueID());
    }

    public static InventoryPC getPC(UUID uuid)
    {
        if (uuid != null)
        {
            if (!map.containsKey(uuid))
            {
                PCSaveHandler.getInstance().loadPC(uuid);
            }
            if (map.containsKey(uuid)) { return map.get(uuid); }
            return new InventoryPC(uuid);
        }
        return null;
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
            String id = boxes.getString("UUID");
            if ((id == "" || id == null))
            {
                continue;
            }
            UUID uuid;
            try
            {
                uuid = UUID.fromString(id);
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.WARNING, "Error with " + id, e);
                continue;
            }
            if (!replace && map.containsKey(uuid)) continue;
            if (PokecubeMod.debug) PokecubeMod.log("Loading PC for " + uuid);
            InventoryPC load = null;
            for (int k = 0; k < PAGECOUNT; k++)
            {
                if (k == 0)
                {
                    load = replace ? new InventoryPC(uuid) : getPC(uuid);
                    if (load == null)
                    {
                        if (PokecubeMod.debug) PokecubeMod.log("Skipping " + uuid);
                        continue tags;
                    }
                    load.autoToPC = boxes.getBoolean("autoSend");
                    load.seenOwner = boxes.getBoolean("seenOwner");
                    load.setPage(boxes.getInteger("page"));
                }
                if (boxes.getString("name" + k) != null)
                {
                    load.boxes[k] = boxes.getString("name" + k);
                }
            }
            load.contents.clear();
            // TODO change this to a tag list instead of compound...
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
            if (!defaultId.equals(uuid)) map.put(uuid, load);
        }
    }

    public static NBTTagList saveToNBT(UUID uuid)
    {
        if (PokecubeMod.debug) PokecubeMod.log("Saving PC for " + uuid);

        NBTTagList nbttag = new NBTTagList();
        NBTTagCompound items = new NBTTagCompound();
        NBTTagCompound boxes = new NBTTagCompound();
        boxes.setString("UUID", uuid.toString());
        boxes.setBoolean("seenOwner", map.get(uuid).seenOwner);
        boxes.setBoolean("autoSend", map.get(uuid).autoToPC);
        boxes.setInteger("page", map.get(uuid).page);
        for (int i = 0; i < PAGECOUNT; i++)
        {
            boxes.setString("name" + i, map.get(uuid).boxes[i]);
        }
        items.setInteger("page", map.get(uuid).getPage());
        for (int i = 0; i < map.get(uuid).getSizeInventory(); i++)
        {
            ItemStack itemstack = map.get(uuid).getStackInSlot(i);
            // TODO change this to a tag list instead of compound...
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            if (itemstack != ItemStack.EMPTY)
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

    private int                              page      = 0;

    public boolean                           autoToPC  = false;

    public boolean[]                         opened    = new boolean[PAGECOUNT];

    public String[]                          boxes     = new String[PAGECOUNT];
    private Int2ObjectOpenHashMap<ItemStack> contents  = new Int2ObjectOpenHashMap<>();

    public final UUID                        owner;

    public boolean                           seenOwner = false;

    public InventoryPC(UUID player)
    {
        if (player != defaultId && !map.containsKey(player)) map.put(player, this);
        opened = new boolean[PAGECOUNT];
        boxes = new String[PAGECOUNT];
        owner = player;
        for (int i = 0; i < PAGECOUNT; i++)
        {
            boxes[i] = "Box " + String.valueOf(i + 1);
        }
    }

    public void addItem(ItemStack stack)
    {
        for (int i = page * 54; i < getSizeInventory(); i++)
        {
            if (!CompatWrapper.isValid(this.getStackInSlot(i)))
            {
                this.setInventorySlotContents(i, stack);
                return;
            }
        }
        for (int i = 0; i < page * 54; i++)
        {
            if (!CompatWrapper.isValid(this.getStackInSlot(i)))
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
        PCSaveHandler.getInstance().savePC(player.getUniqueID());
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
        return ContainerPC.STACKLIMIT;
    }

    @Override
    public String getName()
    {
        EntityPlayer player = PokecubeCore.getPlayer(owner.toString());
        String name = "Public";
        if (!owner.equals(defaultId))
        {
            name = "Private bound";
        }
        if (player != null)
        {
            name = player.getName() + "'s";
        }
        else if (name.equals("Public")) { return "tile.pc.public"; }
        return "tile.pc.title";
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
        return ContainerPC.isItemValid(stack);
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityplayer)
    {
        return true;
    }

    @Override
    public void markDirty()
    {
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
    public String toString()
    {
        String ret = "Owner: " + owner + ", Current Page, " + (getPage() + 1) + ": Auto Move, " + autoToPC + ": ";
        String eol = System.getProperty("line.separator");
        ret += eol;
        for (int i : contents.keySet())
        {
            if (CompatWrapper.isValid(this.getStackInSlot(i)))
            {
                ret += "Slot " + i + ", " + this.getStackInSlot(i).getDisplayName() + "; ";
            }
        }
        ret += eol;
        for (int i = 0; i < boxes.length; i++)
        {
            ret += "Box " + (i + 1) + ", " + boxes[i] + "; ";
        }
        ret += eol;
        return ret;
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }

}
