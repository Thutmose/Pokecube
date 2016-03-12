package pokecube.core.ai.thread.aiRunnables;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.EnumFacing;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IBerryFruitBlock;
import pokecube.core.interfaces.IBreedingMob;
import pokecube.core.interfaces.IHungrymob;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.ItemBerry;
import thut.api.TickHandler;
import thut.api.maths.Vector3;

public class AIHungry extends AIBase
{

    final EntityLiving entity;
    // final World worldObj;
    final EntityItem   berry;
    final double       distance;
    IHungrymob         hungrymob;
    IPokemob           pokemob;
    Vector3            foodLoc = Vector3.getNewVector();
    boolean            block   = false;
    double             moveSpeed;
    Vector3            v       = Vector3.getNewVector();
    Vector3            v1      = Vector3.getNewVector();

    public AIHungry(final EntityLiving entity, final EntityItem berry_, double distance)
    {
        this.entity = entity;
        berry = berry_;
        this.distance = distance;
        this.hungrymob = (IHungrymob) entity;
        this.pokemob = (IPokemob) entity;
        this.moveSpeed = entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() * 0.75;
    }

    protected void eatBerry(Block block, double distance)
    {
        ItemStack fruit = ((IBerryFruitBlock) block).getBerryStack(world, foodLoc.intX(), foodLoc.intY(),
                foodLoc.intZ());

        if (fruit == null)
        {
            foodLoc.clear();
            hungrymob.noEat(null);
            return;
        }

        if (distance < 3)
        {
            setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
            berry.setEntityItemStack(fruit);
            hungrymob.eat(berry);
            toRun.addElement(new InventoryChange(entity, 2, fruit, true));
            TickHandler.addBlockChange(foodLoc, entity.dimension, Blocks.air);
            foodLoc.clear();
        }
        else if (entity.ticksExisted % 20 == 0)
        {
            boolean shouldChangePath = true;
            if (!this.entity.getNavigator().noPath())
            {
                Vector3 p = v.set(this.entity.getNavigator().getPath().getFinalPathPoint());
                Vector3 v = v1.set(foodLoc);
                if (p.distToSq(v) <= 16) shouldChangePath = false;
            }
            PathEntity path = null;
            if (shouldChangePath
                    && (path = entity.getNavigator().getPathToXYZ(foodLoc.x, foodLoc.y, foodLoc.z)) == null)
            {
                addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
                setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                berry.setEntityItemStack(fruit);
                hungrymob.noEat(berry);
                foodLoc.clear();
            }
            else addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
        }
    }

    protected void eatPlant(Block plant, double dist)
    {
        double diff = 2;
        diff = Math.max(diff, entity.width);
        if (dist < diff)
        {
            setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
            berry.setEntityItemStack(new ItemStack(plant));
            hungrymob.eat(berry);
            TickHandler.addBlockChange(foodLoc, entity.dimension,
                    plant.getMaterial() == Material.grass ? Blocks.dirt : Blocks.air);
            if (plant.getMaterial() != Material.grass)
            {
                for (ItemStack stack : plant.getDrops(world, foodLoc.getPos(), foodLoc.getBlockState(world), 0))
                    toRun.addElement(new InventoryChange(entity, 2, stack, true));
            }
            foodLoc.clear();
            addEntityPath(entity.getEntityId(), entity.dimension, null, moveSpeed);
        }
        else if (entity.ticksExisted % 20 == 0)
        {
            boolean shouldChangePath = true;
            block = false;
            v.set(hungrymob).add(0, entity.height, 0);
            Vector3 p, m;
            if (hungrymob.isHerbivore())
            {
                Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance, PokecubeItems.grasses);
                if (temp != null)
                {
                    block = true;
                    foodLoc.set(temp);
                }
            }

            if (!block && hungrymob.eatsBerries())
            {
                Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance, IBerryFruitBlock.class);
                if (temp != null)
                {
                    block = true;
                    foodLoc.set(temp);
                }
            }

            if (!this.entity.getNavigator().noPath())
            {
                p = v.set(this.entity.getNavigator().getPath().getFinalPathPoint());
                m = v1.set(foodLoc);
                if (p.distToSq(m) <= 16) shouldChangePath = true;
            }
            PathEntity path = null;
            if (shouldChangePath
                    && (path = entity.getNavigator().getPathToXYZ(foodLoc.x, foodLoc.y, foodLoc.z)) == null)
            {
                setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                berry.setEntityItemStack(new ItemStack(plant));
                hungrymob.noEat(berry);
                foodLoc.clear();
                addEntityPath(entity.getEntityId(), entity.dimension, null, moveSpeed);
            }
            else addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
        }
    }

    protected void eatRocks(Block rock, double dist)
    {
        double diff = 2;
        diff = Math.max(diff, entity.width);
        if (dist < diff)
        {
            if (PokecubeMod.pokemobsDamageBlocks && Math.random() > 0.0075)
            {
                if (rock == Blocks.cobblestone)
                {
                    TickHandler.addBlockChange(foodLoc, entity.dimension, Blocks.gravel);
                }
                else if (rock == Blocks.gravel && PokecubeMod.core.getConfig().pokemobsEatGravel)
                {
                    TickHandler.addBlockChange(foodLoc, entity.dimension, Blocks.air);
                }
                else if (rock.getMaterial() == Material.rock)
                {
                    TickHandler.addBlockChange(foodLoc, entity.dimension, Blocks.cobblestone);
                }
            }

            setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
            berry.setEntityItemStack(new ItemStack(rock));
            hungrymob.eat(berry);
            foodLoc.clear();
            addEntityPath(entity.getEntityId(), entity.dimension, null, moveSpeed);
        }
        else if (entity.ticksExisted % 20 == 0)
        {
            boolean shouldChangePath = true;
            block = false;
            v.set(hungrymob).add(0, entity.height, 0);

            Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance, PokecubeMod.core.getConfig().getRocks());
            if (temp != null)
            {
                block = true;
                foodLoc.set(temp);
            }

            Vector3 p, m;
            if (!this.entity.getNavigator().noPath())
            {
                p = v.set(this.entity.getNavigator().getPath().getFinalPathPoint());
                m = v1.set(foodLoc);
                if (p.distToSq(m) <= 16) shouldChangePath = true;

            }
            boolean pathed = false;
            PathEntity path = null;
            if (shouldChangePath)
            {
                path = entity.getNavigator().getPathToXYZ(foodLoc.x, foodLoc.y, foodLoc.z);
                pathed = path != null;
                addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
            }

            if (shouldChangePath && !pathed)
            {
                setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                berry.setEntityItemStack(new ItemStack(rock));
                hungrymob.noEat(berry);
                foodLoc.clear();
                if (pokemob.hasHomeArea())
                {
                    path = entity.getNavigator().getPathToXYZ(pokemob.getHome().getX(), pokemob.getHome().getY(),
                            pokemob.getHome().getZ());
                    addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
                }
                else
                {
                    addEntityPath(entity.getEntityId(), entity.dimension, null, moveSpeed);
                }
            }
        }
    }

    protected void findFood()
    {
        v.set(entity);
        if (hungrymob.isPhototroph())
        {
            if (entity.worldObj.provider.isDaytime() && v.canSeeSky(world))
            {
                hungrymob.setHungerTime(0);
                setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                return;
            }
        }
        if (hungrymob.isLithotroph())
        {
            Block b = v.getBlock(world, EnumFacing.DOWN);
            if (!PokecubeMod.core.getConfig().getRocks().contains(b) || b == Blocks.gravel)
            {
                Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance, PokecubeMod.core.getConfig().getRocks());
                if (temp != null)
                {
                    block = true;
                    foodLoc.set(temp);
                }
                if (!foodLoc.isEmpty()) return;
            }
            else
            {
                if (PokecubeMod.pokemobsDamageBlocks && Math.random() > 0.0075)
                {
                    v.set(hungrymob).offsetBy(EnumFacing.DOWN);
                    if (b == Blocks.cobblestone)
                    {
                        TickHandler.addBlockChange(v, entity.dimension, Blocks.gravel);
                    }
                    else if (b == Blocks.gravel && PokecubeMod.core.getConfig().pokemobsEatGravel)
                    {
                        TickHandler.addBlockChange(v, entity.dimension, Blocks.air);
                    }
                    else if (b.getMaterial() == Material.rock)
                    {
                        TickHandler.addBlockChange(v, entity.dimension, Blocks.cobblestone);
                    }
                }
                berry.setEntityItemStack(new ItemStack(b));
                setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                hungrymob.eat(berry);
                return;
            }
        }
        if (hungrymob.isElectrotroph())
        {
            int num = v.blockCount(world, Blocks.redstone_block, 8);
            if (num < 1)
            {

            }
            else
            {
                hungrymob.setHungerTime(0);
                setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                return;
            }
        }

        if (pokemob.getPokedexEntry().swims())
        {
            // TODO find a fish hook to attack
        }

        // check inventory for berries
        for (int i = 2; i < 7; i++)
        {
            ItemStack stack = pokemob.getPokemobInventory().getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBerry)
            {
                stack.stackSize--;
                if (stack.stackSize <= 0)
                {
                    pokemob.getPokemobInventory().setInventorySlotContents(i, null);
                }
                setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                hungrymob.eat(berry);
                return;
            }
        }
        // Tame pokemon can eat berries out of trapped chests, so check for
        // one
        // of those here.
        if (pokemob.getPokemonAIState(IMoveConstants.TAMED))
        {
            IInventory container = null;
            v.set(hungrymob).add(0, entity.height, 0);

            Vector3 temp = v.findClosestVisibleObject(world, true, 10, Blocks.trapped_chest);

            if (temp != null && temp.getBlock(world) == Blocks.trapped_chest)
            {
                container = (IInventory) temp.getTileEntity(world);

                for (int i1 = 0; i1 < 27; i1++)
                {
                    ItemStack stack = container.getStackInSlot(i1);
                    if (stack != null && stack.getItem() instanceof ItemBerry)
                    {
                        stack.stackSize--;
                        if (stack.stackSize <= 0)
                        {
                            container.setInventorySlotContents(i1, null);
                        }
                        setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);

                        PathEntity path = entity.getNavigator().getPathToXYZ(temp.x, temp.y, temp.z);
                        addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
                        hungrymob.eat(berry);
                        return;
                    }
                }
            }
        }

        // No food already obtained, reset mating rules, hungry things don't
        // mate
        if (pokemob instanceof IBreedingMob) ((IBreedingMob) pokemob).resetLoveStatus();

        if (pokemob.getPokemonAIState(IMoveConstants.TAMED) && pokemob.getPokemonAIState(IMoveConstants.SITTING))
        {
            hungrymob.setHungerCooldown(100);
            setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
            return;
        }
        block = false;
        v.set(hungrymob).add(0, entity.height, 0);

        if (foodLoc.isEmpty())
        {
            if (!block && hungrymob.eatsBerries())
            {
                Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance, IBerryFruitBlock.class);
                if (temp != null)
                {
                    block = true;
                    foodLoc.set(temp);
                }
            }
            if (!block && hungrymob.isHerbivore())
            {
                Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance, PokecubeItems.grasses);
                if (temp != null)
                {
                    block = true;
                    foodLoc.set(temp);
                }
            }
            if (!block && hungrymob.filterFeeder())
            {
                Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance, Blocks.water);
                if (entity.isInWater())
                {
                    hungrymob.eat(berry);
                    setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                    return;
                }
                if (temp != null)
                {
                    block = true;
                    foodLoc.set(temp);
                }
            }
        }

        if (foodLoc.isEmpty())
        {
            hungrymob.setHungerCooldown(10);
            setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
        }
    }

    @Override
    public void reset()
    {
        foodLoc.clear();
    }

    @Override
    public void run()
    {
        if (foodLoc.isEmpty())
        {
            findFood();
        }
        else
        {
            // Go find and eat the block
            double d = foodLoc.addTo(0.5, 0.5, 0.5).distToEntity(entity);
            foodLoc.addTo(-0.5, -0.5, -0.5);
            Block b = foodLoc.getBlock(world);

            if (b instanceof IBerryFruitBlock)
            {
                eatBerry(b, d);
            }
            else if (b != null)
            {
                if (PokecubeItems.grasses.contains(b))
                {
                    eatPlant(b, d);
                }
                else if ((PokecubeMod.core.getConfig().getRocks().contains(b) || b == Blocks.gravel) && hungrymob.isLithotroph())
                {
                    eatRocks(b, d);
                }
            }
            else
            {

            }
        }
    }

    @Override
    public boolean shouldRun()
    {
        world = TickHandler.getInstance().getWorldCache(entity.dimension);
        if (world == null || pokemob.isAncient() || entity.getAttackTarget()!=null) return false;

        hungrymob.setHungerCooldown(hungrymob.getHungerCooldown() - 1);
        hungrymob.setHungerTime(hungrymob.getHungerTime() + 1);
        boolean hunting = pokemob.getPokemonAIState(IMoveConstants.HUNTING);
        if (pokemob.getPokemonAIState(IMoveConstants.SLEEPING) || !hunting || hungrymob.neverHungry())
        {
            if (hungrymob.neverHungry()) hungrymob.setHungerTime(0);
            if (hunting) setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
            return false;
        }
        if (foodLoc.distToEntity(entity) > 32) foodLoc.clear();
        if (hungrymob.getHungerCooldown() > 0) return false;

        if (!foodLoc.isEmpty()) return true;

        return pokemob.getPokemonAIState(IMoveConstants.HUNTING);
    }
}
