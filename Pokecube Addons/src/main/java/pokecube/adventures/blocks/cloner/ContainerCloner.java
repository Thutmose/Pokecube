package pokecube.adventures.blocks.cloner;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;

public class ContainerCloner extends Container// Workbench
{
    public InventoryPlayer inv;
    public World           worldObj;
    int                    energy;
    TileEntityCloner       tile;
    public BlockPos        pos;
    ItemStack              cube   = null;
    ItemStack              egg    = null;
    ItemStack              star   = null;
    ItemStack              result = null;

    public ContainerCloner(InventoryPlayer inv, TileEntityCloner tile)
    {
        super();
        this.inv = inv;
        this.worldObj = tile.getWorld();
        this.tile = tile;
        this.pos = tile.getPos();

        tile.craftMatrix = new TileEntityCloner.CraftMatrix(this, tile);
        tile.result = new TileEntityCloner.CraftResult(tile);

        this.addSlotToContainer(new SlotCrafting(inv.player, tile.craftMatrix, tile.result, 0, 124, 35));

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 3; ++j)
            {
                this.addSlotToContainer(new Slot(tile.craftMatrix, j + i * 3, 30 + j * 18, 17 + i * 18));
            }
        }

        for (int k = 0; k < 3; ++k)
        {
            for (int i1 = 0; i1 < 9; ++i1)
            {
                this.addSlotToContainer(new Slot(inv, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l)
        {
            this.addSlotToContainer(new Slot(inv, l, 8 + l * 18, 142));
        }

        this.onCraftMatrixChanged(tile.craftMatrix);
        tile.openInventory(inv.player);
    }

    @Override
    /** Callback for when the crafting matrix is changed. */
    public void onCraftMatrixChanged(IInventory p_75130_1_)
    {
        ItemStack item = CraftingManager.getInstance().findMatchingRecipe(tile.craftMatrix, this.worldObj);
        egg = null;
        cube = null;
        star = null;
        result = null;
        if (worldObj == null) return;
        int energy = tile.getField(0);
        // System.out.println(energy + " " + worldObj.isRemote);
        if (item != null)
        {
            tile.setInventorySlotContents(9, item);
            return;
        }
        else if (energy > 10000)
        {
            boolean wrongnum = false;
            for (int i = 0; i < tile.craftMatrix.getSizeInventory(); i++)
            {
                item = tile.craftMatrix.getStackInSlot(i);
                if (item == null) continue;
                if (PokecubeManager.isFilled(item))
                {
                    if (cube != null)
                    {
                        wrongnum = true;
                        break;
                    }
                    cube = item.copy();
                    continue;
                }
                else if (item.getItem() instanceof ItemPokemobEgg)
                {
                    if (egg != null)
                    {
                        wrongnum = true;
                        break;
                    }
                    egg = item.copy();
                    continue;
                }
                else if (item.getItem() == Items.nether_star)
                {
                    if (star != null)
                    {
                        wrongnum = true;
                        break;
                    }
                    star = item.copy();
                    continue;
                }
                wrongnum = true;
                break;
            }
            if (!wrongnum && cube != null && egg != null)
            {
                int pokenb = PokecubeManager.getPokedexNb(cube);
                PokedexEntry entry = Database.getEntry(pokenb);
                pokenb = entry.getChildNb();
                if (egg.getTagCompound() == null) egg.setTagCompound(new NBTTagCompound());
                egg.getTagCompound().setInteger("pokemobNumber", pokenb);

                IPokemob mob = PokecubeManager.itemToPokemob(cube, worldObj);
                if (mob.isShiny() && egg.hasTagCompound()) egg.getTagCompound().setBoolean("shiny", true);

                egg.stackSize = 1;
                tile.setInventorySlotContents(9, egg);
                return;
            }
            tile.setInventorySlotContents(9, null);
        }
    }

    @Override
    /** This is called on shift click Take a stack from the specified inventory
     * slot. */
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index == 0)
            {
                if (!this.mergeItemStack(itemstack1, 10, 46, true)) { return null; }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index >= 10 && index < 37)
            {
                if (!this.mergeItemStack(itemstack1, 37, 46, false)) { return null; }
            }
            else if (index >= 37 && index < 46)
            {
                if (!this.mergeItemStack(itemstack1, 10, 37, false)) { return null; }
            }
            else if (!this.mergeItemStack(itemstack1, 10, 46, false)) { return null; }

            if (itemstack1.stackSize == 0)
            {
                slot.putStack((ItemStack) null);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize) { return null; }

            slot.onPickupFromSlot(playerIn, itemstack1);
        }

        return itemstack;
    }

    @Override
    /** Handles slot click.
     * 
     * @param mode
     *            0 = basic click, 1 = shift click, 2 = hotbar, 3 = pick block,
     *            4 = drop, 5 = ?, 6 = double click */
    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn)
    {
        if (slotId == 0 && getSlot(0).getHasStack() && egg != null)
        {
            tile.setField(0, tile.getField(0) - 10000);
            // If there is a nether star, consume it, but leave a pokecube
            // behind.
            for (int i = 0; i < tile.craftMatrix.getSizeInventory(); i++)
            {
                ItemStack item = tile.craftMatrix.getStackInSlot(i);
                if (star != null && item != null && item.isItemEqual(cube))
                {
                    cube.stackSize = 2;
                    tile.craftMatrix.setInventorySlotContents(i, cube);
                    break;
                }
            }

        }
        return super.slotClick(slotId, clickedButton, mode, playerIn);
    }

    @Override
    /** Looks for changes made in the container, sends them to every
     * listener. */
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        for (int i = 0; i < this.crafters.size(); ++i)
        {
            ICrafting icrafting = (ICrafting) this.crafters.get(i);
            if (energy != tile.getField(0))
            {
                icrafting.sendProgressBarUpdate(this, 0, this.tile.getField(0));
                this.onCraftMatrixChanged(tile.craftMatrix);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data)
    {
        this.tile.setField(id, data);
        this.onCraftMatrixChanged(tile.craftMatrix);
    }

    @Override
    public boolean canInteractWith(EntityPlayer p)
    {
        return true;
    }

    /** Called when the container is closed. */
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        tile.closeInventory(playerIn);
    }
}
