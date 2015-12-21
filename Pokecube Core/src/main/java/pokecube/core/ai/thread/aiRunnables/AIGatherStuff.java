package pokecube.core.ai.thread.aiRunnables;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.world.World;
import pokecube.core.interfaces.IBerryFruitBlock;
import pokecube.core.interfaces.IHungrymob;
import pokecube.core.interfaces.IPokemob;
import thut.api.TickHandler;
import thut.api.maths.Vector3;

public class AIGatherStuff extends AIBase
{
    public static int  COOLDOWN  = 200;
    final EntityLiving entity;
    final double       distance;
    IHungrymob         hungrymob;
    IPokemob           pokemob;
    boolean            block     = false;
    EntityItem         stuff     = null;
    Vector3            stuffLoc  = Vector3.getNewVectorFromPool();
    final boolean[]    states    = { false, false };
    final int[]        cooldowns = { 0, 0 };
    Vector3            seeking   = Vector3.getNewVectorFromPool();
    Vector3            v         = Vector3.getNewVectorFromPool();
    Vector3            v1        = Vector3.getNewVectorFromPool();

    public AIGatherStuff(EntityLiving entity, double distance)
    {
        this.entity = entity;
        this.hungrymob = (IHungrymob) entity;
        this.pokemob = (IPokemob) entity;
        this.distance = distance;
    }

    @Override
    public boolean shouldRun()
    {
        world = TickHandler.getInstance().getWorldCache(entity.dimension);
        if (world == null || pokemob.isAncient() || tameCheck()) return false;

        if (pokemob.getHome() == null) return false;

        if (stuffLoc.distToEntity(entity) > 32) stuffLoc.clear();
        if (cooldowns[0] > 0) return false;
        IInventory inventory = pokemob.getPokemobInventory();

        for (int i = 3; i < inventory.getSizeInventory() && !states[1]; i++)
        {
            states[0] = inventory.getStackInSlot(i) == null;
        }
        return states[0] && cooldowns[0] < 0;
    }

    /** Only tame pokemobs set to "stay" should run this AI.
     * 
     * @return */
    private boolean tameCheck()
    {
        return pokemob.getPokemonAIState(IPokemob.TAMED) && !pokemob.getPokemonAIState(IPokemob.STAYING);
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
    public void reset()
    {
        stuffLoc.clear();
        stuff = null;
    }

    @Override
    public void doMainThreadTick(World world)
    {
        super.doMainThreadTick(world);
        if (cooldowns[0]-- < 0 && !stuffLoc.isEmpty())
        {
            if (stuff != null)
            {
                double close = entity.width*entity.width;
                close = Math.max(close, 2);
                if (stuff.getDistanceToEntity(entity) < close)
                {
                    AIStoreStuff.addItemStackToInventory(stuff.getEntityItem(), pokemob.getPokemobInventory(), 2);
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

    private void findStuff()
    {
        if (pokemob.getPokemonAIState(IPokemob.TAMED) && pokemob.getPokemonAIState(IPokemob.SITTING)) { return; }

        block = false;
        v.set(hungrymob).add(0, entity.height, 0);

        List<Object> list = getEntitiesWithinDistance(entity, 16, EntityItem.class);
        EntityItem newTarget = null;
        double closest = 1000;

        for (Object o : list)
        {
            EntityItem e = (EntityItem) o;
            double dist = e.getDistanceSqToEntity(entity);
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
            Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance, IBerryFruitBlock.class);
            if (temp != null)
            {
                block = true;
                stuffLoc.set(temp);
            }
            if (temp != null) temp.freeVectorFromPool();
        }
//        if (!block && hungrymob.isHerbivore())
//        {
//            Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance, PokecubeItems.grasses);
//            if (temp != null)
//            {
//                block = true;
//                stuffLoc.set(temp);
//            }
//            if (temp != null) temp.freeVectorFromPool();
//        }
//        if (hungrymob.isLithotroph())
//        {
//            Block b = v.getBlock(world, EnumFacing.DOWN);
//            if (!Mod_Pokecube_Helper.getRocks().contains(b) || b == Blocks.gravel)
//            {
//                Vector3 temp = v.findClosestVisibleObject(world, true, (int) distance, Mod_Pokecube_Helper.getRocks());
//                if (temp != null)
//                {
//                    block = true;
//                    stuffLoc.set(temp);
//                }
//                if (temp != null) temp.freeVectorFromPool();
//                if (!stuffLoc.isEmpty()) return;
//            }
//            else
//            {
//                stuffLoc.set(v.set(hungrymob).offsetBy(EnumFacing.DOWN));
//                return;
//            }
//        }
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
                if (stuff != null)
                {
                    stuffLoc.set(stuff);
                    PathEntity path = entity.getNavigator().getPathToXYZ(stuffLoc.x, stuffLoc.y, stuffLoc.z);
                    addEntityPath(entity.getEntityId(), entity.dimension, path, entity.getAIMoveSpeed());
                }
                else
                {
                    PathEntity path = entity.getNavigator().getPathToXYZ(stuffLoc.x, stuffLoc.y, stuffLoc.z);
                    addEntityPath(entity.getEntityId(), entity.dimension, path, entity.getAIMoveSpeed());
                }
            }
        }
        else if(!stuffLoc.isEmpty())
        {
            double diff = 2;
            diff = Math.max(diff, entity.width);
            double dist = stuffLoc.distToEntity(entity);
            v.set(entity).subtractFrom(stuffLoc);
            double dot = v.normalize().dot(Vector3.secondAxis);
            if(dot<-0.9 && entity.onGround)
            {
                diff = Math.max(3, diff);
            }
            if (dist < diff)
            {
                setPokemobAIState(pokemob, IPokemob.HUNTING, false);
                Block plant = stuffLoc.getBlock(world);
                TickHandler.addBlockChange(stuffLoc, entity.dimension, Blocks.air);
                if (plant.getMaterial() != Material.grass)
                {
                    for (ItemStack stack : plant.getDrops(world, stuffLoc.getPos(), stuffLoc.getBlockState(world), 0))
                        toRun.addElement(new InventoryChange(entity, 2, stack, true));
                }
                stuffLoc.clear();
                addEntityPath(entity.getEntityId(), entity.dimension, null, 0);
            }
        }
    }
}
