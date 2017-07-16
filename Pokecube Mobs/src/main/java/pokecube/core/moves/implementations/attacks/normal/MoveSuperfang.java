package pokecube.core.moves.implementations.attacks.normal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_Basic;

public class MoveSuperfang extends Move_Basic
{

    public MoveSuperfang()
    {
        super("superfang");
        this.setFixedDamage();
    }

    @Override
    public int getPWR(IPokemob attacker, Entity attacked)
    {
        if (!(attacked instanceof EntityLivingBase)) return 0;
        return (int) Math.ceil(((EntityLivingBase) attacked).getHealth() / 2);
    }
}
