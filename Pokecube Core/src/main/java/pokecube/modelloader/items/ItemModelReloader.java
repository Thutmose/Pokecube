package pokecube.modelloader.items;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.PokedexEntryLoader.XMLDatabase;
import pokecube.core.database.PokedexEntryLoader.XMLPokedexEntry;
import pokecube.modelloader.ModPokecubeML;

public class ItemModelReloader extends Item
{
    public static ItemModelReloader instance;

    public ItemModelReloader()
    {
        super();
        instance = this;
    }

    // 1.11
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        return onItemRightClick(player.getHeldItem(hand), world, player, hand);
    }

    public Object getSerializableCopy(Class<?> type, Object original)
            throws InstantiationException, IllegalAccessException
    {
        Field fields[] = new Field[] {};
        try
        {
            // returns the array of Field objects representing the public fields
            fields = type.getDeclaredFields();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Object copy = null;
        try
        {
            // if (type.isPrimitive()) copy = original;
            // else
            copy = type.newInstance();
        }
        catch (Exception e1)
        {
            copy = original;
        }
        if (copy == original) return copy;
        Object value;
        Object defaultvalue;
        for (Field field : fields)
        {
            try
            {
                if (Modifier.isFinal(field.getModifiers())) continue;
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;
                field.setAccessible(true);
                value = field.get(original);
                defaultvalue = field.get(copy);
                if (value == null) continue;
                if (value.getClass().isPrimitive()) field.set(copy, value);
                else if (defaultvalue != null && defaultvalue.equals(value))
                {
                    field.set(copy, null);
                }
                else if (value instanceof String)
                {
                    if (((String) value).isEmpty())
                    {
                        field.set(copy, null);
                    }
                    else field.set(copy, value);
                }
                else if (value instanceof Object[])
                {
                    if (((Object[]) value).length == 0) field.set(copy, null);
                    else field.set(copy, value);
                }
                else if (value instanceof Map)
                {
                    if (((Map) value).isEmpty()) field.set(copy, null);
                    else field.set(copy, value);
                }
                else if (value instanceof Collection)
                {
                    if (((Collection) value).isEmpty()) field.set(copy, null);
                    else field.set(copy, value);
                }
                else field.set(copy, getSerializableCopy(value.getClass(), value));
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
        return copy;
    }

    // 1.10
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World world, EntityPlayer player,
            EnumHand hand)
    {
        if (itemstack.getItemDamage() != 0)
        {
            if (!world.isRemote)
            {

                try
                {
                    // Gson gson = new Gson();

                    Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

                    XMLDatabase newDatabase = new XMLDatabase();
                    List<XMLPokedexEntry> list = Lists.newArrayList(PokedexEntryLoader.database.pokemon);
                    for (XMLPokedexEntry entry : list)
                    {
                        newDatabase.pokemon.add((XMLPokedexEntry) getSerializableCopy(XMLPokedexEntry.class, entry));
                    }
                    String json = prettyGson.toJson(newDatabase);
                    FileWriter writer = new FileWriter(new File("./pokemobs.json"));
                    writer.append(json);
                    writer.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
        }
        if (!world.isRemote) return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
        player.openGui(ModPokecubeML.instance, 0, player.getEntityWorld(), 0, 0, 0);
        return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
    }
}
