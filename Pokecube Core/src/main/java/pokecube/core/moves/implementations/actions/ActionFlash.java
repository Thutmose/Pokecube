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
        EntityLivingBase owner = user.getPokemonOwner();
        if (owner == null) return false;
        IHungrymob mob = (IHungrymob) user;
        int count = 1;
        int level = user.getLevel();
        int hungerValue = PokecubeMod.core.getConfig().pokemobLifeSpan / 16;
        count = (int) Math.max(1, Math.ceil(count * Math.pow((100 - level) / 100d, 3))) * hungerValue;
        PotionEffect effect = new PotionEffect(Potion.getPotionFromResourceLocation("night_vision"), 5000);
        owner.addPotionEffect(effect);
        mob.setHungerTime(mob.getHungerTime() + count);
        return true;
    }

    @Override
    public String getMoveName()
    {
        return "flash";
    }
}
