package pokecube.core.database.abilities.grass;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.utils.PokeType;

public class OverGrow extends Ability
{

    @Override
    public void onUpdate(IPokemob mob)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {

        if (!move.pre) return;
        if (mob == move.attacker && move.attackType == PokeType.grass
                && ((EntityLivingBase) mob).getHealth() < ((EntityLivingBase) mob).getMaxHealth() / 3)
        {
            move.PWR *= 1.5;
        }
    }

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

}
