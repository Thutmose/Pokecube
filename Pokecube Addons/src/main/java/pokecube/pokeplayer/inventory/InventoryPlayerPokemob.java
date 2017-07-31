package pokecube.pokeplayer.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import pokecube.core.entity.pokemobs.AnimalChest;
import pokecube.core.interfaces.IPokemob;
import pokecube.pokeplayer.PokeInfo;
import pokecube.pokeplayer.PokePlayer;

public class InventoryPlayerPokemob extends AnimalChest
{
    final PokeInfo info;

    public InventoryPlayerPokemob(PokeInfo info, World world)
    {
        super(info.getPokemob(world).getPokemobInventory().getName(),
                info.getPokemob(world).getPokemobInventory().getSizeInventory());
        for (int i = 0; i < info.getPokemob(world).getPokemobInventory().getSizeInventory(); i++)
        {
            this.setInventorySlotContents(i, info.getPokemob(world).getPokemobInventory().getStackInSlot(i));
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

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
        if (player.getEntityWorld().isRemote) return;
        IPokemob e = PokePlayer.PROXY.getPokemob(player);
        saveToPokemob(e, player);
    }
}
