package pokecube.core.entity.professor;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.INpc;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.commands.CommandTools;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.handlers.Config;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;

public class EntityProfessor extends EntityAgeable implements IEntityAdditionalSpawnData, INpc
{
    public static enum ProfessorType
    {
        PROFESSOR, HEALER;
    }

    public ProfessorType type       = ProfessorType.PROFESSOR;
    public String        name       = "";
    public String        playerName = "";
    public boolean       male       = true;
    public boolean       stationary = false;
    public Vector3       location   = null;
    public GuardAI       guardAI;

    public EntityProfessor(World par1World)
    {
        this(par1World, null);
    }

    public EntityProfessor(World world, Vector3 location)
    {
        this(world, location, false);
    }

    public EntityProfessor(World par1World, Vector3 location, boolean stationary)
    {
        super(par1World);
        this.setSize(0.6F, 1.8F);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
        this.guardAI = new GuardAI(this, this.getCapability(EventsHandler.GUARDAI_CAP, null));
        this.tasks.addTask(1, guardAI);
        if (location != null)
        {
            location.moveEntity(this);
            setStationary(location);
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float i)
    {
        Entity e = source.getSourceOfDamage();
        if (e instanceof EntityPlayer && ((EntityPlayer) e).capabilities.isCreativeMode)
        {
            this.setDead();
        }
        return false;
    }

    @Override
    protected boolean canDespawn()
    {
        return false;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable p_90011_1_)
    {
        return null;
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
    }

    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack stack)
    {
        if (!worldObj.isRemote && hand == EnumHand.MAIN_HAND)
        {
            if (type == ProfessorType.PROFESSOR)
            {
                if (!PokecubeSerializer.getInstance().hasStarter(player))
                {
                    PokecubeClientPacket packet;
                    PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(4));
                    boolean hasStarter = PokecubeSerializer.getInstance().hasStarter(player);
                    buffer.writeByte(PokecubeClientPacket.CHOOSE1ST);
                    buffer.writeBoolean(!hasStarter);
                    if (!hasStarter) buffer.writeBoolean(hasStarter);
                    else
                    {
                        buffer.writeBoolean(
                                PokecubePacketHandler.specialStarters.containsKey(player.getName().toLowerCase()));
                        buffer.writeInt(0);
                    }
                    packet = new PokecubeClientPacket(buffer);
                    PokecubePacketHandler.sendToClient(packet, player);
                }
                else
                {
                    CommandTools.sendError(player, "pokecube.professor.deny");
                }
            }
            else if (type == ProfessorType.HEALER)
            {
                player.openGui(PokecubeCore.instance, Config.GUIPOKECENTER_ID, worldObj, 0, 0, 0);
            }
        }
        return false;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        stationary = nbt.getBoolean("stationary");
        male = nbt.getBoolean("gender");
        name = nbt.getString("name");
        playerName = nbt.getString("playerName");
        try
        {
            type = ProfessorType.valueOf(nbt.getString("type"));
        }
        catch (Exception e)
        {
            type = ProfessorType.PROFESSOR;
            e.printStackTrace();
        }
    }

    public void setStationary(boolean stationary)
    {
        if (stationary && !this.stationary) setStationary(Vector3.getNewVector().set(this));
        else if (!stationary && this.stationary)
        {
            for (Object o : this.tasks.taskEntries)
                if (o instanceof GuardAI) this.tasks.removeTask((EntityAIBase) o);
            this.stationary = stationary;
        }
    }

    public void setStationary(Vector3 location)
    {
        this.location = location;
        if (location == null)
        {
            stationary = false;
            guardAI.setPos(new BlockPos(0, 0, 0));
            guardAI.setTimePeriod(new TimePeriod(0, 0));
            return;
        }
        guardAI.setTimePeriod(TimePeriod.fullDay);
        guardAI.setPos(getPosition());
        stationary = true;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setBoolean("gender", male);
        nbt.setString("name", name);
        nbt.setBoolean("stationary", stationary);
        nbt.setString("playerName", playerName);
        nbt.setString("type", type.toString());
    }

    @Override
    public void writeSpawnData(ByteBuf buffer)
    {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeEntityToNBT(tag);
        new PacketBuffer(buffer).writeNBTTagCompoundToBuffer(tag);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData)
    {
        try
        {
            NBTTagCompound tag = new PacketBuffer(additionalData).readNBTTagCompoundFromBuffer();
            this.readEntityFromNBT(tag);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
