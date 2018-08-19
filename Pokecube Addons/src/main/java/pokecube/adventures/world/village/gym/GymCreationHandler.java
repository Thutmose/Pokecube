package pokecube.adventures.world.village.gym;

import java.util.List;
import java.util.Random;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraft.world.gen.structure.StructureVillagePieces.Village;
import net.minecraftforge.fml.common.registry.VillagerRegistry.IVillageCreationHandler;
import pokecube.core.world.gen.village.buildings.TemplateStructureBase;

public class GymCreationHandler implements IVillageCreationHandler
{

    @Override
    public Village buildComponent(PieceWeight villagePiece, Start startPiece, List<StructureComponent> pieces,
            Random random, int minX, int minY, int minZ, EnumFacing facing, int componentType)
    {
        BlockPos pos = new BlockPos(minX, minY, minZ);
        TemplateStructureBase component = new TemplateGym(pos, facing);
        StructureBoundingBox structureboundingbox = component.getBoundingBox();
        structureboundingbox.maxX += 2 * facing.getFrontOffsetX();
        structureboundingbox.maxZ += 2 * facing.getFrontOffsetZ();
        structureboundingbox.minX += 2 * facing.getFrontOffsetX();
        structureboundingbox.minZ += 2 * facing.getFrontOffsetZ();
        StructureComponent conf = StructureComponent.findIntersecting(pieces, structureboundingbox);
        boolean conflict = conf == null;
        return conflict ? component : null;
    }

    @Override
    public Class<?> getComponentClass()
    {
        return TemplateGym.class;
    }

    @Override
    public PieceWeight getVillagePieceWeight(Random random, int i)
    {
        return new PieceWeight(TemplateGym.class, 50, 1);
    }

    protected static boolean canVillageGoDeeper(StructureBoundingBox par0StructureBoundingBox)
    {
        return (par0StructureBoundingBox != null) && (par0StructureBoundingBox.minY > 10);
    }

}
