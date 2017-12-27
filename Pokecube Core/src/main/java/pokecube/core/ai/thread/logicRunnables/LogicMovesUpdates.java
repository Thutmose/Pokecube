package pokecube.core.ai.thread.logicRunnables;

import net.minecraft.init.SoundEvents;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemobUseable;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

/** This applies ongoing moves, applies status effects, and manages sounds when
 * explosion moves are used. It also deals with setting/resetting the
 * transformed target accordingly, as well as ticking the abilities, and
 * activating the held item (like berries) if it should be used. */
public class LogicMovesUpdates extends LogicBase
{
    Vector3 v     = Vector3.getNewVector();
    int     index = -1;

    public LogicMovesUpdates(IPokemob entity)
    {
        super(entity);
    }

    private void doExplosionChecks()
    {
        pokemob.getMoveStats().lastActiveTime = pokemob.getMoveStats().timeSinceIgnited;

        int i = pokemob.getExplosionState();

        if (i > 0 && pokemob.getMoveStats().timeSinceIgnited == 0)
        {
            entity.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F);
        }
        pokemob.getMoveStats().timeSinceIgnited += i;

        if (pokemob.getMoveStats().timeSinceIgnited < 0)
        {
            pokemob.getMoveStats().timeSinceIgnited = 0;
        }
        if (entity.getAttackTarget() == null && pokemob.getMoveStats().timeSinceIgnited > 50) //
        {
            pokemob.setExplosionState(-1);
            pokemob.getMoveStats().timeSinceIgnited--;

            if (pokemob.getMoveStats().timeSinceIgnited < 0)
            {
                pokemob.getMoveStats().timeSinceIgnited = 0;
            }
        }
    }

    @Override
    public void doServerTick(World world)
    {
        super.doServerTick(world);
        v.set(entity);
        pokemob.setAttackCooldown(Math.max(0, pokemob.getAttackCooldown() - 1));

        for (int i = 0; i < 4; i++)
        {
            int timer = pokemob.getDisableTimer(i);
            if (timer > 0) pokemob.setDisableTimer(i, timer - 1);
        }

        if (pokemob.getMoveStats().DEFENSECURLCOUNTER > 0) pokemob.getMoveStats().DEFENSECURLCOUNTER--;
        if (pokemob.getMoveStats().SPECIALCOUNTER > 0) pokemob.getMoveStats().SPECIALCOUNTER--;

        updateStatusEffect();
        doExplosionChecks();

        // Reset move specific counters if the move index has changed.
        if (index != pokemob.getMoveIndex())
        {
            pokemob.getMoveStats().FURYCUTTERCOUNTER = 0;
            pokemob.getMoveStats().PHYSICALDAMAGETAKENCOUNTER = 0;
            pokemob.getMoveStats().SPECIALDAMAGETAKENCOUNTER = 0;
        }
        index = pokemob.getMoveIndex();

        if (pokemob.getMoves()[0] == null)
        {
            pokemob.learn(IMoveNames.MOVE_TACKLE);
        }

        if (pokemob.getTransformedTo() != null && entity.getAttackTarget() == null
                && !(pokemob.getPokemonAIState(IMoveConstants.MATING) || pokemob.getLover() != null))
        {
            pokemob.setTransformedTo(null);
        }

        if (pokemob.getTransformedTo() == null && pokemob.getLover() != null && hasMove(IMoveNames.MOVE_TRANSFORM))
        {
            pokemob.setTransformedTo(pokemob.getLover());
        }
        if (pokemob.getAbility() != null && entity.isServerWorld())
        {
            pokemob.getAbility().onUpdate(pokemob);
        }
        IPokemobUseable usable = IPokemobUseable.getUsableFor(pokemob.getHeldItem());
        if (!entity.isDead && usable != null)
        {
            usable.onTick(pokemob, pokemob.getHeldItem());
            if (!CompatWrapper.isValid(pokemob.getHeldItem())) pokemob.setHeldItem(CompatWrapper.nullStack);
        }
    }

    public boolean hasMove(String move)
    {
        for (String s : pokemob.getMoves())
        {
            if (s != null && s.equalsIgnoreCase(move)) return true;
        }
        return false;
    }

    protected void updateStatusEffect()
    {
        byte status = pokemob.getStatus();
        if (status == IMoveConstants.STATUS_NON)
        {
            if (pokemob.getPokemonAIState(IMoveConstants.SLEEPING))
            {
                int duration = 10;
                entity.addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("blindness"), duration * 2, 100));
                entity.addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), duration * 2, 100));
            }
            return;
        }
    }

    @Override
    public void doLogic()
    {
    }
}
