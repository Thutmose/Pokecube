package pokecube.adventures.blocks.cloner;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import pokecube.adventures.PokecubeAdv;

public class BlockCloner extends Block {

	public BlockCloner() {
		super(Material.iron);
        this.setLightOpacity(0);
		this.setHardness(10);
		this.setResistance(10);
        this.setLightLevel(1f);
	}

	public boolean onBlockActivated(World world, int x, int y, int z,
			EntityPlayer player, int par6, float par7, float par8, float par9) {
		player.openGui(PokecubeAdv.instance, PokecubeAdv.GUICLONER_ID, world, x, y, z);
		return true;
	}
	
}
