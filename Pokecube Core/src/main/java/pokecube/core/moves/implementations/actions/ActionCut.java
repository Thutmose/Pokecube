package pokecube.core.moves.implementations.actions;

import java.util.Random;

import net.minecraft.util.EnumFacing;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.TreeRemover;
import thut.api.maths.Vector3;

public class ActionCut implements IMoveAction
{
    public ActionCut()
    {
    }

    @Override
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        if (user.getCombatState(CombatStates.ANGRY)) return false;
        boolean used = false;
        int count = 10;
        int level = user.getLevel();
        int hungerValue = PokecubeMod.core.getConfig().pokemobLifeSpan / 4;
        if (!MoveEventsHandler.canEffectBlock(user, location)) return false;
        TreeRemover remover = new TreeRemover(user.getEntity().getEntityWorld(), location);
        int cut = remover.cut(true);
        if (cut == 0)
        {
            int index = new Random().nextInt(6);
            for (int i = 0; i < 6; i++)
            {
                EnumFacing dir = EnumFacing.VALUES[(i + index) % 6];
                remover = new TreeRemover(user.getEntity().getEntityWorld(), location.offset(dir));
                cut = remover.cut(true);
                if (cut != 0) break;
            }
        }
        count = (int) Math.max(1, Math.ceil(cut * Math.pow((100 - level) / 100d, 3))) * hungerValue;
        if (count > 0)
        {
            remover.cut(false);
            used = true;
            user.setHungerTime(user.getHungerTime() + count);
        }
        remover.clear();
        return used;
    }

    @Override
    public String getMoveName()
    {
        return "cut";
    }
}
