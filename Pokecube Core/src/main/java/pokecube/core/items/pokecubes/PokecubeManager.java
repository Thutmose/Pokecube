package pokecube.core.items.pokecubes;

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
import pokecube.core.utils.Tools;

public class PokecubeManager
{
    public static int getCheckSum(ItemStack itemStack)
    {
        return itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("checkSum")
                ? itemStack.getTagCompound().getInteger("checkSum") : 0;
    }

    public static ItemStack getHeldItemMainhand(ItemStack stack)
    {
        if (!isFilled(stack)) return null;

        try
        {
            NBTTagList equipmentTags = (NBTTagList) stack.getTagCompound().getCompoundTag("Pokemob").getTag("Items");

            for (int i = 0; i < equipmentTags.tagCount(); i++)
            {
                byte slot = equipmentTags.getCompoundTagAt(i).getByte("Slot");
                if (slot != 1) continue;
                ItemStack held = ItemStack.loadItemStackFromNBT(equipmentTags.getCompoundTagAt(i));
                return held;
            }

        }
        catch (Exception e)
        {
        }
        return null;
    }

    public static String getOwner(ItemStack itemStack)
    {
        String ret = "";
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("Owner"))
        {
            ret = itemStack.getTagCompound().getString("Owner");
        }
        if ((ret == null || ret.equals("")) && itemStack.hasTagCompound())
        {
            if (itemStack.getTagCompound().hasKey("Pokemob"))
            {
                NBTTagCompound nbt = itemStack.getTagCompound().getCompoundTag("Pokemob");
                if (nbt.hasKey("OwnerUUID"))
                {
                    ret = nbt.getString("OwnerUUID");
                }
            }
        }
        return ret;
    }

    public static int getPokedexNb(ItemStack itemStack)
    {
        return itemStack != null && itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("PokedexNb")
                ? itemStack.getTagCompound().getInteger("PokedexNb") : 0;
    }

    public static NBTTagCompound getSealTag(Entity pokemob)
    {
        return pokemob.getEntityData().getCompoundTag("sealtag");
    }

    public static NBTTagCompound getSealTag(ItemStack stack)
    {
        if (isFilled(stack))
        {
            return stack.getTagCompound().getCompoundTag("Pokemob").getCompoundTag("ForgeData")
                    .getCompoundTag("sealtag");
        }
        else if (stack.hasTagCompound()) { return stack.getTagCompound().getCompoundTag("Explosion"); }
        return null;
    }

    public static byte getStatus(ItemStack itemStack)
    {
        return itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("Status")
                ? itemStack.getTagCompound().getByte("Status") : 0;
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
            IPokemob pokemob = (IPokemob) PokecubeMod.core.createEntityByPokedexNb(num, world);
            if (pokemob == null) { return null; }
            Entity poke = (Entity) pokemob;
            NBTTagCompound pokeTag = itemStack.getTagCompound().getCompoundTag("Pokemob");
            poke.readFromNBT(pokeTag);
            pokemob.popFromPokecube();// should reinit status
            ItemStack cubeStack = itemStack.copy();
            cubeStack.getTagCompound().removeTag("Pokemob");
            pokemob.setPokecube(cubeStack);
            pokemob.setStatus(getStatus(itemStack));
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
            NBTTagCompound poketag = cube.getTagCompound().getCompoundTag("Pokemob");
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
        if (itemStack == null)
        {
            itemStack = new ItemStack(PokecubeItems.getFilledCube(0), 1, damage);
        }
        itemStack = itemStack.copy();
        itemStack.setItemDamage(damage);
        // setUID(itemStack, pokemob.getUid());
        setOwner(itemStack, pokemob.getPokemonOwner());
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
            itemStack.getTagCompound().setInteger("PokedexNb", pokemob.getPokedexNb());
            Entity poke = (Entity) pokemob;
            NBTTagCompound mobTag = new NBTTagCompound();
            poke.writeToNBT(mobTag);
            itemStack.getTagCompound().setTag("Pokemob", mobTag);
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

    public static void setOwner(ItemStack itemStack, Entity owner)
    {
        if (!itemStack.hasTagCompound())
        {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        itemStack.getTagCompound().setString("Owner", owner != null ? owner.getCachedUniqueIdString() : "");
        if (itemStack.getTagCompound().hasKey("Pokemob"))
        {
            NBTTagCompound nbt = itemStack.getTagCompound().getCompoundTag("Pokemob");
            if (nbt.hasKey("OwnerUUID"))
            {
                nbt.setString("OwnerUUID", owner != null ? owner.getCachedUniqueIdString() : "");
            }
            if (itemStack.getTagCompound().hasKey("ownerUUID"))
            {
                nbt.setString("ownerUUID", owner != null ? owner.getCachedUniqueIdString() : "");
            }
            itemStack.getTagCompound().setTag("Pokemob", nbt);
        }

    }

    public static void setStatus(ItemStack itemStack, byte status)
    {
        if (!itemStack.hasTagCompound())
        {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        itemStack.getTagCompound().setByte("Status", status);
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
        if (stack != null)
        {
            int serialization = Tools.getHealedPokemobSerialization();
            stack.setItemDamage(serialization);
            try
            {
                byte oldStatus = PokecubeManager.getStatus(stack);

                if (oldStatus > IMoveConstants.STATUS_NON)
                {
                    String itemName = stack.getDisplayName();
                    if (itemName.contains(" (")) itemName = itemName.substring(0, itemName.lastIndexOf(" "));
                    stack.setStackDisplayName(itemName);
                }
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
            stack.getTagCompound().getCompoundTag("Pokemob").setInteger("hungerTime",
                    -PokecubeMod.core.getConfig().pokemobLifeSpan / 4);
            PokecubeManager.setStatus(stack, IMoveConstants.STATUS_NON);
        }
    }

}
