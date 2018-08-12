package pokecube.core.moves;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.world.World;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class TreeRemover
{
    World         world;
    Vector3       centre;

    List<Vector3> blocks  = new LinkedList<Vector3>();
    List<Vector3> checked = new LinkedList<Vector3>();

    public TreeRemover(World world, Vector3 pos)
    {
        this.world = world;
        centre = pos;
    }

    public void clear()
    {
        blocks.clear();
        checked.clear();
    }

    public int cut(boolean count)
    {
        if (!count && cutTree(true) == 0) cutGrass();
        return cutTree(count);
    }

    public void cutGrass()
    {
        Vector3 temp = Vector3.getNewVector();
        for (int i = -4; i < 5; i++)
            for (int j = -4; j < 5; j++)
                for (int k = -1; k < 6; k++)
                {
                    temp.set(centre).addTo(i, k, j);
                    if (PokecubeTerrainChecker.isPlant(temp.getBlockState(world)))
                    {
                        temp.breakBlock(world, true);
                    }
                }
    }

    private int cutPoints(boolean count)
    {
        int ret = 0;
        for (Vector3 v : blocks)
        {
            if (!count) v.breakBlock(world, true);
            ret++;
        }
        return ret;
    }

    public int cutTree(boolean count)
    {
        if (blocks.size() > 0 && count)
        {
            clear();
        }
        else if (blocks.size() > 0) { return cutPoints(count); }
        Vector3 base = findTreeBase();
        int ret = 0;
        if (!base.isEmpty())
        {
            populateList(base);
            ret = cutPoints(count);
        }
        return ret;
    }

    private Vector3 findTreeBase()
    {
        Vector3 base = Vector3.getNewVector();
        int k = -1;
        Vector3 temp = Vector3.getNewVector();

        if (PokecubeTerrainChecker.isWood(temp.set(centre).getBlockState(world)))
        {
            boolean valid = false;
            while (centre.intY() + k > 0)
            {
                if (PokecubeTerrainChecker.isWood(temp.set(centre).addTo(0, k, 0).getBlockState(world)))
                {
                }
                else if (PokecubeTerrainChecker.isDirt(temp.set(centre).addTo(0, k, 0).getBlockState(world)))
                {
                    valid = true;
                }
                else
                {
                    break;
                }
                if (valid) break;
                k--;
            }
            if (valid)
            {
                base.set(temp).set(centre).addTo(0, k + 1, 0);
            }
        }
        return base;
    }

    private boolean nextPoint(Vector3 prev, List<Vector3> tempList)
    {
        boolean ret = false;

        Vector3 temp = Vector3.getNewVector();
        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                for (int k = -1; k <= 1; k++)
                {
                    temp.set(prev).addTo(i, k, j);
                    if (PokecubeTerrainChecker.isWood(temp.getBlockState(world)))
                    {
                        tempList.add(temp.copy());
                        ret = true;
                    }
                }
        checked.add(prev);
        return ret;
    }

    private void populateList(Vector3 base)
    {
        blocks.add(base);
        while (checked.size() < blocks.size())
        {
            List<Vector3> toAdd = new ArrayList<Vector3>();
            for (Vector3 v : blocks)
            {
                if (!checked.contains(v))
                {
                    nextPoint(v, toAdd);
                }
            }
            for (Vector3 v : toAdd)
            {
                if (!blocks.contains(v)) blocks.add(v);
            }
        }
    }
}
