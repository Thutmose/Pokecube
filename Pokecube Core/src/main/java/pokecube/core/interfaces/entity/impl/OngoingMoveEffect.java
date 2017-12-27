package pokecube.core.interfaces.entity.impl;

import net.minecraft.util.ResourceLocation;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.moves.templates.Move_Ongoing;

public class OngoingMoveEffect extends BaseEffect
{
    public static final ResourceLocation ID = new ResourceLocation(PokecubeMod.ID, "move_effects");

    public OngoingMoveEffect()
    {
        super(ID);
    }

    public Move_Ongoing move;

    @Override
    public void affectTarget(IOngoingAffected target)
    {
        if (move != null) move.doOngoingEffect(target, this);
    }

    @Override
    public boolean onSavePersistant()
    {
        return false;
    }

    @Override
    public boolean allowMultiple()
    {
        return true;
    }

    @Override
    public AddType canAdd(IOngoingAffected affected, IOngoingEffect toAdd)
    {
        if (toAdd instanceof OngoingMoveEffect && ((OngoingMoveEffect) toAdd).move == move) return AddType.DENY;
        return AddType.ACCEPT;
    }

}
