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
import pokecube.core.world.gen.WorldGenTemplates.TemplateGen;
import pokecube.core.world.gen.village.buildings.TemplateStructure;

public class WorldGenMultiTemplate implements IWorldGenerator
{
    public static class Template
    {
        public TemplateGen template;
        public BlockPos    offset;
        protected BlockPos relativeOffset;
        protected BlockPos size;
        public EnumFacing  dir;
        public int         averageGround = -1;
        boolean            setFloor      = false;
        boolean            done          = false;
    }

    public List<Template> subTemplates = Lists.newArrayList();
    public boolean        syncGround   = false;
    public BlockPos       origin;
    public EnumFacing     dir;

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider)
    {
        if (origin == null)
        {
            int rX = random.nextInt(20);
            int rZ = random.nextInt(20);
            int x = ((rX) % 16) + chunkX * 16;
            int y = 255;
            int z = ((rZ) % 16) + chunkZ * 16;
            origin = new BlockPos(x, y, z);
            dir = EnumFacing.HORIZONTALS[random.nextInt(EnumFacing.HORIZONTALS.length)];
        }
        BlockPos pos = origin;
        int average = -1;

        if (syncGround) for (Template template : subTemplates)
        {
            if (template.averageGround != -1)
            {
                average = template.averageGround;
                break;
            }
        }

        for (Template template : subTemplates)
        {
            if (template.done) continue;
            if (template.dir == null)
            {
                template.dir = dir;
            }
            else
            {
                dir = template.dir;
            }
            BlockPos offset = new BlockPos(0, 0, 0);
            BlockPos relativeOffset = template.relativeOffset;
            BlockPos size = template.size;
            if (dir == EnumFacing.SOUTH)
            {
                // Default
            }
            if (dir == EnumFacing.EAST)
            {
                relativeOffset = new BlockPos(-template.relativeOffset.getZ(), template.relativeOffset.getY(),
                        template.relativeOffset.getX());
                size = new BlockPos(0, template.size.getY(), template.size.getX());
            }
            if (dir == EnumFacing.NORTH)
            {
                relativeOffset = new BlockPos(-template.relativeOffset.getX(), template.relativeOffset.getY(),
                        -template.relativeOffset.getZ());
                size = new BlockPos(0, template.size.getY(), -template.size.getZ());
            }
            if (dir == EnumFacing.WEST)
            {
                relativeOffset = new BlockPos(template.relativeOffset.getZ(), template.relativeOffset.getY(),
                        -template.relativeOffset.getX());
                size = new BlockPos(template.size.getZ(), template.size.getY(), template.size.getX());
                size = offset;
            }
            offset = relativeOffset.add(pos).subtract(size);
            int i = chunkX << 4;
            int j = chunkZ << 4;
            StructureBoundingBox chunkBox = new StructureBoundingBox(i, 0, j, i + 15, 255, j + 15);
            template.template.getTemplate(dir, offset, chunkX, chunkZ, world, chunkBox);
            if (syncGround && average != -1 && template.template.building != null)
            {
                template.template.building.averageGroundLevel = average + template.offset.getY();
                syncTemplateGround(template.template.building);
            }
            build:
            if (template.template.building != null)
            {
                StructureBoundingBox buildingBox = template.template.building.getBoundingBox();
                if (!chunkBox.intersectsWith(template.template.building.getBoundingBox())) break build;
                template.template.building.addComponentParts(world, random, chunkBox);
                if (syncGround && template.averageGround == -1 && template.template.building != null)
                {
                    syncTemplateGround(template.template.building);
                    average = template.averageGround;
                }
                if (template.template.isDone(buildingBox, chunkBox))
                {
                    template.template.building = null;
                    for (int k = 0; k < 4; k++)
                        template.template.cornersDone[k] = false;
                    break build;
                }
            }
            if (template.template.building == null)
            {
                template.done = true;
            }
        }
        boolean done = true;
        for (Template template : subTemplates)
        {
            done &= template.done;
        }
        if (done)
        {
            origin = null;
            dir = null;
            for (Template template : subTemplates)
            {
                template.done = false;
                template.dir = null;
                template.setFloor = false;
                template.averageGround = -1;
            }
        }
    }

    public void init()
    {
        EnumFacing dir = EnumFacing.SOUTH;
        int minX, minZ;
        int maxX, maxZ;
        minX = minZ = Integer.MAX_VALUE;
        maxX = maxZ = Integer.MIN_VALUE;
        Map<Template, TemplateStructure> templates = Maps.newHashMap();
        for (Template template : subTemplates)
        {
            String temp = template.template.template;
            TemplateStructure building = new TemplateStructure(temp, template.offset, dir);
            minX = Integer.min(minX, building.getBoundingBox().minX);
            minZ = Integer.min(minZ, building.getBoundingBox().minZ);
            maxX = Integer.max(maxX, building.getBoundingBox().maxX);
            maxZ = Integer.max(maxZ, building.getBoundingBox().maxZ);
            templates.put(template, building);
        }
        BlockPos mid = new BlockPos((minX + maxX) / 2, 0, (minZ + maxZ) / 2);
        for (Template template : templates.keySet())
        {
            TemplateStructure structure = templates.get(template);
            BlockPos offset = structure.getBoundingBoxCenter();
            template.size = new BlockPos(structure.getBoundingBox().getXSize(), 0,
                    structure.getBoundingBox().getZSize());
            offset = offset.down(offset.getY());
            template.relativeOffset = offset.add(mid);
            System.out.println(template.template.template + " " + offset + " " + mid);
        }
        System.out.println(templates.size() + " " + subTemplates);
    }

    private void syncTemplateGround(TemplateStructure from)
    {
        int average = from.averageGroundLevel;
        for (Template template : subTemplates)
        {
            template.averageGround = average;
            TemplateStructure struct = template.template.building;
            if (struct != null && !template.setFloor)
            {
                template.setFloor = true;
                System.out.println(template.template.template + " " + average + " " + template.offset);
                struct.averageGroundLevel = average;
                Map<BlockPos, String> map = struct.template.getDataBlocks(new BlockPos(0, 0, 0), struct.placeSettings);
                for (BlockPos blockpos : map.keySet())
                {
                    String s = (String) map.get(blockpos);
                    if (s.equals("Floor"))
                    {
                        struct.setOffset(-blockpos.getY() + 1 + template.offset.getY());
                    }
                }
            }
        }
    }
}
