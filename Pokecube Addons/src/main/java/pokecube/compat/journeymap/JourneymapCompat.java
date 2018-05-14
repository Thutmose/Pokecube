package pokecube.compat.journeymap;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.event.ClientEvent.Type;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import net.minecraftforge.common.MinecraftForge;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.commands.Config;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.utils.ChunkCoordinate;

@SuppressWarnings("unchecked")
@ClientPlugin
public class JourneymapCompat implements IClientPlugin
{
    private static final Map<ChunkCoordinate, Integer> forbiddenSpawningCoords;
    static
    {
        try
        {
            Field f = SpawnHandler.class.getDeclaredField("forbiddenSpawningCoords");
            f.setAccessible(true);
            forbiddenSpawningCoords = (Map<ChunkCoordinate, Integer>) f.get(null);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    private IClientAPI                                        clientAPI;
    private static final Map<ChunkCoordinate, PolygonOverlay> POLYGONS = new HashMap<>();

    public JourneymapCompat()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void initialize(IClientAPI api)
    {
        clientAPI = api;
        api.subscribe(getModId(), EnumSet.of(ClientEvent.Type.DISPLAY_UPDATE, ClientEvent.Type.MAPPING_STARTED,
                ClientEvent.Type.MAPPING_STOPPED));
    }

    @Override
    public String getModId()
    {
        return PokecubeAdv.ID;
    }

    @Override
    public void onEvent(ClientEvent event)
    {
        if (!Config.instance.journeymapRepels) return;

        if (event.type == Type.DISPLAY_UPDATE)
        {
            Set<ChunkCoordinate> processed = Sets.newHashSet();
            for (ChunkCoordinate c : forbiddenSpawningCoords.keySet())
            {
                try
                {
                    if (c.dim != event.dimension) continue;
                    processed.add(c);
                    PolygonOverlay overlay = POLYGONS.get(c);
                    if (overlay != null)
                    {
                        clientAPI.remove(overlay);
                    }
                    int size = forbiddenSpawningCoords.get(c);

                    MapPolygon repelArea = new MapPolygon(c.east(size + 1).north(size),
                            c.west(size).north(size).up(size), c.west(size).south(size + 1),
                            c.east(size + 1).south(size + 1));
                    ShapeProperties shapeProperties = new ShapeProperties();

                    shapeProperties.setFillOpacity(0.2F);
                    shapeProperties.setStrokeOpacity(0.1F);

                    shapeProperties.setFillColor(0xffff2200);
                    shapeProperties.setStrokeColor(shapeProperties.getFillColor());

                    overlay = new PolygonOverlay(getModId(), "repel_" + c.dim + '_' + c.getX() + '_' + c.getZ(), c.dim,
                            shapeProperties, repelArea);
                    overlay.setOverlayGroupName("Repels").setTitle("Repelled Area");
                    POLYGONS.put(c, overlay);
                    clientAPI.show(overlay);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            Set<ChunkCoordinate> toRemove = Sets.newHashSet();
            for (ChunkCoordinate c1 : POLYGONS.keySet())
            {
                if (c1.dim != event.dimension) continue;
                if (!processed.contains(c1)) toRemove.add(c1);
            }
            for (ChunkCoordinate c : toRemove)
            {
                PolygonOverlay overlay = POLYGONS.remove(c);
                if (overlay != null)
                {
                    clientAPI.remove(overlay);
                }
            }
        }
        else if (event.type == Type.MAPPING_STOPPED)
        {
            clear();
        }
    }

    private void clear()
    {
        POLYGONS.clear();
        clientAPI.removeAll(PokecubeAdv.ID);
    }
}
