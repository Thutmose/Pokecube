package pokecube.core.database.abilities.m;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.utils.PokeType;

public class Multitype extends Ability
{

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        PokedexEntry entry = mob.getPokedexEntry();

        if (!entry.getName().contains("Arceus")) return;

        ItemStack held = ((EntityLivingBase) mob).getHeldItemMainhand();
        if (held != null && held.getItem().getRegistryName().getResourceDomain().contains("pokecube")
                && held.getItem().getRegistryName().getResourcePath().contains("badge"))
        {
            String name = held.getItem().getRegistryName().getResourcePath();
            String typename = name.replace("badge", "");
            PokeType type = PokeType.getType(typename);
            if (type != PokeType.unknown)
            {
                mob.changeForme("arceus" + type);
                return;
            }
        }
        if (entry.getBaseForme() != null && entry.getBaseName().equals("Arceus"))
        {
            mob.changeForme(entry.getBaseName());
            return;
        }

    }

}
