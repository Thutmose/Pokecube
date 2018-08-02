package pokecube.core.interfaces.capabilities;

import java.util.logging.Level;

import net.minecraft.entity.EntityCreature;
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
import pokecube.core.ai.pokemob.PokemobAIHurt;
import pokecube.core.ai.pokemob.PokemobAIUtilityMove;
import pokecube.core.ai.thread.aiRunnables.AIAttack;
import pokecube.core.ai.thread.aiRunnables.AIBase.PathManager;
import pokecube.core.ai.thread.aiRunnables.AICombatMovement;
import pokecube.core.ai.thread.aiRunnables.AIFindTarget;
import pokecube.core.ai.thread.aiRunnables.AIFollowOwner;
import pokecube.core.ai.thread.aiRunnables.AIGatherStuff;
import pokecube.core.ai.thread.aiRunnables.AIGuardEgg;
import pokecube.core.ai.thread.aiRunnables.AIHungry;
import pokecube.core.ai.thread.aiRunnables.AIIdle;
import pokecube.core.ai.thread.aiRunnables.AIMate;
import pokecube.core.ai.thread.aiRunnables.AIStoreStuff;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.ai.utils.PokemobMoveHelper;
import pokecube.core.ai.utils.pathing.PokemobNavigator;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.InitAIEvent;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.impl.PokemobSaves;
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
    public void setEntity(EntityLiving entityIn)
    {
        super.setEntity(entityIn);
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
        setPokemonAIState(SITTING, false);
        setTargetID(entity.getEntityId());
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
        if (getPokemonAIState(CONTROLLED))
        {
            getEntity().rotationYaw = heading;
            dataManager.set(params.HEADINGDW, heading);
        }
    }

    @Override
    public float getHeading()
    {
        if (getPokemonAIState(CONTROLLED)) { return dataManager.get(params.HEADINGDW); }
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
            if (material.equalsIgnoreCase("water"))
            {
                entity.setPathPriority(PathNodeType.WATER, -1);
            }
            else if (material.equalsIgnoreCase("fire"))
            {
                entity.setPathPriority(PathNodeType.DAMAGE_FIRE, -1);
                entity.setPathPriority(PathNodeType.DANGER_FIRE, -1);
            }

        // These are used by pokecube's implementation of IPokemob.
        this.navi = new PokemobNavigator(this, entity.getEntityWorld());
        this.mover = new PokemobMoveHelper(entity);

        // Add in some vanilla-like AI classes
        entity.tasks.addTask(5,
                new GuardAI(entity, this.guardCap = entity.getCapability(EventsHandler.GUARDAI_CAP, null)));
        entity.tasks.addTask(5, this.utilMoveAI = new PokemobAIUtilityMove(this));
        if (entity instanceof EntityCreature) entity.targetTasks.addTask(3, new PokemobAIHurt(this, entry.isSocial));

        // Generate a PathManager to use to ensure AI doesn't clear paths for
        // more important runnables.
        PathManager manager = new PathManager();

        // Add in the Custom type of AI tasks.
        this.getAI().addAITask(new AIAttack(this).setPathManager(manager).setPriority(200));
        this.getAI().addAITask(new AICombatMovement(this).setPathManager(manager).setPriority(250));
        if (!entry.isStationary)
        {
            this.getAI()
                    .addAITask(new AIFollowOwner(this, 2 + entity.width + this.length, 2 + entity.width + this.length)
                            .setPathManager(manager).setPriority(400));
        }
        this.getAI().addAITask(new AIGuardEgg(this).setPathManager(manager).setPriority(250));
        this.getAI().addAITask(new AIMate(this).setPathManager(manager).setPriority(300));
        this.getAI().addAITask(new AIHungry(this, new EntityItem(entity.getEntityWorld()), 16).setPathManager(manager)
                .setPriority(300));
        AIStoreStuff ai = new AIStoreStuff(this);
        this.getAI().addAITask(ai.setPathManager(manager).setPriority(350));
        this.getAI().addAITask(new AIGatherStuff(this, 32, ai).setPathManager(manager).setPriority(400));
        this.getAI().addAITask(new AIIdle(this).setPathManager(manager).setPriority(500));
        this.getAI().addAITask(new AIFindTarget(this).setPathManager(manager).setPriority(400));

        // Send notification event of AI initilization, incase anyone wants to
        // affect it.
        MinecraftForge.EVENT_BUS.post(new InitAIEvent(this));
    }
}