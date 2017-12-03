package pokecube.adventures.entity.helper;

import net.minecraft.entity.player.EntityPlayer;

public class Action
{
    final String command;

    public Action(String command)
    {
        this.command = command;
    }

    public void doAction(EntityPlayer target)
    {
        if (command == null || command.trim().isEmpty()) return;
        String editedCommand = command;
        editedCommand = editedCommand.replace("@p", target.getGameProfile().getName());
        editedCommand = editedCommand.replace("'x'", target.posX + "");
        editedCommand = editedCommand.replace("'y'", (target.posY + 1) + "");
        editedCommand = editedCommand.replace("'z'", target.posZ + "");
        target.getServer().getCommandManager().executeCommand(target.getServer(), editedCommand);
    }

    public String getCommand()
    {
        return command;
    }
}
