package pokecube.core.ai.thread.aiRunnables.utility;

import java.util.List;
import java.util.Random;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.thread.aiRunnables.AIBase;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.TickHandler;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;
import thut.lib.ItemStackTools;

/** This IAIRunnable gets the mob to look for and collect dropped items and
 * berries. It requires an AIStoreStuff to have located a suitable storage
 * before it will run. */
public class AIGatherStuff extends AIBase
{
    public static int                           COOLDOWN     = 200;

    // Matcher used to determine if a block is a fruit or crop to be picked.
    private static final Predicate<IBlockState> berryMatcher = new Predicate<IBlockState>()
                                                             {
                                                                 @Override
                                                                 public boolean apply(IBlockState input)
                                                                 {
                                                                     return PokecubeTerrainChecker.isFruit(input);
                                                                 }
                                                             };

    private static class ReplantTask implements IRunnable
    {
        final int       entityID;
        final ItemStack seeds;
        final BlockPos  pos;

        public ReplantTask(Entity entity, ItemStack seeds, BlockPos pos)
        {
            this.seeds = seeds.copy();
            this.pos = new BlockPos(pos);
            this.entityID = entity.getEntityId();
        }

        @Override
        public boolean run(World world)
        {
            if (!CompatWrapper.isValid(seeds)) return true;
            if (seeds.getItem() instanceof IPlantable)
            {
                EntityPlayer player = PokecubeCore.getFakePlayer(world);
                player.setHeldItem(EnumHand.MAIN_HAND, seeds);
                seeds.getItem().onItemUse(player, world, pos.down(), EnumHand.MAIN_HAND, EnumFacing.UP, 0.5f, 1, 0.5f);
                Entity mob = world.getEntityByID(entityID);
                IPokemob pokemob;
                if (CompatWrapper.isValid(seeds) && ((pokemob = CapabilityPokemob.getPokemobFor(mob)) != null))
                {
                    if (!ItemStackTools.addItemStackToInventory(seeds, pokemob.getPokemobInventory(), 2))
                    {
                        mob.entityDropItem(seeds, 0);
                    }
                }
            }
            return true;
        }

    }

    final EntityLiving entity;
    final double       distance;
    IPokemob           pokemob;
    boolean            block           = false;
    EntityItem         stuff           = null;
    Vector3            stuffLoc        = Vector3.getNewVector();
    boolean            hasRoom         = true;
    int                collectCooldown = 0;
    final AIStoreStuff storage;
    Vector3            seeking         = Vector3.getNewVector();
    Vector3            v               = Vector3.getNewVector();
    Vector3            v1              = Vector3.getNewVector();

    public AIGatherStuff(IPokemob entity, double distance, AIStoreStuff storage)
    {
        this.entity = entity.getEntity();
        this.pokemob = entity;
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
            if (collectCooldown-- < 0 && !stuffLoc.isEmpty())
            {
                if (stuff != null)
                {
                    double close = entity.width * entity.width;
                    close = Math.max(close, 2);
                    if (stuff.getDistance(entity) < close)
                    {
                        ItemStackTools.addItemStackToInventory(stuff.getItem(), pokemob.getPokemobInventory(), 2);
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
        // Only mobs that are standing with homes should look for stuff.
        if (pokemob.getHome() == null || pokemob.getGeneralState(GeneralStates.TAMED)
                && pokemob.getLogicState(LogicStates.SITTING)) { return; }
        block = false;
        v.set(pokemob.getHome()).add(0, entity.height, 0);

        int distance = pokemob.getGeneralState(GeneralStates.TAMED) ? PokecubeMod.core.getConfig().tameGatherDistance
                : PokecubeMod.core.getConfig().wildGatherDistance;

        List<Entity> list = getEntitiesWithinDistance(entity, distance, EntityItem.class);
        EntityItem newTarget = null;
        double closest = 1000;

        // Check for items to possibly gather.
        for (Entity o : list)
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
        // Found an item, return.
        if (newTarget != null)
        {
            stuffLoc.set(newTarget);
            stuff = newTarget;
            return;
        }
        v.set(entity).addTo(0, entity.getEyeHeight(), 0);
        // check for berries to collect.
        if (!block && pokemob.eatsBerries())
        {
            Vector3 temp = v.findClosestVisibleObject(world, true, distance, berryMatcher);
            if (temp != null)
            {
                block = true;
                stuffLoc.set(temp);
            }
        }
        if (pokemob.isElectrotroph())
        {

        }
        // Nothing found, enter cooldown.
        if (stuffLoc.isEmpty())
        {
            collectCooldown = COOLDOWN;
        }
    }

    private void gatherStuff(boolean mainThread)
    {
        if (!mainThread)
        {
            // Set path to the stuff found.
            if (entity.getNavigator().noPath())
            {
                double speed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
                if (stuff != null)
                {
                    stuffLoc.set(stuff);
                    Path path = entity.getNavigator().getPathToXYZ(stuffLoc.x, stuffLoc.y, stuffLoc.z);
                    addEntityPath(entity, path, speed);
                }
                else
                {
                    Path path = entity.getNavigator().getPathToXYZ(stuffLoc.x, stuffLoc.y, stuffLoc.z);
                    addEntityPath(entity, path, speed);
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
                setCombatState(pokemob, CombatStates.HUNTING, false);
                IBlockState state = stuffLoc.getBlockState(entity.getEntityWorld());
                Block plant = stuffLoc.getBlock(entity.getEntityWorld());
                TickHandler.addBlockChange(stuffLoc, entity.dimension, Blocks.AIR);
                if (state.getMaterial() != Material.GRASS)
                {
                    NonNullList<ItemStack> list = NonNullList.create();
                    plant.getDrops(list, entity.getEntityWorld(), stuffLoc.getPos(), state, 0);
                    boolean replanted = false;
                    for (ItemStack stack : list)
                    {
                        if (stack.getItem() instanceof IPlantable && !replanted)
                        {
                            toRun.addElement(new ReplantTask(entity, stack.copy(), stuffLoc.getPos()));
                            replanted = true;
                        }
                        else toRun.addElement(new InventoryChange(entity, 2, stack.copy(), true));
                    }
                    if (!replanted)
                    {
                        // Try to find a seed in our inventory for this plant.
                        for (int i = 2; i < pokemob.getPokemobInventory().getSizeInventory(); i++)
                        {
                            ItemStack stack = pokemob.getPokemobInventory().getStackInSlot(i);
                            if (CompatWrapper.isValid(stack) && stack.getItem() instanceof IPlantable)
                            {
                                IPlantable plantable = (IPlantable) stack.getItem();
                                IBlockState plantState = plantable.getPlant(world, stuffLoc.getPos().up());
                                if (plantState.getBlock() == state.getBlock())
                                {
                                    toRun.addElement(new ReplantTask(entity, stack.copy(), stuffLoc.getPos()));
                                    replanted = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                stuffLoc.clear();
                addEntityPath(entity, null, 0);
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
        // Check if gather is enabled first.
        if (!pokemob.isRoutineEnabled(AIRoutine.GATHER)) return false;
        world = TickHandler.getInstance().getWorldCache(entity.dimension);
        boolean wildCheck = !PokecubeMod.core.getConfig().wildGather && !pokemob.getGeneralState(GeneralStates.TAMED);
        // Check if this should be doing something else instead, if so return
        // false.
        if (world == null || tameCheck() || entity.getAttackTarget() != null || wildCheck) return false;
        int rate = pokemob.getGeneralState(GeneralStates.TAMED) ? PokecubeMod.core.getConfig().tameGatherDelay
                : PokecubeMod.core.getConfig().wildGatherDelay;
        Random rand = new Random(pokemob.getRNGValue());
        // Check if it has a location, if so, apply a delay and return false if
        // not correct tick for this pokemob.
        if (pokemob.getHome() == null || entity.ticksExisted % rate != rand.nextInt(rate)) return false;
        // Apply cooldown.
        if (collectCooldown < -2000)
        {
            collectCooldown = COOLDOWN;
        }
        // If too far, clear location.
        if (stuffLoc.distToEntity(entity) > 32) stuffLoc.clear();
        // If on cooldown, return.
        if (collectCooldown > 0) return false;

        // check if pokemob has room in inventory for stuff, if so, return true.
        IInventory inventory = pokemob.getPokemobInventory();
        for (int i = 3; i < inventory.getSizeInventory(); i++)
        {
            hasRoom = !CompatWrapper.isValid(inventory.getStackInSlot(i));
            if (hasRoom) return true;
        }
        // Otherwise return false.
        return false;
    }

    /** Only tame pokemobs set to "stay" should run this AI.
     * 
     * @return */
    private boolean tameCheck()
    {
        return pokemob.getGeneralState(GeneralStates.TAMED)
                && (!pokemob.getGeneralState(GeneralStates.STAYING) || !PokecubeMod.core.getConfig().tameGather);
    }
}
