package pokecube.core.database.abilities.f;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class Forecast extends Ability
{
    static PokedexEntry rain;
    static PokedexEntry sun;
    static PokedexEntry snow;
    static PokedexEntry base;

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        if (mob.getPokedexNb() != 351) return;// Only affect castform.

        Entity pokemob = (Entity) mob;
        if (pokemob.ticksExisted % 20 != 9) return;// Only check once per
                                                   // second.
        System.out.println("tick");
        if(rain==null) rain = Database.getEntry("castformrain");
        if(base==null) base = Database.getEntry("castform");
        if(sun==null) sun = Database.getEntry("castformsun");
        if(snow==null) snow = Database.getEntry("castformsnow");
        
        //TODO check for weather canceling effects in the area, then set to base and return early.

        TerrainSegment terrain = TerrainManager.getInstance().getTerrainForEntity(pokemob);
        PokemobTerrainEffects effect = (PokemobTerrainEffects) terrain.geTerrainEffect("pokemobEffects");
        long terrainDuration = effect.getEffect(PokemobTerrainEffects.EFFECT_WEATHER_RAIN);
        if (terrainDuration > 0)
        {
            mob.setPokedexEntry(rain);
            return;
        }
        terrainDuration = effect.getEffect(PokemobTerrainEffects.EFFECT_WEATHER_SUN);
        if (terrainDuration > 0)
        {
            mob.setPokedexEntry(sun);
            return;
        }
        terrainDuration = effect.getEffect(PokemobTerrainEffects.EFFECT_WEATHER_HAIL);
        if (terrainDuration > 0)
        {
            mob.setPokedexEntry(snow);
            return;
        }
        mob.setPokedexEntry(base);
    }

}
