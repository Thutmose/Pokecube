/**
 * 
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.item.EntityItem;
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
                if (stack != CompatWrapper.nullStack) entityDropItem(stack, 0.5f);
                pokeChest.setInventorySlotContents(i, CompatWrapper.nullStack);
            }
        }

        if (wasEaten || !wasRecentlyHit) return;
        if (getPokemonAIState(IMoveConstants.TAMED)) return;
        List<ItemStack> drops = getPokedexEntry().getRandomDrops(lootingModifier);
        for (ItemStack stack : drops)
        {
            if (isBurning() && stack != CompatWrapper.nullStack)
            {
                ItemStack newDrop = FurnaceRecipes.instance().getSmeltingResult(stack);
                if (newDrop != CompatWrapper.nullStack) stack = newDrop.copy();
            }
            if (stack != CompatWrapper.nullStack) entityDropItem(stack, 0.5f);
        }
        dropItem();
        for (int i = 2; i < this.pokeChest.getSizeInventory(); i++)
        {
            ItemStack stack = this.pokeChest.getStackInSlot(i);
            if (stack != CompatWrapper.nullStack) entityDropItem(stack, 0.5f);
        }
    }

    public void dropItem()
    {
        ItemStack toDrop = this.getHeldItemMainhand();
        if (toDrop == CompatWrapper.nullStack) return;

        EntityItem drop = new EntityItem(this.worldObj, this.posX, this.posY + 0.5, this.posZ, toDrop);
        this.worldObj.spawnEntityInWorld(drop);
        this.setHeldItem(CompatWrapper.nullStack);
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
    @Nullable // TODO move stuff over to using a loot table instead?
    protected ResourceLocation getLootTable()
    {
        return null;
    }

    @Override
    /** Get the experience points the entity currently has. */
    protected int getExperiencePoints(EntityPlayer player)
    {
        float scale = PokecubeCore.core.getConfig().expFromDeathDropScale;
        int exp = (int) Math.max(1, this.getBaseXP() * scale * 0.01 * Math.sqrt(getLevel()));
        return exp;
    }
}
