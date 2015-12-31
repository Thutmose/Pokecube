/**
 * 
 */
package pokecube.core.moves;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IWorldAccess;
import pokecube.core.interfaces.IBreedingMob;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.moves.templates.Move_Basic;
import thut.api.maths.Vector3;

/**
 * @author Manchou
 *
 */
public class Move_Transform extends Move_Basic {

	/**
	 * @param name
	 * @param type
	 * @param PWR
	 * @param PRE
	 * @param PP
	 * @param attackCategory
	 */
	public Move_Transform(String name) {
		super(name);
		setAnimation(new Animation());
		this.setSelf();
		this.setNotInterceptable();
		
	}

	@Override
	public void attack(IPokemob attacker, Entity attacked, float f) {
		if (!(attacker instanceof IPokemob))
			return;
		if (((IPokemob) attacker).getTransformedTo() == null && attacked instanceof EntityLivingBase){
			if (MovesUtils.contactAttack(attacker, attacked, f)) {
				MovesUtils.displayMoveMessages(attacker, attacked, IMoveNames.MOVE_TRANSFORM);
				if (attacked instanceof IPokemob){
		        	
		        	if (attacked instanceof IPokemob){
		        		//((EntityPokemob) attacked).setTarget((Entity) attacker);
						attacker.setStats(((IPokemob)attacked).getBaseStats());
						if(!(attacked instanceof IBreedingMob) || attacked!=((IBreedingMob) attacker).getLover())
							((EntityCreature) attacked).setAttackTarget((EntityLivingBase) attacker);
		            }
		        	
				}
				((IPokemob) attacker).setTransformedTo(attacked);
	        	Vector3 v = Vector3.getNewVectorFromPool().set(attacked);
	        	notifyClient((Entity) attacker, v, attacked);
	        	v.freeVectorFromPool();
			}
		}
		else {
			if (attacked instanceof IPokemob){
				String move = ((IPokemob) attacked).getMove(0);
				if (move!=null &&  !IMoveNames.MOVE_TRANSFORM.equals(move))
					MovesUtils.doAttack(move, attacker, attacked, f);
				else if (MovesUtils.contactAttack(attacker, attacked, f)){
					MovesUtils.displayMoveMessages(attacker, attacked, IMoveNames.MOVE_TRANSFORM);
					MovesUtils.displayEfficiencyMessages(attacker, attacked, 0F, 1F);
				}
			}
			else if (attacked instanceof EntityPlayer) {
				if (MovesUtils.contactAttack(attacker, attacked, f)) {
					if (doAttack(attacker, attacked, f))
						MovesUtils.attack(new MovePacket(attacker, attacked, name, move.type, 25, 1, IMoveConstants.STATUS_NON, IMoveConstants.CHANGE_NONE));
				}
			}
		}
	}
	
	public static class Animation implements IMoveAnimation {

		@Override
		public void clientAnimation(MovePacketInfo info, IWorldAccess world, float partialTick) {
			if(info.attacker==null) return;
			
			((IPokemob) info.attacker).setTransformedTo(info.attacked);
		}

		@Override
		public int getDuration() {
			return 0;
		}

        @Override
        public void setDuration(int arg0)
        {
            
        }
		
	}
}
