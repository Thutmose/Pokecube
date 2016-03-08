package pokecube.core.handlers;

import net.minecraft.item.ItemStack;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.utils.PokeType;

public class HeldItemHandler
{
    public static void processHeldItemUse(MovePacket moveUse, IPokemob mob, ItemStack held)
    {
        if (moveUse.attacker == mob && moveUse.pre)
        {
            moveUse.PWR = (int) (moveUse.PWR * getMoveMultiplier(held, moveUse.attackType));
        }
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
}
