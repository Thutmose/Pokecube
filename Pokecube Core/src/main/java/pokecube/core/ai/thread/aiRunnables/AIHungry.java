package pokecube.core.ai.thread.aiRunnables;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.interfaces.IBerryFruitBlock;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.berries.ItemBerry;
import pokecube.core.utils.ChunkCoordinate;
import pokecube.core.utils.TimePeriod;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.TickHandler;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;
import thut.lib.ItemStackTools;

/** This IAIRunnable is responsible for finding food for the mobs. It also is
 * what adds berries to their inventories based on which biome they are
 * currently in. */
public class AIHungry extends AIBase
{
    private static class GenBerries implements IRunnable
    {
        final IPokemob pokemob;

        public GenBerries(IPokemob mob)
        {
            pokemob = mob;
        }

        @Override
        public boolean run(World world)
        {
            ItemStack stack = BerryGenManager.getRandomBerryForBiome(world, pokemob.getEntity().getPosition());
            if (stack != null)
            {
                ItemStackTools.addItemStackToInventory(stack, pokemob.getPokemobInventory(), 2);
                pokemob.eat(new EntityItem(world, 0, 0, 0, stack));
            }
            return true;
        }

    }

    final EntityLiving entity;
    // final World world;
    final EntityItem   berry;
    final double       distance;
    IPokemob           pokemob;
    Vector3            foodLoc = Vector3.getNewVector();
    boolean            block   = false;
    boolean            sleepy  = false;
    double             moveSpeed;
    Vector3            v       = Vector3.getNewVector();
    Vector3            v1      = Vector3.getNewVector();
    Random             rand;

    public AIHungry(final IPokemob pokemob, final EntityItem berry_, double distance)
    {
        this.entity = pokemob.getEntity();
        berry = berry_;
        this.distance = distance;
        this.pokemob = pokemob;
        this.moveSpeed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * 0.75;
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);

        int hungerTime = pokemob.getHungerTime();
        sleepy = true;
        for (TimePeriod p : pokemob.getPokedexEntry().activeTimes())
        {
            if (p != null && p.contains(entity.getEntityWorld().getWorldTime()))
            {
                sleepy = false;
                break;
            }
        }
        v.set(entity);
        ChunkCoordinate c = new ChunkCoordinate(v, entity.dimension);
        int deathTime = PokecubeMod.core.getConfig().pokemobLifeSpan;
        if (!pokemob.neverHungry() && pokemob.getHungerCooldown() < 0)
        {
            if (hungerTime > 0 && !pokemob.getPokemonAIState(IMoveConstants.HUNTING))
            {
                pokemob.setPokemonAIState(IMoveConstants.HUNTING, true);
            }
        }

        if (shouldRun() || Math.random() > 0.99)
        {
            if (pokemob.getPokedexEntry().swims())
            {// grow in 1.12
                AxisAlignedBB bb = v.set(entity).addTo(0, entity.getEyeHeight(), 0).getAABB()
                        .grow(PokecubeMod.core.getConfig().fishHookBaitRange);
                List<EntityFishHook> hooks = entity.getEntityWorld().getEntitiesWithinAABB(EntityFishHook.class, bb);
                pokemob.setPokemonAIState(IPokemob.HUNTING, true);
                if (!hooks.isEmpty())
                {
                    Collections.shuffle(hooks);
                    EntityFishHook hook = hooks.get(0);
                    if (v.isVisible(world, v1.set(hook)))
                    {
                        Path path = entity.getNavigator().getPathToEntityLiving(hook);
                        addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
                        addTargetInfo(entity, hook);
                        if (entity.getDistanceSqToEntity(hook) < 2)
                        {
                            hook.caughtEntity = entity;
                            pokemob.eat(hook);
                        }
                    }
                }
            }
        }

        double hurtTime = deathTime / 2d;
        Random rand = new Random(pokemob.getRNGValue());
        int tick = rand.nextInt(100);
        if (hungerTime > hurtTime && !entity.getEntityWorld().isRemote && entity.getAttackTarget() == null
                && !pokemob.neverHungry() && entity.ticksExisted % 100 == tick)
        {
            boolean ate = false;
            for (int i = 2; i < 7; i++)
            {
                ItemStack stack = pokemob.getPokemobInventory().getStackInSlot(i);
                if (stack != null && stack.getItem() instanceof ItemBerry)
                {
                    setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                    pokemob.eat(berry);
                    CompatWrapper.increment(stack, -1);
                    if (!CompatWrapper.isValid(stack))
                    {
                        pokemob.getPokemobInventory().setInventorySlotContents(i, CompatWrapper.nullStack);
                    }
                    ate = true;
                }
            }
            if (!ate)
            {
                float ratio = (float) ((hungerTime - hurtTime) / deathTime);
                boolean dead = entity.getMaxHealth() * ratio > entity.getHealth();
                entity.attackEntityFrom(DamageSource.STARVE, entity.getMaxHealth() * ratio);
                if (!dead) pokemob.displayMessageToOwner(
                        new TextComponentTranslation("pokemob.hungry.hurt", pokemob.getPokemonDisplayName()));
                else pokemob.displayMessageToOwner(
                        new TextComponentTranslation("pokemob.hungry.dead", pokemob.getPokemonDisplayName()));
                boolean tameCheck = !pokemob.isPlayerOwned() || pokemob.getPokemonAIState(IMoveConstants.STAYING);
                if (tameCheck)
                {
                    toRun.add(new GenBerries(pokemob));
                }
            }
        }
        boolean ownedSleepCheck = pokemob.getPokemonAIState(IMoveConstants.TAMED)
                && !(pokemob.getPokemonAIState(IMoveConstants.STAYING));
        if (sleepy && hungerTime < 0 && !ownedSleepCheck)
        {
            if (!isGoodSleepingSpot(c))
            {
                pokemob.setPokemonAIState(IMoveConstants.IDLE, true);
                Path path = this.entity.getNavigator().getPathToPos(pokemob.getHome());
                if (path != null && path.getCurrentPathLength() > 32) path = null;
                addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
                pokemob.setPokemonAIState(IMoveConstants.IDLE, false);
            }
            else if (entity.getAttackTarget() == null && entity.getNavigator().noPath())
            {
                pokemob.setPokemonAIState(IMoveConstants.SLEEPING, true);
                pokemob.setPokemonAIState(IMoveConstants.HUNTING, false);
            }
            else if (!entity.getNavigator().noPath() || entity.getAttackTarget() != null)
            {
                pokemob.setPokemonAIState(IMoveConstants.SLEEPING, false);
            }
        }
        else if (!pokemob.getPokemonAIState(IMoveConstants.TIRED))
        {
            pokemob.setPokemonAIState(IMoveConstants.SLEEPING, false);
        }
        if (ownedSleepCheck)
        {
            pokemob.setPokemonAIState(IMoveConstants.SLEEPING, false);
        }

        // cap hunger.
        hungerTime = pokemob.getHungerTime();
        int hunger = Math.max(hungerTime, -deathTime / 4);
        if (hunger != hungerTime) pokemob.setHungerTime(hunger);

        if (entity.getAttackTarget() == null && !entity.isDead && entity.ticksExisted % 100 == tick
                && !entity.getEntityWorld().isRemote && pokemob.getHungerCooldown() < 0 && pokemob.getHungerTime() < 0)
        {
            float dh = Math.max(1, entity.getMaxHealth() * 0.05f);
            float toHeal = entity.getHealth() + dh;
            entity.setHealth(Math.min(toHeal, entity.getMaxHealth()));
        }
    }

    protected void eatBerry(IBlockState b, double distance)
    {
        ItemStack fruit = ((IBerryFruitBlock) b.getBlock()).getBerryStack(world, foodLoc.getPos());

        if (!CompatWrapper.isValid(fruit))
        {
            foodLoc.clear();
            pokemob.noEat(null);
            return;
        }

        if (distance < 3)
        {
            setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
            berry.setItem(fruit);
            pokemob.eat(berry);
            toRun.addElement(new InventoryChange(entity, 2, fruit, true));
            TickHandler.addBlockChange(foodLoc, entity.dimension, Blocks.AIR);
            foodLoc.clear();
        }
        else if (entity.ticksExisted % 20 == rand.nextInt(20))
        {
            boolean shouldChangePath = true;
            if (!this.entity.getNavigator().noPath())
            {
                Vector3 p = v.set(this.entity.getNavigator().getPath().getFinalPathPoint());
                Vector3 v = v1.set(foodLoc);
                if (p.distToSq(v) <= 16) shouldChangePath = false;
            }
            Path path = null;
            if (shouldChangePath
                    && (path = entity.getNavigator().getPathToXYZ(foodLoc.x, foodLoc.y, foodLoc.z)) == null)
            {
                addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
                setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                berry.setItem(fruit);
                pokemob.noEat(berry);
                foodLoc.clear();
            }
            else addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
        }
    }

    protected void eatPlant(IBlockState b, Vector3 location, double dist)
    {
        double diff = 2;
        diff = Math.max(diff, entity.width);
        if (dist < diff)
        {
            setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
            berry.setItem(new ItemStack(b.getBlock()));
            pokemob.eat(berry);
            if (PokecubeMod.core.getConfig().pokemobsEatPlants)
            {
                TickHandler.addBlockChange(foodLoc, entity.dimension,
                        location.getBlockState(world).getMaterial() == Material.GRASS ? Blocks.DIRT : Blocks.AIR);
                if (location.getBlockState(world).getMaterial() != Material.GRASS)
                {
                    NonNullList<ItemStack> list = NonNullList.create();
                    b.getBlock().getDrops(list, world, foodLoc.getPos(), foodLoc.getBlockState(world), 0);
                    for (ItemStack stack : list)
                        toRun.addElement(new InventoryChange(entity, 2, stack, true));
                }
            }
            foodLoc.clear();
            addEntityPath(entity.getEntityId(), entity.dimension, null, moveSpeed);
        }
        else if (entity.ticksExisted % 20 == rand.nextInt(20))
        {
            boolean shouldChangePath = true;
            block = false;
            v.set(entity).add(0, entity.height, 0);
            Vector3 p, m;
            if (pokemob.isHerbivore())
            {
                Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance,
                        PokecubeMod.core.getConfig().getPlantTypes());
                if (temp != null)
                {
                    block = true;
                    foodLoc.set(temp);
                }
            }

            if (!block && pokemob.eatsBerries())
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
            Path path = null;
            if (shouldChangePath
                    && (path = entity.getNavigator().getPathToXYZ(foodLoc.x, foodLoc.y, foodLoc.z)) == null)
            {
                setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                berry.setItem(new ItemStack(b.getBlock()));
                pokemob.noEat(berry);
                foodLoc.clear();
                addEntityPath(entity.getEntityId(), entity.dimension, null, moveSpeed);
            }
            else addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
        }
    }

    protected void eatRocks(IBlockState b, Vector3 location, double dist)
    {
        double diff = 2;
        diff = Math.max(diff, entity.width);
        if (dist < diff)
        {
            if (PokecubeMod.core.getConfig().pokemobsEatRocks && Math.random() > 0.0075)
            {
                if (b.getBlock() == Blocks.COBBLESTONE)
                {
                    TickHandler.addBlockChange(foodLoc, entity.dimension, Blocks.GRAVEL);
                }
                else if (b.getBlock() == Blocks.GRAVEL && PokecubeMod.core.getConfig().pokemobsEatGravel)
                {
                    TickHandler.addBlockChange(foodLoc, entity.dimension, Blocks.AIR);
                }
                else if (location.getBlockState(world).getMaterial() == Material.ROCK)
                {
                    TickHandler.addBlockChange(foodLoc, entity.dimension, Blocks.COBBLESTONE);
                }
            }
            setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
            berry.setItem(new ItemStack(b.getBlock()));
            pokemob.eat(berry);
            foodLoc.clear();
            addEntityPath(entity.getEntityId(), entity.dimension, null, moveSpeed);
        }
        else if (entity.ticksExisted % 20 == rand.nextInt(20))
        {
            boolean shouldChangePath = true;
            block = false;
            v.set(entity).add(0, entity.height, 0);

            Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance,
                    PokecubeMod.core.getConfig().getRocks());
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
            Path path = null;
            if (shouldChangePath)
            {
                path = entity.getNavigator().getPathToXYZ(foodLoc.x, foodLoc.y, foodLoc.z);
                pathed = path != null;
                addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
            }

            if (shouldChangePath && !pathed)
            {
                setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                berry.setItem(new ItemStack(b.getBlock()));
                pokemob.noEat(berry);
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
        v.set(entity).addTo(0, entity.getEyeHeight(), 0);
        if (pokemob.isPhototroph())
        {
            if (entity.getEntityWorld().provider.isDaytime() && v.canSeeSky(world))
            {
                pokemob.setHungerTime(pokemob.getHungerTime() - PokecubeMod.core.getConfig().pokemobLifeSpan / 4);
                setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                return;
            }
        }
        if (pokemob.isLithotroph())
        {
            IBlockState state = v.offset(EnumFacing.DOWN).getBlockState(world);
            Block b = state.getBlock();
            if (!PokecubeTerrainChecker.isRock(state))
            {
                Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance,
                        PokecubeMod.core.getConfig().getRocks());
                if (temp != null)
                {
                    block = true;
                    foodLoc.set(temp);
                }
                if (!foodLoc.isEmpty()) return;
            }
            else
            {
                if (PokecubeMod.core.getConfig().pokemobsEatRocks && Math.random() > 0.0075)
                {
                    v.set(entity).offsetBy(EnumFacing.DOWN);
                    if (b == Blocks.COBBLESTONE)
                    {
                        TickHandler.addBlockChange(v, entity.dimension, Blocks.GRAVEL);
                    }
                    else if (b == Blocks.GRAVEL && PokecubeMod.core.getConfig().pokemobsEatGravel)
                    {
                        TickHandler.addBlockChange(v, entity.dimension, Blocks.AIR);
                    }
                    else if (state.getMaterial() == Material.ROCK)
                    {
                        TickHandler.addBlockChange(v, entity.dimension, Blocks.COBBLESTONE);
                    }
                }
                berry.setItem(new ItemStack(b));
                setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                pokemob.eat(berry);
                return;
            }
        }
        if (pokemob.isElectrotroph())
        {
            int num = v.blockCount(world, Blocks.REDSTONE_BLOCK, 8);
            if (num < 1)
            {

            }
            else
            {
                pokemob.setHungerTime(pokemob.getHungerTime() - PokecubeMod.core.getConfig().pokemobLifeSpan / 4);
                setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                return;
            }
        }
        if (pokemob.getPokedexEntry().swims())
        {// grow in 1.12
            AxisAlignedBB bb = v.set(entity).addTo(0, entity.getEyeHeight(), 0).getAABB()
                    .grow(PokecubeMod.core.getConfig().fishHookBaitRange);
            List<EntityFishHook> hooks = entity.getEntityWorld().getEntitiesWithinAABB(EntityFishHook.class, bb);
            if (!hooks.isEmpty())
            {
                Collections.shuffle(hooks);
                EntityFishHook hook = hooks.get(0);
                if (v.isVisible(world, v1.set(hook)))
                {// We return here, as main thread tick will deal with the
                 // eating of this fish hook as to prevent concurrency issues
                 // with trying to get it.
                    return;
                }
            }
        }

        // check inventory for berries
        for (int i = 2; i < 7; i++)
        {
            ItemStack stack = pokemob.getPokemobInventory().getStackInSlot(i);
            if (CompatWrapper.isValid(stack) && stack.getItem() instanceof ItemBerry)
            {
                CompatWrapper.increment(stack, -1);
                if (!CompatWrapper.isValid(stack))
                {
                    pokemob.getPokemobInventory().setInventorySlotContents(i, CompatWrapper.nullStack);
                }
                setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                berry.setItem(stack.copy());
                pokemob.eat(berry);
                return;
            }
        }
        // Tame pokemon can eat berries out of trapped chests, so check for
        // one
        // of those here.
        if (pokemob.getPokemonAIState(IMoveConstants.TAMED))
        {
            IInventory container = null;
            v.set(entity).add(0, entity.height, 0);

            Vector3 temp = v.findClosestVisibleObject(world, true, 10, Blocks.TRAPPED_CHEST);

            if (temp != null && temp.getBlock(world) == Blocks.TRAPPED_CHEST)
            {
                container = (IInventory) temp.getTileEntity(world);

                for (int i1 = 0; i1 < 27; i1++)
                {
                    ItemStack stack = container.getStackInSlot(i1);
                    if (CompatWrapper.isValid(stack) && stack.getItem() instanceof ItemBerry)
                    {
                        CompatWrapper.increment(stack, -1);
                        if (!CompatWrapper.isValid(stack))
                        {
                            container.setInventorySlotContents(i1, CompatWrapper.nullStack);
                        }
                        setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);

                        Path path = entity.getNavigator().getPathToXYZ(temp.x, temp.y, temp.z);
                        addEntityPath(entity.getEntityId(), entity.dimension, path, moveSpeed);
                        pokemob.eat(berry);
                        return;
                    }
                }
            }
        }

        // No food already obtained, reset mating rules, hungry things don't
        // mate
        pokemob.resetLoveStatus();

        if (pokemob.getPokemonAIState(IMoveConstants.TAMED) && pokemob.getPokemonAIState(IMoveConstants.SITTING))
        {
            pokemob.setHungerCooldown(100);
            setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
            return;
        }
        block = false;
        v.set(entity).add(0, entity.height, 0);

        if (foodLoc.isEmpty())
        {
            if (!block && pokemob.isHerbivore())
            {
                Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance,
                        PokecubeCore.core.getConfig().getPlantTypes());
                if (temp != null)
                {
                    block = true;
                    foodLoc.set(temp);
                }
            }
            if (!block && pokemob.filterFeeder())
            {
                Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance, Blocks.WATER);
                if (entity.isInWater())
                {
                    pokemob.eat(berry);
                    setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                    return;
                }
                if (temp != null)
                {
                    block = true;
                    foodLoc.set(temp);
                }
            }
            if (!block && pokemob.eatsBerries())
            {
                if (pokemob.getPokemonAIState(IMoveConstants.TAMED))
                {
                    Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance, IBerryFruitBlock.class);
                    if (temp != null)
                    {
                        block = true;
                        foodLoc.set(temp);
                    }
                }
                else
                {
                    pokemob.setHungerCooldown(10);
                    toRun.add(new GenBerries(pokemob));
                    setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                }
            }
        }

        if (foodLoc.isEmpty())
        {
            pokemob.setHungerCooldown(10);
            setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
        }
    }

    // 0 is sunrise, 6000 noon, 12000 dusk, 18000 midnight, 23999
    public boolean isGoodSleepingSpot(ChunkCoordinate c)
    {
        if (pokemob.getHome() == null
                || (pokemob.getHome().getX() == 0 && pokemob.getHome().getY() == 0 & pokemob.getHome().getZ() == 0))
        {
            v1.set(entity);
            pokemob.setHome(v1.intX(), v1.intY(), v1.intZ(), 16);
        }
        if (pokemob.hasHomeArea() && entity.getPosition().distanceSq(pokemob.getHome()) > 9) return false;
        // TODO search for possible better place to sleep
        return true;
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
            rand = new Random(pokemob.getRNGValue());
            // Go find and eat the block
            double d = foodLoc.addTo(0.5, 0.5, 0.5).distToEntity(entity);
            foodLoc.addTo(-0.5, -0.5, -0.5);
            IBlockState b = foodLoc.getBlockState(world);
            if (b == null) return;
            if (b.getBlock() instanceof IBerryFruitBlock)
            {
                eatBerry(b, d);
            }
            if (PokecubeTerrainChecker.isPlant(b))
            {
                eatPlant(b, foodLoc, d);
            }
            else if ((PokecubeTerrainChecker.isRock(b)) && pokemob.isLithotroph())
            {
                eatRocks(b, foodLoc, d);
            }
        }
    }

    @Override
    public boolean shouldRun()
    {
        world = TickHandler.getInstance().getWorldCache(entity.dimension);
        boolean ownedSleepCheck = pokemob.getPokemonAIState(IMoveConstants.TAMED)
                && !(pokemob.getPokemonAIState(IMoveConstants.STAYING));
        if (ownedSleepCheck)
        {
            pokemob.setPokemonAIState(IMoveConstants.SLEEPING, false);
        }
        if (world == null || entity.getAttackTarget() != null) return false;
        pokemob.setHungerCooldown(pokemob.getHungerCooldown() - 1);
        pokemob.setHungerTime(pokemob.getHungerTime() + 1);
        boolean hunting = pokemob.getPokemonAIState(IMoveConstants.HUNTING);
        if (pokemob.getPokemonAIState(IMoveConstants.SLEEPING) || !hunting || pokemob.neverHungry())
        {
            if (pokemob.neverHungry()) pokemob.setHungerTime(0);
            if (hunting) setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
            return false;
        }
        if (foodLoc.distToEntity(entity) > 32) foodLoc.clear();
        if (pokemob.getHungerCooldown() > 0) return false;
        if (!foodLoc.isEmpty()) return true;
        return hunting;
    }
}
