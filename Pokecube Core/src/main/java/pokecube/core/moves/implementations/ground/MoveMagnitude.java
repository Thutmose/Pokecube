package pokecube.core.moves.implementations.ground;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.Thunder;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

public class MoveMagnitude extends Move_Basic
{

    public MoveMagnitude()
    {
        super("magnitude");
        setSound("ambient.weather.thunder");
    }

    @Override
    public void attack(IPokemob attacker, Vector3 location)
    {
        List<Entity> targets = new ArrayList<Entity>();

        Entity entity = (Entity) attacker;

        if (!move.notIntercepable)
        {
            Vec3d loc1 = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
            Vec3d loc2 = new Vec3d(location.x, location.y, location.z);
            RayTraceResult pos = entity.getEntityWorld().rayTraceBlocks(loc1, loc2, false);
            if (pos != null)
            {
                location.set(pos.hitVec);
            }

        }
        targets.addAll(MovesUtils.targetsHit(((Entity) attacker), location, 2, 8));
        int n = targets.size();
        if (n > 0)
        {
            for (Entity e : targets)
            {
                if (e != null)
                {
                    Entity attacked = e;
                    if (!(e.onGround || e.fallDistance < 0.5)) continue;
                    if (e instanceof IPokemob && ((IPokemob) e).isType(flying)
                            || (e instanceof EntityPlayer && !PokecubeMod.pokemobsDamagePlayers))
                        continue;
                    if (getAnimation() instanceof Thunder)
                    {
                        EntityLightningBolt lightning = new EntityLightningBolt(attacked.getEntityWorld(), 0, 0, 0,
                                false);
                        attacked.onStruckByLightning(lightning);
                    }
                    if (attacked instanceof EntityCreeper)
                    {
                        EntityCreeper creeper = (EntityCreeper) attacked;
                        if (move.type == PokeType.psychic && creeper.getHealth() > 0)
                        {
                            creeper.explode();
                        }
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
                    MovePacket packet = new MovePacket(attacker, attacked, name, move.type, getPWR(attacker, attacked),
                            move.crit, statusChange, changeAddition);
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
    public int getPWR(IPokemob attacker, Entity attacked)
    {
        int PWR = 0;
        int rand = (new Random()).nextInt(20);
        if (rand == 0) PWR = 10;
        else if (rand <= 2) PWR = 30;
        else if (rand <= 6) PWR = 50;
        else if (rand <= 12) PWR = 70;
        else if (rand <= 16) PWR = 90;
        else if (rand <= 18) PWR = 110;
        else PWR = 150;
        return PWR;
    }

}
