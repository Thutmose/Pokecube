package pokecube.adventures.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import pokecube.adventures.network.packets.PacketTrainer;
import pokecube.core.utils.Tools;

public class ItemTrainer extends Item
{
    public ItemTrainer()
    {
        super();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        if (world.isRemote) { return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand)); }
        Entity target = Tools.getPointedEntity(player, 8);
        if (player.capabilities.isCreativeMode)
        {
            PacketTrainer.sendEditOpenPacket(target, (EntityPlayerMP) player);
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }

}
