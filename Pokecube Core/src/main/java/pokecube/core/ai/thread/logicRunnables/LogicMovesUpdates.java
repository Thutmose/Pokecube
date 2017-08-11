package pokecube.core.ai.thread.logicRunnables;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.events.StatusEffectEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemPokemobUseable;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.moves.templates.Move_Ongoing;
import thut.api.maths.Vector3;
import thut.core.common.commands.CommandTools;
import thut.lib.CompatWrapper;

/** This applies ongoing moves, applies status effects, and manages sounds when
 * explosion moves are used. It also deals with setting/resetting the
 * transformed target accordingly, as well as ticking the abilities, and
 * activating the held item (like berries) if it should be used. */
public class LogicMovesUpdates extends LogicBase
{
    Vector3 v = Vector3.getNewVector();

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
        updateOngoingMoves();
        updateStatusEffect();
        doExplosionChecks();

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
        if (!entity.isDead && CompatWrapper.isValid(pokemob.getHeldItem())
                && pokemob.getHeldItem().getItem() instanceof ItemPokemobUseable)
        {
            boolean used = ((IPokemobUseable) pokemob.getHeldItem().getItem()).itemUse(pokemob.getHeldItem(), entity,
                    null);
            if (used)
            {
                ItemStack stack = pokemob.getHeldItem().splitStack(1);
                entity.setHeldItem(EnumHand.MAIN_HAND, stack);
            }
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

    protected void updateOngoingMoves()
    {
        Random rand = new Random(entity.getEntityId());
        if (entity.ticksExisted % 40 == rand.nextInt(40))
        {
            Set<Move_Ongoing> effects = new HashSet<Move_Ongoing>();
            for (Move_Ongoing m : pokemob.getMoveStats().ongoingEffects.keySet())
            {
                effects.add(m);
            }
            for (Move_Ongoing m : effects)
            {
                m.doOngoingEffect(entity);
                int duration = pokemob.getMoveStats().ongoingEffects.get(m);
                if (duration == 0) pokemob.getMoveStats().ongoingEffects.remove(m);
                else if (duration > 0) pokemob.getMoveStats().ongoingEffects.put(m, duration - 1);
            }
        }
        if (pokemob.getMoveStats().DEFENSECURLCOUNTER > 0) pokemob.getMoveStats().DEFENSECURLCOUNTER--;
        if (pokemob.getMoveStats().SPECIALCOUNTER > 0) pokemob.getMoveStats().SPECIALCOUNTER--;
    }

    protected void updateStatusEffect()
    {
        int duration = 10;

        short timer = pokemob.getStatusTimer();
        Random rand = new Random(pokemob.getRNGValue());

        if (timer > 0) pokemob.setStatusTimer((short) (timer - 1));
        byte status = pokemob.getStatus();

        ItemStack held = pokemob.getHeldItem();
        if (held != null && held.getItem() instanceof ItemBerry)
        {
            if (BerryManager.berryEffect(pokemob, held))
            {
                int[] flavours = BerryManager.berryFlavours.get(held.getItemDamage());
                if (flavours != null)
                {
                    for (int i = 0; i < 5; i++)
                    {
                        pokemob.setFlavourAmount(i, pokemob.getFlavourAmount(i) + flavours[i]);
                    }
                }
                HappinessType.applyHappiness(pokemob, HappinessType.BERRY);
                pokemob.setHeldItem(CompatWrapper.nullStack);
            }
        }

        int statusTimer = Math.max(1, PokecubeMod.core.getConfig().attackCooldown);

        if (entity.ticksExisted % statusTimer == rand.nextInt(statusTimer))
        {
            int statusChange = pokemob.getChanges();

            if ((statusChange & IMoveConstants.CHANGE_CURSE) != 0)
            {
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.status.curse", "red",
                        pokemob.getPokemonDisplayName().getFormattedText());
                pokemob.displayMessageToOwner(mess);
                entity.setHealth(entity.getHealth() - entity.getMaxHealth() * 0.25f);
            }

        }

        if (status == IMoveConstants.STATUS_NON)
        {
            if (pokemob.getPokemonAIState(IMoveConstants.SLEEPING))
            {
                entity.addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("blindness"), duration * 2, 100));
                entity.addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), duration * 2, 100));
            }
            return;
        }
        if (entity.ticksExisted % statusTimer == rand.nextInt(statusTimer))
        {
            StatusEffectEvent event = new StatusEffectEvent(entity, status);
            MinecraftForge.EVENT_BUS.post(event);
        }
    }

    @Override
    public void doLogic()
    {
    }
}
