package pokecube.core.world.gen;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.fml.common.IWorldGenerator;
import pokecube.core.database.BiomeMatcher;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.world.gen.template.PokecubeTemplates;
import pokecube.core.world.gen.village.buildings.TemplateStructure;
import thut.api.maths.Vector3;

public class WorldGenTemplates implements IWorldGenerator
{
    public static class TemplateGen implements IWorldGenerator
    {
        protected final float        chance;
        protected final String       template;
        protected final int          offset;
        protected final BiomeMatcher matcher;
        protected final boolean[]    cornersDone = new boolean[4];

        protected TemplateStructure  building;

        public TemplateGen(String template, String matcher, float chance, int offset)
        {
            this.chance = chance;
            this.template = template;
            this.offset = offset;
            this.matcher = new BiomeMatcher(matcher);
        }

        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
                IChunkProvider chunkProvider)
        {
            if (building == null && chance < random.nextFloat()) return;
            build(random, chunkX, chunkZ, world);
        }

        protected void getTemplate(Random random, int chunkX, int chunkZ, World world, StructureBoundingBox chunkBox)
        {
            int rX = random.nextInt(20);
            int rZ = random.nextInt(20);
            int x = ((rX) % 16) + chunkX * 16;
            int y = 255;
            int z = ((rZ) % 16) + chunkZ * 16;
            BlockPos pos = new BlockPos(x, y, z);
            if (!matcher.matches(Vector3.getNewVector().set(pos), world)) return;
            EnumFacing dir = EnumFacing.HORIZONTALS[random.nextInt(EnumFacing.HORIZONTALS.length)];
            building = new TemplateStructure(template, pos, dir);
            if (!building.getBoundingBox().intersectsWith(chunkBox))
            {
                building = null;
                return;
            }
            building.offset = offset;
        }

        protected void buildTemplate(Random random, int chunkX, int chunkZ, World world, StructureBoundingBox chunkBox)
        {
            int i, j;
            StructureBoundingBox buildingBox = building.getBoundingBox();
            for (int x = buildingBox.minX >> 4; x <= buildingBox.maxX >> 4; x++)
                for (int z = buildingBox.minZ >> 4; z <= buildingBox.maxZ >> 4; z++)
                {
                    i = x << 4;
                    j = z << 4;
                    if (building == null || !world.isBlockLoaded(new BlockPos(i, 1, j))) continue;
                    chunkBox = new StructureBoundingBox(i, 0, j, i + 16, 255, j + 16);
                    building.addComponentParts(world, random, chunkBox);
                    if (isDone(buildingBox, chunkBox))
                    {
                        building = null;
                        for (int k = 0; k < 4; k++)
                            cornersDone[k] = false;
                        return;
                    }
                }
        }

        protected void build(Random random, int chunkX, int chunkZ, World world)
        {
            int i = chunkX << 4;
            int j = chunkZ << 4;
            StructureBoundingBox chunkBox = new StructureBoundingBox(i, 0, j, i + 16, 255, j + 16);
            if (building == null)
            {
                getTemplate(random, chunkX, chunkZ, world, chunkBox);
            }
            if (building != null)
            {
                buildTemplate(random, chunkX, chunkZ, world, chunkBox);
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
        public TemplateGenStartBuilding()
        {
            super(PokecubeTemplates.POKECENTER, "BTall", 1, -2);
        }

        protected void getTemplate(Random random, int chunkX, int chunkZ, World world, StructureBoundingBox chunkBox)
        {
            building = null;
            if (!PokecubeMod.core.getConfig().doSpawnBuilding || world.provider.getDimension() != 0) return;
            int x = world.getSpawnPoint().getX() / 16;
            int z = world.getSpawnPoint().getZ() / 16;
            if (x != chunkX || z != chunkZ) return;
            super.getTemplate(random, chunkX, chunkZ, world, chunkBox);
        }
    }

    public static List<TemplateGen> templates = Lists.newArrayList();

    static
    {
        templates.add(new TemplateGenStartBuilding());
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider)
    {
        for (TemplateGen gen : templates)
            gen.generate(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
    }
}
