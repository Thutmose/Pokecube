package pokecube.pokeplayer.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.pokeplayer.PokePlayer;

public class TileEntityTransformer extends TileEntityOwnable
{
    ItemStack stack;

    public ItemStack getStack(ItemStack stack)
    {
        return stack;
    }

    public void onInteract(EntityPlayer player)
    {
        System.out.println(player.getEntityBoundingBox());

        if (worldObj.isRemote) return;

        if (canEdit(player))
        {
            if (stack == null && PokecubeManager.isFilled(player.getHeldItem()))
            {
                setStack(player.getHeldItem());
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
            else
            {
                player.inventory.addItemStackToInventory(stack);
                stack = null;
            }
        }
    }

    public void onStepped(EntityPlayer player)
    {
        if (worldObj.isRemote) return;
        boolean isPokemob = player.getEntityData().getBoolean("isPokemob");
        if (stack != null && !isPokemob)
        {
            IPokemob pokemob = PokecubeManager.itemToPokemob(stack, worldObj);
            PokePlayer.proxy.setPokemob(player, pokemob);
        }
        else if (stack == null && isPokemob)
        {
            PokePlayer.proxy.setPokemob(player, null);
        }
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
    }

}
