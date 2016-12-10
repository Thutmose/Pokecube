package pokecube.core.blocks.pc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

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
    public static HashMap<String, InventoryPC> map       = new HashMap<String, InventoryPC>();
    // blank PC for client use.
    public static InventoryPC                  blank;
    public static UUID                         defaultId = new UUID(1234, 4321);
    public static int                          PAGECOUNT = 32;

    public static void addPokecubeToPC(ItemStack mob, World world)
    {
        if (!(PokecubeManager.isFilled(mob))) return;
        String player = PokecubeManager.getOwner(mob);
        try
        {
            UUID.fromString(player);
        }
        catch (Exception e)
        {

        }
        addStackToPC(player, mob);
    }

    public static void addStackToPC(String uuid, ItemStack mob)
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
                    .addChatMessage(new TextComponentTranslation("tile.pc.sentto", mob.getDisplayName()));
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
            return blank == null ? blank = new InventoryPC("blank") : blank;
        return getPC(player.getCachedUniqueIdString());
    }

    public static InventoryPC getPC(String uuid)
    {
        if (uuid != null)
        {
            if (!map.containsKey(uuid))
            {
                PCSaveHandler.getInstance().loadPC(uuid);
            }
            if (map.containsKey(uuid))
            {
                if (PokecubeCore.proxy.getPlayer(uuid) != null)
                {
                    String username = PokecubeCore.proxy.getPlayer(uuid).getName();
                    map.remove(username);
                }
                return map.get(uuid);
            }
            boolean isUid = true;
            try
            {
                UUID.fromString(uuid);
            }
            catch (Exception e)
            {
                isUid = false;
            }
            if (!isUid) return getPC(PokecubeMod.fakeUUID.toString());
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
            String player = boxes.getString("username");

            String uuid = boxes.getString("UUID");

            if ((uuid == "" || uuid == null) && (player == "" || player == null))
            {
                continue;
            }
            if (uuid == "" || uuid == null)
            {
                uuid = player;
            }
            if (!replace && map.containsKey(uuid)) continue;

            InventoryPC load = null;
            for (int k = 0; k < PAGECOUNT; k++)
            {
                if (k == 0)
                {
                    load = replace ? new InventoryPC(uuid) : getPC(uuid);

                    if (load == null) continue tags;
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
            for (int k = 0; k < load.getSizeInventory(); k++)
            {
                if (!items.hasKey("item" + k)) continue;
                NBTTagCompound nbttagcompound = items.getCompoundTag("item" + k);
                int j = nbttagcompound.getShort("Slot");
                if (j >= 0 && j < load.getSizeInventory())
                {
                    if (load.contents.containsKey(j)) continue;
                    ItemStack itemstack = CompatWrapper.fromTag(nbttagcompound);
                    load.setInventorySlotContents(j, itemstack);
                }
            }
            map.put(uuid, load);
        }
    }

    public static NBTTagList saveToNBT()
    {
        NBTTagList nbttag = new NBTTagList();

        HashSet<String> keys = new HashSet<String>();
        for (String s : map.keySet())
            keys.add(s);

        for (String uuid : keys)
        {
            if (map.get(uuid) == null || uuid.equals(""))
            {
                continue;
            }

            boolean isUid = true;
            try
            {
                UUID.fromString(uuid);
            }
            catch (Exception e)
            {
                isUid = false;
            }
            if (!isUid) continue;

            NBTTagCompound items = new NBTTagCompound();
            NBTTagCompound boxes = new NBTTagCompound();
            boxes.setString("UUID", uuid);
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
                NBTTagCompound nbttagcompound = new NBTTagCompound();

                if (itemstack != CompatWrapper.nullStack)
                {
                    nbttagcompound.setShort("Slot", (short) i);
                    itemstack.writeToNBT(nbttagcompound);
                    items.setTag("item" + i, nbttagcompound);
                }
            }
            items.setTag("boxes", boxes);
            nbttag.appendTag(items);
        }

        return nbttag;
    }

    public static NBTTagList saveToNBT(String uuid)
    {
        NBTTagList nbttag = new NBTTagList();

        String name = "";

        for (String player : map.keySet())
        {
            if (map.get(player) == null || player.equals("") || !(name.equalsIgnoreCase(player) || uuid.equals(player)))
            {
                continue;
            }
            NBTTagCompound items = new NBTTagCompound();
            NBTTagCompound boxes = new NBTTagCompound();
            boxes.setString("UUID", player);
            boxes.setBoolean("seenOwner", map.get(player).seenOwner);
            // System.out.println(map.get(player).seenOwner);
            boxes.setBoolean("autoSend", map.get(player).autoToPC);
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

                if (itemstack != CompatWrapper.nullStack)
                {
                    nbttagcompound.setShort("Slot", (short) i);
                    itemstack.writeToNBT(nbttagcompound);
                    items.setTag("item" + i, nbttagcompound);
                }
            }
            items.setTag("boxes", boxes);
            nbttag.appendTag(items);
        }

        return nbttag;
    }

    private int                              page      = 0;

    public boolean                           autoToPC  = false;

    public boolean[]                         opened    = new boolean[PAGECOUNT];

    public String[]                          boxes     = new String[PAGECOUNT];
    private Int2ObjectOpenHashMap<ItemStack> contents  = new Int2ObjectOpenHashMap<>();

    public final String                      owner;

    public boolean                           seenOwner = false;

    public InventoryPC(InventoryPC from)
    {
        owner = from.owner;
        opened = from.opened.clone();
        boxes = from.boxes.clone();
        page = from.page;
    }

    public InventoryPC(String player)
    {
        if (!player.equals("") && !map.containsKey(player)) map.put(player, this);
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
        PCSaveHandler.getInstance().savePC(player.getCachedUniqueIdString());
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
        return CompatWrapper.nullStack;
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
        EntityPlayer player = PokecubeCore.getPlayer(owner);
        String name = "Public";

        if (!owner.equals(defaultId.toString()))
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
        return CompatWrapper.validate(contents.get(i));
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
    public boolean isUseableByPlayer(EntityPlayer entityplayer)
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
        return CompatWrapper.validate(contents.remove(i));
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

    // 1.11
    public boolean func_191420_l()
    {
        return true;
    }

}
