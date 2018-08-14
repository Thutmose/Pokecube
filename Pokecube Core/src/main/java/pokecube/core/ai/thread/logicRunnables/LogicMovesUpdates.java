package pokecube.core.ai.thread.logicRunnables;

import java.util.Collection;
import java.util.logging.Level;

import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.World;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityAffected;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.impl.PersistantStatusEffect;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

/** This applies ongoing moves, applies status effects, and manages sounds when
 * explosion moves are used. It also deals with setting/resetting the
 * transformed target accordingly, as well as ticking the abilities, and
 * activating the held item (like berries) if it should be used. */
public class LogicMovesUpdates extends LogicBase
{
    Vector3 v          = Vector3.getNewVector();
    int     index      = -1;
    int     statusTick = 0;

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

        // Run tasks that only should go on server side.
        if (!world.isRemote)
        {
            int num = pokemob.getAttackCooldown();

            // Check if active move is done, if so, clear it.
            if (pokemob.getActiveMove() != null && pokemob.getActiveMove().isDone()) pokemob.setActiveMove(null);

            // Only reduce cooldown if the pokemob does not currently have a
            // move being fired.
            if (num > 0 && pokemob.getActiveMove() == null) pokemob.setAttackCooldown(num - 1);

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
        }

        // Run tasks that can be on server or client.
        if (pokemob.getTransformedTo() != null && entity.getAttackTarget() == null
                && !(pokemob.getGeneralState(GeneralStates.MATING) || pokemob.getLover() != null))

        {
            pokemob.setTransformedTo(null);
        }
        if (pokemob.getTransformedTo() == null && pokemob.getLover() != null &&

                hasMove(IMoveNames.MOVE_TRANSFORM))
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
            ActionResult<ItemStack> result = usable.onTick(pokemob, pokemob.getHeldItem());
            if (result.getType() == EnumActionResult.SUCCESS) pokemob.setHeldItem(result.getResult());
            if (!CompatWrapper.isValid(pokemob.getHeldItem())) pokemob.setHeldItem(ItemStack.EMPTY);
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
            if (pokemob.getLogicState(LogicStates.SLEEPING))
            {
                int duration = 10;
                entity.addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("blindness"), duration * 2, 100));
                entity.addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), duration * 2, 100));
            }
            return;
        }
        else
        {
            /** Heals the status effects if the capability is soemhow removed,
             * yet it still thinks it has a status. */
            IOngoingAffected affected = CapabilityAffected.getAffected(pokemob.getEntity());
            if (affected == null) return;
            Collection<?> set = affected.getEffects(PersistantStatusEffect.ID);
            if (set.isEmpty() && statusTick++ > 20)
            {
                PokecubeMod.log(Level.WARNING,
                        "Fixed Broken Status " + pokemob.getStatus() + " for " + pokemob.getEntity());
                statusTick = 0;
                pokemob.healStatus();
            }
            else if (!set.isEmpty())
            {
                statusTick = 0;
            }
        }
    }

    @Override
    public void doLogic()
    {
    }
}
