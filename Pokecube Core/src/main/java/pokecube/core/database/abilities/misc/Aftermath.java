package pokecube.core.database.abilities.misc;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Explosion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;
import pokecube.core.interfaces.Move_Base;

public class Aftermath extends Ability
{

    @Override
    public void onUpdate(IPokemob mob)
    {
    }

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob != move.attacked || move.pre || move.attacker == move.attacked) return;
        Move_Base attack = move.getMove();
        if(attack==null || (attack.getAttackCategory() & IMoveConstants.CATEGORY_CONTACT)== 0) return;
        
        if (((EntityLiving) mob).getHealth() <= 0)
        {
            Explosion boom = new Explosion(move.attacked.worldObj, move.attacked, move.attacked.posX,
                    move.attacked.posY, move.attacked.posZ, 0, false, false);
            ExplosionEvent evt = new ExplosionEvent.Start(move.attacked.worldObj, boom);
            MinecraftForge.EVENT_BUS.post(evt);
            if(!evt.isCanceled())
            {
                EntityLiving attacker = (EntityLiving) move.attacker;
                float hp = attacker.getHealth();
                attacker.attackEntityFrom(DamageSource.magic, hp/4);
            }
        }
    }

    @Override
    public void onAgress(IPokemob mob, EntityLivingBase target)
    {
    }

}
