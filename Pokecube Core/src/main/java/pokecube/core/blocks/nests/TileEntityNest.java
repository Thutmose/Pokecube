package pokecube.core.blocks.nests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.events.EggEvent;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class TileEntityNest extends TileEntity implements ITickable, IInventory
{

    private ItemStack[] inventory = new ItemStack[27];

    int                 pokedexNb = 0;

    HashSet<IPokemob>   residents = new HashSet<IPokemob>();
    int                 time      = 0;

    @Override
    public void update()
    {
        time++;
        int power = worldObj.getRedstonePower(getPos(), EnumFacing.DOWN);// .getBlockPowerInput(xCoord,
                                                                         // yCoord,
                                                                         // zCoord);

        if (worldObj.isRemote || (worldObj.getDifficulty() == EnumDifficulty.PEACEFUL && power == 0)) return;

        if (worldObj.getClosestPlayer(getPos().getX(), getPos().getY(), getPos().getZ(),
                PokecubeMod.core.getConfig().maxSpawnRadius) == null)
            return;

        if (pokedexNb == 0 && time >= 200)
        {
            time = 0;
            init();
        }
        if (pokedexNb == 0) return;
        int num = 3;
        PokedexEntry entry = Database.getEntry(pokedexNb);

        SpawnData data = entry.getSpawnData();
        if (data != null)
        {
            Vector3 here = Vector3.getNewVector().set(this);

            TerrainSegment t = TerrainManager.getInstance().getTerrian(worldObj, here);
            int b = t.getBiome(here);
            int min = data.getMin(b);
            num = min + worldObj.rand.nextInt(data.getMax(b) - min + 1);
        }
        // System.out.println("tick");
        if (residents.size() < num && time > 200 + worldObj.rand.nextInt(2000))
        {
            time = 0;
            ItemStack eggItem = ItemPokemobEgg.getEggStack(pokedexNb);
            NBTTagCompound nbt = eggItem.getTagCompound();
            nbt.setIntArray("nestLocation", new int[] { getPos().getX(), getPos().getY(), getPos().getZ() });
            eggItem.setTagCompound(nbt);
            Random rand = new Random();
            EntityPokemobEgg egg = new EntityPokemobEgg(worldObj, getPos().getX() + rand.nextGaussian(),
                    getPos().getY() + 1, getPos().getZ() + rand.nextGaussian(), eggItem, null);
            EggEvent.Lay event = new EggEvent.Lay(egg);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled())
            {
                worldObj.spawnEntityInWorld(egg);
            }
        }
    }

    public void addResident(IPokemob resident)
    {
        residents.add(resident);
    }

    public void removeResident(IPokemob resident)
    {
        residents.remove(resident);
    }

    public void init()
    {
        Vector3 here = Vector3.getNewVector().set(this);

        TerrainSegment t = TerrainManager.getInstance().getTerrian(worldObj, here);
        t.refresh(worldObj);
        t.checkIndustrial(worldObj);
        int b = t.getBiome(here);
        // System.out.println("init");
        if (SpawnHandler.spawns.containsKey(b))
        {
            ArrayList<PokedexEntry> entries = SpawnHandler.spawns.get(b);
            if (entries.isEmpty())
            {
                SpawnHandler.spawns.remove(b);
            }
            Collections.shuffle(entries);
            int index = 0;
            while (pokedexNb == 0 && index < 2 * entries.size())
            {
                PokedexEntry dbe = entries.get((index++) % entries.size());
                float weight = dbe.getSpawnData().getWeight(b);
                if (Math.random() > weight) continue;
                if (!(!SpawnHandler.canSpawn(t, dbe.getSpawnData(),
                        here.set(this).offsetBy(EnumFacing.UP).offsetBy(EnumFacing.UP), worldObj)))
                    continue;
                if (!SpawnHandler.isPointValidForSpawn(worldObj, here, dbe)) continue;

                pokedexNb = dbe.getPokedexNb();
            }
        }
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        pokedexNb = nbt.getInteger("pokedexNb");
        time = nbt.getInteger("time");
        NBTBase temp = nbt.getTag("Inventory");
        if (temp instanceof NBTTagList)
        {
            NBTTagList tagList = (NBTTagList) temp;
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);
                byte slot = tag.getByte("Slot");

                if (slot >= 0 && slot < inventory.length)
                {
                    inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
                }
            }
        }
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("pokedexNb", pokedexNb);
        nbt.setInteger("time", time);
        NBTTagList itemList = new NBTTagList();

        for (int i = 0; i < inventory.length; i++)
        {
            ItemStack stack = inventory[i];

            if (stack != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        nbt.setTag("Inventory", itemList);
    }

    @Override
    public void validate()
    {
        super.validate();

        addForbiddenSpawningCoord();
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        pokedexNb = 0;
        removeForbiddenSpawningCoord();
    }

    public boolean addForbiddenSpawningCoord()
    {
        return SpawnHandler.addForbiddenSpawningCoord(getPos(), worldObj.provider.getDimensionId(), 10);
    }

    public boolean removeForbiddenSpawningCoord()
    {
        return SpawnHandler.removeForbiddenSpawningCoord(getPos(), worldObj.provider.getDimensionId());
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public IChatComponent getDisplayName()
    {
        return null;
    }

    @Override
    public int getSizeInventory()
    {
        return 27;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return inventory[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        if (this.inventory[index] != null)
        {
            ItemStack itemStack;

            itemStack = inventory[index].splitStack(count);

            if (inventory[index].stackSize <= 0) inventory[index] = null;

            return itemStack;
        }
        return null;
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        if (inventory[index] != null)
        {
            ItemStack stack = inventory[index];
            inventory[index] = null;
            return stack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        inventory[index] = stack;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return true;
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void clear()
    {

    }
}
