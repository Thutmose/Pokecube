package pokecube.core.interfaces.capabilities;

import java.util.logging.Level;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
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

}