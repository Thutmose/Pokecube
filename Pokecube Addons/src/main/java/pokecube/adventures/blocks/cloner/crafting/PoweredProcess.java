package pokecube.adventures.blocks.cloner.crafting;

import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.blocks.cloner.block.BlockCloner;
import pokecube.adventures.blocks.cloner.recipe.IPoweredProgress;
import pokecube.adventures.blocks.cloner.recipe.IPoweredRecipe;
import pokecube.adventures.blocks.cloner.recipe.RecipeFossilRevive;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.Tools;
import thut.api.network.PacketHandler;

public class PoweredProcess
{
    public final IPoweredRecipe recipe;
    final IPoweredProgress      tile;
    final World                 world;
    final BlockPos              pos;
    public int                  needed = 0;

    public PoweredProcess(IPoweredRecipe recipe, IPoweredProgress tile)
    {
        this.recipe = recipe;
        this.tile = tile;
        this.world = ((TileEntity) tile).getWorld();
        this.pos = ((TileEntity) tile).getPos();
        needed = recipe.getEnergyCost();
    }

    public boolean valid()
    {
        if (world == null) return false;
        return recipe.matches(tile.getCraftMatrix(), world);
    }

    public void reset()
    {
        needed = recipe.getEnergyCost();
        tile.setProgress(getProgress());
    }

    public boolean tick()
    {
        if (needed > 0)
        {
            needed -= Math.min(needed, tile.getEnergy());
            tile.setEnergy(0);
            tile.setProgress(getProgress());
            return true;
        }
        return !complete();
    }

    public int getProgress()
    {
        return recipe.getEnergyCost() - needed;
    }

    public boolean complete()
    {
        if (recipe instanceof RecipeFossilRevive)
        {
            ItemStack[] remaining = recipe.getRemainingItems(tile.getCraftMatrix());
            for (int i = 0; i < remaining.length; i++)
            {
                if (remaining[i] != null) tile.setInventorySlotContents(i, remaining[i]);
                else tile.decrStackSize(i, 1);
            }
            RecipeFossilRevive recipe = (RecipeFossilRevive) this.recipe;
            EntityLiving entity = (EntityLiving) PokecubeMod.core.createPokemob(recipe.pokedexEntry, world);
            if (entity != null)
            {
                entity.setHealth(entity.getMaxHealth());
                // to avoid the death on spawn
                int exp = Tools.levelToXp(recipe.pokedexEntry.getEvolutionMode(), recipe.level);
                // that will make your pokemob around level 3-5.
                // You can give him more XP if you want
                entity = (EntityLiving) ((IPokemob) entity).setForSpawn(exp);
                if (tile.getUser() != null && recipe.tame) ((IPokemob) entity).setPokemonOwner(tile.getUser());
                EnumFacing dir = world.getBlockState(pos).getValue(BlockCloner.FACING);
                entity.setLocationAndAngles(pos.getX() + 0.5 + dir.getFrontOffsetX(), pos.getY() + 1,
                        pos.getZ() + 0.5 + dir.getFrontOffsetZ(), world.rand.nextFloat() * 360F, 0.0F);
                world.spawnEntityInWorld(entity);
                entity.playLivingSound();
            }
            return true;
        }
        if (tile.getStackInSlot(9) == null)
        {
            tile.setInventorySlotContents(9, recipe.getCraftingResult(tile.getCraftMatrix()));
            if (tile.getCraftMatrix().eventHandler != null)
                tile.getCraftMatrix().eventHandler.onCraftMatrixChanged(tile);
            PacketHandler.sendTileUpdate((TileEntity) tile);
        }
        return false;
    }
}
