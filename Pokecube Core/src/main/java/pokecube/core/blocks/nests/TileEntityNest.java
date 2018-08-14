package pokecube.core.blocks.nests;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.events.EggEvent;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public class TileEntityNest extends TileEntity implements ITickable, IInventory
{

    private List<ItemStack> inventory = NonNullList.<ItemStack> withSize(27, ItemStack.EMPTY);
    int                     pokedexNb = 0;
    HashSet<IPokemob>       residents = new HashSet<IPokemob>();
    int                     time      = 0;

    List<PokedexEntry>      spawns    = Lists.newArrayList();

    public TileEntityNest()
    {
    }

    public boolean addForbiddenSpawningCoord()
    {
        return SpawnHandler.addForbiddenSpawningCoord(getPos(), world.provider.getDimension(), 10);
    }

    public void addResident(IPokemob resident)
    {
        residents.add(resident);
    }

    @Override
    public void clear()
    {
        for (int i = 0; i < inventory.size(); i++)
            inventory.set(i, ItemStack.EMPTY);
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
    }

    @Override
    public ItemStack decrStackSize(int slot, int count)
    {
        if (CompatWrapper.isValid(getStackInSlot(slot)))
        {
            ItemStack itemStack;
            itemStack = getStackInSlot(slot).splitStack(count);
            if (!CompatWrapper.isValid(getStackInSlot(slot)))
            {
                setInventorySlotContents(slot, ItemStack.EMPTY);
            }
            return itemStack;
        }
        return ItemStack.EMPTY;
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

    @Override
    public int getSizeInventory()
    {
        return inventory.size();
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return inventory.get(index);
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    public void init()
    {
        // TODO init spawn for nest here.
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        pokedexNb = 0;
        removeForbiddenSpawningCoord();
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return true;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player)
    {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
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

                if (slot >= 0 && slot < inventory.size())
                {
                    inventory.set(slot, new ItemStack(tag));
                }
            }
        }
    }

    public boolean removeForbiddenSpawningCoord()
    {
        return SpawnHandler.removeForbiddenSpawningCoord(getPos(), world.provider.getDimension());
    }

    public void removeResident(IPokemob resident)
    {
        residents.remove(resident);
    }

    @Override
    public ItemStack removeStackFromSlot(int slot)
    {
        if (CompatWrapper.isValid(getStackInSlot(slot)))
        {
            ItemStack stack = getStackInSlot(slot);
            setInventorySlotContents(slot, ItemStack.EMPTY);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setField(int id, int value)
    {
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (CompatWrapper.isValid(stack)) inventory.set(index, ItemStack.EMPTY);
        inventory.set(index, stack);
    }

    @Override
    public void update()
    {
        time++;
        int power = world.getRedstonePower(getPos(), EnumFacing.DOWN);// .getBlockPowerInput(x,
                                                                      // y,
                                                                      // z);

        if (world.isRemote || (world.getDifficulty() == EnumDifficulty.PEACEFUL && power == 0)) return;

        if (world.getClosestPlayer(getPos().getX(), getPos().getY(), getPos().getZ(),
                PokecubeMod.core.getConfig().maxSpawnRadius, false) == null)
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
            SpawnBiomeMatcher matcher = data.getMatcher(world, here);
            int min = data.getMin(matcher);
            num = min + world.rand.nextInt(data.getMax(matcher) - min + 1);
        }
        // System.out.println("tick");
        if (residents.size() < num && time > 200 + world.rand.nextInt(2000))
        {
            time = 0;
            ItemStack eggItem = ItemPokemobEgg.getEggStack(pokedexNb);
            NBTTagCompound nbt = eggItem.getTagCompound();
            nbt.setIntArray("nestLocation", new int[] { getPos().getX(), getPos().getY(), getPos().getZ() });
            eggItem.setTagCompound(nbt);
            Random rand = new Random();
            EntityPokemobEgg egg = new EntityPokemobEgg(world, getPos().getX() + rand.nextGaussian(),
                    getPos().getY() + 1, getPos().getZ() + rand.nextGaussian(), eggItem, null);
            EggEvent.Lay event = new EggEvent.Lay(egg);
            MinecraftForge.EVENT_BUS.post(event);
            if (!event.isCanceled())
            {
                world.spawnEntity(egg);
            }
        }
    }

    @Override
    public void validate()
    {
        super.validate();

        addForbiddenSpawningCoord();
    }

    /** Writes a tile entity to NBT.
     * 
     * @return */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("pokedexNb", pokedexNb);
        nbt.setInteger("time", time);
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
        nbt.setTag("Inventory", itemList);
        return nbt;
    }

    // 1.11
    public boolean isEmpty()
    {
        return true;
    }
}
