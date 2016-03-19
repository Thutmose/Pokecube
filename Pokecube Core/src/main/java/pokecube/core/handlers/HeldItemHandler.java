package pokecube.core.handlers;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.utils.PokeType;

public class HeldItemHandler
{
    public static ArrayList<String> megaVariants = new ArrayList<>();

    static
    {
        megaVariants.add("megastone");
        megaVariants.add("shiny_charm");
        megaVariants.add("omegaorb");
        megaVariants.add("alphaorb");
        megaVariants.add("gardevoirmega");
        megaVariants.add("charizardmega-y");
        megaVariants.add("scizormega");
        megaVariants.add("sceptilemega");
        megaVariants.add("salamencemega");
        megaVariants.add("gallademega");
        megaVariants.add("absolmega");
        megaVariants.add("blastoisemega");
        megaVariants.add("slowbromega");
    }

    private static double getMoveMultiplier(ItemStack held, PokeType move)
    {
        double ret = 1;
        String name = held.getItem().getRegistryName().split(":")[1];
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
