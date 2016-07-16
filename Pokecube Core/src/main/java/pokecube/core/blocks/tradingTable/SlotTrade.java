package pokecube.core.blocks.tradingTable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pokecube.core.PokecubeCore;
import pokecube.core.items.pokecubes.PokecubeManager;

public class SlotTrade extends Slot
{

    public SlotTrade(IInventory par1iInventory, int par2, int par3, int par4)
    {
        super(par1iInventory, par2, par3, par4);
    }

    /** Return whether this slot's stack can be taken from this slot. */
    @Override
    public boolean canTakeStack(EntityPlayer player)
    {
        if (!(PokecubeManager.isFilled(getStack()))) return true;
        String name = PokecubeManager.getOwner(getStack());
        EntityPlayer player2 = PokecubeCore.getPlayer(name);
        if (player2 == null || player2.isDead) return true;
        return player == player2;
    }

    @Override
    public boolean isItemValid(ItemStack itemstack)
    {
        return ContainerTradingTable.isItemValid(itemstack);
    }
}
