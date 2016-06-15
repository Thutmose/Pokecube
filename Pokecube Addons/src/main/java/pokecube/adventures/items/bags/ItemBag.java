package pokecube.adventures.items.bags;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
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
    public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot armorType, Entity entity)
    {
        return armorType == EntityEquipmentSlot.CHEST;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World world, EntityPlayer player,
            EnumHand hand)
    {
        if (!world.isRemote) player.openGui(PokecubeAdv.instance, PokecubeAdv.GUIBAG_ID, player.worldObj,
                InventoryBag.getBag(player).getPage() + 1, 0, 0);
        return super.onItemRightClick(itemstack, world, player, hand);
    }
}
