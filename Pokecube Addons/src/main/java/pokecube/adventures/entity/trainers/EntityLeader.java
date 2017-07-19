package pokecube.adventures.entity.trainers;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import pokecube.adventures.ai.trainers.AITrainerBattle;
import pokecube.adventures.ai.trainers.AITrainerFindTarget;
import pokecube.adventures.entity.helper.capabilities.CapabilityAIStates.IHasAIStates;
import pokecube.adventures.items.ItemBadge;
import pokecube.core.database.Database;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class EntityLeader extends EntityTrainer
{
    public EntityLeader(World world)
    {
        super(world);
        aiStates.setAIState(IHasAIStates.STATIONARY, true);
        trades = false;
        resetTime = 0;
    }

    public EntityLeader(World world, TypeTrainer type, int maxXp, Vector3 location)
    {
        this(world, location);
        initTrainer(type, maxXp);
    }

    public EntityLeader(World par1World, Vector3 location)
    {
        super(par1World);
        trades = false;

        this.setSize(0.6F, 1.8F);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new AITrainerFindTarget(this, EntityPlayer.class));
        this.tasks.addTask(1, new AITrainerBattle(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
        if (location != null)
        {
            location.moveEntity(this);
            setStationary(location);
        }
    }

    @Override
    public void initTrainer(TypeTrainer type, int level)
    {
        int alevel = Tools.xpToLevel(Database.getEntry(1).getEvolutionMode(), level);
        level = Tools.levelToXp(Database.getEntry(1).getEvolutionMode(), alevel + 5);

        this.setType(type);
        byte genders = type.genders;
        if (genders == 1) male = true;
        if (genders == 2) male = false;
        if (genders == 3) male = Math.random() < 0.5;

        TypeTrainer.getRandomTeam(pokemobsCap, this, level, getEntityWorld());
        setTypes();
        trades = false;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack stack)
    {
        if (!player.capabilities.isCreativeMode || hand == EnumHand.OFF_HAND) return false;

        if (ItemBadge.isBadge(player.getHeldItem(hand)))
        {
            if (!getRewards().isEmpty()) getRewards().set(0, player.getHeldItem(hand).copy());
            else getRewards().add(player.getHeldItem(hand).copy());
            if (!getEntityWorld().isRemote) player.sendMessage(
                    new TextComponentString("Badge set to " + player.getHeldItem(hand).getDisplayName()));
            this.setHeldItem(EnumHand.OFF_HAND, getRewards().get(0));
        }
        return super.processInteract(player, hand, stack);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
    }
}
