package pokecube.core.commands;

import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.boom.ExplosionCustom;
import thut.api.maths.Vector3;

public class MeteorCommand extends CommandBase
{
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getCommandName()
    {
        return "pokemeteor";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/pokemeteor <optional:number -> power>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender cSender, String[] args) throws CommandException
    {
        Random rand = new Random();
        float energy = (float) Math.abs((rand.nextGaussian() + 1) * 50);
        if (args.length > 0)
        {
            try
            {
                energy = Float.parseFloat(args[0]);
            }
            catch (NumberFormatException e)
            {
            }
        }
        Vector3 v = Vector3.getNewVector().set(cSender).add(0, 255 - cSender.getPosition().getY(), 0);
        if (energy > 0)
        {
            Vector3 location = Vector3.getNextSurfacePoint(cSender.getEntityWorld(), v, Vector3.secondAxisNeg, 255);
            ExplosionCustom boom = new ExplosionCustom(cSender.getEntityWorld(),
                    PokecubeMod.getFakePlayer(cSender.getEntityWorld()), location, energy).setMeteor(true);
            boom.doExplosion();
        }
        PokecubeSerializer.getInstance().addMeteorLocation(v);
    }
}
