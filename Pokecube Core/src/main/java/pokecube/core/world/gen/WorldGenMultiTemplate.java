package pokecube.core.world.gen;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.world.gen.WorldGenTemplates.TemplateGen.TemplateSet;
import pokecube.core.world.gen.template.PokecubeTemplate;
import pokecube.core.world.gen.template.PokecubeTemplates;
import pokecube.core.world.gen.village.buildings.TemplateStructure;
import thut.api.maths.Vector3;

public class WorldGenMultiTemplate implements IWorldGenerator
{
    public static class MultiTemplateSet
    {
        List<TemplateSet> templates  = Lists.newArrayList();
        public boolean    syncGround = false;
        public BlockPos   origin;
        public EnumFacing dir;
        public boolean    done       = false;
        private boolean   made       = false;

        protected void getTemplate(EnumFacing dir, int chunkX, int chunkZ, World world)
        {
            if (made) return;
            made = true;
            /** Filter templates by "floor", which is the y value of the
             * set.position */
            Map<Integer, Set<TemplateSet>> floors = Maps.newHashMap();
            Map<TemplateSet, PokecubeTemplate> templatesMap = Maps.newHashMap();
            List<Integer> floorList = Lists.newArrayList();
            Map<Integer, Integer> floorHeights = Maps.newHashMap();
            for (TemplateSet set : templates)
            {
                PokecubeTemplate template = PokecubeTemplates.getTemplate(set.template);
                templatesMap.put(set, template);
                Integer floor = set.position.getY();
                Set<TemplateSet> thisFloor = floors.get(floor);
                if (thisFloor == null)
                {
                    floors.put(floor, thisFloor = Sets.newHashSet());
                    floorList.add(floor);
                }
                thisFloor.add(set);
            }
            Collections.sort(floorList);
            /** Deal with vertical offsets of the templates, starting at 1 */
            for (int i = 1; i < floorList.size(); i++)
            {
                Set<TemplateSet> prevSet = floors.get(floorList.get(i - 1));
                int prevHeight = 0;
                for (TemplateSet set : prevSet)
                {
                    PokecubeTemplate template = templatesMap.get(set);
                    prevHeight = Math.max(prevHeight, template.size.getY());
                }
                int var = prevHeight;
                if (i > 1)
                {
                    var += floorHeights.get(i - 1);
                }
                floorHeights.put(i, var);
            }

            /** Deal with horizonal offsets of the templates. */
            for (TemplateSet set : templates)
            {
                if (set.building != null) continue;
                int dy = 0;
                if (floorHeights.containsKey(set.position.getY()))
                {
                    dy = floorHeights.get(set.position.getY());
                }
                BlockPos posOffset = new BlockPos(0, 0, 0);
                PokecubeTemplate template = templatesMap.get(set);
                set.size = template.size;

                int dx = (int) (set.position.getX() * set.size.getX() / 2d);
                int dz = (int) (set.position.getZ() * set.size.getZ() / 2d);

                if (dir == EnumFacing.SOUTH)
                {
                    posOffset = new BlockPos(dx, 0, dz);
                }
                if (dir == EnumFacing.EAST)
                {
                    posOffset = new BlockPos(-dz, 0, dx);
                }
                if (dir == EnumFacing.NORTH)
                {
                    posOffset = new BlockPos(-dx, 0, -dz);
                }
                if (dir == EnumFacing.WEST)
                {
                    posOffset = new BlockPos(dz, 0, -dx);
                }
                System.out.println(posOffset + " " + set.template);
                set.getTemplate(set.dir, origin.add(posOffset), chunkX, chunkZ, world);
                set.building.setOffset(set.offset + dy);
                set.building.offset(posOffset.getX(), 0, posOffset.getZ());
            }
        }

        protected void buildTemplate(Random random, int chunkX, int chunkZ, World world)
        {
            int average = -1;
            for (TemplateSet template : templates)
            {
                if (template.building.averageGroundLevel != -1)
                {
                    average = template.building.averageGroundLevel;
                    break;
                }
            }
            for (TemplateSet template : templates)
            {
                if (template.building == null) template.done = true;
                if (template.done) continue;
                if (template.buildTemplate(random, chunkX, chunkZ, world))
                {
                    if (average == -1 && template.building != null)
                    {
                        average = template.building.averageGroundLevel;
                        if (template.building.averageGroundLevel != -1)
                        {
                            template.setFloor = true;
                            template.building.averageGroundLevel = average;
                            syncTemplateGround(template.building);
                        }
                    }
                }
            }
        }

        public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
                IChunkProvider chunkProvider)
        {
            getTemplate(dir, chunkX, chunkZ, world);
            buildTemplate(random, chunkX, chunkZ, world);
            for (TemplateSet template : templates)
            {
                done &= template.done;
            }
        }

        private void syncTemplateGround(TemplateStructure from)
        {
            int average = from.averageGroundLevel;
            for (TemplateSet template : templates)
            {
                TemplateStructure struct = template.building;
                if (struct != null && !template.setFloor)
                {
                    template.setFloor = true;
                    struct.averageGroundLevel = average;

                    Map<BlockPos, String> map = struct.template.getDataBlocks(new BlockPos(0, 0, 0),
                            struct.placeSettings);
                    for (BlockPos blockpos : map.keySet())
                    {
                        String s = (String) map.get(blockpos);
                        if (s.equals("Floor"))
                        {
                            struct.setOffset(-blockpos.getY() + 1);
                            struct.averageGroundLevel -= blockpos.getY() + 1;
                            blockpos = blockpos.add(struct.templatePosition);
                            break;
                        }
                    }

                }
            }
        }
    }

    public static class Template
    {
        public TemplateSet template;
        public BlockPos    position      = BlockPos.ORIGIN;
        public String      biome         = null;
        public EnumFacing  dir;
        public Rotation    rotation;
        public Mirror      mirror;
        public int         averageGround = -1;
        boolean            setFloor      = false;
        boolean            done          = false;
        public float       chance;
        public Integer     priority      = 10;
    }

    Map<BlockPos, List<Template>>  parts        = Maps.newHashMap();
    public List<Template>          subTemplates = Lists.newArrayList();
    public final SpawnBiomeMatcher spawnRule;
    public boolean                 syncGround   = false;
    public float                   chance       = 1;
    public BlockPos                origin;
    public EnumFacing              dir;
    Set<MultiTemplateSet>          templates    = Sets.newHashSet();

    public WorldGenMultiTemplate(SpawnBiomeMatcher spawnRule)
    {
        this.spawnRule = spawnRule;
    }

    private void initParts()
    {
        for (Template template : subTemplates)
        {
            if (template.chance <= 0) continue;
            BlockPos pos = template.position;
            List<Template> set = parts.get(pos);
            if (set == null)
            {
                parts.put(pos, set = Lists.newArrayList());
            }
            set.add(template);
            set.sort(new Comparator<Template>()
            {
                @Override
                public int compare(Template o1, Template o2)
                {
                    return o1.priority.compareTo(o2.priority);
                }
            });
        }
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider)
    {
        setTemplate(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
        if (templates.isEmpty()) return;
        MultiTemplateSet[] arr = templates.toArray(new MultiTemplateSet[templates.size()]);
        for (MultiTemplateSet set : arr)
        {
            set.generate(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
        }
    }

    private void setTemplate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider)
    {
        Iterator<MultiTemplateSet> set = templates.iterator();
        while (set.hasNext())
        {
            if (set.next().done) set.remove();
        }
        if (random.nextFloat() > chance) return;
        int rX = random.nextInt(20) % 16 + chunkX * 16;
        int rZ = random.nextInt(20) % 16 + chunkZ * 16;
        BlockPos offset = new BlockPos(rX, world.getHeight(rX, rZ), rZ);
        if (!spawnRule.matches(new SpawnCheck(Vector3.getNewVector().set(offset), world))) return;
        offset = this.origin == null ? new BlockPos(rX, 255, rZ) : this.origin;
        EnumFacing dir = this.dir == null ? EnumFacing.HORIZONTALS[random.nextInt(EnumFacing.HORIZONTALS.length)]
                : this.dir;
        if (parts.isEmpty()) initParts();
        List<Template> toBuild = Lists.newArrayList();
        for (BlockPos pos : parts.keySet())
        {
            List<Template> subs = parts.get(pos);
            if (subs.isEmpty()) continue;
            Template here = null;
            here:
            for (Template sub : subs)
            {
                if (sub.chance > random.nextFloat())
                {
                    here = sub;
                    break here;
                }
            }
            if (here == null) continue;
            toBuild.add(here);
        }
        if (toBuild.isEmpty()) return;
        MultiTemplateSet mTSet = new MultiTemplateSet();
        mTSet.dir = dir;
        mTSet.origin = offset;
        mTSet.syncGround = syncGround;
        for (Template template : toBuild)
        {
            TemplateSet tSet = template.template.clone();
            tSet.origin = offset.add(template.position);
            if (template.mirror != null) dir = template.mirror.mirror(dir);
            if (template.rotation != null) dir = template.rotation.rotate(dir);
            tSet.dir = dir;
            tSet.position = template.position;
            tSet.setFloor = template.setFloor;
            tSet.biomeType = template.biome;
            mTSet.templates.add(tSet);
        }
        mTSet.templates.sort(new Comparator<TemplateSet>()
        {
            @Override
            public int compare(TemplateSet o1, TemplateSet o2)
            {
                if (o1.position.getY() == o2.position.getY()) return o1.position.compareTo(o2.position);
                return o1.position.getY() - o2.position.getY();
            }
        });
        templates.add(mTSet);
    }
}
