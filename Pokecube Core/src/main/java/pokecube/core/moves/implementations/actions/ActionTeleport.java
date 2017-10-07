package pokecube.core.moves.implementations.actions;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import thut.api.maths.Vector3;

public class ActionTeleport implements IMoveAction
{
    /** Teleport the entity to a random nearby position */
    public static boolean teleportRandomly(EntityLivingBase toTeleport)
    {
        double var1;
        double var3;
        double var5;
        Vector3 v = SpawnHandler.getRandomSpawningPointNearEntity(toTeleport.getEntityWorld(), toTeleport, 32, 0);
        if (v == null) return false;
        v = Vector3.getNextSurfacePoint(toTeleport.getEntityWorld(), v, Vector3.secondAxisNeg, Math.max(v.y, 10));
        if (v == null) return false;
        var1 = v.x;
        var3 = v.y + 1;
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
            toTeleport.getEntityWorld().spawnParticle(EnumParticleTypes.PORTAL, var24, var26, var28, var21, var22,
                    var23);
        }

        toTeleport.getEntityWorld().playSound(par1, par3, par5, SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                SoundCategory.HOSTILE, 1.0F, 1.0F, false);
        toTeleport.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
        return true;

    }

    public ActionTeleport()
    {
    }

    @Override
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        boolean angry = user.getPokemonAIState(IMoveConstants.ANGRY)
                || !(user.getPokemonAIState(IPokemob.TAMED) && !user.getPokemonAIState(IPokemob.STAYING));
        if (!angry && user.getPokemonOwner() instanceof EntityPlayer && user.getEntity().isServerWorld())
        {
            EntityPlayer target = (EntityPlayer) user.getPokemonOwner();
            EventsHandler.recallAllPokemobsExcluding(target, null);
            PokecubeClientPacket packet = new PokecubeClientPacket(new byte[] { PokecubeClientPacket.TELEPORTINDEX });
            PokecubePacketHandler.sendToClient(packet, target);
        }
        else if (angry)
        {
            user.setPokemonAIState(IMoveConstants.ANGRY, false);
            if (user.getPokemonAIState(IMoveConstants.TAMED)) user.returnToPokecube();
            else teleportRandomly(user.getEntity());
        }
        return true;
    }

    @Override
    public String getMoveName()
    {
        return "teleport";
    }
}
