package pokecube.core.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Helpers
{
    public static final ItemStack nullStack = null;

    public static ItemStack fromTag(NBTTagCompound tag)
    {
        return ItemStack.loadItemStackFromNBT(tag);
    }

    public static void registerTileEntity(Class<? extends TileEntity> tileClass, String id)
    {
        GameRegistry.registerTileEntity(tileClass, id);
    }

}
