package pokecube.core.world.gen.village.buildings;

import java.util.Random;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.Template;

public class TemplateStructure extends TemplateStructureBase
{
    public TemplateStructure()
    {
        super();
    }

    public TemplateStructure(String type, BlockPos pos, EnumFacing dir)
    {
        super(type, pos, dir);
    }

    public TemplateStructure(String type, StructureBoundingBox structureboundingbox, EnumFacing facing)
    {
        this(type, new BlockPos(structureboundingbox.minX, structureboundingbox.minY, structureboundingbox.minZ),
                facing);
        setBoundingBoxFromTemplate(structureboundingbox);
    }

    @Override
    protected Template getTemplate()
    {
        return template;
    }

    @Override
    protected int getOffset()
    {
        return 0;
    }

    @Override
    protected void handleDataMarker(String marker, BlockPos pos, World world, Random rand, StructureBoundingBox box)
    {

    }

}
