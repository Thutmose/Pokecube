package pokecube.pokeplayer.tileentity;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.database.Database;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.pokeplayer.PokePlayer;

public class TileEntityTransformer extends TileEntityOwnable
{
    ItemStack stack;
    int[]     nums;
    boolean   random = false;

    public ItemStack getStack(ItemStack stack)
    {
        return stack;
    }

    public void onInteract(EntityPlayer player)
    {
        System.out.println(player.getEntityBoundingBox());

        if (worldObj.isRemote || random) return;

        if (canEdit(player))
        {
            if (stack == null && PokecubeManager.isFilled(player.getHeldItem()))
            {
                setStack(player.getHeldItem());
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
            else
            {
                EntityItem entityitem = player.dropPlayerItemWithRandomChoice(stack, false);

                if (entityitem != null)
                {
                    entityitem.setNoPickupDelay();
                    entityitem.setOwner(player.getName());
                }
                stack = null;
            }
        }
    }

    public void onStepped(EntityPlayer player)
    {
        if (worldObj.isRemote) return;
        boolean isPokemob = player.getEntityData().getBoolean("isPokemob");

        if ((stack != null || random) && !isPokemob)
        {
            IPokemob pokemob = getPokemob();
            if (pokemob != null) PokePlayer.proxy.setPokemob(player, pokemob);
        }
        else if (stack == null && !random && isPokemob)
        {
            PokePlayer.proxy.setPokemob(player, null);
        }
    }

    private IPokemob getPokemob()
    {
        if (random)
        {
            int num = 0;
            if (nums != null && nums.length > 0)
            {
                num = nums[worldObj.rand.nextInt(nums.length)];
            }
            else
            {
                List<Integer> numbers = Lists.newArrayList(Database.data.keySet());
                num = numbers.get(worldObj.rand.nextInt(numbers.size()));
            }
            Entity entity = PokecubeMod.core.createEntityByPokedexNb(num, worldObj);

            if (entity != null)
            {
                ((IPokemob) entity).specificSpawnInit();
            }

            return (IPokemob) entity;
        }
        IPokemob pokemob = PokecubeManager.itemToPokemob(stack, worldObj);
        return pokemob;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
        if (tagCompound.hasKey("stack"))
        {
            NBTTagCompound tag = tagCompound.getCompoundTag("stack");
            stack = ItemStack.loadItemStackFromNBT(tag);
        }
        if (tagCompound.hasKey("nums"))
        {
            nums = tagCompound.getIntArray("nums");
        }
        random = tagCompound.getBoolean("random");
    }

    public void setStack(ItemStack stack)
    {
        this.stack = stack;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
        if (stack != null)
        {
            NBTTagCompound tag = new NBTTagCompound();
            stack.writeToNBT(tag);
            tagCompound.setTag("stack", tag);
        }
        if (nums != null)
        {
            tagCompound.setIntArray("nums", nums);
        }
        tagCompound.setBoolean("random", random);
    }

}
