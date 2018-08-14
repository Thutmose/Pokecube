/**
 * 
 */
package pokecube.core.moves.templates;

import java.util.BitSet;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.Explosion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Pokedex;
import pokecube.core.database.moves.MoveEntry;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Tools;
import thut.api.boom.ExplosionCustom;
import thut.api.boom.ExplosionCustom.IEntityHitter;
import thut.api.maths.Vector3;

/** @author Manchou */
public class Move_Explode extends Move_Basic
{
    private static class Hitter implements IEntityHitter
    {
        private final IPokemob     user;
        private final Move_Explode move;
        private final BitSet       hit = new BitSet();

        public Hitter(IPokemob user, Move_Explode move)
        {
            this.user = user;
            this.move = move;
        }

        @Override
        public void hitEntity(Entity e, float power, Explosion boom)
        {
            if (hit.get(e.getEntityId()) || !(e instanceof EntityLivingBase)) return;
            hit.set(e.getEntityId());

            byte statusChange = STATUS_NON;
            byte changeAddition = CHANGE_NONE;
            if (move.move.statusChange != STATUS_NON && MovesUtils.rand.nextFloat() <= move.move.statusChance)
            {
                statusChange = move.move.statusChange;
            }
            if (move.move.change != CHANGE_NONE && MovesUtils.rand.nextFloat() <= move.move.chanceChance)
            {
                changeAddition = move.move.change;
            }
            MovePacket packet = new MovePacket(user, e, move.name, move.move.type, move.getPWR(user, e), move.move.crit,
                    statusChange, changeAddition);
            move.onAttack(packet);
        }

    }

    /** @param name
     * @param attackCategory */
    public Move_Explode(String name)
    {
        super(name);
        move.selfDamage = 100;
        move.selfDamageType = MoveEntry.TOTALHP;
    }

    @Override
    public void attack(IPokemob attacker, Entity attacked)
    {
        if (attacker.getEntity().isDead) return;
        EntityLiving mob = attacker.getEntity();
        IPokemob pokemob = attacker;
        if (pokemob.getMoveStats().timeSinceIgnited-- <= 0)
        {
            mob.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F);
            pokemob.setExplosionState(1);
            pokemob.getMoveStats().timeSinceIgnited = 10;
        }
        if (attacker.getStatus() == STATUS_SLP)
        {
            MovesUtils.displayStatusMessages(attacker, attacked, STATUS_SLP, false);
            return;
        }
        if (attacker.getStatus() == STATUS_FRZ)
        {
            MovesUtils.displayStatusMessages(attacker, attacked, STATUS_FRZ, false);
            return;
        }
        if (attacker.getStatus() == STATUS_PAR && Math.random() > 0.75)
        {
            MovesUtils.displayStatusMessages(attacker, attacked, STATUS_PAR, false);
            return;
        }
        playSounds(mob, attacked, null);
        float f1 = (float) (getPWR(pokemob, attacked) * PokecubeMod.core.getConfig().blastStrength
                * pokemob.getStat(Stats.ATTACK, true) / 500000f);

        ExplosionCustom boom = MovesUtils.newExplosion(mob, mob.posX, mob.posY, mob.posZ, f1, false, true);
        boom.hitter = new Hitter(pokemob, this);
        ExplosionEvent.Start evt = new ExplosionEvent.Start(mob.getEntityWorld(), boom);
        MinecraftForge.EVENT_BUS.post(evt);
        if (!evt.isCanceled())
        {
            mob.setHealth(0);// kill the mob.
            if (PokecubeMod.core.getConfig().explosions && MoveEventsHandler.canEffectBlock(pokemob, v.set(mob)))
            {
                ((ExplosionCustom) boom).doExplosion();
            }
            else
            {
                mob.getEntityWorld().playSound((EntityPlayer) null, mob.posX, mob.posY, mob.posZ,
                        SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F,
                        (1.0F + (mob.getEntityWorld().rand.nextFloat() - mob.getEntityWorld().rand.nextFloat()) * 0.2F)
                                * 0.7F);

                if (getPWR() > 200)
                {
                    mob.getEntityWorld().spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, mob.posX, mob.posY, mob.posZ,
                            1.0D, 0.0D, 0.0D, new int[0]);
                }
                else
                {
                    mob.getEntityWorld().spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, mob.posX, mob.posY, mob.posZ,
                            1.0D, 0.0D, 0.0D, new int[0]);
                }
                actualAttack(pokemob, Vector3.getNewVector().set(pokemob.getEntity()).add(0,
                        pokemob.getSize() * pokemob.getPokedexEntry().height / 2, 0));
            }
        }
        attacker.returnToPokecube();

    }

    @Override
    public void attack(IPokemob attacker, Vector3 attacked)
    {
        if (attacker.getEntity().isDead) return;
        if (PokecubeMod.core.getConfig().explosions) attack(attacker, attacker.getEntity());
        else
        {
            super.attack(attacker, attacked);
        }
    }

    /** This does the somewhat normal attack code.
     * 
     * @param attacker
     * @param location */
    public void actualAttack(IPokemob attacker, Vector3 location)
    {
        List<Entity> targets = attacker.getEntity().getEntityWorld()
                .getEntitiesWithinAABBExcludingEntity(attacker.getEntity(), location.getAABB().grow(8));
        List<Entity> toRemove = Lists.newArrayList();
        for (Entity e : targets)
            if (!(e instanceof EntityLivingBase)) toRemove.add(e);
        targets.removeAll(toRemove);
        int n = targets.size();
        if (n > 0)
        {
            for (Entity e : targets)
            {
                if (e != null)
                {
                    Entity attacked = e;
                    MovePacket packet = new MovePacket(attacker, attacked, name, move.type, getPWR(attacker, attacked),
                            move.crit, STATUS_NON, CHANGE_NONE);
                    packet.applyOngoing = false;
                    onAttack(packet);
                }
            }
        }
        else
        {
            MovesUtils.displayEfficiencyMessages(attacker, null, -1, 0);
        }
        doWorldAction(attacker, location);
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        Entity attacked = packet.attacked;
        IPokemob pokemob = packet.attacker;
        IPokemob target = CapabilityPokemob.getPokemobFor(attacked);
        if (!PokecubeMod.core.getConfig().explosions) if ((pokemob.getEntity().getHealth() <= 0) && target != null
                && (pokemob.getEntity().getHealth() >= 0 && attacked != pokemob))
        {
            boolean giveExp = true;
            if ((target.getGeneralState(GeneralStates.TAMED) && !PokecubeMod.core.getConfig().pvpExp)
                    && (target.getPokemonOwner() instanceof EntityPlayer))
            {
                giveExp = false;
            }
            if ((target.getGeneralState(GeneralStates.TAMED) && !PokecubeMod.core.getConfig().trainerExp))
            {
                giveExp = false;
            }
            if (giveExp)
            {
                // voltorb's enemy wins XP and EVs even if it didn't
                // attack
                target.setExp(target.getExp() + Tools.getExp(PokecubeCore.core.getConfig().expScaleFactor,
                        pokemob.getBaseXP(), pokemob.getLevel()), true);
                byte[] evsToAdd = Pokedex.getInstance().getEntry(pokemob.getPokedexNb()).getEVs();
                target.addEVs(evsToAdd);
            }
        }
        super.postAttack(packet);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMoveAnimation getAnimation()
    {
        return null;
    }
}
