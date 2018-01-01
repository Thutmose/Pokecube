package pokecube.core.handlers;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.item.ItemStack;
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

    public static void sortMegaVariants()
    {
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
