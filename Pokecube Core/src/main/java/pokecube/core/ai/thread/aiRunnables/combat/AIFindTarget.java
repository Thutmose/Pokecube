package pokecube.core.ai.thread.aiRunnables.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.thread.aiRunnables.AIBase;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.handlers.TeamManager;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.TickHandler;
import thut.api.entity.ai.AIThreadManager;
import thut.api.entity.ai.IAICombat;
import thut.api.maths.Vector3;

/** This IAIRunnable is to find targets for the pokemob to try to kill. */
public class AIFindTarget extends AIBase implements IAICombat
{
    private static class ValidCheck implements Predicate<Entity>
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
            if (input instanceof EntityPlayerMP)
            {
                EntityPlayerMP player = (EntityPlayerMP) input;
                if (player.isSpectator()) return false;
            }
            return true;
        }
    }

    public static class AgroCheck implements Predicate<IPokemob>
    {
        @Override
        public boolean apply(IPokemob input)
        {
            boolean tame = input.getGeneralState(GeneralStates.TAMED);
            boolean wildAgress = !tame;
            if (PokecubeCore.core.getConfig().mobAgroRate > 0)
                wildAgress = wildAgress && new Random().nextInt(PokecubeCore.core.getConfig().mobAgroRate) == 0;
            else wildAgress = false;
            // Check if the mob should always be agressive.
            if (!tame && !wildAgress && input.getEntity().ticksExisted % 20 == 0)
            {
                wildAgress = input.getEntity().getEntityData().getBoolean("alwaysAgress");
            }
            return wildAgress;
        }
    }

    public static boolean handleDamagedTargets = true;
    static
    {
        MinecraftForge.EVENT_BUS.register(AIFindTarget.class);
    }

    public static int           DEAGROTIMER    = 50;
    public static Set<Class<?>> invalidClasses = Sets.newHashSet();
    public static Set<String>   invalidIDs     = Sets.newHashSet();

    public static void initIDs()
    {
        for (String s : PokecubeCore.core.getConfig().guardBlacklistClass)
        {
            try
            {
                Class<?> c = Class.forName(s, false, PokecubeCore.core.getConfig().getClass().getClassLoader());
                AIFindTarget.invalidClasses.add(c);
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        for (String s : PokecubeCore.core.getConfig().guardBlacklistId)
        {
            if (s.endsWith("*"))
            {
                s = s.substring(0, s.length() - 1);
                for (ResourceLocation res : EntityList.getEntityNameList())
                {
                    if (res.toString().startsWith(s))
                    {
                        AIFindTarget.invalidIDs.add(res.toString());
                    }
                }
            }
            else AIFindTarget.invalidIDs.add(s);
        }
    }

    /** Checks the blacklists set via configs, to see whether the target is a
     * valid choice. */
    public static final Predicate<Entity> validTargets            = new ValidCheck();

    /** Checks to see if the wild pokemob should try to agro the nearest visible
     * player. */
    public static Predicate<IPokemob>     shouldAgroNearestPlayer = new AgroCheck();

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
        if (pokemobCap.getGeneralState(GeneralStates.TAMED)
                && ((attacker instanceof EntityPlayer && ((EntityPlayer) attacker) == pokemobCap.getOwner())))
        {
            event.setCanceled(true);
            event.setResult(Result.DENY);
            return;
        }
        pokemobCap.setLogicState(LogicStates.SITTING, false);

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
                        && agres.getCombatState(CombatStates.HUNTING))
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
        if (pokemobCap.getGeneralState(GeneralStates.TAMED)
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

        // Prevent mob from targetting self.
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
        if (pokemob != null)
        {
            // Prevent pokemob from targetting its owner.
            if (pokemob.getOwner() != null && evt.getTarget() == pokemob.getOwner())
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
        // Attempt to swap target onto a pokemob owned by the target entity.
        if (evt.getTarget() != null && evt.getEntityLiving() instanceof EntityLiving)
        {
            List<IPokemob> pokemon = EventsHandler.getPokemobs(evt.getTarget(), 32);
            if (pokemon.isEmpty()) return;
            double closest = 1000;
            IPokemob newtarget = null;
            // Find nearest pokemob owned by the target
            for (IPokemob e : pokemon)
            {
                double dist = e.getEntity().getDistanceSq(evt.getEntityLiving());
                if (e.getEntity() == evt.getEntityLiving()) continue;
                if (e.getEntity().isDead) continue;
                if (dist < closest
                        && !(e.getGeneralState(GeneralStates.STAYING) && e.getLogicState(LogicStates.SITTING))
                        && e.isRoutineEnabled(AIRoutine.AGRESSIVE))
                {
                    closest = dist;
                    newtarget = e;
                }
            }
            // swap target onto the pokemob found.
            if (newtarget != null)
            {
                ((EntityLiving) evt.getEntityLiving()).setAttackTarget(newtarget.getEntity());
                IPokemob mob = CapabilityPokemob.getPokemobFor(evt.getEntityLiving());
                if (mob != null)
                {
                    mob.setCombatState(CombatStates.ANGRY, true);
                    mob.setLogicState(LogicStates.SITTING, false);
                }
                newtarget.getEntity().setAttackTarget(evt.getEntityLiving());
                newtarget.setCombatState(CombatStates.ANGRY, true);
            }
        }
    }

    final IPokemob           pokemob;
    final EntityLiving       entity;
    Vector3                  v                = Vector3.getNewVector();
    Vector3                  v1               = Vector3.getNewVector();

    /** Checks the validTargts as well as team settings, will not allow
     * targetting things on the same team. */
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
            if (entityplayer1.isCreative()) continue;
            if (entityplayer1.isSpectator()) continue;

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

    /** Check if owner is under attack, if so, agress the attacker. <br>
     * <br>
     * This is called from {@link AIFindTarget#run()}
     * 
     * @return if target was found. */
    protected boolean checkOwner()
    {
        List<Entity> list = getEntitiesWithinDistance(entity, 16, EntityLivingBase.class);
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
                    setCombatState(pokemob, CombatStates.ANGRY, true);
                    setLogicState(pokemob, LogicStates.SITTING, false);
                    return true;
                }
            }
        }
        return false;
    }

    /** Check if there is a target to hunt, if so, sets it as target. <br>
     * <br>
     * This is called from {@link AIFindTarget#run()}
     * 
     * @return if a hunt target was found. */
    protected boolean checkHunt()
    {
        List<Entity> list = getEntitiesWithinDistance(entity, 16, EntityLivingBase.class);
        if (!list.isEmpty())
        {
            for (int j = 0; j < list.size(); j++)
            {
                Entity entity = list.get(j);
                IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
                if (mob != null && pokemob.getPokedexEntry().isFood(mob.getPokedexEntry())
                        && pokemob.getLevel() > mob.getLevel() && Vector3.isVisibleEntityFromEntity(entity, entity))
                {
                    addTargetInfo(this.entity, entity);
                    entityTarget = (EntityLivingBase) entity;
                    setCombatState(pokemob, CombatStates.ANGRY, true);
                    setLogicState(pokemob, LogicStates.SITTING, false);
                    return true;
                }
            }
        }
        return false;
    }

    /** Check for and agress any guard targets. <br>
     * <br>
     * This is called from {@link AIFindTarget#run()}
     * 
     * @return a guard target was found */
    protected boolean checkGuard()
    {
        List<EntityLivingBase> ret = new ArrayList<EntityLivingBase>();
        List<Object> pokemobs = new ArrayList<Object>();

        // Select either owner or home position as the centre of the check,
        // this results in it guarding either its home or its owner. Home is
        // used if it is on stay, or it has no owner.
        Vector3 centre = Vector3.getNewVector();
        if (pokemob.getGeneralState(GeneralStates.STAYING) || pokemob.getPokemonOwner() == null)
            centre.set(pokemob.getHome());
        else centre.set(pokemob.getPokemonOwner());

        pokemobs = getEntitiesWithinDistance(centre, entity.dimension, 16, EntityLivingBase.class);

        // Only allow valid guard targets.
        for (Object o : pokemobs)
        {
            if (validGuardTarget.apply((Entity) o)) ret.add((EntityLivingBase) o);
        }

        if (ret.isEmpty()) return false;

        EntityLivingBase newtarget = null;
        double closest = Integer.MAX_VALUE;
        Vector3 here = v1.set(entity, true);

        // Select closest visible guard target.
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

        // Agro the target.
        if (newtarget != null && Vector3.isVisibleEntityFromEntity(entity, newtarget))
        {
            addTargetInfo(entity, newtarget);
            setCombatState(pokemob, CombatStates.ANGRY, true);
            entityTarget = newtarget;
            return true;
        }
        return false;
    }

    @Override
    public void run()
    {
        // No need to find a target if we have one.
        if (entity.getAttackTarget() != null)
        {
            // If target is dead, lets forget about it.
            if (entity.getAttackTarget().isDead || entity.getAttackTarget().getHealth() <= 0)
            {
                addTargetInfo(this.entity, null);
            }
            return;
        }
        // Check if the pokemob is set to follow, and if so, look for mobs
        // nearby trying to attack the owner of the pokemob, if any such are
        // found, try to aggress them immediately.
        if (!pokemob.getGeneralState(GeneralStates.STAYING) && pokemob.getGeneralState(GeneralStates.TAMED))
        {
            if (checkOwner()) return;
        }

        // If hunting, look for valid prey, and if found, agress it.
        if (!pokemob.getLogicState(LogicStates.SITTING) && pokemob.isCarnivore()
                && pokemob.getCombatState(CombatStates.HUNTING))
        {
            if (checkHunt()) return;
        }
        // If guarding, look for mobs not on the same team as you, and if you
        // find them, try to agress them.
        if (pokemob.getCombatState(CombatStates.GUARDING))
        {
            if (checkGuard()) return;
        }
    }

    /** Check if there are any mobs nearby that will help us. <br>
     * <br>
     * This is called from {@link AIFindTarget#shouldRun()}
     * 
     * @return someone needed help. */
    protected boolean checkForHelp(EntityLivingBase from)
    {
        // No need to get help against null
        if (from == null) return false;

        // Not social. doesn't do this.
        if (!pokemob.getPokedexEntry().isSocial) return false;

        // Random factor for this ai to apply
        if (Math.random() < 0.95) return false;

        List<EntityLiving> ret = new ArrayList<EntityLiving>();
        List<Object> pokemobs = new ArrayList<Object>();

        // Select either owner or home position as the centre of the check,
        // this results in it guarding either its home or its owner. Home is
        // used if it is on stay, or it has no owner.
        Vector3 centre = Vector3.getNewVector();
        if (pokemob.getGeneralState(GeneralStates.STAYING) || pokemob.getPokemonOwner() == null)
            centre.set(pokemob.getHome());
        else centre.set(pokemob.getPokemonOwner());

        pokemobs = getEntitiesWithinDistance(centre, entity.dimension, 16, EntityLiving.class);

        // We check for whether it is the same species and, has the same owner
        // (including null) or is on the team.
        Predicate<EntityLiving> relationCheck = new Predicate<EntityLiving>()
        {
            @Override
            public boolean apply(EntityLiving input)
            {
                IPokemob other = CapabilityPokemob.getPokemobFor(input);
                // No pokemob, no helps.
                if (other == null) return false;
                // Not related, no helps.
                if (!other.getPokedexEntry().areRelated(pokemob.getPokedexEntry())) return false;
                // Same owner (owned or null), helps.
                if ((other.getOwnerId() == null && pokemob.getOwnerId() == null)
                        || (other.getOwnerId() != null && other.getOwnerId().equals(pokemob.getOwnerId())))
                    return true;
                // Same team, helps.
                if (TeamManager.sameTeam(input, entity)) return true;
                return false;
            }
        };

        // Only allow valid guard targets.
        for (Object o : pokemobs)
        {
            if (relationCheck.apply((EntityLiving) o)) ret.add((EntityLiving) o);
        }

        for (EntityLiving mob : ret)
        {
            // Only agress mobs that can see you are really under attack.
            if (!mob.canEntityBeSeen(entity)) continue;
            // Only agress if not currently in combat.
            if (mob.getAttackTarget() != null) continue;
            // Make all valid ones agress the target.
            IPokemob other = CapabilityPokemob.getPokemobFor(mob);
            addTargetInfo(mob, from);
            setCombatState(other, CombatStates.ANGRY, false);
        }

        return false;
    }

    @Override
    public boolean shouldRun()
    {
        world = TickHandler.getInstance().getWorldCache(entity.dimension);
        if (world == null || !pokemob.isRoutineEnabled(AIRoutine.AGRESSIVE)) return false;
        EntityLivingBase target = entity.getAttackTarget();

        // Don't look for targets if you are sitting.
        boolean ret = target == null && !pokemob.getLogicState(LogicStates.SITTING);
        boolean tame = pokemob.getGeneralState(GeneralStates.TAMED);

        /*
         * Check for others to try to help you.
         */
        if (checkForHelp(target)) return false;

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
                if (agroTimer == -1 || !pokemob.getCombatState(CombatStates.ANGRY))
                {
                    target = null;
                    agroTimer = -1;
                }
                else
                {
                    if (PokecubeMod.debug)
                        PokecubeMod.log(Level.INFO, "Somehow lost target? Well, found it back again!");
                    addTargetInfo(entity, entityTarget);
                }
            }
        }

        // If we have a target, we don't need to look for another.
        if (target != null)
        {
            // Prevents swapping to owner as target if we are owned and we just
            // defeated someone, only applies to tame mobs, wild mobs will still
            // try to kill the owner if they run away.
            if (entityTarget != null && entityTarget != target && entityTarget instanceof IEntityOwnable
                    && ((IEntityOwnable) entityTarget).getOwner() == target
                    && pokemob.getGeneralState(GeneralStates.TAMED) && (entityTarget.getHealth() <= 0))
            {
                if (PokecubeMod.debug) PokecubeMod.log(Level.INFO, "Battle is over.");
                addTargetInfo(entity, null);
                setCombatState(pokemob, CombatStates.ANGRY, false);
                entityTarget = null;
                target = null;
                agroTimer = -1;
                return false;
            }

            entityTarget = target;
            // If our target is dead, we can forget it, so long as it isn't
            // owned
            if (target.isDead || target.getHealth() <= 0)
            {
                addTargetInfo(entity, null);
                entityTarget = null;
                if (PokecubeMod.debug) PokecubeMod.log(Level.INFO, "Target is dead!");
                return false;
            }

            // If our target is us, we should forget it.
            if (target == entity)
            {
                addTargetInfo(entity, null);
                entityTarget = null;
                if (PokecubeMod.debug) PokecubeMod.log(Level.INFO, "Cannot target self.");
                return false;
            }

            // If we are not angry, we should forget target.
            if (!pokemob.getCombatState(CombatStates.ANGRY))
            {
                addTargetInfo(entity, null);
                entityTarget = null;
                if (PokecubeMod.debug) PokecubeMod.log(Level.INFO, "Not Angry. losing target now.");
                return false;
            }

            // If our target is owner, we should forget it.
            if (target == pokemob.getPokemonOwner())
            {
                addTargetInfo(entity, null);
                entityTarget = null;
                if (PokecubeMod.debug) PokecubeMod.log(Level.INFO, "Cannot target owner.");
                return false;
            }

            // If your owner is too far away, shouldn't have a target, should be
            // going back to the owner.
            if (tame)
            {
                Entity owner = pokemob.getPokemonOwner();
                boolean stayOrGuard = pokemob.getCombatState(CombatStates.GUARDING)
                        || pokemob.getGeneralState(GeneralStates.STAYING);
                if (owner != null && !stayOrGuard
                        && owner.getDistance(entity) > PokecubeMod.core.getConfig().chaseDistance)
                {
                    addTargetInfo(entity, null);
                    entityTarget = null;
                    return false;
                }
            }
            return false;
        }

        // If wild, randomly decided to agro a nearby player instead.
        if (ret && shouldAgroNearestPlayer.apply(pokemob))
        {
            EntityPlayer player = getClosestVulnerablePlayerToEntity(entity,
                    PokecubeMod.core.getConfig().mobAggroRadius);

            if (player != null && Vector3.isVisibleEntityFromEntity(entity, player))
            {
                setCombatState(pokemob, CombatStates.ANGRY, true);
                addTargetInfo(entity, player);
                entityTarget = player;
                if (PokecubeMod.debug) PokecubeMod.log(Level.INFO, "Found player to be angry with, agressing.");
                return false;
            }
        }
        return ret;
    }

}