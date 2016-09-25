/**
 * 
 */
package pokecube.core.moves.templates;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.Explosion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Pokedex;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Tools;
import thut.api.boom.ExplosionCustom;
import thut.api.maths.Vector3;

/** @author Manchou */
public class Move_Explode extends Move_Ongoing
{

    /** @param name
     * @param attackCategory */
    public Move_Explode(String name)
    {
        super(name);
    }

    @Override
    public void attack(IPokemob attacker, Entity attacked)
    {
        if (attacker.getMoveStats().ongoingEffects.containsKey(this)) return;
        if (attacker instanceof EntityLiving)
        {
            EntityLiving voltorb = (EntityLiving) attacker;
            IPokemob pokemob = attacker;
            if (pokemob.getMoveStats().timeSinceIgnited-- <= 0)
            {
                voltorb.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F);
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
            if (sound != null)
            {
                ((Entity) attacker).playSound(sound, 0.5F, 0.4F / (MovesUtils.rand.nextFloat() * 0.4F + 0.8F));
            }
            byte statusChange = STATUS_NON;
            byte changeAddition = CHANGE_NONE;
            if (move.statusChange != STATUS_NON && MovesUtils.rand.nextInt(100) <= move.statusChance)
            {
                statusChange = move.statusChange;
            }
            if (move.change != CHANGE_NONE && MovesUtils.rand.nextInt(100) <= move.chanceChance)
            {
                changeAddition = move.change;
            }
            MovePacket packet = new MovePacket(attacker, attacked, name, move.type, 0, move.crit, statusChange,
                    changeAddition);
            onAttack(packet);
        }
    }

    @Override
    public void attack(IPokemob attacker, Vector3 attacked)
    {
        if (attacker.getMoveStats().ongoingEffects.containsKey(this)) return;
        if (PokecubeMod.core.getConfig().explosions) attack(attacker, (Entity) attacker);
        else
        {
            super.attack(attacker, attacked);
        }
    }

    public void actualAttack(IPokemob attacker, Vector3 location)
    {
        List<Entity> targets = ((Entity) attacker).getEntityWorld()
                .getEntitiesWithinAABBExcludingEntity((Entity) attacker, location.getAABB().expandXyz(8));
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
    public void doOngoingEffect(EntityLiving mob)
    {
        if (!(mob instanceof IPokemob)) return;
        IPokemob pokemob = (IPokemob) mob;

        Entity attacked = mob.getAttackTarget();
        float f1 = (float) (getPWR() * PokecubeCore.core.getConfig().blastStrength * Tools.getStats(pokemob)[1]
                / 100000f);

        if (pokemob.isType(normal)) f1 *= 1.5f;

        Explosion boom = MovesUtils.newExplosion(mob, mob.posX, mob.posY, mob.posZ, f1, false, true);
        ExplosionEvent.Start evt = new ExplosionEvent.Start(mob.getEntityWorld(), boom);
        MinecraftForge.EVENT_BUS.post(evt);
        if (!evt.isCanceled())
        {
            if (PokecubeMod.core.getConfig().explosions) ((ExplosionCustom) boom).doExplosion();
            else
            {
                mob.worldObj.playSound((EntityPlayer) null, mob.posX, mob.posY, mob.posZ,
                        SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F,
                        (1.0F + (mob.worldObj.rand.nextFloat() - mob.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);

                if (getPWR() > 200)
                {
                    mob.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, mob.posX, mob.posY, mob.posZ, 1.0D,
                            0.0D, 0.0D, new int[0]);
                }
                else
                {
                    mob.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, mob.posX, mob.posY, mob.posZ, 1.0D,
                            0.0D, 0.0D, new int[0]);
                }
                actualAttack(pokemob, Vector3.getNewVector().set(pokemob).add(0,
                        pokemob.getSize() * pokemob.getPokedexEntry().height / 2, 0));
            }
            mob.setHealth(0);
            mob.onDeath(DamageSource.generic);
            if (attacked instanceof IPokemob && (((EntityLivingBase) attacked).getHealth() >= 0 && attacked != mob))
            {
                boolean giveExp = true;
                if ((((IPokemob) attacked).getPokemonAIState(IMoveConstants.TAMED)
                        && !PokecubeMod.core.getConfig().pvpExp)
                        && (((IPokemob) attacked).getPokemonOwner() instanceof EntityPlayer))
                {
                    giveExp = false;
                }
                if ((((IPokemob) attacked).getPokemonAIState(IMoveConstants.TAMED)
                        && !PokecubeMod.core.getConfig().trainerExp))
                {
                    giveExp = false;
                }
                if (giveExp)
                {
                    // voltorb's enemy wins XP and EVs even if it didn't
                    // attack
                    ((IPokemob) attacked).setExp(
                            ((IPokemob) attacked).getExp() + Tools.getExp(1, pokemob.getBaseXP(), pokemob.getLevel()),
                            true);
                    byte[] evsToAdd = Pokedex.getInstance().getEntry(pokemob.getPokedexNb()).getEVs();
                    ((IPokemob) attacked).addEVs(evsToAdd);
                }
            }
            pokemob.returnToPokecube();
        }
        else
        {
            pokemob.setExplosionState(-1);
            pokemob.getMoveStats().timeSinceIgnited = 0;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMoveAnimation getAnimation()
    {
        return null;
    }

    @Override
    public int getDuration()
    {
        return 3;
    }

    @Override
    public boolean onSource()
    {
        return true;
    }

    @Override
    public boolean onTarget()
    {
        return false;
    }
}
