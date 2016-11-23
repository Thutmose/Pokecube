package pokecube.core.interfaces;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import thut.api.maths.Vector3;

@SuppressWarnings("rawtypes")
public abstract class CommonProxy
{
    protected static CommonProxy instance;

    public static CommonProxy getClientInstance()
    {
        return instance;
    }

    public void initClient()
    {
    }

    public boolean isSoundPlaying(Vector3 location)
    {
        return false;
    }

    public void toggleSound(String sound, BlockPos location)
    {

    }

    public void registerPokecubeRenderer(int cubeId, Render renderer, Object mod)
    {
    }
    
    public StatisticsManager getManager(EntityPlayer player)
    {
        return ((EntityPlayerMP) player).getStatFile();
    }

    public abstract void registerPokemobModel(int nb, ModelBase model, Object mod);

    public abstract void registerPokemobModel(String name, ModelBase model, Object mod);

    /** Used to register a custom renderer for the pokemob
     * 
     * @param nb
     *            - the pokedex number
     * @param renderer
     *            - the renderer */
    public abstract void registerPokemobRenderer(int nb, IRenderFactory renderer, Object mod);

    public abstract void registerPokemobRenderer(String name, IRenderFactory renderer, Object mod);;
}
