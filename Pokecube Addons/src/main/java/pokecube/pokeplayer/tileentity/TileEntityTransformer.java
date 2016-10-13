package pokecube.pokeplayer.tileentity;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import pokecube.core.blocks.TileEntityOwnable;
import pokecube.core.database.Database;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;
import pokecube.pokeplayer.PokePlayer;

public class TileEntityTransformer extends TileEntityOwnable implements ITickable
{
    ItemStack stack;
    int[]     nums;
    boolean   random   = false;
    int       stepTick = 20;

    public ItemStack getStack(ItemStack stack)
    {
        return stack;
    }

    public void onInteract(EntityPlayer player)
    {
        if (worldObj.isRemote || random) return;

        if (canEdit(player))
        {
            if (stack == null && PokecubeManager.isFilled(player.getHeldItemMainhand()))
            {
                setStack(player.getHeldItemMainhand());
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
            else
            {
                Tools.giveItem(player, stack);
            }
        }
    }

    public void onStepped(EntityPlayer player)
    {
        if (worldObj.isRemote || stepTick > 0) return;
        boolean isPokemob = PokePlayer.PROXY.getMap().get(player.getUniqueID()) != null;
        if ((stack != null || random) && !isPokemob)
        {
            IPokemob pokemob = getPokemob();
            if (pokemob != null) PokePlayer.PROXY.setPokemob(player, pokemob);
            if (stack != null && pokemob != null)
            {
                stack = null;
                stepTick = 50;
            }
            return;
        }
        else if (stack == null && !random && isPokemob)
        {
            stepTick = 50;
            IPokemob poke = PokePlayer.PROXY.getPokemob(player);
            NBTTagCompound tag = ((Entity) poke).getEntityData();
            poke.setPokemonNickname(tag.getString("oldNickname"));
            tag.removeTag("oldNickname");
            tag.removeTag("isPlayer");
            tag.removeTag("playerID");
            ItemStack pokemob = PokecubeManager.pokemobToItem(poke);
            PokePlayer.PROXY.setPokemob(player, null);
            stack = pokemob;
            return;
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
        stepTick = tagCompound.getInteger("stepTick");
        random = tagCompound.getBoolean("random");
    }

    public void setStack(ItemStack stack)
    {
        this.stack = stack;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
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
        tagCompound.setInteger("stepTick", stepTick);
        tagCompound.setBoolean("random", random);
        return tagCompound;
    }

    @Override
    public void update()
    {
        stepTick--;
    }

}
