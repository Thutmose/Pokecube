package pokecube.core.handlers;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.utils.PokeType;

public class HeldItemHandler
{
    public static ArrayList<String> megaVariants   = new ArrayList<>();
    public static ArrayList<String> fossilVariants = new ArrayList<>();

    static
    {
        megaVariants.add("megastone");
        megaVariants.add("shiny_charm");
        megaVariants.add("omegaorb");
        megaVariants.add("alphaorb");
        megaVariants.add("absolmega");
        megaVariants.add("aggronmega");
        megaVariants.add("alakazammega");
        megaVariants.add("ampharosmega");
        megaVariants.add("banettemega");
        megaVariants.add("beedrillmega");
        megaVariants.add("blastoisemega");
        megaVariants.add("blazikenmega");
        megaVariants.add("cameruptmega");
        megaVariants.add("charizardmega-y");
        megaVariants.add("charizardmega-x");
        megaVariants.add("gallademega");
        megaVariants.add("gardevoirmega");
        megaVariants.add("gengarmega");
        megaVariants.add("glaliemega");
        megaVariants.add("mawilemega");
        megaVariants.add("mewtwomega-y");
        megaVariants.add("mewtwomega-x");
        megaVariants.add("pidgeotmega");
        megaVariants.add("pinsirmega");
        megaVariants.add("sableyemega");
        megaVariants.add("salamencemega");
        megaVariants.add("sceptilemega");
        megaVariants.add("scizormega");
        megaVariants.add("slowbromega");
        megaVariants.add("steelixmega");
        megaVariants.add("swampertmega");
        megaVariants.add("venusaurmega");
        // TODO rest of the mega stoneS here.

        fossilVariants.add("omanyte");
        fossilVariants.add("kabuto");
        fossilVariants.add("lileep");
        fossilVariants.add("anorith");
        fossilVariants.add("cranidos");
        fossilVariants.add("shieldon");
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
