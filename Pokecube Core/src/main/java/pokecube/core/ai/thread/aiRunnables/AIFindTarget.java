package pokecube.core.ai.thread.aiRunnables;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.TeamManager;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.TickHandler;
import thut.api.entity.ai.AIThreadManager;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;

/** This IAIRunnable is to find targets for the pokemob to try to kill. */
public class AIFindTarget extends AIBase implements IAICombat
{
    public static Set<Class<?>>           invalidClasses   = Sets.newHashSet();
    public static Set<String>             invalidIDs       = Sets.newHashSet();

    public static final Predicate<Entity> validTargets     = new Predicate<Entity>()
                                                           {
                                                               @Override
                                                               public boolean apply(Entity input)
                                                               {
                                                                   String id = EntityList.getEntityString(input);
                                                                   if (invalidIDs.contains(id)) return false;
                                                                   id = EntityList.getKey(input).toString();
                                                                   if (invalidIDs.contains(id)) return false;
                                                                   for (Class<?> clas : invalidClasses)
                                                                   {
                                                                       if (clas.isInstance(input)) return false;
                                                                   }
                                                                   return true;
                                                               }
                                                           };

    final IPokemob                        pokemob;
    final EntityLiving                    entity;
    Vector3                               v                = Vector3.getNewVector();
    Vector3                               v1               = Vector3.getNewVector();

    final Predicate<Entity>               validGuardTarget = new Predicate<Entity>()
                                                           {
                                                               @Override
                                                               public boolean apply(Entity input)
                                                               {
                                                                   IPokemob testMob = CapabilityPokemob
                                                                           .getPokemobFor(input);
                                                                   if (testMob != null && input != pokemob.getEntity())
                                                                   {
                                                                       if (!TeamManager.sameTeam(entity,
                                                                               input)) { return true; }
                                                                   }
                                                                   else if (input instanceof EntityLivingBase)
                                                                   {
                                                                       if (!validTargets.apply(input)) return false;
                                                                       if (!TeamManager.sameTeam(entity,
                                                                               input)) { return true; }
                                                                   }
                                                                   return false;
                                                               }
                                                           };

    public AIFindTarget(IPokemob mob)
    {
        this.pokemob = mob;
        this.entity = mob.getEntity();
    }

    /** Returns the closest vulnerable player within the given radius, or null
     * if none is found. */
    EntityPlayer getClosestVulnerablePlayer(double x, double y, double z, double distance, int dimension)
    {
        double d4 = -1.0D;
        EntityPlayer entityplayer = null;

        Vector<?> playerEntities = AIThreadManager.worldPlayers.get(dimension);
        ArrayList<?> list = new ArrayList<Object>(playerEntities);
        if (list.isEmpty()) return null;

        for (int i = 0; i < list.size(); ++i)
        {
            if (!(list.get(i) instanceof EntityPlayer)) continue;

            EntityPlayer entityplayer1 = (EntityPlayer) list.get(i);

            if (!entityplayer1.capabilities.disableDamage && entityplayer1.isEntityAlive())
            {
                double d5 = entityplayer1.getDistanceSq(x, y, z);
                double d6 = distance;

                if (entityplayer1.isSneaking())
                {
                    d6 = distance * 0.800000011920929D;
                }

                if (entityplayer1.isInvisible())
                {
                    float f = entityplayer1.getArmorVisibility();

                    if (f < 0.1F)
                    {
                        f = 0.1F;
                    }

                    d6 *= 0.7F * f;
                }

                if ((distance < 0.0D || d5 < d6 * d6) && (d4 == -1.0D || d5 < d4))
                {
                    d4 = d5;
                    entityplayer = entityplayer1;
                }
            }
        }

        return entityplayer;
    }

    /** Returns the closest vulnerable player to this entity within the given
     * radius, or null if none is found */
    EntityPlayer getClosestVulnerablePlayerToEntity(Entity entity, double distance)
    {
        return this.getClosestVulnerablePlayer(entity.posX, entity.posY, entity.posZ, distance, entity.dimension);
    }

    @Override
    public void reset()
    {

    }

    @Override
    public void run()
    {
        // Check if the pokemob is set to follow, and if so, look for mobs
        // nearby trying to attack the owner of the pokemob, if any such are
        // found, try to aggress them immediately.
        if (entity.getAttackTarget() == null && !pokemob.getPokemonAIState(IMoveConstants.STAYING)
                && pokemob.getPokemonAIState(IMoveConstants.TAMED) && !PokecubeCore.isOnClientSide())
        {
            List<Object> list = getEntitiesWithinDistance(entity, 16, EntityLivingBase.class);
            if (!list.isEmpty() && pokemob.getPokemonOwner() != null)
            {
                for (int j = 0; j < list.size(); j++)
                {
                    Entity entity = (Entity) list.get(j);

                    if (entity instanceof EntityCreature && ((EntityCreature) entity).getAttackTarget() != null
                            && ((EntityCreature) entity).getAttackTarget().equals(pokemob.getPokemonOwner())
                            && Vector3.isVisibleEntityFromEntity(entity, entity))
                    {
                        addTargetInfo(entity, entity);
                        setPokemobAIState(pokemob, IMoveConstants.ANGRY, true);
                        setPokemobAIState(pokemob, IMoveConstants.SITTING, false);
                        return;
                    }
                }
            }
        }

        // If hunting, look for valid prey, and if found, agress it.
        if (entity.getAttackTarget() == null && !pokemob.getPokemonAIState(IMoveConstants.SITTING)
                && pokemob.isCarnivore() && pokemob.getPokemonAIState(IMoveConstants.HUNTING))
        {
            List<Object> list = getEntitiesWithinDistance(entity, 16, EntityLivingBase.class);
            if (!list.isEmpty())
            {
                for (int j = 0; j < list.size(); j++)
                {
                    Entity entity = (Entity) list.get(j);
                    IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
                    if (mob != null && pokemob.getPokedexEntry().isFood(mob.getPokedexEntry())
                            && pokemob.getLevel() > mob.getLevel() && Vector3.isVisibleEntityFromEntity(entity, entity))
                    {
                        addTargetInfo(entity, entity);
                        setPokemobAIState(pokemob, IMoveConstants.ANGRY, true);
                        setPokemobAIState(pokemob, IMoveConstants.SITTING, false);
                        return;
                    }
                }
            }
        }
        // If guarding, look for mobs not on the same team as you, and if you
        // find them, try to agress them.
        if (pokemob.getPokemonAIState(IMoveConstants.GUARDING))
        {
            List<EntityLivingBase> ret = new ArrayList<EntityLivingBase>();
            List<Object> pokemobs = new ArrayList<Object>();

            Vector3 centre = Vector3.getNewVector();
            if (pokemob.getPokemonAIState(IMoveConstants.STAYING) || pokemob.getPokemonOwner() == null)
                centre.set(pokemob.getHome());
            else centre.set(pokemob.getPokemonOwner());

            pokemobs = getEntitiesWithinDistance(centre, entity.dimension, 16, EntityLivingBase.class);
            for (Object o : pokemobs)
            {
                if (validGuardTarget.apply((Entity) o)) ret.add((EntityLivingBase) o);
            }
            EntityLivingBase newtarget = null;
            double closest = Integer.MAX_VALUE;
            Vector3 here = v1.set(entity, true);
            for (EntityLivingBase e : ret)
            {
                double dist = e.getDistanceSqToEntity(entity);
                v.set(e, true);
                if (dist < closest && here.isVisible(world, v))
                {
                    closest = dist;
                    newtarget = e;
                }
            }

            if (newtarget != null && Vector3.isVisibleEntityFromEntity(entity, newtarget))
            {
                addTargetInfo(entity, newtarget);
                setPokemobAIState(pokemob, IMoveConstants.ANGRY, true);
                setPokemobAIState(pokemob, IMoveConstants.SITTING, false);
                return;
            }
        }
    }

    @Override
    public boolean shouldRun()
    {
        world = TickHandler.getInstance().getWorldCache(entity.dimension);
        if (world == null || !pokemob.isRoutineEnabled(AIRoutine.AGRESSIVE)) return false;

        // Don't look for targets if you are sitting.
        boolean ret = entity.getAttackTarget() == null && entity.getAttackTarget() == null
                && !pokemob.getPokemonAIState(IMoveConstants.SITTING);
        // If target is dead, return false.
        if (entity.getAttackTarget() != null && entity.getAttackTarget().isDead)
        {
            setPokemobAIState(pokemob, IMoveConstants.ANGRY, false);
            addTargetInfo(entity, null);
            return false;
        }
        // If your owner is too far away, don't go looking for targets, you
        // should be trying to walk to your owner instead.
        boolean tame = pokemob.getPokemonAIState(IMoveConstants.TAMED);
        if (tame && entity.getAttackTarget() != null)
        {
            Entity owner = pokemob.getPokemonOwner();
            boolean stayOrGuard = pokemob.getPokemonAIState(IMoveConstants.GUARDING)
                    || pokemob.getPokemonAIState(IMoveConstants.STAYING);
            if (owner != null && !stayOrGuard
                    && owner.getDistanceToEntity(entity) > PokecubeMod.core.getConfig().chaseDistance)
            {
                setPokemobAIState(pokemob, IMoveConstants.ANGRY, false);
                addTargetInfo(entity, null);
                return false;
            }
        }
        boolean wildAgress = !tame && entity.getRNG().nextInt(200) == 0;
        if (!tame && !wildAgress && entity.ticksExisted % 20 == 0)
        {
            wildAgress = entity.getEntityData().getBoolean("alwaysAgress");
        }
        // If wild, randomly decided to agro a nearby player instead.
        // TODO make this configurable somehow based on specifics
        if (ret && wildAgress)
        {
            EntityPlayer player = getClosestVulnerablePlayerToEntity(entity,
                    PokecubeMod.core.getConfig().mobAggroRadius);

            if (player != null && Vector3.isVisibleEntityFromEntity(entity, player))
            {
                setPokemobAIState(pokemob, IMoveConstants.ANGRY, true);
                addTargetInfo(entity, player);
                return false;
            }
        }
        return ret;
    }

}