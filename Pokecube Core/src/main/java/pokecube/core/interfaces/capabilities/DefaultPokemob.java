package pokecube.core.interfaces.capabilities;

import java.util.logging.Level;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import pokecube.core.ai.pokemob.PokemobAIHurt;
import pokecube.core.ai.pokemob.PokemobAIUtilityMove;
import pokecube.core.ai.thread.aiRunnables.AIAttack;
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
import pokecube.core.ai.utils.PokeNavigator;
import pokecube.core.ai.utils.PokemobMoveHelper;
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (hasCapability(capability, facing)) return (T) this;
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
        if (entity == null || remote) return;
        setPokemonAIState(SITTING, false);
        setTargetID(entity.getEntityId());
        if (entity != getEntity().getAttackTarget() && getAbility() != null && !entity.getEntityWorld().isRemote)
        {
            getAbility().onAgress(this, entity);
        }
    }

    @Override
    public int getTotalAIState()
    {
        return dataManager.get(params.AIACTIONSTATESDW);
    }

    @Override
    public void setTotalAIState(int state)
    {
        dataManager.set(params.AIACTIONSTATESDW, state);
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
        return true;
    }

    @Override
    public void setHeading(float heading)
    {
        if (getEntity().isBeingRidden())
        {
            getEntity().rotationYaw = heading;
            dataManager.set(params.HEADINGDW, heading);
        }
    }

    @Override
    public float getHeading()
    {
        if (getEntity().isBeingRidden()) { return dataManager.get(params.HEADINGDW); }
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

        // These are used by pokecube's implementation of IPokemob.
        this.navi = new PokeNavigator(this, entity.getEntityWorld());
        this.mover = new PokemobMoveHelper(entity);

        // Add in some vanilla-like AI classes
        this.guardAI = new GuardAI(entity, entity.getCapability(EventsHandler.GUARDAI_CAP, null));
        entity.tasks.addTask(5, this.guardAI);
        entity.tasks.addTask(5, this.utilMoveAI = new PokemobAIUtilityMove(this));
        if (entity instanceof EntityCreature) entity.targetTasks.addTask(3, new PokemobAIHurt(this, entry.isSocial));

        // None of the AI below should ever run on the client.
        if (entity.getEntityWorld().isRemote) return;

        // Add in the Custom type of AI tasks.
        this.getAI().addAITask(new AIAttack(this).setPriority(200));
        this.getAI().addAITask(new AICombatMovement(this).setPriority(250));
        if (!entry.isStationary)
        {
            this.getAI()
                    .addAITask(new AIFollowOwner(this, 2 + entity.width + this.length, 2 + entity.width + this.length)
                            .setPriority(400));
        }
        this.getAI().addAITask(new AIGuardEgg(this).setPriority(250));
        this.getAI().addAITask(new AIMate(this).setPriority(300));
        this.getAI().addAITask(new AIHungry(this, new EntityItem(entity.getEntityWorld()), 16).setPriority(300));
        AIStoreStuff ai = new AIStoreStuff(this);
        this.getAI().addAITask(ai.setPriority(350));
        this.getAI().addAITask(new AIGatherStuff(this, 32, ai).setPriority(400));
        this.getAI().addAITask(new AIIdle(this).setPriority(500));
        this.getAI().addAITask(new AIFindTarget(this).setPriority(400));

        // Send notification event of AI initilization, incase anyone wants to
        // affect it.
        MinecraftForge.EVENT_BUS.post(new InitAIEvent(this));
    }

}