package pokecube.adventures.blocks.cloner.recipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import pokecube.adventures.blocks.cloner.crafting.CraftMatrix;

public interface IPoweredProgress extends IInventory
{
    int getEnergy();

    void setEnergy(int energy);

    int getProgress();

    void setProgress(int progress);

    CraftMatrix getCraftMatrix();

    void setCraftMatrix(CraftMatrix matrix);

    EntityPlayer getUser();
}
