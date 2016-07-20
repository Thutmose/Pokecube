/**
 *
 */
package pokecube.core.blocks.healtable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;

/** @author Manchou */
public class SlotHealTable extends Slot
{
    /** @param par1iInventory
     * @param par2
     * @param par3
     * @param par4 */
    public SlotHealTable(EntityPlayer par1EntityPlayer, IInventory inventory, int slotIndex, int xDisplayPosition,
            int yDisplayPosition)
    {
        super(inventory, slotIndex, xDisplayPosition, yDisplayPosition);
    }

    public void heal()
    {
        ItemStack stack = this.getStack();

        if (stack != null)
        {
            // int pokedexNumber =
            // Tools.getPokedexNumber(stack.getItemDamage());
            int serialization = Tools.getHealedPokemobSerialization();
            stack.setItemDamage(serialization);
            try
            {
                byte oldStatus = PokecubeManager.getStatus(stack);

                if (oldStatus > IMoveConstants.STATUS_NON)
                {
                    String itemName = stack.getDisplayName();
                    if (itemName.contains(" (")) itemName = itemName.substring(0, itemName.lastIndexOf(" "));
                    stack.setStackDisplayName(itemName);
                }
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
            stack.getTagCompound().getCompoundTag("Pokemob").setInteger("hungerTime",
                    -PokecubeMod.core.getConfig().pokemobLifeSpan / 4);
            PokecubeManager.setStatus(stack, IMoveConstants.STATUS_NON);
        }
    }

    @Override
    public boolean isItemValid(ItemStack itemstack)
    {
        return ContainerHealTable.isItemValid(itemstack);
    }

    @Override
    public void onSlotChanged()
    {
        super.onSlotChanged();
    }
}
