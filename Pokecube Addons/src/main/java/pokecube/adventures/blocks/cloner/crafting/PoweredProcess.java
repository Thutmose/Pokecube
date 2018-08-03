package pokecube.adventures.blocks.cloner.crafting;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.blocks.cloner.recipe.IPoweredProgress;
import pokecube.adventures.blocks.cloner.recipe.IPoweredRecipe;
import pokecube.adventures.blocks.cloner.recipe.RecipeExtract;
import pokecube.adventures.blocks.cloner.recipe.RecipeFossilRevive;
import pokecube.adventures.blocks.cloner.recipe.RecipeSplice;
import pokecube.adventures.blocks.cloner.tileentity.TileClonerBase;
import thut.api.network.PacketHandler;
import thut.lib.CompatWrapper;

public class PoweredProcess
{
    private static final RecipeExtract EXTRACT = new RecipeExtract();
    private static final RecipeSplice  SPLICE  = new RecipeSplice();

    public static IPoweredRecipe findRecipe(IPoweredProgress tile, World world)
    {
        if (CompatWrapper.isValid(tile.getStackInSlot(tile.getOutputSlot()))) return null;
        if (tile.isValid(RecipeFossilRevive.class))
        {
            for (RecipeFossilRevive recipe : RecipeFossilRevive.getRecipeList())
                if (recipe.matches(tile.getCraftMatrix(), world)) { return recipe; }
            if (RecipeFossilRevive.ANYMATCH.matches(tile.getCraftMatrix(), world)) return RecipeFossilRevive.ANYMATCH;
        }
        if (tile.isValid(RecipeExtract.class) && EXTRACT.matches(tile.getCraftMatrix(), world)) { return EXTRACT; }
        if (tile.isValid(RecipeSplice.class) && SPLICE.matches(tile.getCraftMatrix(), world)) { return SPLICE; }
        return null;
    }

    public IPoweredRecipe recipe;
    IPoweredProgress      tile;
    World                 world;
    BlockPos              pos;
    public int            needed = 0;

    public PoweredProcess()
    {
    }

    public PoweredProcess setTile(IPoweredProgress tile)
    {
        this.tile = tile;
        this.world = ((TileEntity) tile).getWorld();
        this.pos = ((TileEntity) tile).getPos();
        this.recipe = findRecipe(tile, world);
        if (recipe != null) needed = recipe.getEnergyCost();
        return this;
    }

    public boolean valid()
    {
        if (world == null || recipe == null) return false;
        return recipe.matches(tile.getCraftMatrix(), world);
    }

    public void reset()
    {
        if (recipe != null) needed = recipe.getEnergyCost();
        else needed = 0;
        if (tile != null) tile.setProgress(getProgress());
    }

    public boolean tick()
    {
        if (needed > 0)
        {
            tile.setProgress(getProgress());
            return true;
        }
        return !complete();
    }

    /** @return the amount of energy already consumed. */
    public int getProgress()
    {
        if (recipe == null) return 0;
        return recipe.getEnergyCost() - needed;
    }

    public boolean complete()
    {
        if (recipe == null || tile == null) return false;
        boolean ret = recipe.complete(tile);
        if (!CompatWrapper.isValid(tile.getStackInSlot(tile.getOutputSlot())))
        {
            tile.setInventorySlotContents(tile.getOutputSlot(), recipe.getCraftingResult(tile.getCraftMatrix()));
            if (tile.getCraftMatrix().eventHandler != null)
                tile.getCraftMatrix().eventHandler.onCraftMatrixChanged(tile);
            PacketHandler.sendTileUpdate((TileEntity) tile);
        }
        if (ret)
        {
            setTile(tile);
            PacketHandler.sendTileUpdate((TileEntity) tile);
        }
        return ret;
    }

    public NBTTagCompound save()
    {
        NBTTagCompound tag = new NBTTagCompound();
        // TODO save things here
        return tag;
    }

    public static PoweredProcess load(NBTTagCompound tag, TileClonerBase tile)
    {
        // TODO load what is saved in save()
        return null;
    }
}
