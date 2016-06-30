package pokecube.core.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Tools;

public class ItemTM extends ItemPokemobUseable
{

    public static void addMoveToStack(String move, ItemStack stack)
    {
        if (stack.getItem() instanceof ItemTM)
        {
            Move_Base attack = MovesUtils.getMoveFromName(move.trim());
            NBTTagCompound nbt = stack.getTagCompound() == null ? new NBTTagCompound() : stack.getTagCompound();

            nbt.setString("move", move.trim());
            stack.setTagCompound(nbt);
            stack.setItemDamage(attack.getType(null).ordinal());
            stack.setStackDisplayName(MovesUtils.getLocalizedMove(move.trim()));
        }
    }

    public static boolean feedToPokemob(ItemStack stack, Entity entity)
    {
        if (entity instanceof IPokemob)
        {
            int num = stack.getItemDamage();
            // If candy, raise level by one
            if (num == 20)
            {
                int level = ((IPokemob) entity).getLevel();
                if (level == 100) return false;

                int xp = Tools.levelToXp(((IPokemob) entity).getExperienceMode(), level + 1);
                ((IPokemob) entity).setExp(xp, true, false);
                PokecubeItems.deValidate(stack);
                return true;
            }
            else// it is a TM, should try to teach the move
            {
                return teachToPokemob(stack, (IPokemob) entity);
            }
        }
        return false;
    }

    public static String getMoveFromStack(ItemStack stack)
    {
        if (stack.getItem() instanceof ItemTM)
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null) return null;
            String name = nbt.getString("move");
            if (!name.contentEquals("")) return name;
        }
        return null;
    }

    public static boolean teachToPokemob(ItemStack tm, IPokemob mob)
    {
        if (tm.getItem() instanceof ItemTM)
        {
            NBTTagCompound nbt = tm.getTagCompound();
            if (nbt == null) return false;
            String name = nbt.getString("move");
            if (name.contentEquals("") || name == null) return false;
            for (String move : mob.getMoves())
            {
                if (name.equals(move)) return false;
            }
            String[] learnables = mob.getPokedexEntry().getMoves().toArray(new String[0]);
            for (String s : learnables)
            {
                if (mob.getPokedexNb() == 151 || s.toLowerCase().contentEquals(name.toLowerCase()) || PokecubeMod.debug)
                {

                    if (mob.getMove(0) == null)
                    {
                        mob.setMove(0, name);
                    }
                    else if (mob.getMove(1) == null)
                    {
                        mob.setMove(1, name);
                    }
                    else if (mob.getMove(2) == null)
                    {
                        mob.setMove(2, name);
                    }
                    else
                    {
                        mob.setMove(3, name);
                    }
                    tm.setTagCompound(null);
                    tm.splitStack(1);
                    tm.setItemDamage(0);
                    return true;
                }
            }
        }

        return false;
    }

    public ItemTM()
    {
        super();
        this.setMaxStackSize(64);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public boolean applyEffect(EntityLivingBase mob, ItemStack stack)
    {
        if (mob.getEntityWorld().isRemote) return true;

        if (stack.hasTagCompound())
        {
            int num = stack.getItemDamage();
            // Check if is TM or valid candy
            if (num != 20 || PokecubeItems.isValid(stack))
            {
                return feedToPokemob(stack, mob);
            }
            else
            {
                // If invalid candy, drop level since it is bad candy
                if (num == 20)
                {
                    int xp = Tools.levelToXp(((IPokemob) mob).getExperienceMode(), ((IPokemob) mob).getLevel() - 1);
                    ((IPokemob) mob).setExp(xp, true, false);
                    stack.setTagCompound(null);
                    stack.splitStack(1);
                    return true;
                }
            }
        }
        else
        {
            int num = stack.getItemDamage();
            // If invalid candy, drop level since it is bad candy
            if (num == 20)
            {
                int xp = Tools.levelToXp(((IPokemob) mob).getExperienceMode(), ((IPokemob) mob).getLevel() - 1);
                ((IPokemob) mob).setExp(xp, true, false);
                stack.setTagCompound(null);
                stack.splitStack(1);
                return true;
            }
        }
        return false;
    }

    /** If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client. */
    @Override
    public boolean getShareTag()
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    /** returns a list of items with the same ID, but different meta (eg: dye
     * returns 16 items) */
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List)
    {
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, 19));
    }

    /** Returns the unlocalized name of this item. This version accepts an
     * ItemStack so different stacks can have different names based on their
     * damage or NBT. */
    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        int i = stack.getItemDamage();

        if (i == 20) return "item.candy";
        if (i == 19) return "item.emerald_shard";
        return super.getUnlocalizedName() + i;
    }

}
