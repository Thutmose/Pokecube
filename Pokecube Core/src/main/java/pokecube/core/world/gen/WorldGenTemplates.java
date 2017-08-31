package pokecube.core.world.gen;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.fml.common.IWorldGenerator;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.world.gen.template.PokecubeTemplates;
import pokecube.core.world.gen.village.buildings.TemplateStructure;
import thut.api.maths.Vector3;

public class WorldGenTemplates implements IWorldGenerator
{
    public static class TemplateGen implements IWorldGenerator
    {
        public float                   chance;
        public final String            template;
        public final int               offset;
        public final SpawnBiomeMatcher spawnRule;
        public final boolean[]         cornersDone = new boolean[4];
        public BlockPos                origin;
        public EnumFacing              dir;

        protected TemplateStructure    building;

        public TemplateGen(String template, SpawnBiomeMatcher matcher, float chance, int offset)
        {
            this.chance = chance;
            this.template = template;
            this.offset = offset;
            this.spawnRule = matcher;
        }

        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
                IChunkProvider chunkProvider)
        {
            if (building == null && chance < random.nextFloat()) return;
            if (origin == null)
            {
                int rX = random.nextInt(20) % 16 + chunkX * 16;
                int rZ = random.nextInt(20) % 16 + chunkZ * 16;
                origin = new BlockPos(rX, 255, rZ);
                dir = EnumFacing.HORIZONTALS[random.nextInt(EnumFacing.HORIZONTALS.length)];
            }
            build(origin, dir, random, chunkX, chunkZ, world);
        }

        protected void getTemplate(EnumFacing dir, BlockPos offset, int chunkX, int chunkZ, World world,
                StructureBoundingBox chunkBox)
        {
            if (building != null) return;
            if (!spawnRule.matches(new SpawnCheck(Vector3.getNewVector().set(offset), world))) return;
            building = new TemplateStructure(template, offset, dir);
            building.offset = this.offset;
            if (building.getBoundingBox() == null)
            {
                building = null;
                return;
            }
        }

        protected void buildTemplate(Random random, BlockPos offset, int chunkX, int chunkZ, World world,
                StructureBoundingBox chunkBox)
        {
            StructureBoundingBox buildingBox = building.getBoundingBox();
            building.addComponentParts(world, random, chunkBox);
            if (isDone(buildingBox, chunkBox))
            {
                origin = null;
                dir = null;
                building = null;
                for (int k = 0; k < 4; k++)
                    cornersDone[k] = false;
                return;
            }
        }

        protected void build(BlockPos offset, EnumFacing dir, Random random, int chunkX, int chunkZ, World world)
        {
            int i = chunkX << 4;
            int j = chunkZ << 4;
            StructureBoundingBox chunkBox = new StructureBoundingBox(i, 0, j, i + 15, 255, j + 15);
            if (building == null)
            {
                getTemplate(dir, offset, chunkX, chunkZ, world, chunkBox);
            }
            if (building != null)
            {
                buildTemplate(random, offset, chunkX, chunkZ, world, chunkBox);
            }
        }

        protected boolean isDone(StructureBoundingBox buildingBox, StructureBoundingBox chunkBox)
        {
            BlockPos negneg = new BlockPos(buildingBox.minX, 10, buildingBox.minZ);
            BlockPos negpos = new BlockPos(buildingBox.minX, 10, buildingBox.maxZ);
            BlockPos posneg = new BlockPos(buildingBox.maxX, 10, buildingBox.minZ);
            BlockPos pospos = new BlockPos(buildingBox.maxX, 10, buildingBox.maxZ);

            cornersDone[0] = cornersDone[0] || chunkBox.isVecInside(negneg);
            cornersDone[1] = cornersDone[1] || chunkBox.isVecInside(negpos);
            cornersDone[2] = cornersDone[2] || chunkBox.isVecInside(posneg);
            cornersDone[3] = cornersDone[3] || chunkBox.isVecInside(pospos);

            return cornersDone[0] && cornersDone[1] && cornersDone[2] && cornersDone[3];
        }
    }

    public static class TemplateGenStartBuilding extends TemplateGen
    {
        static TemplateGenStartBuilding instance = new TemplateGenStartBuilding();
        boolean                         done     = false;

        public static void clear()
        {
            instance.building = null;
            instance.done = false;
            instance.origin = null;
            instance.dir = null;
        }

        private static SpawnBiomeMatcher matcher()
        {
            SpawnRule rules = new SpawnRule();
            rules.values.put(SpawnBiomeMatcher.TYPES, "all");
            SpawnBiomeMatcher matcher = new SpawnBiomeMatcher(rules);
            return matcher;
        }

        public TemplateGenStartBuilding()
        {
            super(PokecubeTemplates.POKECENTER, matcher(), 1, -2);
        }

        protected void buildTemplate(Random random, BlockPos offset, int chunkX, int chunkZ, World world,
                StructureBoundingBox chunkBox)
        {
            StructureBoundingBox buildingBox = building.getBoundingBox();
            building.addComponentParts(world, random, chunkBox);
            if (isDone(buildingBox, chunkBox))
            {
                origin = null;
                dir = null;
                done = true;
                for (int k = 0; k < 4; k++)
                    cornersDone[k] = false;
                return;
            }
        }

        protected void build(BlockPos offset, EnumFacing dir, Random random, int chunkX, int chunkZ, World world)
        {
            if (done || !PokecubeMod.core.getConfig().doSpawnBuilding
                    || world.provider.getDimension() != PokecubeMod.core.getConfig().spawnDimension)
                return;
            int x = world.getSpawnPoint().getX() / 16;
            int z = world.getSpawnPoint().getZ() / 16;
            int rX = 4 + x * 16;
            int rZ = 4 + z * 16;
            offset = new BlockPos(rX, 255, rZ);
            int i = chunkX << 4;
            int j = chunkZ << 4;
            StructureBoundingBox chunkBox = new StructureBoundingBox(i, 0, j, i + 15, 255, j + 15);
            if (building == null)
            {
                getTemplate(dir, offset, chunkX, chunkZ, world, chunkBox);
            }
            if (building != null)
            {
                buildTemplate(random, offset, chunkX, chunkZ, world, chunkBox);
            }
        }
    }

    public static List<IWorldGenerator>        templates      = Lists.newArrayList();
    public static Map<String, IWorldGenerator> namedTemplates = Maps.newHashMap();

    static
    {
        templates.add(TemplateGenStartBuilding.instance);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider)
    {
        if (SpawnHandler.dimensionBlacklist.contains(world.provider.getDimension())) return;
        if (PokecubeMod.core.getConfig().whiteListEnabled
                && !SpawnHandler.dimensionWhitelist.contains(world.provider.getDimension()))
            return;
        for (IWorldGenerator gen : templates)
            gen.generate(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
    }
}
