package pokecube.compat.atomicstryker;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.adventures.commands.Config;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.lib.CompatClass;
import thut.lib.CompatClass.Phase;

public class RuinsCompat
{
    @Optional.Method(modid = "ruins")
    @CompatClass(phase = Phase.POST)
    public static void AS_RuinsCompat()
    {
        System.out.println("ruins Compat");
        MinecraftForge.EVENT_BUS.register(new pokecube.compat.atomicstryker.RuinsCompat());
    }

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
        if (Config.biomeMap.containsKey(data.name.toLowerCase(java.util.Locale.ENGLISH).replace("tml", "")))
        {
            biome = Config.biomeMap.get(data.name.toLowerCase(java.util.Locale.ENGLISH).replace("tml", ""));
        }

        for (int i = data.xMin; i < data.xMax; i++)
            for (int j = data.yMin; j < data.yMax; j++)
                for (int k = data.zMin; k < data.zMax; k++)
                {
                    TerrainManager.getInstance().getTerrain(event.getWorld(), i, j, k).setBiome(i, j, k, biome);
                }
    }
}
