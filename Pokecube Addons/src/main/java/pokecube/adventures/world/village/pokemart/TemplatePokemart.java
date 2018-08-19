package pokecube.adventures.world.village.pokemart;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.Template;
import pokecube.core.world.gen.template.PokecubeTemplates;
import pokecube.core.world.gen.village.buildings.TemplateStructure;

public class TemplatePokemart extends TemplateStructure
{
    public static final String POKEMART = "pokemart";

    public TemplatePokemart()
    {
        super();
        setOffset(-2);
    }

    public TemplatePokemart(BlockPos pos, EnumFacing dir)
    {
        super(POKEMART, pos, dir);
    }

    @Override
    public Template getTemplate()
    {
        if (template != null) return template;
        return template = PokecubeTemplates.getTemplate(POKEMART);
    }
}
