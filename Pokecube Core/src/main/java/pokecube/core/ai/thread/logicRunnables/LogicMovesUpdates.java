package pokecube.core.ai.thread.logicRunnables;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.items.ItemPokemobUseable;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.moves.templates.Move_Ongoing;
import thut.api.entity.IBreedingMob;
import thut.api.maths.Vector3;

public class LogicMovesUpdates extends LogicBase
{
    final private EntityAnimal entity;
    final IPokemob             pokemob;
    final PokedexEntry         entry;
    Vector3                    v = Vector3.getNewVector();

    public LogicMovesUpdates(EntityAnimal entity)
    {
        super((IPokemob) entity);
        this.entity = entity;
        pokemob = (IPokemob) entity;
        entry = pokemob.getPokedexEntry();
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
                && !(pokemob.getPokemonAIState(IMoveConstants.MATING) || entity.isInLove()
                        || ((IBreedingMob) entity).getLover() != null))
        {
            pokemob.setTransformedTo(null);
        }

        if (pokemob.getTransformedTo() == null && ((IBreedingMob) entity).getLover() != null
                && hasMove(IMoveNames.MOVE_TRANSFORM))
        {
            pokemob.setTransformedTo(((IBreedingMob) entity).getLover());
        }
        if (pokemob.getAbility() != null)
        {
            pokemob.getAbility().onUpdate(pokemob);
        }
        if (!entity.isDead && entity.getHeldItemMainhand() != null
                && entity.getHeldItemMainhand().getItem() instanceof ItemPokemobUseable)
        {
            ((IPokemobUseable) entity.getHeldItemMainhand().getItem()).itemUse(entity.getHeldItemMainhand(), entity,
                    null);
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

    protected void spawnPoisonParticle()
    {
        // TODO Poison Effects
    }

    protected void updateOngoingMoves()
    {
        if (entity.ticksExisted % 40 == 0)
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
        if (pokemob.getMoveStats().SELFRAISECOUNTER > 0) pokemob.getMoveStats().SELFRAISECOUNTER--;
        if (pokemob.getMoveStats().TARGETLOWERCOUNTER > 0) pokemob.getMoveStats().TARGETLOWERCOUNTER--;
        if (pokemob.getMoveStats().SPECIALCOUNTER > 0) pokemob.getMoveStats().SPECIALCOUNTER--;
    }

    protected void updateStatusEffect()
    {
        int duration = 10;

        short timer = pokemob.getStatusTimer();

        if (timer > 0) pokemob.setStatusTimer((short) (timer - 1));
        byte status = pokemob.getStatus();

        ItemStack held = entity.getHeldItemMainhand();
        if (held != null && held.getItem() instanceof ItemBerry)
        {
            if (BerryManager.berryEffect(pokemob, held))
            {
                HappinessType.applyHappiness(pokemob, HappinessType.BERRY);
                pokemob.setHeldItem(null);
            }
        }

        if (entity.ticksExisted % 20 == 0)
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
        if (entity.ticksExisted % 20 == 0)
        {
            if (status == IMoveConstants.STATUS_BRN)
            {
                entity.setFire(1);
                entity.attackEntityFrom(DamageSource.magic, entity.getMaxHealth() / 16f);
            }
            else if (status == IMoveConstants.STATUS_FRZ)
            {
                entity.addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), duration * 2, 100));
                entity.addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("blindness"), duration * 2, 100));
                if (Math.random() > 0.9)
                {
                    pokemob.healStatus();
                }
            }
            else if (status == IMoveConstants.STATUS_PSN)
            {
                entity.attackEntityFrom(DamageSource.magic, entity.getMaxHealth() / 8f);
                spawnPoisonParticle();

            }
            else if (status == IMoveConstants.STATUS_PSN2)
            {
                entity.attackEntityFrom(DamageSource.magic,
                        (pokemob.getMoveStats().TOXIC_COUNTER + 1) * entity.getMaxHealth() / 16f);
                spawnPoisonParticle();
                spawnPoisonParticle();
                pokemob.getMoveStats().TOXIC_COUNTER++;
            }
            else if (status == IMoveConstants.STATUS_SLP)
            {
                entity.addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("slowness"), duration * 2, 100));
                entity.addPotionEffect(
                        new PotionEffect(Potion.getPotionFromResourceLocation("blindness"), duration * 2, 100));
                if (Math.random() > 0.9 || timer <= 0)
                {
                    pokemob.healStatus();
                }
            }
            else
            {
                pokemob.getMoveStats().TOXIC_COUNTER = 0;
            }
        }
    }

    @Override
    public void doLogic()
    {
    }
}
