package pokecube.core.ai.thread.aiRunnables;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.world.World;
import pokecube.core.interfaces.IBerryFruitBlock;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.TickHandler;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;
import thut.lib.ItemStackTools;

/** This IAIRunnable gets the mob to look for and collect dropped items and
 * berries. It requires an AIStoreStuff to have located a suitable storage
 * before it will run. */
public class AIGatherStuff extends AIBase
{
    public static int  COOLDOWN  = 200;
    final EntityLiving entity;
    final double       distance;
    IHungrymob         hungrymob;
    IPokemob           pokemob;
    boolean            block     = false;
    EntityItem         stuff     = null;
    Vector3            stuffLoc  = Vector3.getNewVector();
    final boolean[]    states    = { false, false };
    final int[]        cooldowns = { 0, 0 };
    final AIStoreStuff storage;
    Vector3            seeking   = Vector3.getNewVector();
    Vector3            v         = Vector3.getNewVector();
    Vector3            v1        = Vector3.getNewVector();

    public AIGatherStuff(EntityLiving entity, double distance, AIStoreStuff storage)
    {
        this.entity = entity;
        this.hungrymob = (IHungrymob) entity;
        this.pokemob = (IPokemob) entity;
        this.distance = distance;
        this.storage = storage;
        this.setMutex(1);
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);
        synchronized (stuffLoc)
        {
            if (cooldowns[0]-- < 0 && !stuffLoc.isEmpty())
            {
                if (stuff != null)
                {
                    double close = entity.width * entity.width;
                    close = Math.max(close, 2);
                    if (stuff.getDistanceToEntity(entity) < close)
                    {
                        ItemStackTools.addItemStackToInventory(stuff.getEntityItem(), pokemob.getPokemobInventory(), 2);
                        stuff.setDead();
                        reset();
                    }
                }
                else
                {
                    gatherStuff(true);
                }
            }
        }
    }

    private void findStuff()
    {
        if (pokemob.getHome() == null || pokemob.getPokemonAIState(IMoveConstants.TAMED)
                && pokemob.getPokemonAIState(IMoveConstants.SITTING)) { return; }
        block = false;
        v.set(pokemob.getHome()).add(0, entity.height, 0);

        int distance = pokemob.getPokemonAIState(IMoveConstants.TAMED) ? PokecubeMod.core.getConfig().tameGatherDistance
                : PokecubeMod.core.getConfig().wildGatherDistance;

        List<Object> list = getEntitiesWithinDistance(entity, distance, EntityItem.class);
        EntityItem newTarget = null;
        double closest = 1000;

        for (Object o : list)
        {
            EntityItem e = (EntityItem) o;
            double dist = e.getDistanceSqToCenter(pokemob.getHome());
            v.set(e);
            if (dist < closest && Vector3.isVisibleEntityFromEntity(entity, e))
            {
                closest = dist;
                newTarget = e;
            }
        }
        if (newTarget != null)
        {
            stuffLoc.set(newTarget);
            stuff = newTarget;
            return;
        }
        v.set(entity).addTo(0, entity.getEyeHeight(), 0);
        if (!block && hungrymob.eatsBerries())
        {
            Vector3 temp = v.findClosestVisibleObject(world, true, distance, IBerryFruitBlock.class);
            if (temp != null)
            {
                block = true;
                stuffLoc.set(temp);
            }
        }
        if (hungrymob.isElectrotroph())
        {

        }

        if (stuffLoc.isEmpty())
        {
            cooldowns[0] = COOLDOWN;
        }
    }

    private void gatherStuff(boolean mainThread)
    {
        if (!mainThread)
        {
            if (entity.getNavigator().noPath())
            {
                double speed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
                if (stuff != null)
                {
                    stuffLoc.set(stuff);
                    Path path = entity.getNavigator().getPathToXYZ(stuffLoc.x, stuffLoc.y, stuffLoc.z);
                    addEntityPath(entity.getEntityId(), entity.dimension, path, speed);
                }
                else
                {
                    Path path = entity.getNavigator().getPathToXYZ(stuffLoc.x, stuffLoc.y, stuffLoc.z);
                    addEntityPath(entity.getEntityId(), entity.dimension, path, speed);
                }
            }
        }
        else if (!stuffLoc.isEmpty())
        {
            double diff = 3;
            diff = Math.max(diff, entity.width);
            double dist = stuffLoc.distToEntity(entity);
            v.set(entity).subtractFrom(stuffLoc);
            double dot = v.normalize().dot(Vector3.secondAxis);
            if (dot < -0.9 && entity.onGround)
            {
                diff = Math.max(3, diff);
            }
            if (dist < diff)
            {
                setPokemobAIState(pokemob, IMoveConstants.HUNTING, false);
                IBlockState state = stuffLoc.getBlockState(world);
                Block plant = stuffLoc.getBlock(world);
                TickHandler.addBlockChange(stuffLoc, entity.dimension, Blocks.AIR);
                if (state.getMaterial() != Material.GRASS)
                {
                    for (ItemStack stack : plant.getDrops(world, stuffLoc.getPos(), stuffLoc.getBlockState(world), 0))
                        toRun.addElement(new InventoryChange(entity, 2, stack, true));
                }
                stuffLoc.clear();
                addEntityPath(entity.getEntityId(), entity.dimension, null, 0);
            }
        }
    }

    @Override
    public void reset()
    {
        stuffLoc.clear();
        stuff = null;
    }

    @Override
    public void run()
    {
        if (stuffLoc.isEmpty())
        {
            findStuff();
        }
        if (!stuffLoc.isEmpty())
        {
            gatherStuff(false);
        }
    }

    @Override
    public boolean shouldRun()
    {
        world = TickHandler.getInstance().getWorldCache(entity.dimension);
        boolean wildCheck = !PokecubeMod.core.getConfig().wildGather
                && !pokemob.getPokemonAIState(IMoveConstants.TAMED);
        if (world == null || pokemob.isAncient() || tameCheck() || entity.getAttackTarget() != null || wildCheck)
            return false;
        int rate = pokemob.getPokemonAIState(IMoveConstants.TAMED) ? PokecubeMod.core.getConfig().tameGatherDelay
                : PokecubeMod.core.getConfig().wildGatherDelay;
        Random rand = new Random(pokemob.getRNGValue());
        if (pokemob.getHome() == null || entity.ticksExisted % rate != rand.nextInt(rate)) return false;
        if (cooldowns[0] < -2000)
        {
            cooldowns[0] = COOLDOWN;
        }
        if (stuffLoc.distToEntity(entity) > 32) stuffLoc.clear();
        if (cooldowns[0] > 0) return false;
        IInventory inventory = pokemob.getPokemobInventory();
        for (int i = 3; i < inventory.getSizeInventory() && !states[1]; i++)
        {
            states[0] = !CompatWrapper.isValid(inventory.getStackInSlot(i));
        }
        return states[0];
    }

    /** Only tame pokemobs set to "stay" should run this AI.
     * 
     * @return */
    private boolean tameCheck()
    {
        return pokemob.getPokemonAIState(IMoveConstants.TAMED)
                && (!pokemob.getPokemonAIState(IMoveConstants.STAYING) || !PokecubeMod.core.getConfig().tameGather);
    }
}
