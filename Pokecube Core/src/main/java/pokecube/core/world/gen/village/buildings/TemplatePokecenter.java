package pokecube.core.world.gen.village.buildings;

import java.util.Random;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.Template;
import pokecube.core.world.gen.template.PokecubeTemplates;

public class TemplatePokecenter extends TemplateStructure
{
    public TemplatePokecenter()
    {
        super();
    }

    public TemplatePokecenter(BlockPos pos, EnumFacing dir)
    {
        super(PokecubeTemplates.POKECENTER, pos, dir);
    }

    public TemplatePokecenter(StructureBoundingBox structureboundingbox, EnumFacing facing)
    {
        this(new BlockPos(structureboundingbox.minX, structureboundingbox.minY, structureboundingbox.minZ), facing);
        setBoundingBoxFromTemplate(structureboundingbox);
    }

    @Override
    protected void handleDataMarker(String marker, BlockPos pos, World world, Random rand, StructureBoundingBox box)
    {

    }

    @Override
    protected Template getTemplate()
    {
        if (template != null) return template;
        return template = PokecubeTemplates.getTemplate(PokecubeTemplates.POKECENTER);
    }

    @Override
    protected int getOffset()
    {
        return -3;
    }

}
