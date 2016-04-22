package pokecube.core.moves.teleport;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_Utility;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import thut.api.maths.Vector3;

public class Move_Teleport extends Move_Utility
{

    /** Teleport the entity to a random nearby position */
    public static boolean teleportRandomly(EntityLivingBase toTeleport)
    {
        double var1;
        double var3;
        double var5;
        Vector3 v = SpawnHandler.getRandomSpawningPointNearEntity(toTeleport.worldObj, toTeleport, 32);
        var1 = v.x;
        var3 = v.y;
        var5 = v.z;
        return teleportTo(toTeleport, var1, var3, var5);
    }

    /** Teleport the entity */
    protected static boolean teleportTo(EntityLivingBase toTeleport, double par1, double par3, double par5)
    {

        short var30 = 128;
        int num;

        toTeleport.setPosition(par1, par3, par5);

        for (num = 0; num < var30; ++num)
        {
            double var19 = num / (var30 - 1.0D);
            float var21 = (toTeleport.getRNG().nextFloat() - 0.5F) * 0.2F;
            float var22 = (toTeleport.getRNG().nextFloat() - 0.5F) * 0.2F;
            float var23 = (toTeleport.getRNG().nextFloat() - 0.5F) * 0.2F;
            double var24 = par1 + (toTeleport.posX - par1) * var19
                    + (toTeleport.getRNG().nextDouble() - 0.5D) * toTeleport.width * 2.0D;
            double var26 = par3 + (toTeleport.posY - par3) * var19
                    + toTeleport.getRNG().nextDouble() * toTeleport.height;
            double var28 = par5 + (toTeleport.posZ - par5) * var19
                    + (toTeleport.getRNG().nextDouble() - 0.5D) * toTeleport.width * 2.0D;
            toTeleport.worldObj.spawnParticle(EnumParticleTypes.PORTAL, var24, var26, var28, var21, var22, var23);
        }

        toTeleport.worldObj.playSoundEffect(par1, par3, par5, "mob.endermen.portal", 1.0F, 1.0F);
        toTeleport.playSound("mob.endermen.portal", 1.0F, 1.0F);
        return true;

    }

    public Move_Teleport(String name)
    {
        super(name);
    }

    @Override
    public void attack(IPokemob attacker, Entity attacked, float f)
    {
        Entity target = ((EntityCreature) attacker).getAttackTarget();
        boolean angry = attacker.getPokemonAIState(IMoveConstants.ANGRY);
        ((EntityCreature) attacker).setAttackTarget(null);
        attacker.setPokemonAIState(IMoveConstants.ANGRY, false);

        if (attacked instanceof EntityLiving)
        {
            ((EntityLiving) attacked).setAttackTarget(null);
        }
        if (attacked instanceof EntityCreature)
        {
            ((EntityCreature) attacker).setAttackTarget(null);
        }
        if (attacked instanceof IPokemob)
        {
            ((IPokemob) attacked).setPokemonAIState(IMoveConstants.ANGRY, false);
        }
        if (attacker instanceof IPokemob && angry)
        {
            if (attacker.getPokemonAIState(IMoveConstants.TAMED)) attacker.returnToPokecube();
            else teleportRandomly((EntityLivingBase) attacker);
        }
        if (attacker instanceof IPokemob && attacker.getPokemonAIState(IMoveConstants.TAMED) && !angry)
        {
            if (target == null)
            {
                if (attacker.getPokemonOwner() instanceof EntityPlayer && ((EntityLivingBase) attacker).isServerWorld())
                {
                    EventsHandler.recallAllPokemobsExcluding((EntityPlayer) attacker.getPokemonOwner(),
                            (IPokemob) null);

                    PokecubeClientPacket packet = new PokecubeClientPacket(
                            new byte[] { PokecubeClientPacket.TELEPORTINDEX });
                    PokecubePacketHandler.sendToClient(packet, (EntityPlayer) attacker.getPokemonOwner());
                }
            }
        }
    }

    @Override
    public void doWorldAction(IPokemob user, Vector3 location)
    {
        boolean angry = user.getPokemonAIState(IMoveConstants.ANGRY);
        if (!angry && user.getPokemonOwner() instanceof EntityPlayer && ((EntityLivingBase) user).isServerWorld())
        {
            EventsHandler.recallAllPokemobsExcluding((EntityPlayer) user.getPokemonOwner(), (IPokemob) null);
            PokecubeClientPacket packet = new PokecubeClientPacket(new byte[] { PokecubeClientPacket.TELEPORTINDEX });
            PokecubePacketHandler.sendToClient(packet, (EntityPlayer) user.getPokemonOwner());
        }
    }
}
