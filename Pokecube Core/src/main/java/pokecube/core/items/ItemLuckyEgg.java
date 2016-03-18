/**
 * 
 */
package pokecube.core.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
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

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        if (world.isRemote) { return itemstack; }

        if (player.capabilities.isCreativeMode)
        {
            int metadata = itemstack.getItemDamage();
            Vector3 location = Vector3.getNewVector().set(player).add(Vector3.getNewVector().set(player.getLookVec()));
            if (metadata == 0 && player.isSneaking())
            {
                EntityProfessor p = new EntityProfessor(world, location.offset(EnumFacing.UP));
                world.spawnEntityInWorld(p);
            }
            else
            {
                boolean meteor = PokecubeSerializer.getInstance().canMeteorLand(location);
                player.addChatMessage(new ChatComponentText("Meteor Can Land: " + meteor));
            }
        }
        return itemstack;
    }

}
