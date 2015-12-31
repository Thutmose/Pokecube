/**
 * 
 */
package pokecube.core.client;

import net.minecraft.util.ResourceLocation;
import pokecube.core.interfaces.PokecubeMod;

/**
 * @author Manchou
 *
 */
public class Resources {
	

    public final static String TEXTURE_FOLDER = "textures/";
    public final static String TEXTURE_GUI_FOLDER = TEXTURE_FOLDER+"gui/";

    public final static String TEXTURE_PARTICLES = TEXTURE_FOLDER + "particles.png";

	public final static ResourceLocation GUI_POKEDEX = 
		new ResourceLocation(PokecubeMod.ID, TEXTURE_GUI_FOLDER + "pokedexGui.png");
	
	public final static ResourceLocation GUI_BATTLE = 
		new ResourceLocation(PokecubeMod.ID, TEXTURE_GUI_FOLDER + "battleGui.png");

	public final static ResourceLocation GUI_HEAL_TABLE = 
		new ResourceLocation(PokecubeMod.ID, TEXTURE_GUI_FOLDER + "pokecenterGui.png");

	public final static ResourceLocation STATUS_PAR = 
		new ResourceLocation(PokecubeMod.ID, TEXTURE_FOLDER + "PAR.png");
	
	public final static ResourceLocation STATUS_FRZ = 
		new ResourceLocation(PokecubeMod.ID, TEXTURE_FOLDER + "FRZ.png");
	
	public final static ResourceLocation GUI_POKEMOB =
			new ResourceLocation(PokecubeMod.ID, TEXTURE_GUI_FOLDER + "pokemob.png");
	
	public final static ResourceLocation PARTICLES =
			new ResourceLocation(PokecubeMod.ID, TEXTURE_FOLDER + "particles.png");
	
}
