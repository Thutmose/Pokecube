package pokecube.adventures.items.bags;

import net.minecraft.item.Item;

@net.minecraftforge.fml.common.Optional.Interface(iface = "baubles.api.IBauble", modid = "Baubles")
public class ItemBag extends Item// implements IBauble//, IBaubleRender TODO readd baubles
{
    public ItemBag()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

//    @Override
//    @Optional.Method(modid = "Baubles")
//    public boolean canEquip(ItemStack arg0, EntityLivingBase arg1)
//    {
//        return true;
//    }
//
//    @Override
//    @Optional.Method(modid = "Baubles")
//    public boolean canUnequip(ItemStack arg0, EntityLivingBase arg1)
//    {
//        return true;
//    }
//
//    @Override
//    @Optional.Method(modid = "Baubles")
//    public BaubleType getBaubleType(ItemStack arg0)
//    {
//        return BaubleType.BELT;
//    }
//
//    @Override
//    @Optional.Method(modid = "Baubles")
//    public void onEquipped(ItemStack arg0, EntityLivingBase arg1)
//    {
//
//    }
//
//    @Override
//    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
//    {
//        player.openGui(PokecubeAdv.instance, PokecubeAdv.GUIBAG_ID, player.worldObj, 0, 0, 0);
//        return itemstack;
//    }
//
//    @Override
//    @Optional.Method(modid = "Baubles")
//    public void onUnequipped(ItemStack arg0, EntityLivingBase arg1)
//    {
//
//    }
//
//    @Override
//    @Optional.Method(modid = "Baubles")
//    public void onWornTick(ItemStack arg0, EntityLivingBase arg1)
//    {
//
//    }
}
