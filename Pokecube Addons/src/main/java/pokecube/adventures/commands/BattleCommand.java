package pokecube.adventures.commands;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;

public class BattleCommand extends CommandBase
{

    @Override
    public String getName()
    {
        return "pokebattle";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/pokebattle <player>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length != 1) { throw new CommandException(getUsage(sender)); }
        final EntityPlayer player = getPlayer(server, sender, args[0]);
        // Use 32 blocks as farthest to look for trainer.
        final int NEAR = 32 * 32;

        List<EntityLiving> trainers = player.getEntityWorld().getEntities(EntityLiving.class,
                new Predicate<EntityLiving>()
                {
                    @Override
                    public boolean apply(EntityLiving input)
                    {
                        return input.hasCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null)
                                && input.getDistanceSq(player) < NEAR;
                    }
                });
        EntityLiving target = null;
        int closest = NEAR;
        for (EntityLiving e : trainers)
        {
            double d;
            if ((d = e.getDistanceSq(player)) < closest)
            {
                closest = (int) d;
                target = e;
            }
        }
        if (target != null)
        {
            CapabilityHasPokemobs.IHasPokemobs trainer = target.getCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP,
                    null);
            if (trainer.getTarget() == null) trainer.setTarget(player);
            else throw new CommandException("%s already has a target.", trainer);
            return;
        }

    }

}
