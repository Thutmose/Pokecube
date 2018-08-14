package pokecube.core.moves.implementations.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pokecube.core.events.handlers.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;

public class ActionNaturePower implements IMoveAction
{
    /** Implementers of this interface must have a public constructor that takes
     * no arguments. */
    public abstract interface IBiomeChanger
    {
        /** This method should check whether it should apply a biome change, and
         * if it should, it should do so, then return true. It should return
         * false if it does not change anything. Only the first of these to
         * return true will be used, so if you need to re-order things, reorder
         * ActionNaturePower.changer_classes accordingly. */
        public boolean apply(BlockPos pos, World world);
    }

    public static final List<Class<? extends IBiomeChanger>> changer_classes = Lists.newArrayList();

    public static boolean applyChecker(PointChecker checker, World world, Biome biome)
    {
        // Check if > 1 as it will always at least contain the center.
        if (checker.blocks.size() > 1)
        {
            Set<Chunk> affected = Sets.newHashSet();
            WorldServer sWorld = (WorldServer) world;
            PlayerChunkMap chunkMap = sWorld.getPlayerChunkMap();
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            // Apply the biome to all the locations.
            for (Vector3 loc : checker.blocks)
            {
                loc.setBiome(biome, world);
                affected.add(world.getChunkFromBlockCoords(loc.getPos()));
                sWorld.getPlayerChunkMap().markBlockForUpdate(loc.getPos());
                minY = Math.min(minY, loc.intY() / 16);
                maxY = Math.max(maxY, loc.intY() / 16);
            }
            updateChunks(chunkMap, affected, minY - 1, maxY + 1);
            return true;
        }
        return false;
    }

    public static void updateChunks(PlayerChunkMap chunkMap, Set<Chunk> affected, int minY, int maxY)
    {
        // Send updates about the chunk having changed. If this is not done,
        // the player will need to leave area and return to see the changes
        // on their end.
        for (Chunk chunk : affected)
        {
            PlayerChunkMapEntry entry = chunkMap.getEntry(chunk.x, chunk.z);
            if (entry != null)
            {
                // Reflection is fine here, as not too many chunks should be
                // affected by a single use of naturepower.
                ReflectionHelper.setPrivateValue(PlayerChunkMapEntry.class, entry, false, "sentToPlayers",
                        "field_187290_j", "j");
                entry.sendToPlayers();
                ClassInheritanceMultiMap<Entity>[] entityLists = chunk.getEntityLists();
                // Ensure range is correct.
                minY = Math.max(0, minY);
                maxY = Math.min(entityLists.length - 1, maxY);
                for (int y = minY; y <= maxY; y++)
                {
                    // Iterate over the mobs here, and send updates to clients.
                    // This is needed, as somehow the entry.sendToPlayers();
                    // above removes the entities from the client side mob
                    // lists. This is the simplest way I found to re-add them to
                    // those lists.
                    ClassInheritanceMultiMap<Entity> e = entityLists[y];
                    Iterator<Entity> iter = e.iterator();
                    while (iter.hasNext())
                    {
                        Entity mob = iter.next();
                        PacketHandler.sendEntityUpdate(mob);
                    }
                }
            }
        }
    }

    /** Very basic tree finder, it finds all connected blocks that match the
     * validCheck predicate. */
    public static class PointChecker
    {
        World                     world;
        Vector3                   centre;
        // we use lists here for faster iteration, sets are faster lookups for
        // contains, but lists iterate more GC friendly.
        List<Vector3>             blocks  = new LinkedList<Vector3>();
        List<Vector3>             checked = new LinkedList<Vector3>();
        List<IBlockState>         states  = Lists.newArrayList();
        final Predicate<BlockPos> validCheck;
        boolean                   yaxis   = false;
        int                       maxRSq  = 8 * 8;

        public PointChecker(World world, Vector3 pos, Predicate<BlockPos> validator)
        {
            this.world = world;
            centre = pos;
            this.validCheck = validator;
        }

        public void clear()
        {
            blocks.clear();
            checked.clear();
        }

        public void checkPoints()
        {
            populateList(centre);
        }

        private boolean nextPoint(Vector3 prev, List<Vector3> tempList)
        {
            boolean ret = false;
            Vector3 temp = Vector3.getNewVector();
            // Check the connected blocks, see if they match predicate, if they
            // do, add them to the list. This also checks diagonally connected
            // blocks.
            for (int i = -1; i <= 1; i++)
                for (int j = -1; j <= 1; j++)
                {
                    // If yaxis, also check vertical connections, for
                    // naturepower, we usually only care about horizontal.
                    if (yaxis)
                    {
                        for (int k = -1; k <= 1; k++)
                        {
                            temp.set(prev).addTo(i, k, j);
                            if (validCheck.test(temp.getPos()))
                            {
                                if (temp.distToSq(centre) <= maxRSq)
                                {
                                    tempList.add(temp.copy());
                                    states.add(temp.getBlockState(world));
                                    ret = true;
                                }
                            }
                        }

                    }
                    // Otherwise just check horizontal connections.
                    else
                    {
                        temp.set(prev).addTo(i, 0, j);
                        if (validCheck.test(temp.getPos()))
                        {
                            if (temp.distToSq(centre) <= maxRSq)
                            {
                                tempList.add(temp.copy());
                                states.add(temp.getBlockState(world));
                                ret = true;
                            }
                        }
                    }
                }
            checked.add(prev);
            return ret;
        }

        private void populateList(Vector3 base)
        {
            // Add the initial block.
            blocks.add(base);
            // Loop untill no new blocks have been added.
            while (checked.size() < blocks.size())
            {
                List<Vector3> toAdd = new ArrayList<Vector3>();
                // Add all connecting blocks that match, unless they have
                // already been checked.
                for (Vector3 v : blocks)
                {
                    if (!checked.contains(v))
                    {
                        nextPoint(v, toAdd);
                    }
                }
                // Add any blocks that are new to the list.
                for (Vector3 v : toAdd)
                {
                    if (!blocks.contains(v)) blocks.add(v);
                }
            }
        }
    }

    public static class ForestChanger implements IBiomeChanger
    {
        static final Biome FOREST = Biomes.FOREST;

        public ForestChanger()
        {
        }

        @Override
        public boolean apply(BlockPos pos, World world)
        {
            // This is the predicate we will use for checking whether something
            // is a valid spot.
            Predicate<BlockPos> predicate = new Predicate<BlockPos>()
            {
                @Override
                public boolean test(BlockPos t)
                {
                    IBlockState stateHere = world.getBlockState(t);
                    IBlockState stateUp = world.getBlockState(t.up());
                    Block blockHere = stateHere.getBlock();
                    Block blockUp = stateUp.getBlock();

                    // If already forest biome, this isn't valid, so
                    // we can return false.
                    if (world.getBiome(t) == FOREST) return false;
                    // Only valid surface blocks are dirt and grass
                    // for this.
                    boolean validHere = (blockHere == Blocks.GRASS || blockHere == Blocks.DIRT);
                    // If it is dirt, it must be under a tree,
                    // otherwise it can be under air or a plant.
                    boolean validUp = blockHere == Blocks.DIRT ? PokecubeTerrainChecker.isWood(stateUp)
                            : blockUp.isAir(stateUp, world, t.up()) || PokecubeTerrainChecker.isPlant(stateUp);
                    return validHere && validUp;
                }
            };
            // Used on a tree, spreads outwards from tree along dirt and grass
            // blocks, and converts the area to forest.
            IBlockState state = world.getBlockState(pos);
            IBlockState below = world.getBlockState(pos.down());

            // Has to be wood on dirt, ie at least originally a tree.
            if (state.getBlock().isWood(world, pos) && below.getBlock() == Blocks.DIRT)
            {
                PointChecker checker = new PointChecker(world, Vector3.getNewVector().set(pos.down()), predicate);
                checker.checkPoints();
                return applyChecker(checker, world, FOREST);
            }
            return false;
        }
    }

    public static class PlainsChanger implements IBiomeChanger
    {
        static final Biome PLAINS = Biomes.PLAINS;

        public PlainsChanger()
        {
        }

        @Override
        public boolean apply(BlockPos pos, World world)
        {
            // This is the predicate we will use for checking whether something
            // is a valid spot.
            Predicate<BlockPos> predicate = new Predicate<BlockPos>()
            {
                @Override
                public boolean test(BlockPos t)
                {
                    IBlockState stateHere = world.getBlockState(t);
                    IBlockState stateUp = world.getBlockState(t.up());
                    Block blockHere = stateHere.getBlock();
                    // If already plains biome, this isn't valid, so
                    // we can return false.
                    if (world.getBiome(t) == PLAINS) return false;
                    // Only valid surface blocks are grass
                    // for this.
                    boolean validHere = (blockHere instanceof BlockGrass);
                    // Only counts as plains if it has plants on grass, so say
                    // flowers, tall grass, etc
                    boolean validUp = PokecubeTerrainChecker.isPlant(stateUp);
                    return validHere && validUp;
                }
            };
            // Used on a grass, spreads sideways and only converts blocks that
            // have plants on top of the grass.
            IBlockState state = world.getBlockState(pos);

            // Has to be used on grass.
            if (state.getBlock() instanceof BlockGrass)
            {
                PointChecker checker = new PointChecker(world, Vector3.getNewVector().set(pos), predicate);
                checker.checkPoints();
                return applyChecker(checker, world, PLAINS);
            }
            return false;
        }
    }

    public static class DesertChanger implements IBiomeChanger
    {
        static final Biome DESERT = Biomes.DESERT;

        public DesertChanger()
        {
        }

        @Override
        public boolean apply(BlockPos pos, World world)
        {
            // This is the predicate we will use for checking whether something
            // is a valid spot.
            Predicate<BlockPos> predicate = new Predicate<BlockPos>()
            {
                @Override
                public boolean test(BlockPos t)
                {
                    IBlockState stateHere = world.getBlockState(t);
                    IBlockState stateUp = world.getBlockState(t.up());
                    Block blockHere = stateHere.getBlock();
                    Block blockUp = stateUp.getBlock();
                    // If already desert biome, this isn't valid, so
                    // we can return false.
                    if (world.getBiome(t) == DESERT) return false;
                    // Only valid surface blocks are sand
                    boolean validHere = (blockHere == Blocks.SAND);
                    // Only counts as desert if air or cactus on top
                    boolean validUp = blockUp.isAir(stateUp, world, t.up()) || blockUp instanceof BlockCactus;
                    return validHere && validUp;
                }
            };
            // Used on a sand block, will only apply and return true if there is
            // some cactus found though.
            IBlockState state = world.getBlockState(pos);

            // Has to be used on sand
            if (state.getBlock() == Blocks.SAND)
            {
                PointChecker checker = new PointChecker(world, Vector3.getNewVector().set(pos.down()), predicate);
                checker.checkPoints();
                // Check if any cactus is found, will only allow this change if
                // at least 1 is found.
                boolean cactus = false;
                for (IBlockState found : checker.states)
                {
                    cactus = found.getBlock() instanceof BlockCactus;
                    if (cactus) break;
                }
                if (!cactus) return false;
                return applyChecker(checker, world, DESERT);
            }
            return false;
        }
    }

    public static class HillsChanger implements IBiomeChanger
    {
        final Biome HILLS = Biomes.EXTREME_HILLS;

        public HillsChanger()
        {
        }

        @Override
        public boolean apply(BlockPos pos, World world)
        {
            // Ensure that this is actually a "high" spot.
            if (pos.getY() < world.getActualHeight() / 2) return false;

            // This is the predicate we will use for checking whether something
            // is a valid spot.
            Predicate<BlockPos> predicate = new Predicate<BlockPos>()
            {
                @Override
                public boolean test(BlockPos t)
                {
                    IBlockState stateHere = world.getBlockState(t);
                    Block blockHere = stateHere.getBlock();
                    // If already hills biome, this isn't valid, so
                    // we can return false.
                    if (world.getBiome(t) == HILLS) return false;
                    // Only valid surface blocks are stone
                    boolean validHere = (blockHere == Blocks.STONE);
                    // Block must be the surface
                    boolean validUp = world.getTopSolidOrLiquidBlock(t).getY() <= t.getY();
                    return validHere && validUp;
                }
            };
            // Used on a stone, spreads sideways
            IBlockState state = world.getBlockState(pos);

            // Has to be used on stone.
            if (state.getBlock() == Blocks.STONE)
            {
                PointChecker checker = new PointChecker(world, Vector3.getNewVector().set(pos), predicate);
                checker.checkPoints();
                return applyChecker(checker, world, HILLS);
            }
            return false;
        }
    }

    /** This class will reset the biomes back to whatever worldgen says they
     * should be, it goes out 8 blocks, and checks what the biome is, what it
     * should be, and sets it back. It must be used on a diamond block. */
    public static class ResetChanger implements IBiomeChanger
    {
        public ResetChanger()
        {
        }

        @Override
        public boolean apply(BlockPos pos, World world)
        {
            if (world.getBlockState(pos).getBlock() != Blocks.DIAMOND_BLOCK) return false;
            boolean mod = false;
            Set<Chunk> affected = Sets.newHashSet();
            for (int i = -8; i <= 8; i++)
            {
                for (int j = -8; j <= 8; j++)
                {
                    BlockPos temp = pos.add(i, 0, j);
                    Biome here = world.getBiome(temp);
                    Biome natural = world.getBiomeProvider().getBiome(temp);
                    if (natural != here)
                    {
                        Vector3.getNewVector().set(temp).setBiome(natural, world);
                        affected.add(world.getChunkFromBlockCoords(temp));
                        mod = true;
                    }
                }
            }
            WorldServer sWorld = (WorldServer) world;
            PlayerChunkMap chunkMap = sWorld.getPlayerChunkMap();
            updateChunks(chunkMap, affected, 0, 16);
            return mod;
        }

    }

    static
    {
        changer_classes.add(ForestChanger.class);
        changer_classes.add(PlainsChanger.class);
        changer_classes.add(DesertChanger.class);
        changer_classes.add(HillsChanger.class);
        changer_classes.add(ResetChanger.class);
    }

    /** This is filled with new instances of whatever is in changer_classes. It
     * will have same ordering as changer_classes, and the first of these to
     * return true for a location is the only one that will be used. */
    private final List<IBiomeChanger> changers = Lists.newArrayList();

    public ActionNaturePower()
    {
    }

    @Override
    public boolean applyEffect(IPokemob attacker, Vector3 location)
    {
        if (attacker.getCombatState(CombatStates.ANGRY)) return false;
        if (!(attacker.getPokemonOwner() instanceof EntityPlayerMP)) return false;
        if (!MoveEventsHandler.canEffectBlock(attacker, location)) return false;
        long time = attacker.getEntity().getEntityData().getLong("lastAttackTick");
        if (time + (20 * 3) > attacker.getEntity().getEntityWorld().getTotalWorldTime()) return false;
        BlockPos pos = location.getPos();
        World world = attacker.getEntity().getEntityWorld();
        // Check the changers in order, and apply the first one that returns
        // true.
        for (IBiomeChanger changer : changers)
        {
            if (changer.apply(pos, world))
            {
                // TODO apply a potentially massive hunger cost here.
                return true;
            }
        }
        return false;
    }

    @Override
    public String getMoveName()
    {
        return "naturepower";
    }

    @Override
    public void init()
    {
        for (Class<? extends IBiomeChanger> clazz : changer_classes)
        {
            try
            {
                changers.add(clazz.newInstance());
            }
            catch (Exception e)
            {
                PokecubeMod.log(Level.SEVERE, "error with changer " + clazz, e);
            }
        }
    }
}
