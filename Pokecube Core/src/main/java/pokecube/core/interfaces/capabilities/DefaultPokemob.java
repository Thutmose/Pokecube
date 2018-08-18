package pokecube.core.interfaces.capabilities;

import java.util.logging.Level;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import pokecube.core.ai.thread.aiRunnables.AIBase.PathManager;
import pokecube.core.ai.thread.aiRunnables.AIFollowOwner;
import pokecube.core.ai.thread.aiRunnables.combat.AIAttack;
import pokecube.core.ai.thread.aiRunnables.combat.AICombatMovement;
import pokecube.core.ai.thread.aiRunnables.combat.AIDodge;
import pokecube.core.ai.thread.aiRunnables.combat.AIFindTarget;
import pokecube.core.ai.thread.aiRunnables.combat.AILeap;
import pokecube.core.ai.thread.aiRunnables.combat.AISelectMove;
import pokecube.core.ai.thread.aiRunnables.idle.AIGuardEgg;
import pokecube.core.ai.thread.aiRunnables.idle.AIHungry;
import pokecube.core.ai.thread.aiRunnables.idle.AIIdle;
import pokecube.core.ai.thread.aiRunnables.idle.AIMate;
import pokecube.core.ai.thread.aiRunnables.idle.AIRoutes;
import pokecube.core.ai.thread.aiRunnables.utility.AIGatherStuff;
import pokecube.core.ai.thread.aiRunnables.utility.AIStoreStuff;
import pokecube.core.ai.thread.aiRunnables.utility.AIUseMove;
import pokecube.core.ai.utils.GuardAI.ShouldRun;
import pokecube.core.ai.utils.PokemobMoveHelper;
import pokecube.core.ai.utils.pathing.PokemobNavigator;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.InitAIEvent;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.impl.PokemobSaves;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import thut.api.entity.ai.AIThreadManager.AIStuff;

public class DefaultPokemob extends PokemobSaves implements ICapabilitySerializable<NBTTagCompound>, IPokemob
{
    public DefaultPokemob()
    {
        for (AIRoutine routine : AIRoutine.values())
        {
            setRoutineState(routine, routine.getDefault());
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == POKEMOB_CAP;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (hasCapability(capability, facing)) return POKEMOB_CAP.cast(this);
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag;
        try
        {
            tag = writePokemobData();
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.WARNING, "Error Saving Pokemob", e);
            tag = new NBTTagCompound();
        }
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        try
        {
            readPokemobData(nbt);
        }
        catch (Exception e)
        {
            PokecubeMod.log(Level.WARNING, "Error Loading Pokemob", e);
        }
    }

    @Override
    public void onSetTarget(EntityLivingBase entity)
    {
        boolean remote = getEntity().getEntityWorld().isRemote;
        if (entity == null && !remote)
        {
            setTargetID(-1);
            getEntity().getEntityData().setString("lastMoveHitBy", "");
        }
        else if (entity != null)
        {
            /** Ensure that the target being set is actually a valid target. */
            if (entity == getEntity())
            {
                if (getEntity().getAttackTarget() == getEntity()) getEntity().setAttackTarget(null);
                return;
            }
            else if (entity instanceof IEntityOwnable && ((IEntityOwnable) entity).getOwner() == getOwner())
            {
                getEntity().setAttackTarget(null);
                return;
            }
            else if (!AIFindTarget.validTargets.apply(entity))
            {
                getEntity().setAttackTarget(null);
                return;
            }
        }
        if (entity == null || remote) return;
        setLogicState(LogicStates.SITTING, false);
        setTargetID(entity.getEntityId());
        setCombatState(CombatStates.ANGRY, true);
        if (entity != getEntity().getAttackTarget() && getAbility() != null && !entity.getEntityWorld().isRemote)
        {
            getAbility().onAgress(this, entity);
        }
    }

    @Override
    public int getTargetID()
    {
        return dataManager.get(params.ATTACKTARGETIDDW);
    }

    @Override
    public void setTargetID(int id)
    {
        dataManager.set(params.ATTACKTARGETIDDW, Integer.valueOf(id));
    }

    @Override
    public AIStuff getAI()
    {
        return aiStuff;
    }

    @Override
    public boolean selfManaged()
    {
        return false;
    }

    @Override
    public void setHeading(float heading)
    {
        if (getGeneralState(GeneralStates.CONTROLLED))
        {
            getEntity().rotationYaw = heading;
            dataManager.set(params.HEADINGDW, heading);
        }
    }

    @Override
    public float getHeading()
    {
        if (getGeneralState(GeneralStates.CONTROLLED)) { return dataManager.get(params.HEADINGDW); }
        return getEntity().rotationYaw;
    }

    @Override
    public void initAI()
    {
        EntityLiving entity = getEntity();
        PokedexEntry entry = getPokedexEntry();
        // If the mob was constructed without a world somehow (during init for
        // JEI, etc), do not bother with AI stuff.
        if (entity.getEntityWorld() == null) return;

        // Set the pathing priorities for various blocks
        if (entity.isImmuneToFire)
        {
            entity.setPathPriority(PathNodeType.LAVA, 0);
            entity.setPathPriority(PathNodeType.DAMAGE_FIRE, 0);
            entity.setPathPriority(PathNodeType.DANGER_FIRE, 0);
        }
        else
        {
            entity.setPathPriority(PathNodeType.LAVA, 20);
            entity.setPathPriority(PathNodeType.DAMAGE_FIRE, 8);
            entity.setPathPriority(PathNodeType.DANGER_FIRE, 8);
        }
        if (swims())
        {
            entity.setPathPriority(PathNodeType.WATER, 0);
        }
        if (getPokedexEntry().hatedMaterial != null) for (String material : getPokedexEntry().hatedMaterial)
        {
            if (material.equalsIgnoreCase("water"))
            {
                entity.setPathPriority(PathNodeType.WATER, -1);
            }
            else if (material.equalsIgnoreCase("fire"))
            {
                entity.setPathPriority(PathNodeType.DAMAGE_FIRE, -1);
                entity.setPathPriority(PathNodeType.DANGER_FIRE, -1);
            }
        }

        // These are used by pokecube's implementation of IPokemob.
        this.navi = new PokemobNavigator(this, entity.getEntityWorld());
        this.mover = new PokemobMoveHelper(entity);

        // Generate a PathManager to use to ensure AI doesn't clear paths for
        // more important runnables.
        PathManager manager = new PathManager();

        // Add in the Custom type of AI tasks.

        // Tasks for combat

        // Choose what attacks to use
        this.getAI().addAITask(new AISelectMove(this).setPathManager(manager).setPriority(190));
        // Attack stuff
        this.getAI().addAITask(new AIAttack(this).setPathManager(manager).setPriority(200));
        // Dodge attacks
        this.getAI().addAITask(new AIDodge(this).setPathManager(manager).setPriority(225));
        // Leap at things
        this.getAI().addAITask(new AILeap(this).setPathManager(manager).setPriority(225));
        // Move around in combat
        this.getAI().addAITask(new AICombatMovement(this).setPathManager(manager).setPriority(250));
        // Look for targets to kill
        this.getAI().addAITask(new AIFindTarget(this).setPathManager(manager).setPriority(400));

        // Idle tasks

        // Guard your egg
        this.getAI().addAITask(new AIGuardEgg(this).setPathManager(manager).setPriority(250));
        // Mate with things
        this.getAI().addAITask(new AIMate(this).setPathManager(manager).setPriority(300));
        // Eat things
        this.getAI().addAITask(new AIHungry(this, new EntityItem(entity.getEntityWorld()), 16).setPathManager(manager)
                .setPriority(300));
        // Wander around
        this.getAI().addAITask(new AIIdle(this).setPathManager(manager).setPriority(500));

        // Task for following routes/maintaining home location
        this.guardCap = entity.getCapability(EventsHandler.GUARDAI_CAP, null);
        AIRoutes routes = new AIRoutes(getEntity(), guardCap);
        routes.wrapped.shouldRun = new ShouldRun()
        {
            @Override
            public boolean shouldRun()
            {
                if (!getGeneralState(GeneralStates.TAMED)) return true;
                return getGeneralState(GeneralStates.STAYING);
            }
        };
        // Follow paths or stay near home
        this.getAI().addAITask(routes.setPathManager(manager).setPriority(275));

        // Utility tasks
        AIStoreStuff ai = new AIStoreStuff(this);
        // Store things in chests
        this.getAI().addAITask(ai.setPathManager(manager).setPriority(350));
        // Gather things from ground
        this.getAI().addAITask(new AIGatherStuff(this, 32, ai).setPathManager(manager).setPriority(400));
        // Execute moves when told to
        this.getAI().addAITask(new AIUseMove(this).setPathManager(manager).setPriority(250));

        // Owner related tasks
        if (!entry.isStationary)
        {
            // Follow owner around
            this.getAI()
                    .addAITask(new AIFollowOwner(this, 2 + entity.width + this.length, 2 + entity.width + this.length)
                            .setPathManager(manager).setPriority(400));
        }

        // Send notification event of AI initilization, incase anyone wants to
        // affect it.
        MinecraftForge.EVENT_BUS.post(new InitAIEvent(this));
    }
}