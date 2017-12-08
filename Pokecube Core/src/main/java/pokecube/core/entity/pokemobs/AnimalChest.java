package pokecube.core.entity.pokemobs;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.wrapper.InvWrapper;

public class AnimalChest extends InventoryBasic
{
    final InvWrapper wrapper;

    public AnimalChest(String inventoryName, int slotCount)
    {
        super(inventoryName, false, slotCount);
        wrapper = new InvWrapper(this);
    }

    @SideOnly(Side.CLIENT)
    public AnimalChest(ITextComponent invTitle, int slotCount)
    {
        super(invTitle, slotCount);
        wrapper = new InvWrapper(this);
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return wrapper.getSlotLimit(slot);
    }
}
