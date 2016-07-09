package pokecube.core.moves.templates;

import java.util.Random;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
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
    public Move_Terrain(String name, int effect)
    {
        super(name);
        this.effect = effect;
    }

    @Override
    /** Called after the attack for special post attack treatment.
     * 
     * @param attacker
     * @param attacked
     * @param f
     * @param finalAttackStrength
     *            the number of HPs the attack takes from target */
    public void postAttack(IPokemob attacker, Entity attacked, int pwr, int finalAttackStrength)
    {
        duration = 300 + new Random().nextInt(600);
        TerrainSegment segment = TerrainManager.getInstance().getTerrainForEntity(attacked);

        PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemobEffects");
        if (teffect == null)
        {
            segment.addEffect(teffect = new PokemobTerrainEffects(), "pokemobEffects");
        }

        if (segment != null && attacked.getEntityWorld() != null)
            teffect.setEffect(effect, duration + attacked.getEntityWorld().getTotalWorldTime());
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(73));
            buffer.writeByte(PokecubeClientPacket.TERRAINEFFECTS);
            buffer.writeInt(segment.chunkX);
            buffer.writeInt(segment.chunkY);
            buffer.writeInt(segment.chunkZ);
            for (int i = 0; i < 16; i++)
            {
                buffer.writeLong(teffect.effects[i]);
            }
            PokecubeClientPacket packet = new PokecubeClientPacket(buffer);
            Vector3 v = Vector3.getNewVector().set(attacked);
            PokecubePacketHandler.sendToAllNear(packet, v, attacked.getEntityWorld().provider.getDimension(), 64);
        }

    }

    public void setDuration(int duration)
    {
        this.duration = duration;
    }
}
