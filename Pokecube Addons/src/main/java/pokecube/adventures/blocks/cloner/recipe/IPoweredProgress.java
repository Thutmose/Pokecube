package pokecube.adventures.blocks.cloner.recipe;

import net.minecraft.entity.player.EntityPlayer;
import pokecube.adventures.blocks.cloner.crafting.CraftMatrix;
import pokecube.adventures.blocks.cloner.crafting.PoweredProcess;
import thut.core.common.blocks.DefaultInventory;

public interface IPoweredProgress extends DefaultInventory
{
    int addEnergy(int energy, boolean simulate);

    void setProgress(int progress);

    CraftMatrix getCraftMatrix();

    void setCraftMatrix(CraftMatrix matrix);

    EntityPlayer getUser();

    PoweredProcess getProcess();

    void setProcess(PoweredProcess process);

    boolean isValid(Class<? extends IPoweredRecipe> recipe);

    int getOutputSlot();
}
