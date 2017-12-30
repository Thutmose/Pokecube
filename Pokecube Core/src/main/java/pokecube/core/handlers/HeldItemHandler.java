package pokecube.core.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.item.ItemStack;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.items.megastuff.ItemMegastone;

public class HeldItemHandler
{
    public static interface IMoveModifier
    {
        void processHeldItemUse(MovePacket moveUse, IPokemob mob, ItemStack held);
    }

    public static Map<Predicate<ItemStack>, IMoveModifier> ITEMMODIFIERS  = Maps.newHashMap();

    public static ArrayList<String>                        megaVariants   = new ArrayList<>();
    public static ArrayList<String>                        fossilVariants = new ArrayList<>();

    public static void sortFossilVariants()
    {
        fossilVariants.removeIf(new Predicate<String>()
        {
            @Override
            public boolean test(String t)
            {
                return !Pokedex.getInstance().isRegistered(Database.getEntry(t));
            }
        });
        fossilVariants.sort(new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                PokedexEntry one = Database.getEntry(o1);
                PokedexEntry two = Database.getEntry(o2);
                return Database.COMPARATOR.compare(one, two);
            }
        });
    }

    public static void sortMegaVariants()
    {
        List<String> start = Lists.newArrayList();
        for (int i = 0; i < 4; i++)
            start.add(megaVariants.remove(0));
        Collections.sort(megaVariants);
        megaVariants.addAll(0, start);
        ItemMegastone.resetMap();
        for (String s : megaVariants)
        {
            ItemMegastone.registerStone(s);
        }
    }

    public static void processHeldItemUse(MovePacket moveUse, IPokemob mob, ItemStack held)
    {
        for (Map.Entry<Predicate<ItemStack>, IMoveModifier> entry : ITEMMODIFIERS.entrySet())
        {
            if (entry.getKey().test(held))
            {
                entry.getValue().processHeldItemUse(moveUse, mob, held);
            }
        }
    }
}
