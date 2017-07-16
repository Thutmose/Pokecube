package pokecube.core.database.abilities.b;

import net.minecraft.entity.EntityLivingBase;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;

public class BulletProof extends Ability
{
    private String[] bullets = { "AcidSpray", "AuraSphere", "Barrage", "BulletSeed", "EggBomb", "ElectroBall",
            "EnergyBall", "FocusBlast", "GyroBall", "IceBall", "MagnetBomb", "MistBall", "MudBomb", "Octazooka",
            "RockWrecker", "SearingShot", "SeedBomb", "ShadowBall", "SludgeBomb", "WeatherBall", "ZapCannon" };

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if(move.pre && mob == move.attacked)
        {
            for(String s: bullets)
            {
                if(s.equalsIgnoreCase(move.attack))
                {
                    move.canceled = true;
                    return;
                }
            }
        }
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
    }

}
