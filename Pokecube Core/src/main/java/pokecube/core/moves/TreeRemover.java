package pokecube.core.moves;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import thut.api.maths.Vector3;

public class TreeRemover
{
    public static final List<Block> woodTypes  = new ArrayList<Block>();
    public static final List<Block> plantTypes = new ArrayList<Block>();
    public static final List<Block> dirtTypes  = new ArrayList<Block>();

    static
    {
        woodTypes.add(Blocks.log);
        woodTypes.add(Blocks.log2);

        plantTypes.add(Blocks.double_plant);
        plantTypes.add(Blocks.tallgrass);
        plantTypes.add(Blocks.carrots);
        plantTypes.add(Blocks.cocoa);
        plantTypes.add(Blocks.wheat);
        plantTypes.add(Blocks.potatoes);
        plantTypes.add(Blocks.reeds);
        plantTypes.add(Blocks.leaves);
        plantTypes.add(Blocks.vine);
        plantTypes.add(Blocks.cactus);
        plantTypes.add(Blocks.melon_block);
        plantTypes.add(Blocks.red_flower);
        plantTypes.add(Blocks.yellow_flower);
        plantTypes.add(Blocks.pumpkin);

        dirtTypes.add(Blocks.dirt);
    }

    World   worldObj;
    Vector3 centre;

    List<Vector3> blocks  = new LinkedList<Vector3>();
    List<Vector3> checked = new LinkedList<Vector3>();

    public TreeRemover(World world, Vector3 pos)
    {
        worldObj = world;
        centre = pos;
    }

    public int cut(boolean count)
    {
        cutGrass();
        return cutTree(count);
    }

    public void clear()
    {
        blocks.clear();
        checked.clear();
    }

    public int cutTree(boolean count)
    {
        if (blocks.size() > 0 && count)
        {
            clear();
        }
        else if (blocks.size() > 0) { return cutPoints(count); }

        Vector3 base = Vector3.getNewVector();
        int ret = 0;
        int k = -1;
        Vector3 temp = Vector3.getNewVector();

        for (int i = -3; i < 4; i++)
            for (int j = -3; j < 4; j++)
            {
                if (woodTypes.contains(temp.set(centre).addTo(i, 0, j).getBlock(worldObj)))
                {
                    boolean valid = false;
                    while (centre.intY() + k > 0)
                    {
                        if (woodTypes.contains(temp.set(centre).addTo(i, k, j).getBlock(worldObj)))
                        {

                        }
                        else if (dirtTypes.contains(temp.set(centre).addTo(i, k, j).getBlock(worldObj)))
                        {
                            valid = true;
                        }
                        if (valid) break;
                        k--;
                    }
                    if (valid)
                    {
                        base.set(temp).set(centre).addTo(i, k + 1, j);
                    }

                }
            }

        if (!base.isEmpty())
        {
            populateList(base);
            ret = cutPoints(count);
        }
        return ret;
    }

    public void cutGrass()
    {
        Vector3 temp = Vector3.getNewVector();
        for (int i = -4; i < 5; i++)
            for (int j = -4; j < 5; j++)
                for (int k = -1; k < 6; k++)
                {
                    temp.set(centre).addTo(i, k, j);
                    if (plantTypes.contains(temp.getBlock(worldObj)))
                    {
                        temp.breakBlock(worldObj, true);
                    }
                }
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

    private int cutPoints(boolean count)
    {
        int ret = 0;
        for (Vector3 v : blocks)
        {
            if (!count) v.breakBlock(worldObj, true);
            ret++;
        }
        return ret;
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
                    if (woodTypes.contains(temp.getBlock(worldObj)))
                    {
                        tempList.add(temp.copy());
                        ret = true;
                    }
                }
        checked.add(prev);
        return ret;
    }
}
