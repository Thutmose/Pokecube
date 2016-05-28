package pokecube.core.database.abilities.p;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import pokecube.core.PokecubeItems;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.items.berries.BerryManager;

public class Pickup extends Ability
{

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        EntityLivingBase poke = (EntityLivingBase) mob;
        if (poke.ticksExisted % 200 == 0 && Math.random() < 0.1)
        {
            if (poke.getHeldItem() == null)
            {
                List<?> items = new ArrayList<Object>(PokecubeItems.heldItems);
                ItemStack item = (ItemStack) items.get(poke.getRNG().nextInt(items.size()));
                if(item!=null && item.getItem() == PokecubeItems.berries)
                {
                    items = Lists.newArrayList(BerryManager.berryNames.keySet());
                    item.setItemDamage((int) items.get(poke.getRNG().nextInt(items.size())));
                }
                if (item != null) poke.setCurrentItemOrArmor(0, item.copy());
            }
        }
    }

}
