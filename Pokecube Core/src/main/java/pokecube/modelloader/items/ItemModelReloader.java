package pokecube.modelloader.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pokecube.core.database.Database;
import pokecube.modelloader.client.ClientProxy;

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
        Database.updateSizes();
        ClientProxy.populateModels();
        return itemstack;
    }

}
