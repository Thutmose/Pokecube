package pokecube.adventures.items;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.ai.utils.GuardAI;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.utils.TimePeriod;
import pokecube.core.utils.Tools;
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
        if (!this.isInCreativeTab(tab)) return subItems;
        subItems.add(new ItemStack(itemIn, 1, 0));
        return subItems;
    }

    /** Returns the unlocalized name of this item. This version accepts an
     * ItemStack so different stacks can have different names based on their
     * damage or NBT. */
    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return "item.trainerSpawner";
    }

    /** Returns true if the item can be used on the given entity, e.g. shears on
     * sheep. */
    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target,
            EnumHand hand)
    {
        if (!(target instanceof EntityLiving)) return false;
        EntityLiving v = (EntityLiving) target;
        if (v instanceof IEntityOwnable)
        {
            IEntityOwnable owned = (IEntityOwnable) v;
            if (owned.getOwnerId() == null || !owned.getOwnerId().equals(playerIn.getUniqueID())) return false;
        }

        IGuardAICapability capability = target.getCapability(EventsHandler.GUARDAI_CAP, null);
        if (capability == null) return false;
        for (Object o2 : v.tasks.taskEntries)
        {
            EntityAITaskEntry taskEntry = (EntityAITaskEntry) o2;
            if (taskEntry.action instanceof GuardAI)
            {
                v.tasks.removeTask(taskEntry.action);
                break;
            }
        }
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
        Entity target = Tools.getPointedEntity(player, 8);
        if (target == null && player.isSneaking())
        {
            target = player;
        }
        if (player.capabilities.isCreativeMode)
        {
            PacketTrainer.sendEditOpenPacket(target, (EntityPlayerMP) player);
            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        }
        return new ActionResult<>(EnumActionResult.PASS, itemstack);
    }

}
