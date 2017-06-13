package com.mcf.davidee.nbteditpqb;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Lists;
import com.mcf.davidee.nbteditpqb.packets.MouseOverPacket;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;
import thut.core.common.handlers.PlayerDataHandler.PlayerDataManager;

public class CommandNBTEdit extends CommandBase
{

    @Override
    public String getName()
    {
        return "pcedit";
    }

    @Override
    public String getUsage(ICommandSender par1ICommandSender)
    {
        return "/pcedit OR /pcedit <EntityId> OR /pcedit <TileX> <TileY> <TileZ>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (sender instanceof EntityPlayerMP)
        {
            EntityPlayerMP player = (EntityPlayerMP) sender;

            if (args.length == 3)
            {
                int x = parseInt(args[0]);
                int y = parseInt(args[1]);
                int z = parseInt(args[2]);
                NBTEdit.log(Level.TRACE,
                        sender.getName() + " issued command \"/pcedit " + x + " " + y + " " + z + "\"");
                NBTEdit.NETWORK.sendTile(player, new BlockPos(x, y, z));

            }
            else if (args.length == 1)
            {
                int entityID = (args[0].equalsIgnoreCase("me")) ? player.getEntityId() : parseInt(args[0], 0);
                NBTEdit.log(Level.TRACE, sender.getName() + " issued command \"/pcedit " + entityID + "\"");
                NBTEdit.NETWORK.sendEntity(player, entityID);

            }
            else if (args.length == 0)
            {
                NBTEdit.log(Level.TRACE, sender.getName() + " issued command \"/pcedit\"");
                NBTEdit.NETWORK.INSTANCE.sendTo(new MouseOverPacket(), player);

            }
            else if (args.length == 2)
            {
                EntityPlayer target = getPlayer(server, sender, args[0]);
                String value = args[1];
                NBTEdit.log(Level.TRACE, sender.getName() + " issued command \"/pcedit\"");
                System.out.println(target+" "+value);
                NBTEdit.NETWORK.sendCustomTag(player, target.getEntityId(), value);
            }
            else
            {
                String s = "";
                for (int i = 0; i < args.length; ++i)
                {
                    s += args[i];
                    if (i != args.length - 1) s += " ";
                }
                NBTEdit.log(Level.TRACE, sender.getName() + " issued invalid command \"/pcedit " + s + "\"");
                throw new WrongUsageException("Pass 0, 1, or 3 integers -- ex. /pcedit");
            }
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return sender instanceof EntityPlayer && NBTEdit.proxy.checkPermission((EntityPlayer) sender);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
            @Nullable BlockPos pos)
    {
        try
        {
            EntityPlayer player = getCommandSenderAsPlayer(sender);

            if (args.length == 2)
            {
                PlayerDataManager manager = PokecubePlayerDataHandler.getInstance().getPlayerData(player);
                Collection<PlayerData> data = manager.data.values();
                List<String> options = Lists.newArrayList();
                for (PlayerData d : data)
                {
                    options.add(d.getIdentifier());
                }
                return getListOfStringsMatchingLastWord(args, options);
            }
            else if(args.length == 1)
            {
            }
        }
        catch (PlayerNotFoundException e)
        {

        }
        return Collections.<String> emptyList();
    }
}
