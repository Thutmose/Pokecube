package pokecube.adventures.blocks.cloner.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import pokecube.adventures.blocks.cloner.crafting.CraftMatrix;
import pokecube.adventures.blocks.cloner.crafting.CraftResult;
import pokecube.adventures.blocks.cloner.recipe.IPoweredProgress;
import thut.core.common.blocks.SmartSlot;

public class ContainerGeneExtractor extends ContainerBase
{

    public ContainerGeneExtractor(InventoryPlayer inv, IInventory tile)
    {
        super(inv, tile);
        CraftMatrix matrix = new CraftMatrix(this, tile, 1, 3);
        CraftResult result = new CraftResult(tile, 3);

        if (tile instanceof IPoweredProgress)
        {
            ((IPoweredProgress) tile).setCraftMatrix(matrix);
        }

        this.addSlotToContainer(new SlotCrafting(inv.player, matrix, result, 0, 114, 35));

        int di = 17;
        int di2 = 18;
        int dj2 = 48;
        int dj = 32;

        // DNA Container
        this.addSlotToContainer(
                new SmartSlot(matrix, 0, dj + dj2, di).setTex("minecraft:textures/items/potion_bottle_empty.png"));
        // Stabiliser
        this.addSlotToContainer(new SmartSlot(matrix, 1, dj + dj2, di + 35));
        // DNA Source
        this.addSlotToContainer(new SmartSlot(matrix, 2, 47, di + di2));

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
        tile.openInventory(inv.player);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    protected void updateCrafting()
    {
        // TODO Auto-generated method stub

    }

}
