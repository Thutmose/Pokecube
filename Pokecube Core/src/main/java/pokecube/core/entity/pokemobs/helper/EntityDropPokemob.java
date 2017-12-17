/**
 * 
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveConstants;
import thut.lib.CompatWrapper;

/** @author sebastien */
public abstract class EntityDropPokemob extends EntityMovesPokemob
{

    protected boolean wasEaten = false;

    /** @param world */
    public EntityDropPokemob(World world)
    {
        super(world);
    }

    @Override
    /** Drop the equipment for this entity. */
    protected void dropEquipment(boolean p_82160_1_, int p_82160_2_)
    {
        if (pokemobCap.getPokemonAIState(IMoveConstants.TAMED)) return;
        super.dropEquipment(p_82160_1_, p_82160_2_);
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier)
    {
        if (wasEaten || !wasRecentlyHit) return;
        if (pokemobCap.getPokemonAIState(IMoveConstants.TAMED)) return;
        for (int i = 0; i < pokemobCap.getPokemobInventory().getSizeInventory(); i++)
        {
            ItemStack stack = pokemobCap.getPokemobInventory().getStackInSlot(i);
            if (stack != CompatWrapper.nullStack) entityDropItem(stack, 0.5f);
            pokemobCap.getPokemobInventory().setInventorySlotContents(i, CompatWrapper.nullStack);
        }
        List<ItemStack> drops = pokemobCap.getPokedexEntry().getRandomDrops(lootingModifier);
        for (ItemStack stack : drops)
        {
            if (isBurning() && stack != CompatWrapper.nullStack)
            {
                ItemStack newDrop = FurnaceRecipes.instance().getSmeltingResult(stack);
                if (newDrop != CompatWrapper.nullStack) stack = newDrop.copy();
            }
            if (stack != CompatWrapper.nullStack) entityDropItem(stack, 0.5f);
        }
    }

    @Override
    protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source)
    {
        if (getLootTable() != null)
        {
            if (!pokemobCap.getPokemonAIState(IMoveConstants.TAMED))
            {
                for (int i = 0; i < pokemobCap.getPokemobInventory().getSizeInventory(); i++)
                {
                    ItemStack stack = pokemobCap.getPokemobInventory().getStackInSlot(i);
                    if (stack != CompatWrapper.nullStack) entityDropItem(stack.copy(), 0.0f);
                    pokemobCap.getPokemobInventory().setInventorySlotContents(i, CompatWrapper.nullStack);
                }
            }
            if (wasEaten || !wasRecentlyHit) return;
            if (pokemobCap.getPokemonAIState(IMoveConstants.TAMED)) return;
            super.dropLoot(wasRecentlyHit, lootingModifier, source);
        }
        else dropFewItems(wasRecentlyHit, lootingModifier);
    }

    @Override
    protected Item getDropItem()
    {
        return null;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable()
    {
        return pokemobCap.getPokedexEntry().lootTable;
    }

    @Override
    /** Get the experience points the entity currently has. */
    protected int getExperiencePoints(EntityPlayer player)
    {
        float scale = PokecubeCore.core.getConfig().expFromDeathDropScale;
        int exp = (int) Math.max(1, pokemobCap.getBaseXP() * scale * 0.01 * Math.sqrt(pokemobCap.getLevel()));
        return exp;
    }
}
