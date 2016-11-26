/**
 * 
 */
package pokecube.core.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import pokecube.core.entity.professor.EntityProfessor;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.Vector3;

/** @author Manchou */
public class ItemLuckyEgg extends Item
{

    /** @param par1 */
    public ItemLuckyEgg()
    {
        super();
        this.setHasSubtypes(true);
    }

    // 1.11
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        return onItemRightClick(player.getHeldItem(hand), world, player, hand);
    }

    // 1.10
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World world, EntityPlayer player,
            EnumHand hand)
    {
        if (world.isRemote) { return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack); }
        if (player.capabilities.isCreativeMode)
        {
            int metadata = itemstack.getItemDamage();
            Vector3 location = Vector3.getNewVector().set(player);
            if (metadata == 0 && player.isSneaking())
            {
                EntityProfessor p = new EntityProfessor(world, location.offset(EnumFacing.UP), true);
                world.spawnEntityInWorld(p);
            }
            else
            {
                boolean meteor = PokecubeSerializer.getInstance().canMeteorLand(location);
                player.addChatMessage(new TextComponentString("Meteor Can Land: " + meteor));
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

}
