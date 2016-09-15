package pokecube.core.events.handlers;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.events.MoveWorldAction;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;

public class MoveEventsHandler
{
    public static int WATERSTRONG    = 100;
    public static int FIRESTRONG     = 100;
    public static int ELECTRICSTRONG = 100;

    public static boolean doDefaultFire(IPokemob attacker, Move_Base move, Vector3 location)
    {
        World world = ((Entity) attacker).getEntityWorld();
        IBlockState state = location.getBlockState(world);
        Block block = state.getBlock();
        Vector3 nextBlock = Vector3.getNewVector().set(attacker).subtractFrom(location).reverse().norm()
                .addTo(location);
        IBlockState nextState = nextBlock.getBlockState(world);
        if (block == Blocks.OBSIDIAN)
        {
            location.setBlock(world, Blocks.LAVA);
            return true;
        }
        else if (block.isReplaceable(world, location.getPos()) && nextState.getBlock() == Blocks.OBSIDIAN)
        {
            nextBlock.setBlock(world, Blocks.LAVA);
            return true;
        }
        return false;
    }

    public static boolean doDefaultElectric(IPokemob attacker, Move_Base move, Vector3 location)
    {
        World world = ((Entity) attacker).getEntityWorld();
        IBlockState state = location.getBlockState(world);
        Block block = state.getBlock();
        Vector3 nextBlock = Vector3.getNewVector().set(attacker).subtractFrom(location).reverse().norm()
                .addTo(location);
        IBlockState nextState = nextBlock.getBlockState(world);
        if (block == Blocks.SAND)
        {
            location.setBlock(world, Blocks.GLASS);
            return true;
        }
        else if (block.isReplaceable(world, location.getPos()) && nextState.getBlock() == Blocks.SAND)
        {
            nextBlock.setBlock(world, Blocks.GLASS);
            return true;
        }
        return false;
    }

    public static boolean doDefaultIce(IPokemob attacker, Move_Base move, Vector3 location)
    {
        World world = ((Entity) attacker).getEntityWorld();
        BlockPos pos = location.getPos();
        IBlockState state = location.getBlockState(world);
        Block block = state.getBlock();
        if (block.isAir(state, world, location.getPos()))
        {
            if (location.offset(EnumFacing.DOWN).getBlockState(world).isNormalCube())
            {
                try
                {
                    world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState(), 2);
                    return true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else if (block == Blocks.WATER && state.getValue(BlockLiquid.LEVEL) == 0)
        {
            location.setBlock(world, Blocks.ICE.getDefaultState());
            return true;
        }
        else if (block.isReplaceable(world, pos))
        {
            if (location.offset(EnumFacing.DOWN).getBlockState(world).isNormalCube())
                location.setBlock(world, Blocks.SNOW_LAYER.getDefaultState());
            return true;
        }
        else if (world.isAirBlock(pos.up()) && state.isNormalCube())
        {
            world.setBlockState(pos.up(), Blocks.SNOW_LAYER.getDefaultState());
        }
        return false;
    }

    public static boolean doDefaultWater(IPokemob attacker, Move_Base move, Vector3 location)
    {
        World world = ((Entity) attacker).getEntityWorld();
        IBlockState state = location.getBlockState(world);
        Block block = state.getBlock();
        Vector3 nextBlock = Vector3.getNewVector().set(attacker).subtractFrom(location).reverse().norm()
                .addTo(location);
        IBlockState nextState = nextBlock.getBlockState(world);
        if (move.getPWR() >= WATERSTRONG)
        {
            if (block == Blocks.LAVA)
            {
                location.setBlock(world, Blocks.OBSIDIAN);
                return true;
            }
            else if (block.isReplaceable(world, location.getPos()) && nextState.getBlock() == Blocks.LAVA)
            {
                nextBlock.setBlock(world, Blocks.OBSIDIAN);
                return true;
            }
        }
        boolean done = false;
        if (nextState.getProperties().containsKey(BlockFarmland.MOISTURE))
        {
            nextBlock.setBlock(world, nextState.withProperty(BlockFarmland.MOISTURE, 7));
            done = true;
        }
        if (state.getProperties().containsKey(BlockFarmland.MOISTURE))
        {
            location.setBlock(world, state.withProperty(BlockFarmland.MOISTURE, 7));
            done = true;
        }
        return done;
    }

    private static class DefaultAction implements IMoveAction
    {
        Move_Base move;

        public DefaultAction(Move_Base move)
        {
            this.move = move;
        }

        @Override
        public boolean applyEffect(IPokemob attacker, Vector3 location)
        {
            if (attacker.getPokemonOwner() instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) attacker.getPokemonOwner();
                BreakEvent evt2 = new BreakEvent(player.getEntityWorld(), location.getPos(),
                        location.getBlockState(player.getEntityWorld()), player);
                MinecraftForge.EVENT_BUS.post(evt2);
                if (evt2.isCanceled()) return false;
            }
            if (move.getType(attacker) == PokeType.water) return doDefaultWater(attacker, move, location);
            if (move.getType(attacker) == PokeType.ice
                    && (move.move.attackCategory & IMoveConstants.CATEGORY_DISTANCE) > 0
                    && move.move.power > 0) { return doDefaultIce(attacker, move, location); }
            if (move.getType(attacker) == PokeType.electric && move.getPWR() >= ELECTRICSTRONG)
            {
                doDefaultElectric(attacker, move, location);
            }
            if (move.getType(attacker) == PokeType.fire
                    && move.getPWR() >= FIRESTRONG) { return doDefaultFire(attacker, move, location); }
            return false;
        }

        @Override
        public String getMoveName()
        {
            return move.name;
        }
    }

    private static MoveEventsHandler INSTANCE;

    public static MoveEventsHandler getInstance()
    {
        return INSTANCE == null ? INSTANCE = new MoveEventsHandler() : INSTANCE;
    }

    public static void register(IMoveAction move)
    {
        getInstance().actionMap.put(move.getMoveName(), move);
    }

    Map<String, IMoveAction> actionMap = Maps.newHashMap();

    private MoveEventsHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void doWorldAction(MoveWorldAction.OnAction evt)
    {
        IPokemob attacker = evt.getUser();
        Vector3 location = evt.getLocation();
        Move_Base move = evt.getMove();
        IMoveAction action = actionMap.get(move.name);
        if (action == null)
        {
            register(action = new DefaultAction(move));
        }
        action.applyEffect(attacker, location);
    }

}
