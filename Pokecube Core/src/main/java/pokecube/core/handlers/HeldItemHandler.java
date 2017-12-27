package pokecube.core.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.item.ItemStack;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;

public class HeldItemHandler
{
    public static interface IMoveModifier
    {
        void processHeldItemUse(MovePacket moveUse, IPokemob mob, ItemStack held);
    }

    public static Map<Predicate<ItemStack>, IMoveModifier> ITEMMODIFIERS  = Maps.newHashMap();

    public static ArrayList<String>                        megaVariants   = new ArrayList<>();
    public static ArrayList<String>                        fossilVariants = new ArrayList<>();

    public static void sortMegaVariants()
    {
        List<String> start = Lists.newArrayList();
        for (int i = 0; i < 4; i++)
            start.add(megaVariants.remove(0));
        Collections.sort(megaVariants);
        megaVariants.addAll(0, start);
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
