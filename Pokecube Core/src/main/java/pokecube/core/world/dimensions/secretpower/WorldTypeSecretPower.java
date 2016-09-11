package pokecube.core.world.dimensions.secretpower;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;

public class WorldTypeSecretPower extends WorldType
{
    public WorldTypeSecretPower()
    {
        super("pokecube_sp");
    }
    
    public net.minecraft.world.chunk.IChunkGenerator getChunkGenerator(World world, String generatorOptions)
    {
        return new ChunkProviderSecretPower(world);
    }
}
