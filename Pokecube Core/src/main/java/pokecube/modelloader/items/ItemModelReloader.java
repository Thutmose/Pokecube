package pokecube.modelloader.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pokecube.modelloader.ModPokecubeML;

public class ItemModelReloader extends Item
{
    public static ItemModelReloader instance;

    public ItemModelReloader()
    {
        super();
        instance = this;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        if (!world.isRemote) return itemstack;
        player.openGui(ModPokecubeML.instance, 0, player.worldObj, 0, 0, 0);
        return itemstack;
    }
}
