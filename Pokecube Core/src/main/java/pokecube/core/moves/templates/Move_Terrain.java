package pokecube.core.moves.templates;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.network.packets.PacketSyncTerrain;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class Move_Terrain extends Move_Basic
{

    public final int effect;
    public int       duration = 300;

    /** See TerrainSegment for the types of effects.
     * 
     * @param name
     * @param effect */
    public Move_Terrain(String name)
    {
        super(name);
        this.effect = move.baseEntry.extraInfo;
    }

    @Override
    /** Called after the attack for special post attack treatment.
     * 
     * @param attacker
     * @param attacked
     * @param f
     * @param finalAttackStrength
     *            the number of HPs the attack takes from target */
    public void doWorldAction(IPokemob attacker, Vector3 location)
    {
        if (attacker.getMoveStats().SPECIALCOUNTER > 0) { return; }
        attacker.getMoveStats().SPECIALCOUNTER = 20;

        duration = 300 + new Random().nextInt(600);
        World world = attacker.getEntity().getEntityWorld();
        TerrainSegment segment = TerrainManager.getInstance().getTerrian(world, location);

        PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemobEffects");
        // TODO check if effect already exists, and send message if so.
        // Otherwise send the it starts to effect message

        teffect.setEffect(effect, duration + world.getTotalWorldTime());

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            PacketSyncTerrain.sendTerrainEffects(attacker.getEntity(), segment.chunkX, segment.chunkY, segment.chunkZ,
                    teffect);
        }

    }

    public void setDuration(int duration)
    {
        this.duration = duration;
    }
}
