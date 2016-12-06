package pokecube.adventures.items;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.entity.trainers.EntityLeader;
import pokecube.adventures.entity.trainers.EntityPokemartSeller;
import pokecube.adventures.entity.trainers.EntityTrainer;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.database.Database;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;
import thut.lib.CompatItem;

public class ItemTrainer extends CompatItem
{
    public ItemTrainer()
    {
        super();
        this.setHasSubtypes(true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected List<ItemStack> getTabItems(Item itemIn, CreativeTabs tab)
    {
        List<ItemStack> subItems = Lists.newArrayList();
        subItems.add(new ItemStack(itemIn, 1, 0));
        subItems.add(new ItemStack(itemIn, 1, 1));
        subItems.add(new ItemStack(itemIn, 1, 2));
        return subItems;
    }

    /** Returns the unlocalized name of this item. This version accepts an
     * ItemStack so different stacks can have different names based on their
     * damage or NBT. */
    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        int i = stack.getItemDamage();

        if (i == 0) return "item.trainerSpawner";
        if (i == 1) return "item.leaderSpawner";
        if (i == 2) { return "item.traderSpawner"; }

        return super.getUnlocalizedName();
    }

    /** Returns true if the item can be used on the given entity, e.g. shears on
     * sheep. */
    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target,
            EnumHand hand)
    {
        if (stack.getItemDamage() != 2) return false;
        if (!(target instanceof EntityVillager)) return false;
        EntityVillager v = (EntityVillager) target;
        for (Object o2 : v.tasks.taskEntries)
        {
            EntityAITaskEntry taskEntry = (EntityAITaskEntry) o2;
            if (taskEntry.action instanceof GuardAI)
            {
                v.tasks.removeTask(taskEntry.action);
                break;
            }
        }
        IGuardAICapability capability = v.getCapability(EventsHandler.GUARDAI_CAP, null);
        capability.setActiveTime(TimePeriod.fullDay);
        capability.setPos(v.getPosition());
        v.tasks.addTask(2, new GuardAI(v, capability));
        return true;
    }

    // 1.10
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World world, EntityPlayer player,
            EnumHand hand)
    {
        if (world.isRemote) { return new ActionResult<>(EnumActionResult.PASS, itemstack); }

        Vector3 location = Vector3.getNewVector().set(player).add(Vector3.getNewVector().set(player.getLookVec()));
        if (player.capabilities.isCreativeMode)
        {
            if (player.isSneaking())
            {
                int level = SpawnHandler.getSpawnLevel(world, location, Database.getEntry(1));
                if (itemstack.getItemDamage() == 0)
                {
                    TypeTrainer type = TypeTrainer.getTrainer("");
                    EntityTrainer t = new EntityTrainer(world, type, level, location.offset(EnumFacing.UP), true);
                    world.spawnEntityInWorld(t);
                }
                else if (itemstack.getItemDamage() == 1)
                {
                    TypeTrainer type = TypeTrainer.getTrainer("");
                    EntityLeader t = new EntityLeader(world, type, level, location.offset(EnumFacing.UP));
                    world.spawnEntityInWorld(t);
                }
            }
        }
        if (itemstack.getItemDamage() == 2)
        {
            boolean item = player.capabilities.isCreativeMode;
            if (!item) for (int i = 0; i < player.inventory.getSizeInventory(); i++)
            {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (stack != null && stack.getItem() == Item.getItemFromBlock(Blocks.EMERALD_BLOCK))
                {
                    item = true;
                    player.inventory.decrStackSize(i, 1);
                    player.inventoryContainer.detectAndSendChanges();
                    break;
                }
            }
            if (!item) return new ActionResult<>(EnumActionResult.PASS, itemstack);

            EntityLiving t;

            if (player.isSneaking())
            {
                t = new EntityPokemartSeller(world);

                location.offset(EnumFacing.UP).moveEntity(t);
                world.spawnEntityInWorld(t);
                ArrayList<EntityAIBase> toRemove = Lists.newArrayList();
                for (EntityAITaskEntry task : t.tasks.taskEntries)
                {
                    if (task.action instanceof GuardAI)
                    {
                        toRemove.add(task.action);
                    }
                }
                for (EntityAIBase ai : toRemove)
                    t.tasks.removeTask(ai);
                IGuardAICapability capability = t.getCapability(EventsHandler.GUARDAI_CAP, null);
                capability.setActiveTime(new TimePeriod(0, 0.5));
                capability.setPos(t.getPosition());
                t.tasks.addTask(2, new GuardAI(t, capability));
            }
        }
        return new ActionResult<>(EnumActionResult.PASS, itemstack);
    }

    // 1.10
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {

        if (!playerIn.capabilities.isCreativeMode)
        {
            if (!stack.hasTagCompound())
            {
                stack.setTagCompound(new NBTTagCompound());
            }
            stack.getTagCompound().setIntArray("coords", new int[] { pos.getX(), pos.getY(), pos.getZ() });
            System.out.println("test");
            return EnumActionResult.SUCCESS;
        }

        Vector3 v = Vector3.getNewVector().set(pos);
        Block b = v.getBlock(worldIn);
        b.rotateBlock(worldIn, pos, EnumFacing.DOWN);
        return EnumActionResult.FAIL;
    }

}
