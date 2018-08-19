package pokecube.core.world.gen.village.buildings.pokecenter;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.Template;
import pokecube.core.world.gen.template.PokecubeTemplates;
import pokecube.core.world.gen.village.buildings.TemplateStructure;

public class TemplatePokecenter extends TemplateStructure
{
    public TemplatePokecenter()
    {
        super();
        setOffset(-2);
    }

    public TemplatePokecenter(BlockPos pos, EnumFacing dir)
    {
        super(PokecubeTemplates.POKECENTER, pos, dir);
    }

    @Override
    public Template getTemplate()
    {
        if (template != null) return template;
        return template = PokecubeTemplates.getTemplate(PokecubeTemplates.POKECENTER);
    }
}
