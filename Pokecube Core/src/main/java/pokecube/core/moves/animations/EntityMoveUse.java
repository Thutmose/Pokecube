package pokecube.core.moves.animations;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Rotations;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.interfaces.IMoveAnimation.MovePacketInfo;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public class EntityMoveUse extends Entity
{
    private static final Rotations        EMPTYLOCATION = new Rotations(0.0F, 0.0F, 0.0F);
    static final DataParameter<String>    MOVENAME      = EntityDataManager.<String> createKey(EntityMoveUse.class,
            DataSerializers.STRING);
    static final DataParameter<Rotations> ENDLOC        = EntityDataManager.<Rotations> createKey(EntityMoveUse.class,
            DataSerializers.ROTATIONS);
    static final DataParameter<Rotations> STARTLOC      = EntityDataManager.<Rotations> createKey(EntityMoveUse.class,
            DataSerializers.ROTATIONS);
    static final DataParameter<Integer>   USER          = EntityDataManager.<Integer> createKey(EntityMoveUse.class,
            DataSerializers.VARINT);
    static final DataParameter<Integer>   TARGET        = EntityDataManager.<Integer> createKey(EntityMoveUse.class,
            DataSerializers.VARINT);
    static final DataParameter<Integer>   TICK          = EntityDataManager.<Integer> createKey(EntityMoveUse.class,
            DataSerializers.VARINT);

    Vector3                               end           = Vector3.getNewVector();
    Vector3                               start         = Vector3.getNewVector();

    public EntityMoveUse(World worldIn)
    {
        super(worldIn);
        this.setSize(1f, 1f);
        this.ignoreFrustumCheck = true;
    }

    public EntityMoveUse setMove(Move_Base move)
    {
        String name = "";
        if (move != null)
        {
            name = move.name;
        }
        this.getDataManager().set(MOVENAME, name);
        if (move.getAnimation() != null)
        {
            getDataManager().set(TICK, move.getAnimation().getDuration());
        }
        else getDataManager().set(TICK, 1);
        return this;
    }

    public Move_Base getMove()
    {
        return MovesUtils.getMoveFromName(this.getDataManager().get(MOVENAME));
    }

    public EntityMoveUse setStart(Vector3 location)
    {
        start.set(location);
        start.moveEntity(this);
        getDataManager().set(STARTLOC, new Rotations((float) start.x, (float) start.y, (float) start.z));
        return this;
    }

    public EntityMoveUse setEnd(Vector3 location)
    {
        end.set(location);
        getDataManager().set(ENDLOC, new Rotations((float) end.x, (float) end.y, (float) end.z));
        return this;
    }

    public Vector3 getStart()
    {
        Rotations rot = getDataManager().get(STARTLOC);
        start.x = rot.getX();
        start.y = rot.getY();
        start.z = rot.getZ();
        return start;
    }

    public Vector3 getEnd()
    {
        Rotations rot = getDataManager().get(ENDLOC);
        end.x = rot.getX();
        end.y = rot.getY();
        end.z = rot.getZ();
        return end;
    }

    public EntityMoveUse setUser(Entity user)
    {
        getDataManager().set(USER, user.getEntityId());
        return this;
    }

    public Entity getUser()
    {
        return worldObj.getEntityByID(getDataManager().get(USER));
    }

    public EntityMoveUse setTarget(Entity target)
    {
        if (target != null) getDataManager().set(TARGET, target.getEntityId());
        else getDataManager().set(TARGET, -1);
        return this;
    }

    public Entity getTarget()
    {
        return worldObj.getEntityByID(getDataManager().get(TARGET));
    }

    public int getAge()
    {
        return getDataManager().get(TICK);
    }

    @Override
    public void onUpdate()
    {
        if (getMove() == null)
        {
            this.setDead();
            return;
        }
        int age = getAge() - 1;
        getDataManager().set(TICK, age);
        if (age <= 0)
        {
            this.doMoveUse();
            this.setDead();
        }
    }

    public MovePacketInfo getMoveInfo()
    {
        MovePacketInfo info = new MovePacketInfo(getMove(), getUser(), getTarget(), getStart(), getEnd());
        return info;
    }

    private void doMoveUse()
    {
        Move_Base attack = getMove();
        Entity user;
        if ((user = getUser()) == null) return;
        if (!worldObj.isRemote)
        {
            if (attack.move.notIntercepable)
            {
                MovesUtils.doAttack(attack.name, (IPokemob) user, getTarget());
            }
            else
            {
                MovesUtils.doAttack(attack.name, (IPokemob) user, getEnd());
            }
        }
        else if (attack.getAnimation() != null)
        {
            attack.getAnimation().spawnClientEntities(getMoveInfo());
        }
    }

    @Override
    protected void entityInit()
    {
        this.getDataManager().register(MOVENAME, "");
        this.getDataManager().register(ENDLOC, EMPTYLOCATION);
        this.getDataManager().register(STARTLOC, EMPTYLOCATION);
        this.getDataManager().register(USER, -1);
        this.getDataManager().register(TARGET, -1);
        this.getDataManager().register(TICK, 0);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound)
    {
        // Do nothing, if it needs to load/save, it should delete itself
        // instead.
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound)
    {
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return TileEntity.INFINITE_EXTENT_AABB;
    }
}
