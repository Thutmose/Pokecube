package pokecube.core.world.gen.village.buildings;

import java.util.Locale;
import java.util.Random;

import net.minecraft.block.BlockStairs;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.Template;

public class TemplateStructure extends TemplateStructureBase
{
    public int offset = 0;

    public TemplateStructure()
    {
        super();
    }

    public TemplateStructure(String type, BlockPos pos, EnumFacing dir)
    {
        super(type, pos, dir);
    }

    @Override
    public Template getTemplate()
    {
        return template;
    }

    @Override
    public int getOffset()
    {
        return offset;
    }

    @Override
    protected void handleDataMarker(String marker, BlockPos pos, World world, Random rand, StructureBoundingBox box)
    {
        if (!box.isVecInside(pos) || !world.isAreaLoaded(pos, pos)) return;
        TileEntity below = world.getTileEntity(pos.down());
        if (marker.startsWith("Chest") && below instanceof TileEntityChest)
        {
            String[] args = marker.split(" ");
            if (args.length >= 2)
            {
                ResourceLocation loot = new ResourceLocation(args[1]);
                TileEntityChest chest = (TileEntityChest) below;
                chest.setLootTable(loot, rand.nextLong());
            }
        }
        else if (marker.toLowerCase(Locale.ENGLISH).startsWith("stair")
                && world.getBlockState(pos.down()).getBlock() instanceof BlockStairs)
        {
            if (!world.getBlockState(pos.down(2)).getMaterial().isSolid())
            {
                world.setBlockState(pos.down(), Blocks.AIR.getDefaultState());
            }
        }
    }

    @Override
    public void setOffset(int offset)
    {
        this.offset = offset;
    }

}
