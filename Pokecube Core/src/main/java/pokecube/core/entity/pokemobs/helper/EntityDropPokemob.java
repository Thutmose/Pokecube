/**
 * 
 */
package pokecube.core.entity.pokemobs.helper;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemob;

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
    protected void dropFewItems(boolean flag, int nb)
    {
        if (!getPokemonAIState(IPokemob.TAMED))
        {
            for (int i = 2; i < pokeChest.getSizeInventory(); i++)
            {
                ItemStack stack = pokeChest.getStackInSlot(i);
                if (stack != null) entityDropItem(stack, 0.5f);
                pokeChest.setInventorySlotContents(i, null);
            }
        }

        if (wasEaten || !flag) return;

        ItemStack food = getPokedexEntry().getFoodDrop(nb);
        int j = 0;
        if (food != null) j = food.stackSize;

        if (!getPokemonAIState(IPokemob.TAMED) && food != null)
        {
            if (isBurning())
            {
                if (food.getItem() == Items.chicken) dropItem(Items.cooked_chicken, j);
                else if (food.getItem() == Items.potato) dropItem(Items.baked_potato, j);
                else if (food.getItem() == Items.beef) dropItem(Items.cooked_beef, j);
                else if (food.getItem() == Items.fish) dropItem(Items.cooked_fish, j);
                else if (food.getItem() == Items.porkchop) dropItem(Items.cooked_porkchop, j);
                else dropItem(food.getItem(), j);
            }
            else
            {
                dropItem(food.getItem(), j);
            }
        }

        if (getPokemonAIState(IPokemob.TAMED)) return;

        food = getPokedexEntry().getRandomCommonDrop(nb);
        if (food == null && this.getDropItem() != null) food = new ItemStack(this.getDropItem());
        if (food != null)
        {
            entityDropItem(food, 0.5f);
        }

        dropItem();
        food = getPokedexEntry().getRandomRareDrop(nb);
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
        ItemStack toDrop = this.getHeldItem();
        if (toDrop == null) return;

        EntityItem drop = new EntityItem(this.worldObj, this.posX, this.posY + 0.5, this.posZ, toDrop);
        this.worldObj.spawnEntityInWorld(drop);
        this.setHeldItem(null);
    }

    @Override
    /** Drop the equipment for this entity. */
    protected void dropEquipment(boolean p_82160_1_, int p_82160_2_)
    {
        if (this.getPokemonAIState(IPokemob.TAMED)) return;

        super.dropEquipment(p_82160_1_, p_82160_2_);
    }

    @Override
    public ItemStack wildHeldItem()
    {
        return this.getPokedexEntry().getRandomHeldItem();
    }

    @Override
    protected Item getDropItem()
    {
        return null;// Item.getItemById(itemIdCommonDrop);
    }
}
