package pokecube.adventures.entity.trainers;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import pokecube.adventures.ai.trainers.EntityAITrainer;
import pokecube.adventures.items.ItemBadge;
import pokecube.core.database.Database;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class EntityLeader extends EntityTrainer
{
    public static class DefeatEntry
    {
        static DefeatEntry createFromNBT(NBTTagCompound nbt)
        {
            String defeater = nbt.getString("player");
            long time = nbt.getLong("time");
            return new DefeatEntry(defeater, time);
        }
        final String defeater;

        final long   defeatTime;

        public DefeatEntry(String defeater, long time)
        {
            this.defeater = defeater;
            this.defeatTime = time;
        }

        void writeToNBT(NBTTagCompound nbt)
        {
            nbt.setString("player", defeater);
            nbt.setLong("time", defeatTime);
        }
    }

    private long                  resetTime = 0;
    public ArrayList<DefeatEntry> defeaters = new ArrayList<DefeatEntry>();

    public EntityLeader(World world)
    {
        super(world);
        setAIState(STATIONARY, true);
        trades = false;
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
        this.tasks.addTask(1, new EntityAITrainer(this, EntityPlayer.class));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
        if (location != null)
        {
            location.moveEntity(this);
            setStationary(location);
        }
    }

    public boolean hasDefeated(Entity e)
    {
        if (e == null) return false;
        String name = e.getUniqueID().toString();
        for (DefeatEntry s : defeaters)
        {
            if (s.defeater.equals(name))
            {
                if (resetTime > 0)
                {
                    long diff = worldObj.getTotalWorldTime() - s.defeatTime;
                    if (diff > resetTime)
                    {
                        defeaters.remove(s);
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void initTrainer(TypeTrainer type, int level)
    {
        int alevel = Tools.xpToLevel(level, Database.getEntry(1).getEvolutionMode());
        level = Tools.levelToXp(Database.getEntry(1).getEvolutionMode(), alevel + 5);

        this.type = type;
        byte genders = type.genders;
        if (genders == 1) male = true;
        if (genders == 2) male = false;
        if (genders == 3) male = Math.random() < 0.5;

        TypeTrainer.getRandomTeam(this, level, pokecubes, worldObj);
        setTypes();
        trades = false;
    }

    @Override
    public void onDefeated(Entity defeater)
    {
        if (hasDefeated(defeater)) return;
        defeaters.add(new DefeatEntry(defeater.getUniqueID().toString(), worldObj.getTotalWorldTime()));
        for (int i = 1; i < 5; i++)
        {
            EntityEquipmentSlot slotIn = EntityEquipmentSlot.values()[i];
            ItemStack stack = getItemStackFromSlot(slotIn);
            if (stack != null) this.entityDropItem(stack.copy(), 0.5f);
        }
        if (reward != null)
        {
            for (ItemStack i : reward)
            {
                if (i == null || i.getItem() == null) continue;
                EntityItem item = defeater.entityDropItem(i.copy(), 0.5f);
                item.setPickupDelay(0);
            }
        }
        if (defeater != null)
        {
            ITextComponent text = new TextComponentTranslation("pokecube.trainer.defeat", this.getDisplayName());
            defeater.addChatMessage(text);
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack stack)
    {
        if (!player.capabilities.isCreativeMode || hand == EnumHand.OFF_HAND) return false;

        if (ItemBadge.isBadge(player.getHeldItemMainhand()))
        {
            reward.remove(0);
            reward.set(0,player.getHeldItemMainhand().copy());
            player.addChatMessage(new TextComponentString("Badge set to " + this.getHeldItemOffhand()));
            this.setHeldItem(EnumHand.OFF_HAND, reward.get(0));
        }
        return super.processInteract(player, hand, stack);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        defeaters.clear();
        resetTime = nbt.getLong("resetTime");
        if (nbt.hasKey("DefeatList", 9))
        {
            NBTTagList nbttaglist = nbt.getTagList("DefeatList", 10);
            for (int i = 0; i < nbttaglist.tagCount(); i++)
                defeaters.add(DefeatEntry.createFromNBT(nbttaglist.getCompoundTagAt(i)));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setLong("resetTime", resetTime);
        NBTTagList nbttaglist = new NBTTagList();
        for (DefeatEntry entry : defeaters)
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            entry.writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
        }
        nbt.setTag("DefeatList", nbttaglist);
    }
}
