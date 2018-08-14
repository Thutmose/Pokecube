package pokecube.core.interfaces.capabilities.impl;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.IBlockAccess;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.Nature;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.lib.ItemStackTools;

public abstract class PokemobHungry extends PokemobMoves
{

    @Override
    public void eat(Entity e)
    {
        int hungerValue = PokecubeMod.core.getConfig().pokemobLifeSpan / 4;
        if (e instanceof EntityItem)
        {
            ItemStack item = ((EntityItem) e).getItem();
            IPokemobUseable usable = IPokemobUseable.getUsableFor(item);
            if (usable != null)
            {
                ActionResult<ItemStack> result = usable.onUse(this, item, getEntity());
                if (result.getType() == EnumActionResult.SUCCESS)
                {
                    ItemStackTools.addItemStackToInventory(result.getResult(), this.getPokemobInventory(), 1);
                }
            }
            if (Tools.isStack(item, "leppaberry"))
            {
                hungerValue *= 2;
            }
            if (item.getItem() instanceof ItemBerry)
            {
                int weight = Nature.getBerryWeight(item.getItemDamage(), getNature());
                int current = getHappiness();
                HappinessType type = HappinessType.BERRY;
                if (current < 100)
                {
                    weight *= type.low / 10f;
                }
                else if (current < 200)
                {
                    weight *= type.mid / 10f;
                }
                else
                {
                    weight *= type.high / 10f;
                }
                addHappiness(weight);
            }
        }
        setHungerTime(getHungerTime() - hungerValue);
        hungerCooldown = 0;
        setCombatState(CombatStates.HUNTING, false);
        if (getEntity().isDead) return;
        float missingHp = getEntity().getMaxHealth() - getEntity().getHealth();
        float toHeal = getEntity().getHealth() + Math.max(1, missingHp * 0.25f);
        getEntity().setHealth(Math.min(toHeal, getEntity().getMaxHealth()));
        // Make wild pokemon level up naturally to their cap, to allow wild
        // hatches
        if (!getGeneralState(GeneralStates.TAMED))
        {
            int exp = SpawnHandler.getSpawnXp(getEntity().getEntityWorld(), here.set(getEntity()), getPokedexEntry());
            if (getExp() < exp)
            {
                int n = new Random().nextInt(exp - getExp()) / 3 + 1;
                setExp(getExp() + n, true);
            }
        }
    }

    @Override
    public boolean eatsBerries()
    {
        return getPokedexEntry().foods[5];
    }

    @Override
    public boolean filterFeeder()
    {
        return getPokedexEntry().foods[6];
    }

    // TODO also include effects from external float reasons here
    @Override
    public boolean floats()
    {
        return getPokedexEntry().floats() && !isGrounded();
    }

    @Override
    public boolean flys()
    {
        return getPokedexEntry().flys() && !isGrounded();
    }

    @Override
    public float getBlockPathWeight(IBlockAccess world, Vector3 location)
    {
        IBlockState state = location.getBlockState(world);
        if (state == null) state = Blocks.AIR.getDefaultState();
        Block block = state.getBlock();
        boolean water = getPokedexEntry().swims();
        boolean air = flys() || floats();
        if (getPokedexEntry().hatedMaterial != null)
        {
            String material = getPokedexEntry().hatedMaterial[0];
            if (material.equalsIgnoreCase("water") && state.getMaterial() == Material.WATER) { return 100; }
        }
        if (state.getMaterial() == Material.WATER) return water ? 1 : air ? 100 : 40;
        if (block == Blocks.GRAVEL) return water ? 40 : 5;
        if (!getEntity().isImmuneToFire()
                && (state.getMaterial() == Material.LAVA || state.getMaterial() == Material.FIRE))
            return 200;
        return water ? 40 : 20;
    }

    @Override
    public double getFloatHeight()
    {
        return getPokedexEntry().preferedHeight;
    }

    @Override
    public int getHungerCooldown()
    {
        return hungerCooldown;
    }

    @Override
    public int getHungerTime()
    {
        return getDataManager().get(params.HUNGERDW);
    }

    @Override
    public Vector3 getMobSizes()
    {
        return sizes.set(getPokedexEntry().width, getPokedexEntry().height, getPokedexEntry().length)
                .scalarMult(getSize());
    }

    @Override
    public int getPathTime()
    {
        return 0;
    }

    /** @return does this pokemon hunt for food */
    @Override
    public boolean isCarnivore()
    {
        return this.getPokedexEntry().hasPrey();
    }

    @Override
    public boolean isElectrotroph()
    {
        return getPokedexEntry().foods[2];
    }

    /** @return Does this pokemon eat grass */
    @Override
    public boolean isHerbivore()
    {
        return getPokedexEntry().foods[3];
    }

    @Override
    public boolean isLithotroph()
    {
        return getPokedexEntry().foods[1];
    }

    @Override
    public boolean isPhototroph()
    {
        return getPokedexEntry().foods[0];
    }

    @Override
    public boolean neverHungry()
    {
        return getPokedexEntry().foods[4];
    }

    @Override
    public void noEat(Entity e)
    {
        if (e != null)
        {
            addHappiness(-10);
        }
    }

    @Override
    public void setHungerCooldown(int hungerCooldown)
    {
        this.hungerCooldown = hungerCooldown;
    }

    @Override
    public void setHungerTime(int hungerTime)
    {
        getDataManager().set(params.HUNGERDW, hungerTime);
    }

    @Override
    public boolean swims()
    {
        return getPokedexEntry().swims();
    }

    @Override
    public int getFlavourAmount(int index)
    {
        return dataManager.get(params.FLAVOURS[index]);
    }

    @Override
    public void setFlavourAmount(int index, int amount)
    {
        dataManager.set(params.FLAVOURS[index], amount);
    }
}
