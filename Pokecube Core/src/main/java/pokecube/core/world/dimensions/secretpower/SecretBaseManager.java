package pokecube.core.world.dimensions.secretpower;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;

public class SecretBaseManager
{
    public static class Coordinate implements Comparable<Coordinate>
    {
        public final int x;
        public final int y;
        public final int z;
        public final int w;

        public Coordinate(int x, int y, int z, int w)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        public int distSq(Coordinate other)
        {
            return ((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y) + (z - other.z) * (z - other.z)
                    + (w - other.w) * (w - other.w));
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof Coordinate)) { return false; }
            Coordinate BlockPos = (Coordinate) obj;
            return x == BlockPos.x && y == BlockPos.y && this.z == BlockPos.z && this.w == BlockPos.w;
        }

        @Override
        public int hashCode()
        {
            return x + z << 8 + y << 16 + this.w << 24;
        }

        @Override
        public int compareTo(Coordinate other)
        {
            return y == other.y
                    ? (this.z == other.z ? x - other.x : this.w == other.w ? this.z - other.z : this.w - other.w)
                    : this.y - other.y;
        }

        public NBTTagCompound writeToNBT()
        {
            NBTTagCompound ret = new NBTTagCompound();
            ret.setInteger("x", x);
            ret.setInteger("y", y);
            ret.setInteger("z", z);
            ret.setInteger("w", w);
            return ret;
        }

        public static Coordinate readNBT(NBTTagCompound tag)
        {
            if (!tag.hasKey("x")) return null;
            return new Coordinate(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"), tag.getInteger("w"));
        }
    }

    private static final Int2ObjectOpenHashMap<Set<Coordinate>> baseMap = new Int2ObjectOpenHashMap<>();

    public static boolean addBase(Coordinate base)
    {
        Set<Coordinate> bases = baseMap.get(base.w);
        if (bases == null) baseMap.put(base.w, bases = Sets.newHashSet());
        return bases.add(base);
    }

    public static boolean removeBase(Coordinate base)
    {
        Set<Coordinate> bases = baseMap.get(base.w);
        if (bases == null) baseMap.put(base.w, bases = Sets.newHashSet());
        return bases.remove(base);
    }

    public static void clear()
    {
        baseMap.clear();
    }

    @Nullable
    public static Coordinate getNearestBase(Coordinate pos)
    {
        int dimA = pos.w;
        Set<Coordinate> bases = baseMap.get(dimA);
        if (bases == null) return null;
        Coordinate ret = null;
        int min = Integer.MAX_VALUE;
        for (Coordinate c : bases)
        {
            if (c.distSq(pos) < min)
            {
                min = c.distSq(pos);
                ret = c;
            }
        }
        return ret;
    }

    public static Set<Coordinate> getNearestBases(Coordinate pos, int maxDistance)
    {
        int dimA = pos.w;
        Set<Coordinate> bases = baseMap.get(dimA);
        Set<Coordinate> ret = Sets.newHashSet();
        if (bases == null) return ret;
        int dist = maxDistance * maxDistance;
        for (Coordinate c : bases)
        {
            if (c.distSq(pos) < dist)
            {
                ret.add(c);
            }
        }
        return ret;
    }

}
