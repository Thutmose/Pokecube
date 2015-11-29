package pokecube.adventures.handlers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class GeneralCommands implements ICommand  {

	private List<String> aliases;

	public static boolean TRAINERSDIE = false;
	
	public GeneralCommands() {
		this.aliases = new ArrayList<String>();
		this.aliases.add("pokeAdv");
		this.aliases.add("pokeadv");
	}

	@Override
	public String getCommandName() {
		return "pokeAdv";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "pokeAdv <text>";
	}

	@Override
	public List<String> getCommandAliases() {
		return this.aliases;
	}
	


	public boolean isOp(ICommandSender sender)
	{
		if(sender instanceof EntityPlayer)
		{
			EntityPlayer player = sender.getEntityWorld().getPlayerEntityByName(sender.getCommandSenderName());
			UserListOpsEntry userentry = (UserListOpsEntry)((EntityPlayerMP)player).mcServer.getConfigurationManager().getOppedPlayers().getEntry(player.getGameProfile());
			return userentry!=null&&userentry.getPermissionLevel()>=4;
		}
		else if(sender instanceof TileEntityCommandBlock)
		{
			return true;
		}
		return sender.getCommandSenderName().equalsIgnoreCase("@")||sender.getCommandSenderName().equals("Server");
	}

	@Override
	public void processCommand(ICommandSender cSender, String[] args) {
		if (args.length == 0) {
			cSender.addChatMessage(new ChatComponentText("Invalid arguments"));
			return;
		}
		for(int i = 1; i<args.length;i++)
		{
			String s = args[i];
			if(s.contains("@"))
			{
			}
		}
		boolean isOp = isOp(cSender);

		if(args[0].equalsIgnoreCase("trainerspawn")||args[0].equalsIgnoreCase("tspn"))
		{
			if(args.length == 1)
			{
				return;
			}
			if(args.length == 2)
			{
				String temp = args[1];
				boolean on = temp.equalsIgnoreCase("true")||temp.equalsIgnoreCase("on");
				boolean off = temp.equalsIgnoreCase("false")||temp.equalsIgnoreCase("off");
				if(off||on)
				{
					if(isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
					{
						ConfigHandler.trainerSpawn = on;
						cSender.addChatMessage(new ChatComponentText("Trainer Spawning is set to "+on));
						ConfigHandler.saveConfig();
						return;
					}
					else
					{
						cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
						return;
					}
				}
				
			}
		}

		if(args[0].equalsIgnoreCase("trainerinvul")||args[0].equalsIgnoreCase("tinv"))
		{
			if(args.length == 1)
			{
				return;
			}
			if(args.length == 2)
			{
				String temp = args[1];
				boolean on = temp.equalsIgnoreCase("true")||temp.equalsIgnoreCase("on");
				boolean off = temp.equalsIgnoreCase("false")||temp.equalsIgnoreCase("off");
				if(off||on)
				{
					if(isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
					{
						ConfigHandler.trainersInvul = on;
						cSender.addChatMessage(new ChatComponentText("Trainer Invulnerability is set to "+on));
						ConfigHandler.saveConfig();
						return;
					}
					else
					{
						cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
						return;
					}
						
				}
				
			}
		}

		if(args[0].equalsIgnoreCase("killtrainers"))
		{
			if(args.length == 1)
			{
				return;
			}
			if(args.length == 2)
			{
				String temp = args[1];
				boolean on = temp.equalsIgnoreCase("true")||temp.equalsIgnoreCase("on");
				boolean off = temp.equalsIgnoreCase("false")||temp.equalsIgnoreCase("off");
				if(off||on)
				{
					if(isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
					{
						TRAINERSDIE = on;
						cSender.addChatMessage(new ChatComponentText("Trainer all dieing set to "+on));
						ConfigHandler.saveConfig();
						return;
					}
					else
					{
						cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
						return;
					}
				}
				
			}
		}

	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
		return true;
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender p_71516_1_,
			String[] args, BlockPos pos) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}
	
    @Override
    public int compareTo(ICommand o)
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
