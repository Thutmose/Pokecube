package pokecube.core.database.abilities.fire;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.interfaces.IPokemob.MovePacket;
import thut.api.maths.Vector3;

public class FlameBody extends Ability
{
    int range = 4;

    @Override
    public void onUpdate(IPokemob mob)
    {
        Vector3 v = Vector3.getNewVectorFromPool().set(mob);
        List<EntityPokemobEgg> eggs = ((Entity) mob).worldObj.getEntitiesWithinAABB(EntityPokemobEgg.class,
                v.getAABB().expand(range, range, range));
        for (EntityPokemobEgg egg : eggs)
        {
            egg.incubateEgg();
        }
        v.freeVectorFromPool();
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        Move_Base attack = move.getMove();

        IPokemob attacker = move.attacker;
        if (attacker == mob || move.pre || attacker == move.attacked) return;
        if (move.hit && attack.getAttackCategory() == IMoveConstants.CATEGORY_CONTACT && Math.random() > 0.7)
        {
            move.attacker.setStatus(IMoveConstants.STATUS_BRN);
        }
    }

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

    @Override
    public Ability init(Object... args)
    {
        for (int i = 0; i < 2; i++)
            if (args != null && args.length > i)
            {
                if (args[i] instanceof Integer)
                {
                    range = (int) args[i];
                    return this;
                }
            }
        return this;
    }

}
