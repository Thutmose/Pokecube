package pokecube.core.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.utils.PokeType;

public class HeldItemHandler
{
    public static ArrayList<String> megaVariants   = new ArrayList<>();
    public static ArrayList<String> fossilVariants = new ArrayList<>();

    public static void sortMegaVariants()
    {
        List<String> start = Lists.newArrayList();
        for (int i = 0; i < 4; i++)
            start.add(megaVariants.remove(0));
        Collections.sort(megaVariants);
        megaVariants.addAll(0, start);
    }

    private static double getMoveMultiplier(ItemStack held, PokeType move)
    {
        double ret = 1;
        String name = held.getItem().getRegistryName().getResourcePath();
        String typename = name.replace("badge", "");
        PokeType type = PokeType.getType(typename);
        if (type == move) return 1.2;
        return ret;
    }

    public static void processHeldItemUse(MovePacket moveUse, IPokemob mob, ItemStack held)
    {
        if (moveUse.attacker == mob && moveUse.pre)
        {
            moveUse.PWR = (int) (moveUse.PWR * getMoveMultiplier(held, moveUse.attackType));
        }
    }
}
