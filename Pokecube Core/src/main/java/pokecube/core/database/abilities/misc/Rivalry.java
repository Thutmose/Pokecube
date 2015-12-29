package pokecube.core.database.abilities.misc;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;

public class Rivalry extends Ability
{

    @Override
    public void onUpdate(IPokemob mob)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {

        if (!move.pre) return;

        if (mob == move.attacker && move.attacked instanceof IPokemob)
        {
            IPokemob target = (IPokemob) move.attacked;
            byte mobGender = mob.getSexe();
            byte targetGender = target.getSexe();
            if (mobGender == IPokemob.SEXLEGENDARY || targetGender == IPokemob.SEXLEGENDARY
                    || mobGender == IPokemob.NOSEXE || targetGender == IPokemob.NOSEXE) { return; }

            if (mobGender == targetGender)
            {
                move.PWR *= 1.25;
            }
            else
            {
                move.PWR *= 0.75;
            }
        }
    }

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

}
