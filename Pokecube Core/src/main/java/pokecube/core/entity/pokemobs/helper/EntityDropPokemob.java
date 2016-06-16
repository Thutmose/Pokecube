/**
 * 
 */
package pokecube.core.entity.pokemobs.helper;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IMoveConstants;

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
        if (this.getPokemonAIState(IMoveConstants.TAMED)) return;

        super.dropEquipment(p_82160_1_, p_82160_2_);
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingModifier)
    {
        if (!getPokemonAIState(IMoveConstants.TAMED))
        {
            for (int i = 2; i < pokeChest.getSizeInventory(); i++)
            {
                ItemStack stack = pokeChest.getStackInSlot(i);
                if (stack != null) entityDropItem(stack, 0.5f);
                pokeChest.setInventorySlotContents(i, null);
            }
        }

        if (wasEaten || !wasRecentlyHit) return;

        ItemStack food = getPokedexEntry().getFoodDrop(lootingModifier);
        int j = 0;
        if (food != null) j = food.stackSize;

        if (!getPokemonAIState(IMoveConstants.TAMED) && food != null)
        {
            if (isBurning())
            {
                ItemStack newDrop = FurnaceRecipes.instance().getSmeltingResult(food);
                if (newDrop != null) food = newDrop.copy();
            }
            dropItem(food.getItem(), j);
        }

        if (getPokemonAIState(IMoveConstants.TAMED)) return;

        food = getPokedexEntry().getRandomCommonDrop(lootingModifier);
        if (food == null && this.getDropItem() != null) food = new ItemStack(this.getDropItem());
        if (food != null)
        {
            entityDropItem(food, 0.5f);
        }

        dropItem();
        food = getPokedexEntry().getRandomRareDrop(lootingModifier);
        if (food != null)
        {
            if (rand.nextInt(7) == 0) entityDropItem(food, 0.5f);
        }
        food = PokecubeItems.getStack("revive");
        if (food != null)
        {
            if (rand.nextInt(15) == 0) entityDropItem(food, 0.5f);
        }
    }

    public void dropItem()
    {
        ItemStack toDrop = this.getHeldItemMainhand();
        if (toDrop == null) return;

        EntityItem drop = new EntityItem(this.worldObj, this.posX, this.posY + 0.5, this.posZ, toDrop);
        this.worldObj.spawnEntityInWorld(drop);
        this.setHeldItem(null);
    }

    @Override
    protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source)
    {
        dropFewItems(wasRecentlyHit, lootingModifier);
    }

    @Override
    protected Item getDropItem()
    {
        return null;
    }

    @Override
    public ItemStack wildHeldItem()
    {
        return this.getPokedexEntry().getRandomHeldItem();
    }
}
