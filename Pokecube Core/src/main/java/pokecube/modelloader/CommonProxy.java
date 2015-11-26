/**
 *
 */
package pokecube.modelloader;

/**
 * This class actually does nothing on server side. 
 * But its implementation on client side does.
 * 
 * @author Manchou
 */
public class CommonProxy
{
	public void registerModelProvider(String modid, Object mod)
	{
		
	}
    /**
     * Client side only register stuff...
     */
    public void registerRenderInformation()
    {
        // unused server side. -- see ClientProxyPokecubeTemplate for implementation
    }
    
    public void init()
    {
        
    }
}
