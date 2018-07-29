package pokecube.core.items;

import java.util.Map;
import java.util.logging.Level;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;

public class ItemTM extends Item
{
    private static Map<PokeType, ItemTM> tms = Maps.newHashMap();

    public static ItemStack getTM(String move)
    {
        ItemStack stack = ItemStack.EMPTY;
        Move_Base attack = MovesUtils.getMoveFromName(move.trim());
        if (attack == null)
        {
            PokecubeMod.log(Level.WARNING, "Attempting to make TM for un-registered move: " + move);
            return stack;
        }
        stack = new ItemStack(tms.get(attack.move.type));
        NBTTagCompound nbt = stack.getTagCompound() == null ? new NBTTagCompound() : stack.getTagCompound();
        nbt.setString("move", move.trim());
        stack.setTagCompound(nbt);
        String name = MovesUtils.getMoveName(move.trim()).getFormattedText();
        if (name.startsWith("pokemob.move.")) name = name.replaceFirst("pokemob.move.", "");
        stack.setStackDisplayName(name);
        return stack;
    }

    public static boolean feedToPokemob(ItemStack stack, Entity entity)
    {
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob != null)
        {
            int num = stack.getItemDamage();
            // If candy, raise level by one
            if (num == 20)
            {
                int level = pokemob.getLevel();
                if (level == 100) return false;

                int xp = Tools.levelToXp(pokemob.getExperienceMode(), level + 1);
                pokemob.setExp(xp, true);
                PokecubeItems.deValidate(stack);
                return true;
            }
            // it is a TM, should try to teach the move
            return teachToPokemob(stack, pokemob);
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
            if (name.contentEquals("")) return false;
            for (String move : mob.getMoves())
            {
                if (name.equals(move)) return false;
            }
            String[] learnables = mob.getPokedexEntry().getMoves().toArray(new String[0]);
            int index = mob.getMoveIndex();
            if (index > 3) return false;
            for (String s : learnables)
            {
                if (mob.getPokedexNb() == 151 || s.toLowerCase(java.util.Locale.ENGLISH)
                        .contentEquals(name.toLowerCase(java.util.Locale.ENGLISH)) || PokecubeMod.debug)
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
                    else if (mob.getMove(3) == null)
                    {
                        mob.setMove(3, name);
                    }
                    else
                    {
                        mob.setMove(index, name);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean applyEffect(EntityLivingBase mob, ItemStack stack)
    {
        if (mob.getEntityWorld().isRemote) return stack.hasTagCompound();
        if (stack.hasTagCompound())
        {
            // Check if is TM or valid candy
            return feedToPokemob(stack, mob);
        }
        return false;
    }

    public final PokeType type;

    public ItemTM(PokeType type)
    {
        super();
        this.setMaxStackSize(64);
        this.setMaxDamage(0);
        String name = type.name.equals("???") ? "unknown" : type.name;
        this.setRegistryName(PokecubeMod.ID, "tm_" + name);
        this.setUnlocalizedName("tm_" + name);
        if (type == PokeType.unknown) this.setCreativeTab(PokecubeMod.creativeTabPokecube);
        this.type = type;
        tms.put(type, this);
    }

    /** If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client. */
    @Override
    public boolean getShareTag()
    {
        return true;
    }

}
