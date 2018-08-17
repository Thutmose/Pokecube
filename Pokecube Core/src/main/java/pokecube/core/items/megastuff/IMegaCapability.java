package pokecube.core.items.megastuff;

import net.minecraft.item.ItemStack;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public interface IMegaCapability
{
    /** Check if the itemstack is a mega stone. */
    boolean isStone(ItemStack stack);

    /** Check if the mega stone is valid for the given entry.
     * 
     * @param stack
     * @param entry
     * @return */
    boolean isValid(ItemStack stack, PokedexEntry entry);

    PokedexEntry getEntry(ItemStack stack);

    public static class Default implements IMegaCapability
    {
        public Default()
        {
        }

        @Override
        public boolean isStone(ItemStack stack)
        {
            return false;
        }

        @Override
        public boolean isValid(ItemStack stack, PokedexEntry entry)
        {
            return false;
        }

        @Override
        public PokedexEntry getEntry(ItemStack stack)
        {
            return Database.missingno;
        }
    }
}
