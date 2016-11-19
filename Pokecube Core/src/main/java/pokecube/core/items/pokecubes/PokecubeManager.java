package pokecube.core.items.pokecubes;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.lib.CompatWrapper;

public class PokecubeManager
{
    public static ItemStack getHeldItemMainhand(ItemStack stack)
    {
        if (!isFilled(stack)) return CompatWrapper.nullStack;
        try
        {
            NBTTagList equipmentTags = (NBTTagList) stack.getTagCompound().getCompoundTag(TagNames.POKEMOBTAG)
                    .getCompoundTag(TagNames.INVENTORYTAG).getTag(TagNames.ITEMS);
            for (int i = 0; i < equipmentTags.tagCount(); i++)
            {
                byte slot = equipmentTags.getCompoundTagAt(i).getByte("Slot");
                if (slot != 1) continue;
                ItemStack held = CompatWrapper.fromTag(equipmentTags.getCompoundTagAt(i));
                return held;
            }
        }
        catch (Exception e)
        {
        }
        return CompatWrapper.nullStack;
    }

    public static String getOwner(ItemStack itemStack)
    {
        if (!CompatWrapper.isValid(itemStack) || !itemStack.hasTagCompound()) return "";
        NBTTagCompound poketag = itemStack.getTagCompound().getCompoundTag(TagNames.POKEMOB)
                .getCompoundTag(TagNames.POKEMOBTAG);
        // TODO remove this legacy support.
        if (poketag.hasNoTags())
        {
            if (itemStack.getTagCompound().hasKey(TagNames.POKEMOB))
            {
                NBTTagCompound nbt = itemStack.getTagCompound().getCompoundTag(TagNames.POKEMOB);
                if (nbt.hasKey("OwnerUUID")) { return nbt.getString("OwnerUUID"); }
            }
        }
        return poketag.getCompoundTag(TagNames.OWNERSHIPTAG).getString(TagNames.OWNER);
    }

    public static int getPokedexNb(ItemStack itemStack)
    {
        if (!CompatWrapper.isValid(itemStack) || !itemStack.hasTagCompound()) return 0;
        NBTTagCompound poketag = itemStack.getTagCompound().getCompoundTag(TagNames.POKEMOB)
                .getCompoundTag(TagNames.POKEMOBTAG);
        int number = poketag.getCompoundTag(TagNames.OWNERSHIPTAG).getInteger(TagNames.POKEDEXNB);
        // TODO remove this legacy support as well
        if (poketag.hasNoTags() || number == 0) return itemStack.getTagCompound().hasKey("PokedexNb")
                ? itemStack.getTagCompound().getInteger("PokedexNb") : 0;
        return number;
    }

    public static NBTTagCompound getSealTag(Entity pokemob)
    {
        IPokemob poke = (IPokemob) pokemob;
        ItemStack cube;
        if (!CompatWrapper.isValid((cube = poke.getPokecube()))) return null;
        return CompatWrapper.getTag(cube, TagNames.POKESEAL, false);
    }

    public static NBTTagCompound getSealTag(ItemStack stack)
    {
        if (isFilled(stack))
        {
            return stack.getTagCompound().getCompoundTag(TagNames.POKEMOB).getCompoundTag(TagNames.VISUALSTAG)
                    .getCompoundTag(TagNames.POKECUBE).getCompoundTag("tag").getCompoundTag(TagNames.POKESEAL);
        }
        else if (stack.hasTagCompound()) { return stack.getTagCompound().getCompoundTag(TagNames.POKESEAL); }
        return null;
    }

    public static byte getStatus(ItemStack itemStack)
    {
        if (!itemStack.hasTagCompound()) return 0;
        NBTTagCompound poketag = itemStack.getTagCompound().getCompoundTag(TagNames.POKEMOB)
                .getCompoundTag(TagNames.POKEMOBTAG);
        return poketag.getCompoundTag(TagNames.STATSTAG).getByte(TagNames.STATUS);
    }

    public static int getTilt(ItemStack itemStack)
    {
        return itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("tilt")
                ? itemStack.getTagCompound().getInteger("tilt") : 0;
    }

    public static boolean isFilled(ItemStack stack)
    {
        return (getPokedexNb(stack) != 0);
    }

    public static IPokemob itemToPokemob(ItemStack itemStack, World world)
    {
        if (!itemStack.hasTagCompound()) return null;
        int num = getPokedexNb(itemStack);
        if (num != 0)
        {
            IPokemob pokemob = (IPokemob) PokecubeMod.core.createPokemob(Database.getEntry(num), world);
            if (pokemob == null) { return null; }
            Entity poke = (Entity) pokemob;
            NBTTagCompound pokeTag = itemStack.getTagCompound().getCompoundTag(TagNames.POKEMOB);
            poke.readFromNBT(pokeTag);
            ItemStack cubeStack = pokemob.getPokecube();
            if (!CompatWrapper.isValid(cubeStack))
            {
                cubeStack = itemStack.copy();
                cubeStack.getTagCompound().removeTag(TagNames.POKEMOB);
                pokemob.setPokecube(cubeStack);
            }
            ((EntityLivingBase) pokemob).extinguish();
            return pokemob;
        }
        return null;
    }

    public static PokedexEntry getEntry(ItemStack cube)
    {
        PokedexEntry ret = null;
        if (isFilled(cube))
        {
            NBTTagCompound poketag = cube.getTagCompound().getCompoundTag(TagNames.POKEMOB);
            if (poketag != null)
            {
                String forme = poketag.getString("forme");
                if (forme != null && !forme.isEmpty())
                {
                    ret = Database.getEntry(forme);
                }
            }
            if (ret == null)
            {
                int num = getPokedexNb(cube);
                ret = Database.getEntry(num);
            }
        }
        return ret;
    }

    public static ItemStack pokemobToItem(IPokemob pokemob)
    {
        ItemStack itemStack = pokemob.getPokecube();
        int damage = Tools.serialize(((EntityLivingBase) pokemob).getMaxHealth(),
                ((EntityLivingBase) pokemob).getHealth());
        if (!CompatWrapper.isValid(itemStack))
        {
            itemStack = new ItemStack(PokecubeItems.getFilledCube(0), 1, damage);
        }
        itemStack = itemStack.copy();
        itemStack.setItemDamage(damage);
        setOwner(itemStack, pokemob.getPokemonOwnerID());
        setColor(itemStack);
        int status = pokemob.getStatus();
        setStatus(itemStack, pokemob.getStatus());
        String itemName = pokemob.getPokemonDisplayName().getFormattedText();
        if (status == IMoveConstants.STATUS_BRN) itemName += " (BRN)";
        else if (status == IMoveConstants.STATUS_FRZ) itemName += " (FRZ)";
        else if (status == IMoveConstants.STATUS_PAR) itemName += " (PAR)";
        else if (status == IMoveConstants.STATUS_PSN || status == IMoveConstants.STATUS_PSN2) itemName += " (PSN)";
        else if (status == IMoveConstants.STATUS_SLP) itemName += " (SLP)";

        itemStack.setStackDisplayName(itemName);

        if (pokemob instanceof Entity)
        {
            Entity poke = (Entity) pokemob;
            NBTTagCompound mobTag = new NBTTagCompound();
            poke.writeToNBT(mobTag);
            itemStack.getTagCompound().setTag(TagNames.POKEMOB, mobTag);
            itemStack.getTagCompound().removeTag(TagNames.POKESEAL);
        }
        return itemStack;
    }

    public static void setColor(ItemStack itemStack)
    {
        int color = 0xEEEEEE;

        int id = PokecubeItems.getCubeId(itemStack);

        if (itemStack.getItem() == PokecubeItems.pokemobEgg)
        {
            color = 0x78C848;
        }
        else if (id == 0)
        {
            color = 0xEE0000;
        }
        else if (id == 1)
        {
            color = 0x0B90CE;
        }
        else if (id == 2)
        {
            color = 0xDCA937;
        }
        else if (id == 3)
        {
            color = 0x332F6A;
        }

        NBTTagCompound var3 = itemStack.getTagCompound();

        if (var3 == null)
        {
            var3 = new NBTTagCompound();
            itemStack.setTagCompound(var3);
        }

        NBTTagCompound var4 = var3.getCompoundTag("display");

        if (!var3.hasKey("display"))
        {
            var3.setTag("display", var4);
        }

        var4.setInteger("cubecolor", color);
    }

    public static void setOwner(ItemStack itemStack, UUID owner)
    {
        if (!itemStack.hasTagCompound()) return;
        NBTTagCompound poketag = itemStack.getTagCompound().getCompoundTag(TagNames.POKEMOB)
                .getCompoundTag(TagNames.POKEMOBTAG);
        if (owner == null) poketag.getCompoundTag(TagNames.OWNERSHIPTAG).removeTag(TagNames.OWNER);
        else poketag.getCompoundTag(TagNames.OWNERSHIPTAG).setString(TagNames.OWNER, owner.toString());
    }

    public static void setStatus(ItemStack itemStack, byte status)
    {
        if (!itemStack.hasTagCompound()) return;
        NBTTagCompound poketag = itemStack.getTagCompound().getCompoundTag(TagNames.POKEMOB)
                .getCompoundTag(TagNames.POKEMOBTAG);
        poketag.getCompoundTag(TagNames.STATSTAG).setByte(TagNames.STATUS, status);
    }

    public static void setTilt(ItemStack itemStack, int number)
    {
        if (!itemStack.hasTagCompound())
        {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        itemStack.getTagCompound().setInteger("tilt", number);
    }

    public static void heal(ItemStack stack)
    {
        if (CompatWrapper.isValid(stack))
        {
            int serialization = Tools.getHealedPokemobSerialization();
            NBTTagCompound entityTag = stack.getTagCompound().getCompoundTag(TagNames.POKEMOB);
            NBTTagCompound poketag = entityTag.getCompoundTag(TagNames.POKEMOBTAG);
            stack.setItemDamage(serialization);
            try
            {
                byte oldStatus = PokecubeManager.getStatus(stack);
                entityTag.setShort("Fire", (short) -1);
                entityTag.setShort("DeathTime", (short) 0);
                entityTag.setInteger("HurtByTimestamp", 0);
                if (oldStatus > IMoveConstants.STATUS_NON)
                {
                    String itemName = stack.getDisplayName();
                    if (itemName.contains(" (")) itemName = itemName.substring(0, itemName.lastIndexOf(" "));
                    stack.setStackDisplayName(itemName);
                }
                float maxHealth = 0;
                NBTTagList attrList = entityTag.getTagList("Attributes", 10);
                for (int i = 0; i < attrList.tagCount(); ++i)
                {
                    NBTTagCompound nbttagcompound = attrList.getCompoundTagAt(i);
                    String name = nbttagcompound.getString("Name");
                    if (name.equals("generic.maxHealth"))
                    {
                        maxHealth = (float) nbttagcompound.getDouble("Base");
                        break;
                    }
                }
                entityTag.setFloat("Health", maxHealth);
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
            poketag.getCompoundTag(TagNames.AITAG).setInteger(TagNames.HUNGER,
                    -PokecubeMod.core.getConfig().pokemobLifeSpan / 4);
            PokecubeManager.setStatus(stack, IMoveConstants.STATUS_NON);
        }
    }

    public static UUID getUUID(ItemStack stack)
    {
        if (!isFilled(stack)) return null;
        NBTTagCompound pokeTag = stack.getTagCompound().getCompoundTag(TagNames.POKEMOB);
        long min = pokeTag.getLong("UUIDLeast");
        long max = pokeTag.getLong("UUIDMost");
        return new UUID(max, min);
    }
}
