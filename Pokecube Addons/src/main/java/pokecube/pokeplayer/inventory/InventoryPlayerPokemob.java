package pokecube.pokeplayer.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.AnimalChest;
import pokecube.core.interfaces.IPokemob;
import pokecube.pokeplayer.PokeInfo;
import pokecube.pokeplayer.PokePlayer;

public class InventoryPlayerPokemob extends AnimalChest
{
    final PokeInfo info;

    public InventoryPlayerPokemob(PokeInfo info)
    {
        super(info.pokemob.getPokemobInventory().getName(), info.pokemob.getPokemobInventory().getSizeInventory());
        for (int i = 0; i < info.pokemob.getPokemobInventory().getSizeInventory(); i++)
        {
            this.setInventorySlotContents(i, info.pokemob.getPokemobInventory().getStackInSlot(i));
        }
        this.info = info;
    }

    public InventoryPlayerPokemob(AnimalChest inventory)
    {
        super(inventory.getName(), inventory.getSizeInventory());
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            this.setInventorySlotContents(i, inventory.getStackInSlot(i));
        }
        this.info = null;
    }

    public void saveToPokemob(IPokemob pokemob, EntityPlayer player)
    {
        AnimalChest inventory = pokemob.getPokemobInventory();
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            inventory.setInventorySlotContents(i, this.getStackInSlot(i));
        }
        if (info != null)
        {
            info.save(player);
        }
    }

    public void syncFromPokemob(IPokemob pokemob)
    {
        AnimalChest inventory = pokemob.getPokemobInventory();
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            this.setInventorySlotContents(i, inventory.getStackInSlot(i));
        }
    }

    public void openInventory(EntityPlayer player)
    {
    }

    public void closeInventory(EntityPlayer player)
    {
        if (player.worldObj.isRemote) return;
        IPokemob e = PokePlayer.PROXY.getPokemob(player);
        saveToPokemob(e, player);
    }
}
