package pokecube.core.world.gen;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.fml.common.IWorldGenerator;
import pokecube.core.database.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.world.gen.template.PokecubeTemplate;
import pokecube.core.world.gen.template.PokecubeTemplates;
import pokecube.core.world.gen.village.buildings.TemplateStructure;
import thut.api.maths.Vector3;

public class WorldGenTemplates implements IWorldGenerator
{
    public static class TemplateGen implements IWorldGenerator
    {
        public static class TemplateSet
        {
            public final String template;
            public int          offset;
            public boolean      done = false;

            public TemplateSet(String template, int offset)
            {
                this.template = template;
                this.offset = offset;
            }

            public TemplateSet clone()
            {
                return new TemplateSet(template, offset);
            }

            public boolean[]            cornersDone   = new boolean[4];
            public BlockPos             origin;
            public EnumFacing           dir;
            protected TemplateStructure building;
            public String               biomeType     = null;

            /** Used by MultiTemplates */
            public int                  averageGround = -1;
            /** Used by MultiTemplates */
            public boolean              setFloor      = false;
            /** Used by MultiTemplates */
            public BlockPos             position      = BlockPos.ORIGIN;
            /** Used by MultiTemplates */
            public BlockPos             size;

            protected void build(BlockPos offset, EnumFacing dir, Random random, int chunkX, int chunkZ, World world)
            {
                if (done) return;
                getTemplate(dir, offset, chunkX, chunkZ, world);
                if (building != null) buildTemplate(random, chunkX, chunkZ, world);
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

            protected void getTemplate(EnumFacing dir, BlockPos offset, int chunkX, int chunkZ, World world)
            {
                if (building != null) return;
                building = new TemplateStructure(template, offset, dir);
                building.offset = this.offset;
                if (building.getTemplate() instanceof PokecubeTemplate)
                    ((PokecubeTemplate) building.getTemplate()).expectedBiomeType = biomeType;
                if (building.getBoundingBox() == null)
                {
                    building = null;
                    return;
                }
            }

            protected boolean buildTemplate(Random random, int chunkX, int chunkZ, World world)
            {
                if (done) return false;
                int i = chunkX * 16;
                int j = chunkZ * 16;
                StructureBoundingBox buildingBox = building.getBoundingBox();
                StructureBoundingBox chunkBox = new StructureBoundingBox(i, 0, j, i + 15, 255, j + 15);
                int dx = 1 + ((buildingBox.maxX - buildingBox.minX) >> 4);
                int dz = 1 + ((buildingBox.maxZ - buildingBox.minZ) >> 4);
                // TODO improve this to make sure all chunks in the given box
                // are actually generated.
                for (int x = chunkX - dx; x <= dx + chunkX; x++)
                    for (int z = chunkZ - dz; z <= dz + chunkZ; z++)
                    {
                        if (world.isChunkGeneratedAt(x, z))
                        {
                            i = x << 4;
                            j = z << 4;
                            chunkBox.expandTo(new StructureBoundingBox(i, 0, j, i + 15, 255, j + 15));
                        }
                    }
                if (!buildingBox.intersectsWith(chunkBox)) return false;
                building.addComponentParts(world, random, chunkBox);
                if (isDone(buildingBox, chunkBox))
                {
                    done = true;
                }
                return true;
            }

            public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
                    IChunkProvider chunkProvider)
            {
                if (done) return;
                build(origin, dir, random, chunkX, chunkZ, world);
            }
        }

        public float                     chance;
        public final String              template;
        public final int                 offset;
        public final SpawnBiomeMatcher   spawnRule;
        public BlockPos                  origin;
        public EnumFacing                dir;

        protected final Set<TemplateSet> templates = Sets.newHashSet();

        public TemplateGen(String template, SpawnBiomeMatcher matcher, float chance, int offset)
        {
            this.chance = chance;
            this.template = template;
            this.offset = offset;
            this.spawnRule = matcher;
        }

        public void setTemplate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
                IChunkProvider chunkProvider)
        {
            Iterator<TemplateSet> set = templates.iterator();
            while (set.hasNext())
            {
                if (set.next().done) set.remove();
            }
            if (random.nextFloat() > chance) return;
            int rX = random.nextInt(20) % 16 + chunkX * 16;
            int rZ = random.nextInt(20) % 16 + chunkZ * 16;
            BlockPos offset = new BlockPos(rX, world.getHeight(rX, rZ), rZ);
            if (!spawnRule.matches(new SpawnCheck(Vector3.getNewVector().set(offset), world))) return;
            offset = new BlockPos(rX, 255, rZ);
            EnumFacing dir = EnumFacing.HORIZONTALS[random.nextInt(EnumFacing.HORIZONTALS.length)];
            TemplateSet tSet = new TemplateSet(template, this.offset);
            tSet.dir = this.dir == null ? dir : this.dir;
            tSet.origin = this.origin == null ? offset : this.origin;
            this.templates.add(tSet);
        }

        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
                IChunkProvider chunkProvider)
        {
            if (!SpawnHandler.canSpawnInWorld(world)) return;
            setTemplate(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
            if (templates.isEmpty()) return;
            TemplateSet[] arr = templates.toArray(new TemplateSet[templates.size()]);
            for (TemplateSet set : arr)
                set.generate(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
        }
    }

    public static class TemplateGenStartBuilding extends TemplateGen
    {
        static TemplateGenStartBuilding instance = new TemplateGenStartBuilding();
        TemplateSet                     start    = null;
        boolean                         done     = false;

        public static void clear()
        {
            instance.done = false;
            instance.start = null;
            instance.templates.clear();
            for (IWorldGenerator gen : WorldGenTemplates.templates)
            {
                if (gen instanceof TemplateGen)
                {
                    ((TemplateGen) gen).templates.clear();
                }
                else if (gen instanceof WorldGenMultiTemplate)
                {
                    ((WorldGenMultiTemplate) gen).templates.clear();
                }
            }
            if (!WorldGenTemplates.templates.contains(instance)) WorldGenTemplates.templates.add(instance);
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

        public void setTemplate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
                IChunkProvider chunkProvider)
        {
            if (start != null && start.done)
            {
                done = true;
                start = null;
                templates.clear();
                PokecubeMod.log("Built Start Building.");
            }
            BlockPos spawn = world.getSpawnPoint();
            if (done || start != null || spawn.equals(BlockPos.ORIGIN)) return;
            else
            {
                start = new TemplateSet(PokecubeTemplates.POKECENTER, -2);
                start.origin = world.getSpawnPoint();
                start.dir = EnumFacing.HORIZONTALS[random.nextInt(EnumFacing.HORIZONTALS.length)];
                templates.add(start);
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
        if (!SpawnHandler.canSpawnInWorld(world)) return;
        for (IWorldGenerator gen : templates)
            gen.generate(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
    }
}
