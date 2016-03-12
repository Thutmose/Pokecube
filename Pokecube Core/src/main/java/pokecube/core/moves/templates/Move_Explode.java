/**
 * 
 */
package pokecube.core.moves.templates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Explosion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import pokecube.core.database.Pokedex;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.Tools;
import thut.api.maths.ExplosionCustom;
import thut.api.maths.Vector3;

/** @author Manchou */
public class Move_Explode extends Move_Ongoing
{

    /** @param name
     * @param attackCategory */
    public Move_Explode(String name)
    {
        super(name);
        Move_Utility.moves.add(name);
    }

    @Override
    public void attack(IPokemob attacker, Entity attacked, float f)
    {
        if (attacker instanceof EntityLiving)
        {
            EntityLiving voltorb = (EntityLiving) attacker;
            IPokemob pokemob = attacker;
            int i = pokemob.getExplosionState();
            if (i <= 0 && f < 3F || i > 0 && f < 7F)
            {
                if (pokemob.getMoveStats().timeSinceIgnited == 0)
                {
                    voltorb.worldObj.playSoundAtEntity(voltorb, "game.tnt.primed", 1.0F, 0.5F);
                }
                pokemob.setExplosionState(1);
                if (PokecubeMod.core.getConfig().explosions) attacker.addOngoingEffect(this);
                else
                {
                    super.attack(attacker, attacked, f);
                }
            }
            else
            {
                pokemob.setExplosionState(-1);

                pokemob.getMoveStats().timeSinceIgnited--;

                if (pokemob.getMoveStats().timeSinceIgnited < 0)
                {
                    pokemob.getMoveStats().timeSinceIgnited = 0;
                }
            }
        }
    }

    @Override
    public void attack(IPokemob attacker, Vector3 attacked, float f)
    {
        if (PokecubeMod.core.getConfig().explosions) attack(attacker, (Entity) attacker, f);
        else super.attack(attacker, attacked, f);
    }

    @Override
    public void doOngoingEffect(EntityLiving mob)
    {
        if (!(mob instanceof IPokemob)) return;
        IPokemob pokemob = (IPokemob) mob;

        if (pokemob.getMoveStats().timeSinceIgnited >= 30)
        {
            Entity attacked = mob.getAttackTarget();
            float f1 = getPWR() * Tools.getStats(pokemob)[1] / 1000f;

            if (pokemob.isType(normal)) f1 *= 1.5f;

            Explosion boom = MovesUtils.newExplosion(mob, mob.posX, mob.posY, mob.posZ, f1, false, true);
            ExplosionEvent.Start evt = new ExplosionEvent.Start(mob.worldObj, boom);
            MinecraftForge.EVENT_BUS.post(evt);
            if (!evt.isCanceled())
            {
                if (PokecubeMod.core.getConfig().explosions) ((ExplosionCustom) boom).doExplosion();

                mob.setHealth(0);
                mob.onDeath(DamageSource.generic);
                if (attacked instanceof IPokemob && (((EntityLivingBase) attacked).getHealth() >= 0 && attacked != mob))
                {
                    if (!(((IPokemob) attacked).getPokemonAIState(IMoveConstants.TAMED)
                            && !PokecubeMod.core.getConfig().pvpExp))
                    {
                        // voltorb's enemy wins XP and EVs even if it didn't
                        // attack
                        ((IPokemob) attacked).setExp(((IPokemob) attacked).getExp()
                                + Tools.getExp(1, pokemob.getBaseXP(), pokemob.getLevel()), true, false);
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
        else if (pokemob.getMoveStats().timeSinceIgnited < 0 && pokemob.getExplosionState() > 0)
        {
            pokemob.setExplosionState(-1);

            if (pokemob.getMoveStats().timeSinceIgnited < 0)
            {
                pokemob.getMoveStats().timeSinceIgnited = 0;
            }
        }
    }

    @Override
    public int getDuration()
    {
        return 4;
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
