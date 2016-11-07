package pokecube.core.moves.implementations.actions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class ActionPayDay implements IMoveAction
{
    public ActionPayDay()
    {
    }

    @Override
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        int amountNugget = (int) (Math.random() * 2);
        if (amountNugget > 0)
        {
            EntityItem item = ((Entity) user).dropItem(Items.GOLD_NUGGET, amountNugget);
            location.moveEntity(item);
            item.setPickupDelay(0);
            return true;
        }
        return false;
    }

    @Override
    public String getMoveName()
    {
        return "payday";
    }
}
