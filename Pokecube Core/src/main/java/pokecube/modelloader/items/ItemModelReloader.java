package pokecube.modelloader.items;

import java.io.File;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pokecube.core.database.Database;
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
        if (player.isSneaking()) convImgs();
        player.openGui(ModPokecubeML.instance, 0, player.worldObj, 0, 0, 0);
        return itemstack;
    }

    private void convImgs()
    {
        File dir = new File("./sprites");
        if (!dir.exists()) return;
        int n = 0;
        for (File file : dir.listFiles())
        {
            String name = file.getName();
            try
            {
                File dest = new File(dir,
                        Database.getEntry(Integer.parseInt(name.replace(".png", ""))).getName() + ".png");
                n++;
                file.renameTo(dest);
            }
            catch (Exception e)
            {

            }
        }
        System.out.println(dir.exists() + " " + dir.isDirectory() + " " + n);
    }
}
