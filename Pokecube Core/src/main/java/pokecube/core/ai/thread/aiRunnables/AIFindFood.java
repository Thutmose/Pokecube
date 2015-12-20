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
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IBerryFruitBlock;
import pokecube.core.interfaces.IBreedingMob;
import pokecube.core.interfaces.IHungrymob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.ItemBerry;
import thut.api.TickHandler;
import thut.api.maths.Vector3;

public class AIFindFood extends AIBase
{

    final EntityLiving entity;
    // final World worldObj;
    final EntityItem   berry;
    final double       distance;
    IHungrymob         hungrymob;
    IPokemob           pokemob;
    Vector3            foodLoc = Vector3.getNewVectorFromPool();
    boolean            block   = false;
    double             moveSpeed;
    Vector3            v       = Vector3.getNewVectorFromPool();
    Vector3            v1      = Vector3.getNewVectorFromPool();

    public AIFindFood(final EntityLiving entity, final EntityItem berry_, double distance)
    {
        this.entity = entity;
        berry = berry_;
        this.distance = distance;
        this.hungrymob = (IHungrymob) entity;
        this.pokemob = (IPokemob) entity;
        this.moveSpeed = entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() * 0.75;
    }

    @Override
    public boolean shouldRun()
    {
        world = TickHandler.getInstance().getWorldCache(entity.dimension);
        if (world == null || pokemob.isAncient()) return false;

        hungrymob.setHungerCooldown(hungrymob.getHungerCooldown() - 1);
        hungrymob.setHungerTime(hungrymob.getHungerTime() + 1);
        boolean hunting = pokemob.getPokemonAIState(IPokemob.HUNTING);
        if (pokemob.getPokemonAIState(IPokemob.SLEEPING) || !hunting || hungrymob.neverHungry())
        {
            if (hungrymob.neverHungry()) hungrymob.setHungerTime(0);
            if (hunting) setPokemobAIState(pokemob, IPokemob.HUNTING, false);
            return false;
        }
        if (foodLoc.distToEntity(entity) > 32) foodLoc.clear();
        if (hungrymob.getHungerCooldown() > 0) return false;

        if (!foodLoc.isEmpty()) return true;

        return pokemob.getPokemonAIState(IPokemob.HUNTING);
    }

    @Override
    public void run()
    {
        if (foodLoc.isEmpty())
        {
            v.set(entity);
            if (hungrymob.isPhototroph())
            {
                if (entity.worldObj.provider.isDaytime() && v.canSeeSky(world))
                {
                    hungrymob.setHungerTime(0);
                    setPokemobAIState(pokemob, IPokemob.HUNTING, false);
                    return;
                }
            }
            if (hungrymob.isLithotroph())
            {
                Block b = v.getBlock(world, EnumFacing.DOWN);
                if (!Mod_Pokecube_Helper.getRocks().contains(b) || b == Blocks.gravel)
                {
                    Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance,
                            Mod_Pokecube_Helper.getRocks());
                    if (temp != null)
                    {
                        block = true;
                        foodLoc.set(temp);
                    }
                    if (temp != null) temp.freeVectorFromPool();
                    if (!foodLoc.isEmpty()) return;
                }
                else
                {
                    if (PokecubeMod.semiHardMode && Math.random() > 0.0075)
                    {
                        v.set(hungrymob).offsetBy(EnumFacing.DOWN);
                        if (b == Blocks.cobblestone)
                        {
                            TickHandler.addBlockChange(v, entity.dimension, Blocks.gravel);
                        }
                        else if (b == Blocks.gravel && Mod_Pokecube_Helper.pokemobsEatGravel)
                        {
                            TickHandler.addBlockChange(v, entity.dimension, Blocks.air);
                        }
                        else if (b.getMaterial() == Material.rock)
                        {
                            TickHandler.addBlockChange(v, entity.dimension, Blocks.cobblestone);
                        }
                    }
                    berry.setEntityItemStack(new ItemStack(b));
                    setPokemobAIState(pokemob, IPokemob.HUNTING, false);
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
                    setPokemobAIState(pokemob, IPokemob.HUNTING, false);
                    return;
                }
            }

            if (pokemob.getPokedexEntry().swims())
            {
                // List<Entity> list =
                // world.getEntitiesWithinAABB(EntityFishHook.class,
                // AxisAlignedBB
                // .getBoundingBox(entity.posX, entity.posY, entity.posZ,
                // entity.posX + 1.0D, entity.posY + 1.0D, entity.posZ + 1.0D)
                // .expand(20D, 20D, 20D));//TODO threadsafe this
                // if (!list.isEmpty())
                // {
                // Entity nearest = null;
                // double ds = 600;
                // double dt;
                // Vector3 v = Vector3.getNewVectorFromPool();
                // for (Entity e : list)
                // {
                // dt = e.getDistanceSqToEntity(entity);
                // if (dt < ds && Vector3.isVisibleEntityFromEntity(e, entity))
                // {
                // ds = dt;
                // nearest = e;
                // }
                // }
                // v.freeVectorFromPool();
                // if (nearest != null)
                // {
                // System.out.println("Found a bait " +
                // pokemob.getPokemonDisplayName());
                //
                // PathEntity path
                // =entity.getNavigator().getPathToEntityLiving(nearest);
                // PokemobAIThread.addEntityPath(entity.getEntityId(),
                // entity.dimension, path, moveSpeed);
                // return false;
                // }
                // }
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
                    setPokemobAIState(pokemob, IPokemob.HUNTING, false);
                    hungrymob.eat(berry);
                    return;
                }
            }
            // Tame pokemon can eat berries out of trapped chests, so check for
            // one
            // of those here.
            if (pokemob.getPokemonAIState(IPokemob.TAMED))
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
                            setPokemobAIState(pokemob, IPokemob.HUNTING, false);

                            PathEntity path = entity.getNavigator().getPathToXYZ(temp.x, temp.y, temp.z);
                            addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
                            hungrymob.eat(berry);
                            temp.freeVectorFromPool();
                            return;
                        }
                    }
                }
                if (temp != null)
                {
                    temp.freeVectorFromPool();
                }
            }

            // No food already obtained, reset mating rules, hungry things don't
            // mate
            if (pokemob instanceof IBreedingMob) ((IBreedingMob)pokemob).resetLoveStatus();

            if (pokemob.getPokemonAIState(IPokemob.TAMED) && pokemob.getPokemonAIState(IPokemob.SITTING))
            {
                hungrymob.setHungerCooldown(100);
                setPokemobAIState(pokemob, IPokemob.HUNTING, false);
                return;
            }
            block = false;
            v.set(hungrymob).add(0, entity.height, 0);

            if (foodLoc.isEmpty())
            {
                if (hungrymob.isHerbivore())
                {
                    Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance, PokecubeItems.grasses);
                    if (temp != null)
                    {
                        block = true;
                        foodLoc.set(temp);
                    }
                    if (temp != null) temp.freeVectorFromPool();
                }
                if (!block && hungrymob.filterFeeder())
                {
                    Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance, Blocks.water);
                    if (entity.isInWater())
                    {
                        hungrymob.eat(berry);
                        setPokemobAIState(pokemob, IPokemob.HUNTING, false);
                        return;
                    }

                    if (temp != null)
                    {
                        block = true;
                        foodLoc.set(temp);
                    }
                    if (temp != null) temp.freeVectorFromPool();
                }

                if (!block && hungrymob.eatsBerries())
                {
                    Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance, IBerryFruitBlock.class);
                    if (temp != null)
                    {
                        block = true;
                        foodLoc.set(temp);
                    }
                    if (temp != null) temp.freeVectorFromPool();
                }
            }

            if (foodLoc.isEmpty())
            {
                hungrymob.setHungerCooldown(10);
                setPokemobAIState(pokemob, IPokemob.HUNTING, false);
            }
        }
        else
        {
            // Go find and eat the block
            double d = foodLoc.addTo(0.5, 0.5, 0.5).distToEntity(entity);
            foodLoc.addTo(-0.5, -0.5, -0.5);
            Block b = foodLoc.getBlock(world);

            if (b instanceof IBerryFruitBlock)
            {
                ItemStack fruit = ((IBerryFruitBlock) b).getBerryStack(world, foodLoc.intX(), foodLoc.intY(),
                        foodLoc.intZ());

                if (fruit == null)
                {
                    foodLoc.clear();
                    hungrymob.noEat(null);
                    return;
                }

                if (d < 3)
                {
                    setPokemobAIState(pokemob, IPokemob.HUNTING, false);
                    berry.setEntityItemStack(fruit);
                    hungrymob.eat(berry);
                    TickHandler.addBlockChange(foodLoc, entity.dimension, Blocks.air);
                    // foodLoc.setBlock(world, Blocks.air);
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
                        setPokemobAIState(pokemob, IPokemob.HUNTING, false);
                        berry.setEntityItemStack(fruit);
                        hungrymob.noEat(berry);
                        foodLoc.clear();
                    }
                    else addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
                }
            }
            else if (b != null)
            {
                if (PokecubeItems.grasses.contains(b))
                {
                    double diff = 2;
                    diff = Math.max(diff, entity.width);
                    if (d < diff)
                    {
                        setPokemobAIState(pokemob, IPokemob.HUNTING, false);
                        berry.setEntityItemStack(new ItemStack(b));
                        hungrymob.eat(berry);
                        TickHandler.addBlockChange(foodLoc, entity.dimension,
                                b.getMaterial() == Material.grass ? Blocks.dirt : Blocks.air);
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
                            Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance,
                                    PokecubeItems.grasses);
                            if (temp != null)
                            {
                                block = true;
                                foodLoc.set(temp);
                            }
                            if (temp != null) temp.freeVectorFromPool();
                        }

                        if (!block && hungrymob.eatsBerries())
                        {
                            Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance,
                                    IBerryFruitBlock.class);
                            if (temp != null)
                            {
                                block = true;
                                foodLoc.set(temp);
                            }
                            if (temp != null) temp.freeVectorFromPool();
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
                            setPokemobAIState(pokemob, IPokemob.HUNTING, false);
                            berry.setEntityItemStack(new ItemStack(b));
                            hungrymob.noEat(berry);
                            foodLoc.clear();
                            addEntityPath(entity.getEntityId(), entity.dimension, null, moveSpeed);
                        }
                        else addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
                    }
                }
                else if ((Mod_Pokecube_Helper.getRocks().contains(b) || b == Blocks.gravel) && hungrymob.isLithotroph())
                {
                    double diff = 2;
                    diff = Math.max(diff, entity.width);
                    if (d < diff)
                    {

                        if (PokecubeMod.semiHardMode && Math.random() > 0.0075)
                        {
                            if (b == Blocks.cobblestone)
                            {
                                TickHandler.addBlockChange(foodLoc, entity.dimension, Blocks.gravel);
                            }
                            else if (b == Blocks.gravel && Mod_Pokecube_Helper.pokemobsEatGravel)
                            {
                                TickHandler.addBlockChange(foodLoc, entity.dimension, Blocks.air);
                            }
                            else if (b.getMaterial() == Material.rock)
                            {
                                TickHandler.addBlockChange(foodLoc, entity.dimension, Blocks.cobblestone);
                            }
                        }

                        setPokemobAIState(pokemob, IPokemob.HUNTING, false);
                        berry.setEntityItemStack(new ItemStack(b));
                        hungrymob.eat(berry);
                        foodLoc.clear();
                        addEntityPath(entity.getEntityId(), entity.dimension, null, moveSpeed);
                    }
                    else if (entity.ticksExisted % 20 == 0)
                    {
                        boolean shouldChangePath = true;
                        block = false;
                        v.set(hungrymob).add(0, entity.height, 0);

                        Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance,
                                Mod_Pokecube_Helper.getRocks());
                        if (temp != null)
                        {
                            block = true;
                            foodLoc.set(temp);
                        }
                        if (temp != null) temp.freeVectorFromPool();

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
                            setPokemobAIState(pokemob, IPokemob.HUNTING, false);
                            berry.setEntityItemStack(new ItemStack(b));
                            hungrymob.noEat(berry);
                            foodLoc.clear();
                            if (pokemob.hasHomeArea())
                            {
                                // System.out.println("test");
                                path = entity.getNavigator().getPathToXYZ(pokemob.getHome().getX(),
                                        pokemob.getHome().getY(), pokemob.getHome().getZ());
                                addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
                            }
                            else
                            {
                                addEntityPath(entity.getEntityId(), entity.dimension, null, moveSpeed);
                            }
                        }
                    }
                }
            }
            else
            {

            }
        }
    }

    @Override
    public void reset()
    {
        foodLoc.clear();
    }

}
