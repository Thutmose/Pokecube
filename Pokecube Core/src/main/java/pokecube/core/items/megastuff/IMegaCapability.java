package pokecube.core.items.megastuff;

import net.minecraft.item.ItemStack;
import pokecube.core.database.PokedexEntry;

public interface IMegaCapability
{
    // TODO in here check if the mega wearable is acceptable for the pokemob
    // being mega evolved, handle this server side, not client side.
    boolean isStone(ItemStack stack);

    boolean isValid(ItemStack stack, PokedexEntry entry);

    PokedexEntry getEntry(ItemStack stack);
}
