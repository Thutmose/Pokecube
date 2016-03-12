package pokecube.core.interfaces;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import thut.api.maths.Vector3;

@SuppressWarnings("rawtypes")
public abstract class CommonProxy 
{
	protected static CommonProxy instance;
	public static CommonProxy getClientInstance()
	{
		return instance;
	}
	public void initClient() {}
	public boolean isSoundPlaying(Vector3 location)
    {
    	return false;
    }
	
    public void registerPokecubeRenderer(int cubeId, Render renderer, Object mod){}
	
	public abstract void registerPokemobModel(int nb, ModelBase model, Object mod);

	public abstract void registerPokemobModel(String name, ModelBase model, Object mod);
	
    /**
	 * Used to register a custom renderer for the pokemob
	 * @param nb - the pokedex number
	 * @param renderer - the renderer
	 */
	public abstract void registerPokemobRenderer(int nb, Render renderer, Object mod);
    
    public abstract void registerPokemobRenderer(String name, Render renderer, Object mod);;
}
