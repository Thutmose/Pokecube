package pokecube.core.moves.implementations.attacks.special;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.utils.PokeType;

public class MoveJudgment extends Move_Basic
{

    public MoveJudgment()
    {
        super("judgment");
    }

    /** Type getter
     * 
     * @return the type of this move */
    @Override
    public PokeType getType(IPokemob user)
    {
        if (user == null || !(user instanceof EntityLivingBase)) return move.type;
        ItemStack held = ((EntityLivingBase) user).getHeldItemMainhand();
        if (held != null && held.getItem().getRegistryName().getResourceDomain().contains("pokecube")
                && held.getItem().getRegistryName().getResourcePath().contains("badge"))
        {
            String name = held.getItem().getRegistryName().getResourcePath();
            String typename = name.replace("badge", "");
            PokeType type = PokeType.getType(typename);
            if (type != PokeType.unknown) { return type; }
        }
        return move.type;
    }

}
