package pokecube.core.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.maths.ExplosionCustom;
import thut.api.maths.Vector3;

public class Commands implements ICommand
{
    private List<String> aliases;

    public Commands()
    {
        this.aliases = new ArrayList<String>();
        this.aliases.add("pokecube");
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public int compareTo(ICommand arg0)
    {
        return 0;
    }

    private boolean doDebug(ICommandSender cSender, String[] args, boolean isOp, EntityPlayerMP[] targets)
    {

        if (args[0].equalsIgnoreCase("kill"))
        {
            boolean all = args.length > 1 && args[1].equalsIgnoreCase("all");

            int id = -1;
            if (args.length > 1)
            {
                try
                {
                    id = Integer.parseInt(args[1]);
                }
                catch (NumberFormatException e)
                {

                }
            }

            if (isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
            {
                World world = cSender.getEntityWorld();
                List<?> entities = new ArrayList<Object>(world.loadedEntityList);
                int count = 0;
                for (Object o : entities)
                {
                    if (o instanceof IPokemob)
                    {
                        IPokemob e = (IPokemob) o;
                        if (id == -1 && !e.getPokemonAIState(IMoveConstants.TAMED) || all)
                        {
                            ((Entity) e).setDead();
                            count++;
                        }
                        if (id != -1 && ((Entity) e).getEntityId() == id)
                        {
                            ((Entity) e).setDead();
                            count++;
                        }
                    }
                }
                cSender.addChatMessage(new TextComponentString("Killed " + count));
                return true;
            }
            else
            {
                CommandTools.sendNoPermissions(cSender);
                return false;
            }
        }
        if (args[0].equalsIgnoreCase("count"))
        {
            boolean all = args.length > 1;
            if (isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
            {
                World world = cSender.getEntityWorld();
                List<?> entities = new ArrayList<Object>(world.loadedEntityList);
                int count1 = 0;
                int count2 = 0;
                String name = "";
                if (all)
                {
                    name = args[1];
                }
                for (Object o : entities)
                {
                    if (o instanceof IPokemob)
                    {
                        IPokemob e = (IPokemob) o;
                        // System.out.println(e);
                        if (!all || e.getPokedexEntry() == Database.getEntry(name))
                        {
                            if (((Entity) e).getDistance(cSender.getPositionVector().xCoord,
                                    cSender.getPositionVector().yCoord,
                                    cSender.getPositionVector().zCoord) > PokecubeMod.core.getConfig().maxSpawnRadius)
                                count2++;
                            else count1++;
                        }
                    }
                }
                cSender.addChatMessage(
                        CommandTools.makeTranslatedMessage("pokecube.command.count", "", count1, count2));
                return true;
            }
            else
            {
                CommandTools.sendNoPermissions(cSender);
                return false;
            }
        }
        if (args[0].equalsIgnoreCase("cull"))
        {
            boolean all = args.length > 1;
            if (isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
            {
                World world = cSender.getEntityWorld();
                List<?> entities = new ArrayList<Object>(world.loadedEntityList);
                String name = "";
                if (all)
                {
                    name = args[1];
                }
                int n = 0;
                for (Object o : entities)
                {
                    if (o instanceof IPokemob)
                    {
                        IPokemob e = (IPokemob) o;
                        if (!all || e.getPokedexEntry() == Database.getEntry(name))
                        {
                            if (((Entity) e).worldObj.getClosestPlayerToEntity((Entity) e,
                                    PokecubeMod.core.getConfig().maxSpawnRadius) == null
                                    && !e.getPokemonAIState(IMoveConstants.TAMED))
                            {
                                ((Entity) e).setDead();
                                n++;
                            }
                        }
                    }
                }
                cSender.addChatMessage(new TextComponentString("Culled " + n));
                return true;
            }
            else
            {
                CommandTools.sendNoPermissions(cSender);
                return false;
            }
        }

        if (args[0].equalsIgnoreCase("items"))
        {

            WorldServer world = (WorldServer) cSender.getEntityWorld();
            List<Entity> items = world.loadedEntityList;
            for (Entity e : items)
            {
                if (e instanceof EntityItem) e.setDead();
            }
            return true;
        }
        return false;
    }

    private boolean doMeteor(ICommandSender cSender, String[] args, boolean isOp, EntityPlayerMP[] targets)
    {

        if (args[0].equalsIgnoreCase("meteor"))
        {
            if (isOp)
            {
                Random rand = new Random();
                float energy = (float) Math.abs((rand.nextGaussian() + 1) * 50);
                if (args.length > 1)
                {
                    try
                    {
                        energy = Float.parseFloat(args[1]);
                    }
                    catch (NumberFormatException e)
                    {

                    }
                }
                Vector3 v = Vector3.getNewVector().set(cSender).add(0, 255 - cSender.getPosition().getY(), 0);
                if (energy > 0)
                {
                    Vector3 location = Vector3.getNextSurfacePoint(cSender.getEntityWorld(), v, Vector3.secondAxisNeg,
                            255);
                    ExplosionCustom boom = new ExplosionCustom(cSender.getEntityWorld(),
                            PokecubeMod.getFakePlayer(cSender.getEntityWorld()), location, energy).setMeteor(true);
                    boom.doExplosion();
                }
                PokecubeSerializer.getInstance().addMeteorLocation(v);
                return true;
            }
            else
            {
                CommandTools.sendNoPermissions(cSender);
                return false;
            }
        }
        return false;
    }

    private boolean doRecall(ICommandSender cSender, String[] args, boolean isOp, EntityPlayerMP[] targets)
    {
        if (args[0].equalsIgnoreCase("recall"))
        {
            String sender = cSender.getName();
            boolean all = args.length > 1 && args[1].equalsIgnoreCase("all");
            boolean allall = args.length > 2 && args[2].equalsIgnoreCase("all");
            boolean guard = args.length > 1 && args[1].equalsIgnoreCase("guard");
            boolean stay = args.length > 1 && args[1].equalsIgnoreCase("stay");

            boolean named = !all && !guard && !stay && args.length > 1;
            String specificName = named ? args[1] : "";

            WorldServer world = (WorldServer) cSender.getEntityWorld();

            EntityPlayer player = cSender.getEntityWorld().getPlayerEntityByName(sender);
            if (allall && cSender.getEntityWorld().getPlayerEntityByName(sender) != null)
            {
                allall = isOp;
                if (!allall)
                {
                    allall = false;
                    CommandTools.sendNoPermissions(cSender);
                    return false;
                }

            }
            ArrayList<?> list = new ArrayList<Object>(world.loadedEntityList);
            for (Object o : list)
            {
                if (o instanceof IPokemob)
                {
                    IPokemob mob = (IPokemob) o;

                    boolean isStaying = mob.getPokemonAIState(IMoveConstants.STAYING);
                    boolean isGuarding = mob.getPokemonAIState(IMoveConstants.GUARDING);

                    if (mob.getPokemonAIState(IMoveConstants.TAMED) && (mob.getPokemonOwner() == player || allall)
                            && (named || all || (stay == isStaying && guard == isGuarding))
                            && (named == specificName.equalsIgnoreCase(mob.getPokemonDisplayName().getUnformattedComponentText())))
                        mob.returnToPokecube();
                }
            }
            return true;
        }
        return false;
    }

    private boolean doReset(ICommandSender cSender, String[] args, boolean isOp, EntityPlayerMP[] targets)
    {
        if (args[0].equalsIgnoreCase("reset"))
        {
            if (args.length == 1 && cSender instanceof EntityPlayer)
            {
                if (isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
                {
                    EntityPlayer player = (EntityPlayer) cSender;
                    PokecubeSerializer.getInstance().setHasStarter(player, false);
                    ByteBuf buf = Unpooled.buffer(3);
                    buf.writeByte(PokecubeClientPacket.CHOOSE1ST);
                    buf.writeBoolean(false);
                    buf.writeBoolean(true);
                    PokecubeClientPacket packet = new PokecubeClientPacket(buf);
                    PokecubePacketHandler.sendToClient(packet, player);
                    cSender.addChatMessage(
                            CommandTools.makeTranslatedMessage("pokecube.command.reset", "", player.getName()));
                    CommandTools.sendMessage(player, "pokecube.command.canchoose");

                }
                else
                {
                    CommandTools.sendNoPermissions(cSender);
                    return false;
                }

                return true;
            }
            if (args.length == 2)
            {
                WorldServer world = (WorldServer) cSender.getEntityWorld();
                EntityPlayer player = null;

                int num = 1;
                int index = 0;
                String name = null;

                if (targets != null)
                {
                    num = targets.length;
                }
                else
                {
                    name = args[1];
                    player = world.getPlayerEntityByName(name);
                }

                for (int i = 0; i < num; i++)
                    if (isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
                    {
                        if (targets != null)
                        {
                            player = targets[index];
                        }
                        if (player != null)
                        {
                            PokecubeSerializer.getInstance().setHasStarter(player, false);
                            ByteBuf buf = Unpooled.buffer(3);
                            buf.writeByte(PokecubeClientPacket.CHOOSE1ST);
                            buf.writeBoolean(false);
                            buf.writeBoolean(true);
                            PokecubeClientPacket packet = new PokecubeClientPacket(buf);
                            PokecubePacketHandler.sendToClient(packet, player);

                            cSender.addChatMessage(
                                    CommandTools.makeTranslatedMessage("pokecube.command.reset", "", player.getName()));
                            CommandTools.sendMessage(player, "pokecube.command.canchoose");
                        }
                    }
                    else
                    {
                        CommandTools.sendNoPermissions(cSender);
                        return false;
                    }
                return true;
            }
        }
        return false;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0)
        {
            CommandTools.sendBadArgumentsTryTab(sender);
            return;
        }
        EntityPlayerMP[] targets = null;
        for (int i = 1; i < args.length; i++)
        {
            String s = args[i];
            if (s.contains("@"))
            {
                ArrayList<EntityPlayer> targs = new ArrayList<EntityPlayer>(
                        EntitySelector.matchEntities(sender, s, EntityPlayer.class));
                targets = targs.toArray(new EntityPlayerMP[0]);
            }
        }
        boolean isOp = CommandTools.isOp(sender);

        if (args[0].equalsIgnoreCase("gif") && args.length > 1 && sender instanceof EntityPlayer)
        {
            String name = args[1];
            PokedexEntry entry = Database.getEntry(name);
            if (entry != null)
            {
                ByteBuf buffer = Unpooled.buffer(5);
                buffer.writeByte(PokecubeClientPacket.WIKIWRITE);
                buffer.writeInt(entry.getPokedexNb());
                PokecubeClientPacket packet = new PokecubeClientPacket(buffer);
                PokecubePacketHandler.sendToClient(packet, (EntityPlayer) sender);
            }

            return;
        }
        boolean message = false;
        message |= doRecall(sender, args, isOp, targets);
        message |= doDebug(sender, args, isOp, targets);
        message |= doReset(sender, args, isOp, targets);
        message |= doMeteor(sender, args, isOp, targets);
        if (!message)
        {
            CommandTools.sendBadArgumentsTryTab(sender);
        }
    }

    @Override
    public List<String> getCommandAliases()
    {
        return this.aliases;
    }

    @Override
    public String getCommandName()
    {
        return "pokecube";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "pokecube <text>";
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
            BlockPos pos)
    {
        boolean isOp = CommandTools.isOp(sender);
        if (args[0].isEmpty())
        {
            List<String> ret = new ArrayList<String>();
            ret.add("recall");
            if (isOp)
            {
                ret.add("count");
                ret.add("kill");
                ret.add("cull");
                ret.add("reset");
            }
            return ret;
        }
        if (args[0].equalsIgnoreCase("recall"))
        {
            List<String> ret = new ArrayList<String>();
            if (args.length == 2)
            {
                ret.add("all");
                ret.add("guard");
                ret.add("stay");
                ret.add("<name>");
                if (isOp)
                {
                    ret.add("all all");
                }
            }
            return ret;
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] astring, int i)
    {
        String arg = astring[0];
        if (arg.equalsIgnoreCase("make"))
        {
            int j = astring.length - 1;
            return i == j;
        }
        if (arg.equalsIgnoreCase("tm") || arg.equalsIgnoreCase("reset"))
        {
            if (arg.equalsIgnoreCase("reset")) return i == 1;
            return i == 2;

        }
        return false;
    }
}
