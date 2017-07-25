package pokecube.core.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.packets.PacketChoose;
import pokecube.core.utils.PokecubeSerializer;

public class ResetCommand extends CommandBase
{
    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getName()
    {
        return "pokereset";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/pokereset <player>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender cSender, String[] args) throws CommandException
    {
        EntityPlayer player = args.length == 0 ? getCommandSenderAsPlayer(cSender)
                : getPlayer(server, cSender, args[0]);
        PokecubeSerializer.getInstance().setHasStarter(player, false);
        PacketChoose packet = new PacketChoose(PacketChoose.OPENGUI);
        packet.data.setBoolean("C", false);
        packet.data.setBoolean("H", false);
        PokecubePacketHandler.sendToClient(packet, player);
        cSender.sendMessage(CommandTools.makeTranslatedMessage("pokecube.command.reset", "", player.getName()));
        CommandTools.sendMessage(player, "pokecube.command.canchoose");
    }
}
