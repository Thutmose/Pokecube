package pokecube.core.world.gen.village.handlers;

import java.util.List;
import java.util.Random;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraft.world.gen.structure.StructureVillagePieces.Village;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.fml.common.registry.VillagerRegistry.IVillageCreationHandler;
import pokecube.core.world.gen.template.PokecubeTemplates;
import pokecube.core.world.gen.village.buildings.TemplatePokemart;
import pokecube.core.world.gen.village.buildings.TemplateStructureBase;

public class PokeMartCreationHandler implements IVillageCreationHandler
{

    @Override
    public Village buildComponent(PieceWeight villagePiece, Start startPiece, List<StructureComponent> pieces,
            Random random, int minX, int minY, int minZ, EnumFacing facing, int componentType)
    {
        Template template = PokecubeTemplates.getTemplate(PokecubeTemplates.POKEMART);
        BlockPos size = template.getSize();
        StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(minX, minY, minZ,
                0, 0, 0, size.getX()-1, size.getY(), size.getZ()-1, facing);
        TemplateStructureBase component = new TemplatePokemart(structureboundingbox, facing);
        boolean conflict = StructureComponent.findIntersecting(pieces, structureboundingbox) == null;
        return conflict ? component : null;
    }

    @Override
    public Class<?> getComponentClass()
    {
        return TemplatePokemart.class;
    }

    @Override
    public PieceWeight getVillagePieceWeight(Random random, int i)
    {
        return new PieceWeight(TemplatePokemart.class, 100, 1);
    }

    protected static boolean canVillageGoDeeper(StructureBoundingBox par0StructureBoundingBox)
    {
        return (par0StructureBoundingBox != null) && (par0StructureBoundingBox.minY > 10);
    }

}
