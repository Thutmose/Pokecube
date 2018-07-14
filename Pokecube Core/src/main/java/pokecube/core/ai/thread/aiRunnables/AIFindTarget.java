package pokecube.core.ai.thread.aiRunnables;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.handlers.TeamManager;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.TickHandler;
import thut.api.entity.ai.AIThreadManager;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;

/** This IAIRunnable is to find targets for the pokemob to try to kill. */
public class AIFindTarget extends AIBase implements IAICombat
{
    public static boolean handleDamagedTargets = true;
    static
    {
        MinecraftForge.EVENT_BUS.register(AIFindTarget.class);
    }

    public static int                     DEAGROTIMER    = 50;
    public static Set<Class<?>>           invalidClasses = Sets.newHashSet();
    public static Set<String>             invalidIDs     = Sets.newHashSet();

    public static final Predicate<Entity> validTargets   = new Predicate<Entity>()
                                                         {
                                                             @Override
                                                             public boolean apply(Entity input)
                                                             {
                                                                 String id = EntityList.getEntityString(input);
                                                                 if (invalidIDs.contains(id)) return false;
                                                                 ResourceLocation eid = EntityList.getKey(input);
                                                                 if (eid != null) id = eid.toString();
                                                                 if (invalidIDs.contains(id)) return false;
                                                                 for (Class<?> clas : invalidClasses)
                                                                 {
                                                                     if (clas.isInstance(input)) return false;
                                                                 }
                                                                 return true;
                                                             }
                                                         };

    /** Prevents the owner from attacking their own pokemob, and takes care of
     * properly setting attack targets for whatever was hurt. */
    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent event)
    {
        if (!handleDamagedTargets || event.getEntity().getEntityWorld().isRemote) return;

        DamageSource source = event.getSource();
        EntityLivingBase attacked = event.getEntityLiving();
        IPokemob pokemobCap = CapabilityPokemob.getPokemobFor(attacked);
        if (pokemobCap == null) return;

        Entity attacker = source.getTrueSource();

        // Camcel the event if it is from owner.
        if (pokemobCap.getPokemonAIState(IMoveConstants.TAMED)
                && ((attacker instanceof EntityPlayer && ((EntityPlayer) attacker) == pokemobCap.getOwner())))
        {
            event.setCanceled(true);
            event.setResult(Result.DENY);
            return;
        }
        pokemobCap.setPokemonAIState(IMoveConstants.SITTING, false);

        if (attacked instanceof EntityLiving)
        {
            EntityLiving living = (EntityLiving) attacked;
            EntityLivingBase oldTarget = living.getAttackTarget();

            // Don't include dead old targets.
            if (oldTarget != null && oldTarget.isDead) oldTarget = null;

            if (!(oldTarget == null && attacker != living && attacker instanceof EntityLivingBase
                    && living.getAttackTarget() != attacker))
            {
                attacker = null;
            }

            IPokemob agres = CapabilityPokemob.getPokemobFor(attacker);
            if (agres != null)
            {
                if (agres.getPokedexEntry().isFood(pokemobCap.getPokedexEntry())
                        && agres.getPokemonAIState(IMoveConstants.HUNTING))
                {
                    // track running away.
                }
                if (agres.getLover() == living && attacker != null)
                {
                    agres.setLover(attacker);
                }

            }

            // Either keep old target, or agress the attacker.
            if (oldTarget != null && living.getAttackTarget() != oldTarget) living.setAttackTarget(oldTarget);
            else if (attacker instanceof EntityLivingBase) living.setAttackTarget((EntityLivingBase) attacker);
        }

    }

    /** Prevents the owner from attacking their own pokemob. */
    @SubscribeEvent
    public static void onAttacked(LivingAttackEvent event)
    {
        if (!handleDamagedTargets || event.getEntity().getEntityWorld().isRemote) return;

        DamageSource source = event.getSource();
        EntityLivingBase attacked = event.getEntityLiving();
        IPokemob pokemobCap = CapabilityPokemob.getPokemobFor(attacked);
        if (pokemobCap == null) return;

        Entity attacker = source.getTrueSource();

        // Camcel the event if it is from owner.
        if (pokemobCap.getPokemonAIState(IMoveConstants.TAMED)
                && ((attacker instanceof EntityPlayer && ((EntityPlayer) attacker) == pokemobCap.getOwner())))
        {
            event.setCanceled(true);
            event.setResult(Result.DENY);
            return;
        }
    }

    @SubscribeEvent
    public static void livingSetTargetEvent(LivingSetAttackTargetEvent evt)
    {
        if (!handleDamagedTargets || evt.getEntity().getEntityWorld().isRemote) return;
        // Only handle attack target set, not revenge target set.
        if (evt.getTarget() == evt.getEntityLiving().getRevengeTarget()) return;

        if (evt.getTarget() == evt.getEntityLiving())
        {
            if (PokecubeMod.core.getConfig().debug)
            {
                PokecubeMod.log(Level.WARNING, evt.getTarget() + " is targetting self again.",
                        new IllegalArgumentException());
            }
            return;
        }
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(evt.getEntityLiving());
        if (pokemob != null && pokemob.getOwner() != null)
        {
            if (evt.getTarget() == pokemob.getOwner())
            {
                if (PokecubeMod.core.getConfig().debug)
                {
                    PokecubeMod.log(Level.WARNING, evt.getTarget() + " is targetting owner.",
                            new IllegalArgumentException());
                }
                return;
            }
            pokemob.onSetTarget(evt.getTarget());
        }
        if (evt.getTarget() != null && evt.getEntityLiving() instanceof EntityLiving)
        {
            List<IPokemob> pokemon = EventsHandler.getPokemobs(evt.getTarget(), 32);
            if (pokemon.isEmpty()) return;
            double closest = 1000;
            IPokemob newtarget = null;
            for (IPokemob e : pokemon)
            {
                double dist = e.getEntity().getDistanceSq(evt.getEntityLiving());
                if (e.getEntity() == evt.getEntityLiving()) continue;
                if (dist < closest
                        && !(e.getPokemonAIState(IMoveConstants.STAYING) && e.getPokemonAIState(IMoveConstants.SITTING))
                        && e.isRoutineEnabled(AIRoutine.AGRESSIVE))
                {
                    closest = dist;
                    newtarget = e;
                }
            }
            if (newtarget != null && newtarget.getEntity() != evt.getEntityLiving())
            {
                ((EntityLiving) evt.getEntityLiving()).setAttackTarget(newtarget.getEntity());
                IPokemob mob = CapabilityPokemob.getPokemobFor(evt.getEntityLiving());
                if (mob != null)
                {
                    mob.setPokemonAIState(IMoveConstants.ANGRY, true);
                    mob.setPokemonAIState(IMoveConstants.SITTING, false);
                }
                newtarget.getEntity().setAttackTarget(evt.getEntityLiving());
                newtarget.setPokemonAIState(IMoveConstants.ANGRY, true);
            }
        }
    }

    final IPokemob           pokemob;
    final EntityLiving       entity;
    Vector3                  v                = Vector3.getNewVector();
    Vector3                  v1               = Vector3.getNewVector();

    final Predicate<Entity>  validGuardTarget = new Predicate<Entity>()
                                              {
                                                  @Override
                                                  public boolean apply(Entity input)
                                                  {
                                                      IPokemob testMob = CapabilityPokemob.getPokemobFor(input);
                                                      if (testMob != null && input != pokemob.getEntity())
                                                      {
                                                          if (!TeamManager.sameTeam(entity, input)) { return true; }
                                                      }
                                                      else if (input instanceof EntityLivingBase)
                                                      {
                                                          if (!validTargets.apply(input)) return false;
                                                          if (!TeamManager.sameTeam(entity, input)) { return true; }
                                                      }
                                                      return false;
                                                  }
                                              };
    private int              agroTimer        = -1;
    private EntityLivingBase entityTarget     = null;

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
        // No need to find a target if we have one.
        if (entity.getAttackTarget() != null)
        {
            // If target is dead, lets forget about it.
            if (entity.getAttackTarget().isDead)
            {
                addTargetInfo(this.entity, null);
            }
            return;
        }
        // Check if the pokemob is set to follow, and if so, look for mobs
        // nearby trying to attack the owner of the pokemob, if any such are
        // found, try to aggress them immediately.
        if (!pokemob.getPokemonAIState(IMoveConstants.STAYING) && pokemob.getPokemonAIState(IMoveConstants.TAMED)
                && !PokecubeCore.isOnClientSide())
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
                        addTargetInfo(this.entity, entity);
                        entityTarget = (EntityLivingBase) entity;
                        setPokemobAIState(pokemob, IMoveConstants.ANGRY, true);
                        setPokemobAIState(pokemob, IMoveConstants.SITTING, false);
                        return;
                    }
                }
            }
        }

        // If hunting, look for valid prey, and if found, agress it.
        if (!pokemob.getPokemonAIState(IMoveConstants.SITTING) && pokemob.isCarnivore()
                && pokemob.getPokemonAIState(IMoveConstants.HUNTING))
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
                        addTargetInfo(this.entity, entity);
                        entityTarget = (EntityLivingBase) entity;
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
                double dist = e.getDistanceSq(entity);
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
                entityTarget = newtarget;
                return;
            }
        }
    }

    @Override
    public boolean shouldRun()
    {
        world = TickHandler.getInstance().getWorldCache(entity.dimension);
        if (world == null || !pokemob.isRoutineEnabled(AIRoutine.AGRESSIVE)) return false;
        EntityLivingBase target = entity.getAttackTarget();

        // Don't look for targets if you are sitting.
        boolean ret = target == null && !pokemob.getPokemonAIState(IMoveConstants.SITTING);

        boolean tame = pokemob.getPokemonAIState(IMoveConstants.TAMED);

        if (target == null && entityTarget != null)
        {
            target = entityTarget;
            if (agroTimer == -1)
            {
                agroTimer = DEAGROTIMER;
            }
            else
            {
                agroTimer--;
                if (agroTimer == -1 || !pokemob.getPokemonAIState(IMoveConstants.ANGRY))
                {
                    target = null;
                    agroTimer = -1;
                }
                else
                {
                    addTargetInfo(entity, entityTarget);
                }
            }
        }

        // If we have a target, we don't need to look for another.
        if (target != null)
        {
            entityTarget = target;
            // If our target is dead, we can forget it, so long as it isn't
            // owned
            if (target.isDead)
            {
                if (target instanceof IEntityOwnable)
                {
                    EntityLivingBase newTarget = ((IEntityOwnable) target).getOwner() instanceof EntityLivingBase
                            ? (EntityLivingBase) ((IEntityOwnable) target).getOwner() : null;
                    entityTarget = newTarget;
                    addTargetInfo(entity, entityTarget);
                }
                else
                {
                    addTargetInfo(entity, null);
                    entityTarget = null;
                }
            }

            // If our target is us, we should forget it.
            if (target == entity)
            {
                addTargetInfo(entity, null);
                entityTarget = null;
            }

            // If our target is owner, we should forget it.
            if (target == pokemob.getPokemonOwner())
            {
                addTargetInfo(entity, null);
                entityTarget = null;
            }

            // If your owner is too far away, shouldn't have a target, should be
            // going back to the owner.
            if (tame)
            {
                Entity owner = pokemob.getPokemonOwner();
                boolean stayOrGuard = pokemob.getPokemonAIState(IMoveConstants.GUARDING)
                        || pokemob.getPokemonAIState(IMoveConstants.STAYING);
                if (owner != null && !stayOrGuard
                        && owner.getDistance(entity) > PokecubeMod.core.getConfig().chaseDistance)
                {
                    addTargetInfo(entity, null);
                    entityTarget = null;
                }
            }
            return false;
        }

        boolean wildAgress = !tame && entity.getRNG().nextInt(200) == 0;
        // Check if the mob should always be agressive.
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
                entityTarget = player;
                return false;
            }
        }
        return ret;
    }

}