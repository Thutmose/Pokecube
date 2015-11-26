package pokecube.core.world.gen.village.handlers;

import java.util.List;
import java.util.Random;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraft.world.gen.structure.StructureVillagePieces.Village;
import net.minecraftforge.fml.common.registry.VillagerRegistry.IVillageCreationHandler;
import pokecube.core.world.gen.village.buildings.ComponentPokeMart;

public class PokeMartCreationHandler  implements IVillageCreationHandler
{
	@Override
	public Class<?> getComponentClass() {
		return ComponentPokeMart.class;
	}

	@Override
	public PieceWeight getVillagePieceWeight(Random random,
			int i) 
	{
		return new PieceWeight(ComponentPokeMart.class, 100, 1);
	}
	
	@Override
	public Village buildComponent(PieceWeight villagePiece, Start startPiece,
			List<StructureComponent> pieces, Random random, int p1, int p2, int p3,
			EnumFacing facing, int p5) {
		// TODO Auto-generated method stub
		return ComponentPokeMart.buildComponent(villagePiece, startPiece, pieces, random, p1, p2, p3, facing, p5);
	}

}
