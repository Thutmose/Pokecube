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
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasRewards.Reward;
import pokecube.adventures.entity.helper.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.items.ItemBadge;
import pokecube.core.database.Database;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class EntityLeader extends EntityTrainer
{
    public EntityLeader(World world)
    {
        super(world);
        aiStates.setAIState(IHasNPCAIStates.STATIONARY, true);
        aiStates.setAIState(IHasNPCAIStates.TRADES, false);
        ((DefaultPokemobs) pokemobsCap).resetTime = 0;
    }

    public EntityLeader(World world, TypeTrainer type, int maxXp, Vector3 location)
    {
        this(world, location);
        if (!world.isRemote) initTrainer(type, maxXp);
    }

    public EntityLeader(World par1World, Vector3 location)
    {
        super(par1World);
        aiStates.setAIState(IHasNPCAIStates.TRADES, false);

        this.setSize(0.6F, 1.8F);
        this.tasks.addTask(0, new EntityAISwimming(this));
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

        pokemobsCap.setType(type);
        byte genders = type.genders;
        if (genders == 1) pokemobsCap.setGender((byte) 1);
        if (genders == 2) pokemobsCap.setGender((byte) 2);
        if (genders == 3) pokemobsCap.setGender((byte) (Math.random() < 0.5 ? 1 : 2));

        TypeTrainer.getRandomTeam(pokemobsCap, this, level, getEntityWorld());
        setTypes();
        aiStates.setAIState(IHasNPCAIStates.TRADES, false);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack stack)
    {
        if (!player.capabilities.isCreativeMode || hand == EnumHand.OFF_HAND) return false;

        if (ItemBadge.isBadge(player.getHeldItem(hand)))
        {
            if (!rewardsCap.getRewards().isEmpty())
                rewardsCap.getRewards().set(0, new Reward(player.getHeldItem(hand).copy()));
            else rewardsCap.getRewards().add(new Reward(player.getHeldItem(hand).copy()));
            if (!getEntityWorld().isRemote) player
                    .sendMessage(new TextComponentString("Badge set to " + player.getHeldItem(hand).getDisplayName()));
            this.setHeldItem(EnumHand.OFF_HAND, rewardsCap.getRewards().get(0).stack);
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
