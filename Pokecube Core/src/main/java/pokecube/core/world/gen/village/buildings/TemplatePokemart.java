package pokecube.core.world.gen.village.buildings;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.Template;
import pokecube.core.world.gen.template.PokecubeTemplates;

public class TemplatePokemart extends TemplateStructure
{
    public TemplatePokemart()
    {
        super();
        setOffset(-2);
    }

    public TemplatePokemart(BlockPos pos, EnumFacing dir)
    {
        super(PokecubeTemplates.POKEMART, pos, dir);
    }

    @Override
    public Template getTemplate()
    {
        if (template != null) return template;
        return template = PokecubeTemplates.getTemplate(PokecubeTemplates.POKEMART);
    }
}
