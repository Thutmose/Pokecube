package pokecube.compat.atomicstryker;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.compat.Config;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;

public class RuinsCompat
{
    @SubscribeEvent
    public void RuinsSpawnEvent(atomicstryker.ruins.common.EventRuinTemplateSpawn event)
    {
        if (event.isPrePhase) return;

        int x = event.x;
        int y = event.y;
        int z = event.z;
        atomicstryker.ruins.common.RuinTemplate template = event.template;
        atomicstryker.ruins.common.RuinData data = template.getRuinData(x, y, z, event.rotation);

        int biome = BiomeType.RUIN.getType();
        if (Config.biomeMap.containsKey(data.name.toLowerCase().replace("tml", "")))
        {
            biome = Config.biomeMap.get(data.name.toLowerCase().replace("tml", ""));
        }

        for (int i = data.xMin; i < data.xMax; i++)
            for (int j = data.yMin; j < data.yMax; j++)
                for (int k = data.zMin; k < data.zMax; k++)
                {
                    TerrainManager.getInstance().getTerrain(event.world, i, j, k).setBiome(i, j, k, biome);
                }
    }
}
