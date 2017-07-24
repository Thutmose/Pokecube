package pokecube.adventures.blocks.cloner.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.blocks.cloner.crafting.CraftMatrix;
import pokecube.adventures.blocks.cloner.tileentity.TileEntityCloner;
import thut.core.common.blocks.SmartSlot;

public class ContainerCloner extends ContainerBase
{
    public World            world;
    public TileEntityCloner tile;
    public BlockPos         pos;

    public ContainerCloner(InventoryPlayer inv, TileEntityCloner tile)
    {
        super(inv, tile);
        this.tile = tile;
        this.world = tile.getWorld();
        this.pos = tile.getPos();
        tile.setCraftMatrix(new CraftMatrix(this, tile, 3, 3));

        this.addSlotToContainer(new SmartSlot(tile, tile.getOutputSlot(), 124, 35));

        int di = 17;
        int di2 = 9;
        int dj = 32;

        int i = 0;
        int j = 0;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 0, dj - 21 + j * 18, di + i * 18)
                .setTex("pokecube_adventures:textures/items/slot_bottle.png"));
        i = 2;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 1, dj - 21 + j * 18, di + i * 18)
                .setTex("pokecube_adventures:textures/items/slot_dna.png"));

        i = 0;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 2, dj + j * 18, di + di2 + i * 18));
        i = 1;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 3, dj + j * 18, di + di2 + i * 18));

        i = 0;
        j = 1;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 5, dj + j * 18, di + i * 18));
        i = 1;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 6, dj + j * 18, di + i * 18));
        i = 2;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 7, dj + j * 18, di + i * 18));

        j = 2;
        i = 0;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 8, dj + j * 18, di + di2 + i * 18));
        i = 1;
        this.addSlotToContainer(new SmartSlot(tile.getCraftMatrix(), 4, dj + j * 18, di + di2 + i * 18));

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

        this.onCraftMatrixChanged(tile.getCraftMatrix());
        tile.openInventory(inv.player);
    }

    @Override
    /** Callback for when the crafting matrix is changed. */
    public void onCraftMatrixChanged(IInventory inv)
    {
        if (tile.getProcess() != null)
        {
            if (!tile.getProcess().valid())
            {
                if (tile.getProcess() != null) tile.getProcess().reset();
                tile.setField(0, 0);
                tile.setField(1, 0);
            }
        }
    }

    @Override
    protected void updateCrafting()
    {
        onCraftMatrixChanged(tile);
    }
}
