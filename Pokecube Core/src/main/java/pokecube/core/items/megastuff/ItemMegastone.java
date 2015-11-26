package pokecube.core.items.megastuff;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMegastone extends Item
{
	public ItemMegastone() {
		super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
	}
	
    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
    	return itemstack;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean bool) 
    {
    	if(stack.hasTagCompound() && stack.getTagCompound().hasKey("pokemon"))
    	{
    		String s = stack.getTagCompound().getString("pokemon");
    		list.add(s);
    	}
    }
    
    @Override
    /**
     * If this function returns true (or the item is damageable), the ItemStack's NBT tag will be sent to the client.
     */
    public boolean getShareTag()
    {
        return true;
    }
}
