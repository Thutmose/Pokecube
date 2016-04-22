package pokecube.adventures.items.bags;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;

public class ItemBag extends Item
{
    public ItemBag()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public boolean isValidArmor(ItemStack stack, int armorType, Entity entity)
    {
        return armorType == 1;
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        player.openGui(PokecubeAdv.instance, PokecubeAdv.GUIBAG_ID, player.worldObj, 0, 0, 0);
        return itemstack;
    }
}
