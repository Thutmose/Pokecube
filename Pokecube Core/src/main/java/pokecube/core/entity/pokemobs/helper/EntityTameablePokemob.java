/**
 *
 */
package pokecube.core.entity.pokemobs.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.core.PokecubeItems;
import pokecube.core.database.PokedexEntry.InteractionLogic.Interaction;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.capabilities.DefaultPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import thut.api.entity.IMobColourable;
import thut.api.maths.Vector3;

/** @author Manchou */
public abstract class EntityTameablePokemob extends EntityAnimal implements IShearable, IEntityOwnable, IMobColourable,
        IRangedAttackMob, IEntityAdditionalSpawnData, IJumpingMount
{

    protected boolean              looksWithInterest;
    protected float                headRotation;
    protected float                headRotationOld;
    protected boolean              isPokemonShaking;
    protected boolean              isPokemonWet;
    protected float                timePokemonIsShaking;
    protected float                prevTimePokemonIsShaking;
    public float                   length = 1;
    protected Vector3              here   = Vector3.getNewVector();
    protected Vector3              vec    = Vector3.getNewVector();
    protected Vector3              v1     = Vector3.getNewVector();
    protected Vector3              v2     = Vector3.getNewVector();
    protected Vector3              vBak   = Vector3.getNewVector();

    protected final DefaultPokemob pokemobCap;

    /** @param par1World */
    public EntityTameablePokemob(World world)
    {
        super(world);
        pokemobCap = (DefaultPokemob) getCapability(CapabilityPokemob.POKEMOB_CAP, null);
    }

    public boolean canBeHeld(ItemStack itemStack)
    {
        return PokecubeItems.isValidHeldItem(itemStack);
    }

    @SideOnly(Side.CLIENT)
    public float getInterestedAngle(float f)
    {
        return (headRotationOld + (headRotation - headRotationOld) * f) * 0.15F * (float) Math.PI;
    }

    @SideOnly(Side.CLIENT)
    public float getShakeAngle(float f, float f1)
    {
        float f2 = (prevTimePokemonIsShaking + (timePokemonIsShaking - prevTimePokemonIsShaking) * f + f1) / 1.8F;

        if (f2 < 0.0F)
        {
            f2 = 0.0F;
        }
        else if (f2 > 1.0F)
        {
            f2 = 1.0F;
        }

        return MathHelper.sin(f2 * (float) Math.PI) * MathHelper.sin(f2 * (float) Math.PI * 11F) * 0.15F
                * (float) Math.PI;
    }

    /** returns true if a sheeps wool has been sheared */
    public boolean getSheared()
    {
        return pokemobCap.getGeneralState(GeneralStates.SHEARED);
    }

    @Override
    public Team getTeam()
    {
        if (pokemobCap.getOwner() == this) { return this.getEntityWorld().getScoreboard()
                .getPlayersTeam(this.getCachedUniqueIdString()); }
        return super.getTeam();
    }

    public void init(int nb)
    {
        looksWithInterest = false;
    }

    @Override
    protected boolean isMovementBlocked()
    {
        return isPokemonWet || this.getHealth() <= 0.0F;
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos)
    {
        /** Checks if the pokedex entry has shears listed, if so, then apply to
         * any mod shears as well. */
        ItemStack key = new ItemStack(Items.SHEARS);
        if (pokemobCap.getPokedexEntry().interact(key))
        {
            long last = getEntityData().getLong("lastSheared");
            Interaction action = pokemobCap.getPokedexEntry().interactionLogic.actions
                    .get(pokemobCap.getPokedexEntry().interactionLogic.getKey(key));
            if (last < getEntityWorld().getTotalWorldTime() - (action.cooldown + rand.nextInt(1 + action.variance))
                    && !getEntityWorld().isRemote)
            {
                pokemobCap.setGeneralState(GeneralStates.SHEARED, false);
            }

            return !getSheared();
        }
        return false;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune)
    {
        ItemStack key = new ItemStack(Items.SHEARS);
        if (pokemobCap.getPokedexEntry().interact(key))
        {
            ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
            pokemobCap.setGeneralState(GeneralStates.SHEARED, true);
            getEntityData().setLong("lastSheared", getEntityWorld().getTotalWorldTime());
            List<ItemStack> list = pokemobCap.getPokedexEntry().getInteractResult(key);
            Interaction action = pokemobCap.getPokedexEntry().interactionLogic.actions
                    .get(pokemobCap.getPokedexEntry().interactionLogic.getKey(key));
            int time = pokemobCap.getHungerTime();
            pokemobCap.setHungerTime(time + action.hunger);
            for (ItemStack stack : list)
            {
                ItemStack toAdd = stack.copy();
                if (pokemobCap.getPokedexEntry().dyeable)
                    toAdd.setItemDamage(EnumDyeColor.byDyeDamage(pokemobCap.getSpecialInfo() & 15).getMetadata());
                ret.add(toAdd);
            }
            this.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, 1.0F);
            return ret;
        }
        return null;
    }

    @Override
    public UUID getOwnerId()
    {
        return pokemobCap.getPokemonOwnerID();
    }

    @Override
    public Entity getOwner()
    {
        return pokemobCap.getOwner();
    }

    private InvWrapper inventory;

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return (T) (inventory == null ? inventory = new InvWrapper(pokemobCap.getPokemobInventory()) : inventory);
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }
}
