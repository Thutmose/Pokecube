package pokecube.core.database.abilities.psychic;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;

public class Trace extends Ability
{
    Ability traced;

    @Override
    public void onUpdate(IPokemob mob)
    {
        if (traced != null && ((EntityLiving)mob).getAttackTarget()==null)
        {
            traced.destroy();
            traced = null;
        }
        else if (traced != null)
        {
            traced.onUpdate(mob);
        }
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (traced != null) traced.onMoveUse(mob, move);
    }

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
        if (traced != null) traced.onAgress(mob, target);
        else if (target instanceof IPokemob)
        {
            Ability ability = ((IPokemob) target).getMoveStats().ability;
            if (ability != null)
            {
                traced = AbilityManager.makeAbility(ability.getClass(), mob);
            }
        }
    }

}
