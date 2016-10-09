package pokecube.core.items.megastuff;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;

public class MegaCapability implements ICapabilityProvider, IMegaCapability
{
    public static interface RingChecker
    {
        boolean canMegaEvolve(EntityPlayer player, PokedexEntry toEvolve);
    }

    public static boolean canMegaEvolve(EntityPlayer player, IPokemob target)
    {
        PokedexEntry entry = target.getPokedexEntry();
        return checker.canMegaEvolve(player, entry);
    }

    public static boolean matches(ItemStack stack, PokedexEntry entry)
    {
        IMegaCapability cap = stack.getCapability(MegaCapability.MEGA_CAP, null);
        if (cap != null)
        {
            if (cap.isStone(stack)) return false;
            PokedexEntry stacks;
            if ((stacks = cap.getEntry(stack)) == null) return true;
            PokedexEntry stackbase = stacks.getBaseForme() == null ? stacks : stacks.getBaseForme();
            PokedexEntry entrybase = entry.getBaseForme() == null ? entry : entry.getBaseForme();
            return entrybase == stackbase;
        }
        return false;
    }

    public static RingChecker                       checker  = new RingChecker()
                                                             {
                                                                 @Override
                                                                 public boolean canMegaEvolve(EntityPlayer player,
                                                                         PokedexEntry toEvolve)
                                                                 {
                                                                     for (int i = 0; i < player.inventory
                                                                             .getSizeInventory(); i++)
                                                                     {
                                                                         ItemStack stack = player.inventory
                                                                                 .getStackInSlot(i);
                                                                         if (stack != null)
                                                                         {
                                                                             if (matches(stack, toEvolve)) return true;
                                                                         }
                                                                     }
                                                                     for (int i = 0; i < player.inventory.armorInventory.length; i++)
                                                                     {
                                                                         ItemStack stack = player.inventory.armorInventory[i];
                                                                         if (stack != null)
                                                                         {
                                                                             if (matches(stack, toEvolve)) return true;
                                                                         }
                                                                     }
                                                                     return false;
                                                                 }
                                                             };

    @CapabilityInject(IMegaCapability.class)
    public static final Capability<IMegaCapability> MEGA_CAP = null;
    final ItemStack                                 stack;

    public MegaCapability(ItemStack itemStack)
    {
        this.stack = itemStack;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability != MEGA_CAP) return false;
        if (stack.getItem() instanceof ItemMegawearable) return true;
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("gemTag"))
        {
            ItemStack stack2 = ItemStack.loadItemStackFromNBT(stack.getSubCompound("gemTag", false));
            if (stack2 != null) return getEntry(stack2) != null;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (!hasCapability(MEGA_CAP, facing)) return null;
        if (MEGA_CAP != null && capability == MEGA_CAP)
            return (T) ((stack.getItem() instanceof IMegaCapability) ? stack.getItem() : (T) this);
        return null;
    }

    @Override
    public boolean isStone(ItemStack stack)
    {
        if (stack.getItem() instanceof IMegaCapability) return ((IMegaCapability) stack.getItem()).isStone(stack);
        return stack.getItem() instanceof ItemMegastone && stack.hasTagCompound()
                && stack.getTagCompound().getString("pokemon").contains("mega");
    }

    @Override
    public boolean isValid(ItemStack stack, PokedexEntry entry)
    {
        if (stack.getItem() instanceof IMegaCapability)
            return ((IMegaCapability) stack.getItem()).isValid(stack, entry);
        PokedexEntry stacks = getEntry(stack);
        if (entry == null) return true;
        if (stacks == null) return true;
        PokedexEntry stackbase = stacks.getBaseForme() == null ? stacks : stacks.getBaseForme();
        PokedexEntry entrybase = entry.getBaseForme() == null ? entry : entry.getBaseForme();
        return entrybase == stackbase;
    }

    @Override
    public PokedexEntry getEntry(ItemStack stack)
    {
        if (stack.getItem() instanceof IMegaCapability) return ((IMegaCapability) stack.getItem()).getEntry(stack);
        if (stack.getItem() instanceof ItemMegastone
                && stack.hasTagCompound()) { return Database.getEntry(stack.getTagCompound().getString("pokemon")); }
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("gemTag"))
        {
            ItemStack stack2 = ItemStack.loadItemStackFromNBT(stack.getSubCompound("gemTag", false));
            if (stack2 != null) return getEntry(stack2);
        }
        return null;
    }
}
