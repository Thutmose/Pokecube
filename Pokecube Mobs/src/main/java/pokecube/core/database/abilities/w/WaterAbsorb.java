package pokecube.core.database.abilities.w;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.utils.PokeType;

public class WaterAbsorb extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if(mob == move.attacked && move.pre && move.attackType == PokeType.getType("water"))
        {
            move.canceled = true;
            EntityLivingBase entity = (EntityLivingBase) mob;
            float hp = entity.getHealth();
            float maxHp = entity.getMaxHealth();
            entity.setHealth(Math.min(hp + 0.25f * maxHp, maxHp));
        }
    }

}
