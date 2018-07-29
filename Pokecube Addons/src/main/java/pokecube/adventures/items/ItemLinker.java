package pokecube.adventures.items;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.blocks.warppad.BlockWarpPad;
import pokecube.adventures.blocks.warppad.TileEntityWarpPad;
import pokecube.core.ai.properties.IGuardAICapability;
import pokecube.core.events.handlers.EventsHandler;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.lib.CompatWrapper;

public class ItemLinker extends Item
{
    public ItemLinker()
    {
        super();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void interact(EntityInteract event)
    {
        ItemStack stack = event.getItemStack();
        if (!CompatWrapper.isValid(stack) || stack.getItem() != this || event.getSide() == Side.CLIENT) return;
        EntityPlayer playerIn = event.getEntityPlayer();
        Entity target = event.getTarget();
        IGuardAICapability cap = target.getCapability(EventsHandler.GUARDAI_CAP, null);
        if (stack.getItemDamage() == 1 && cap != null)
        {
            boolean canSet = event.getEntityPlayer().isCreative();
            if (target instanceof IEntityOwnable)
            {
                canSet = ((IEntityOwnable) target).getOwner() == playerIn;
            }
            if (stack.hasTagCompound() && canSet)
            {
                Vector4 pos = new Vector4(stack.getTagCompound().getCompoundTag("link"));
                cap.getPrimaryTask().setPos(new BlockPos((int) (pos.x - 0.5), (int) (pos.y - 1), (int) (pos.z - 0.5)));
                playerIn.sendMessage(new TextComponentString("Set Home to " + pos));
                event.setCanceled(true);
            }
        }
    }

    @Override
    /** If this function returns true (or the item is damageable), the
     * ItemStack's NBT tag will be sent to the client. */
    public boolean getShareTag()
    {
        return true;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
            EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = playerIn.getHeldItem(hand);
        Vector3 hit = Vector3.getNewVector().set(pos);
        Block block = hit.getBlock(worldIn);
        if (block instanceof BlockWarpPad && playerIn.isSneaking() && stack.hasTagCompound())
        {
            TileEntityWarpPad pad = (TileEntityWarpPad) hit.getTileEntity(worldIn);
            if (pad.canEdit(playerIn) && !worldIn.isRemote)
            {
                pad.link = new Vector4(stack.getTagCompound().getCompoundTag("link"));
                playerIn.sendMessage(new TextComponentString("linked pad to " + pad.link));
            }
            return EnumActionResult.SUCCESS;
        }
        else
        {
            if (!worldIn.isRemote)
            {
                if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
                NBTTagCompound linkTag = new NBTTagCompound();
                Vector4 link = new Vector4(hit.x + 0.5, hit.y + 1, hit.z + 0.5, playerIn.dimension);
                link.writeToNBT(linkTag);
                stack.getTagCompound().setTag("link", linkTag);
                playerIn.sendMessage(new TextComponentString("Saved location " + link));
            }
            else
            {
                StringSelection selection = new StringSelection(hit.intX() + " " + hit.intY() + " " + hit.intZ());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                playerIn.sendMessage(new TextComponentString("Copied to clipboard"));
            }
        }
        return EnumActionResult.FAIL;
    }
}
