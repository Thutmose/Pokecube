package pokecube.core.entity.pokemobs;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

public class AnimalChest extends InventoryBasic implements IItemHandlerModifiable
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
    public int getSlots()
    {
        return wrapper.getSlots();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        return wrapper.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        return wrapper.extractItem(slot, amount, simulate);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack)
    {
        wrapper.setStackInSlot(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return wrapper.getSlotLimit(slot);
    }
}
