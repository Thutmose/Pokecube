package pokecube.core.moves.implementations.actions;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;

public class ActionFlash implements IMoveAction
{
    public ActionFlash()
    {
    }

    @Override
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        if (user.getPokemonAIState(IMoveConstants.ANGRY)) return false;
        IHungrymob mob = (IHungrymob) user;
        boolean used = false;
        int count = 10;
        int level = user.getLevel();
        int hungerValue = PokecubeMod.core.getConfig().pokemobLifeSpan / 4;

        count = (int) Math.max(1, Math.ceil(count * Math.pow((100 - level) / 100d, 3))) * hungerValue;
        used = mob.getHungerTime() + count < 0;
        EntityLivingBase owner = user.getPokemonOwner();
        if (used)
        {
            owner.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("night_vision"), 5000));
            mob.setHungerTime(mob.getHungerTime() + count);
        }
        return used;
    }

    @Override
    public String getMoveName()
    {
        return "flash";
    }
}
