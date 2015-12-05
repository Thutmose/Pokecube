package pokecube.adventures.items.bags;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.client.models.items.ModelBag;
import vazkii.botania.api.item.IBaubleRender;

public class ItemBag extends Item implements IBauble, IBaubleRender
{
	public ItemBag() {
		super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
	}
    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {
        player.openGui(PokecubeAdv.instance, PokecubeAdv.GUIBAG_ID, player.worldObj, 0, 0, 0);
    	return itemstack;
    }
    
	@Override
	public boolean canEquip(ItemStack arg0, EntityLivingBase arg1) {
		return true;
	}
	@Override
	public boolean canUnequip(ItemStack arg0, EntityLivingBase arg1) {
		return true;
	}
	@Override
	public BaubleType getBaubleType(ItemStack arg0) {
		return BaubleType.BELT;
	}
	@Override
	public void onEquipped(ItemStack arg0, EntityLivingBase arg1) {
		
	}
	@Override
	public void onUnequipped(ItemStack arg0, EntityLivingBase arg1) {
		
	}
	@Override
	public void onWornTick(ItemStack arg0, EntityLivingBase arg1) {
		
	}
	@SideOnly(Side.CLIENT)
	@Override
	public void onPlayerBaubleRender(ItemStack arg0, RenderPlayerEvent arg1,
			RenderType arg2) {
		if(arg2==RenderType.BODY)
		{
			ModelBag.model.render(0.5f);
		}
	}
}
