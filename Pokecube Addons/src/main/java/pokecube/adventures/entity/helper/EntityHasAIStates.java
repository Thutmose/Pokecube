package pokecube.adventures.entity.helper;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public abstract class EntityHasAIStates extends EntityHasTrades
{
    static final DataParameter<Integer> AIACTIONSTATESDW = EntityDataManager.<Integer> createKey(EntityHasTrades.class,
            DataSerializers.VARINT);
    public static final int             STATIONARY       = 1;
    public static final int             INBATTLE         = 2;
    public static final int             THROWING         = 4;
    public static final int             PERMFRIENDLY     = 8;

    public EntityHasAIStates(World worldIn)
    {
        super(worldIn);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(AIACTIONSTATESDW, 0);// more action states
    }

    public boolean getAIState(int state)
    {
        return (dataManager.get(AIACTIONSTATESDW) & state) != 0;
    }

    public void setAIState(int state, boolean flag)
    {
        int byte0 = dataManager.get(AIACTIONSTATESDW);

        Integer toSet;
        if (flag)
        {
            toSet = Integer.valueOf((byte0 | state));
        }
        else
        {
            toSet = Integer.valueOf((byte0 & -state - 1));
        }
        dataManager.set(AIACTIONSTATESDW, toSet);
    }
    
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        dataManager.set(AIACTIONSTATESDW, nbt.getInteger("aiState"));
    }
    
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("aiState", dataManager.get(AIACTIONSTATESDW));
    }

}
