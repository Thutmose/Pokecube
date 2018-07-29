package pokecube.core.entity.professor;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
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
import pokecube.core.contributors.Contributor;
import pokecube.core.contributors.ContributorManager;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.handlers.Config;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemLuckyEgg;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.packets.PacketChoose;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;
import thut.core.common.commands.CommandTools;

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
        this.tasks.addTask(3, new EntityAIMoveTowardsTarget(this, 0.6, 10));
        this.tasks.addTask(2, new EntityAIMoveIndoors(this));
        this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6D));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 5.0F, 1.0F));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLivingBase.class, 8.0F, 1.0f));
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
        Entity e = source.getTrueSource();
        if (e instanceof EntityPlayer && ((EntityPlayer) e).capabilities.isCreativeMode)
        {
            EntityPlayer player = (EntityPlayer) e;
            if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemLuckyEgg)
            {
                if (!this.getEntityWorld().isRemote)
                {
                    if (this.type == ProfessorType.PROFESSOR) this.type = ProfessorType.HEALER;
                    else if (this.type == ProfessorType.HEALER) this.type = ProfessorType.PROFESSOR;
                    if (this.type == ProfessorType.PROFESSOR) this.male = true;
                    else this.male = false;
                    PacketHandler.sendEntityUpdate(this);
                }
            }
            else this.setDead();
        }
        return super.attackEntityFrom(source, i);
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

    // 1.11
    public boolean processInteract(EntityPlayer player, EnumHand hand)
    {
        return processInteract(player, hand, player.getHeldItem(hand));
    }

    // 1.10
    public boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack stack)
    {
        if (!getEntityWorld().isRemote && hand == EnumHand.MAIN_HAND)
        {
            if (!SpawnHandler.canSpawnInWorld(player.getEntityWorld())) return false;
            if (type == ProfessorType.PROFESSOR)
            {
                if (!PokecubeSerializer.getInstance().hasStarter(player))
                {
                    PacketChoose packet;
                    packet = new PacketChoose(PacketChoose.OPENGUI);
                    boolean hasStarter = PokecubeSerializer.getInstance().hasStarter(player);
                    if (hasStarter)
                    {
                        packet.data.setBoolean("C", false);
                        packet.data.setBoolean("H", hasStarter);
                    }
                    else
                    {
                        Contributor contrib = ContributorManager.instance().getContributor(player.getGameProfile());
                        boolean special = false;
                        boolean pick = false;
                        if (PokecubePacketHandler.specialStarters.containsKey(contrib))
                        {
                            special = true;
                            pick = PacketChoose.canPick(player.getGameProfile());
                        }
                        packet = PacketChoose.createOpenPacket(special, pick, PokecubeMod.core.getStarters());
                    }
                    PokecubePacketHandler.sendToClient(packet, player);
                }
                else
                {
                    CommandTools.sendError(player, "pokecube.professor.deny");
                }
            }
            else if (type == ProfessorType.HEALER)
            {
                BlockPos pos = getPosition();
                player.openGui(PokecubeCore.instance, Config.GUIPOKECENTER_ID, world, pos.getX(), pos.getY() + 1,
                        pos.getZ());
                return true;
            }
        }
        return true;
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
        new PacketBuffer(buffer).writeCompoundTag(tag);
    }

    @Override
    public void readSpawnData(ByteBuf additionalData)
    {
        try
        {
            NBTTagCompound tag = new PacketBuffer(additionalData).readCompoundTag();
            this.readEntityFromNBT(tag);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
