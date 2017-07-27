package pokecube.core.moves.templates;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.animations.AnimationMultiAnimations;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;
import thut.lib.Accessor;

public class Move_AOE extends Move_Basic
{
    public Move_AOE(String name)
    {
        super(name);
    }

    @Override
    public void attack(IPokemob attacker, Vector3 location)
    {
        List<Entity> targets = new ArrayList<Entity>();

        Entity entity = attacker.getEntity();

        if (!move.isNotIntercepable())
        {
            Vec3d loc1 = new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
            Vec3d loc2 = new Vec3d(location.x, location.y, location.z);
            RayTraceResult pos = entity.getEntityWorld().rayTraceBlocks(loc1, loc2, false);
            if (pos != null)
            {
                location.set(pos.hitVec);
            }

        }
        targets.addAll(MovesUtils.targetsHit(entity, location, 2, 8));
        int n = targets.size();
        if (n > 0)
        {
            playSounds(entity, null, location);
            for (Entity e : targets)
            {
                if (e != null)
                {
                    Entity attacked = e;
                    if (AnimationMultiAnimations.isThunderAnimation(getAnimation(attacker)))
                    {
                        EntityLightningBolt lightning = new EntityLightningBolt(attacked.getEntityWorld(), 0, 0, 0,
                                false);
                        attacked.onStruckByLightning(lightning);
                    }
                    if (attacked instanceof EntityCreeper)
                    {
                        EntityCreeper creeper = (EntityCreeper) attacked;
                        if (move.type == PokeType.getType("psychic") && creeper.getHealth() > 0)
                        {
                            Accessor.explode(creeper);
                        }
                    }
                    byte statusChange = STATUS_NON;
                    byte changeAddition = CHANGE_NONE;
                    if (move.statusChange != STATUS_NON && MovesUtils.rand.nextFloat() <= move.statusChance)
                    {
                        statusChange = move.statusChange;
                    }
                    if (move.change != CHANGE_NONE && MovesUtils.rand.nextFloat() <= move.chanceChance)
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

}
