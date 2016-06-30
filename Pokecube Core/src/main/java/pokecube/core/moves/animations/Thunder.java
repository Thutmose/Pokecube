package pokecube.core.moves.animations;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import pokecube.core.interfaces.Move_Base;

public class Thunder extends MoveAnimationBase {

	public Thunder() {
	}

    @Override
    public void clientAnimation(MovePacketInfo info, IWorldEventListener world, float partialTick)
    {
    	World theRealWorld = info.attacker.getEntityWorld();
    	Entity lightning = new EntityLightningBolt(theRealWorld, info.target.x, info.target.y, info.target.z, false);
    	theRealWorld.spawnEntityInWorld(lightning);
        theRealWorld.addWeatherEffect(lightning);
    }


	@Override
	public int getDuration() {
		return 0;
	}

    @Override
    public void initColour(long time, float partialTicks, Move_Base move)
    {
        // TODO Auto-generated method stub
        
    }
}
