package pokecube.core.items.megastuff;

import net.minecraft.item.ItemStack;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public interface IMegaCapability
{
    // TODO in here check if the mega wearable is acceptable for the pokemob
    // being mega evolved, handle this server side, not client side.
    boolean isStone(ItemStack stack);

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
