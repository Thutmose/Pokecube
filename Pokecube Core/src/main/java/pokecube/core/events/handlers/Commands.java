package pokecube.core.events.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.abilities.AbilityManager;
import pokecube.core.handlers.ConfigHandler;
import pokecube.core.interfaces.IMobColourable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.ItemTM;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeClientPacket;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.Tools;
import thut.api.maths.ExplosionCustom;
import thut.api.maths.Vector3;

public class Commands implements ICommand
{
    private List<String> aliases;

    public Commands()
    {
        this.aliases = new ArrayList<String>();
        this.aliases.add("pokecube");
        this.aliases.add("pqb");
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
    public List<String> getCommandAliases()
    {
        return this.aliases;
    }

    public boolean isOp(ICommandSender sender)
    {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() != null
                && !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) { return true; }

        if (sender instanceof EntityPlayer)
        {
            EntityPlayer player = sender.getEntityWorld().getPlayerEntityByName(sender.getName());
            UserListOpsEntry userentry = (UserListOpsEntry) ((EntityPlayerMP) player).mcServer.getConfigurationManager()
                    .getOppedPlayers().getEntry(player.getGameProfile());
            return userentry != null && userentry.getPermissionLevel() >= 4;
        }
        else if (sender instanceof TileEntityCommandBlock) { return true; }
        return sender.getName().equalsIgnoreCase("@") || sender.getName().equals("Server");
    }

    @Override
    public void processCommand(ICommandSender cSender, String[] args)
    {
        if (args.length == 0)
        {
            cSender.addChatMessage(new ChatComponentText("Invalid arguments"));
            return;
        }
        EntityPlayerMP[] targets = null;
        for (int i = 1; i < args.length; i++)
        {
            String s = args[i];
            if (s.contains("@"))
            {
                ArrayList<EntityPlayer> targs = new ArrayList<EntityPlayer>(
                        PlayerSelector.matchEntities(cSender, s, EntityPlayer.class));
                targets = (EntityPlayerMP[]) targs.toArray(new EntityPlayerMP[0]);
            }
        }
        boolean isOp = isOp(cSender);

        if (args[0].equalsIgnoreCase("gif") && args.length > 1 && cSender instanceof EntityPlayer)
        {
            String name = args[1];
            PokedexEntry entry = Database.getEntry(name);
            if (entry != null)
            {
                ByteBuf buffer = Unpooled.buffer(5);
                buffer.writeByte(PokecubeClientPacket.WIKIWRITE);
                buffer.writeInt(entry.getPokedexNb());
                PokecubeClientPacket packet = new PokecubeClientPacket(buffer);
                PokecubePacketHandler.sendToClient(packet, (EntityPlayer) cSender);
            }

            return;
        }
        doRecall(cSender, args, isOp, targets);
        doDebug(cSender, args, isOp, targets);
        doSettings(cSender, args, isOp, targets);
        doGift(cSender, args, isOp, targets);
        doMake(cSender, args, isOp, targets);
        doReset(cSender, args, isOp, targets);
        doTM(cSender, args, isOp, targets);
        doMeteor(cSender, args, isOp, targets);
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

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        boolean isOp = isOp(sender);
        if (args[0].isEmpty())
        {
            List<String> ret = new ArrayList<String>();
            ret.add("tm");
            ret.add("recall");
            ret.add("spawn");
            ret.add("spawns");
            ret.add("semihardmode");
            ret.add("hardmode");
            ret.add("explosions");
            ret.add("gift");
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
        if (args[0].equalsIgnoreCase("make"))
        {
            List<String> ret = new ArrayList<String>();
            if (args.length == 2)
            {
                String text = args[1];
                for (PokedexEntry entry : Database.allFormes)
                {
                    if (entry.getName().toLowerCase().contains(text.toLowerCase()))
                    {
                        String name = entry.getName();
                        if (name.contains(" "))
                        {
                            name = "\'" + name + "\'";
                        }
                        ret.add(name);
                    }
                }
            }
            return ret;
        }
        if (args[0].equalsIgnoreCase("tm"))
        {
            Collection<String> moves = MovesUtils.moves.keySet();
            List<String> ret = new ArrayList<String>();
            if (args.length == 2)
            {
                String text = args[1];
                for (String name : moves)
                {
                    if (name.contains(text))
                    {
                        ret.add(name);
                    }
                }
            }
            return ret;
        }
        return null;
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
                    cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
                    return false;
                }

            }
            ArrayList<?> list = new ArrayList<Object>(world.loadedEntityList);
            for (Object o : list)
            {
                if (o instanceof IPokemob)
                {
                    IPokemob mob = (IPokemob) o;

                    boolean isStaying = mob.getPokemonAIState(IPokemob.STAYING);
                    boolean isGuarding = mob.getPokemonAIState(IPokemob.GUARDING);

                    if (mob.getPokemonAIState(IPokemob.TAMED) && (mob.getPokemonOwner() == player || allall)
                            && (named || all || (stay == isStaying && guard == isGuarding))
                            && (named == specificName.equalsIgnoreCase(mob.getPokemonDisplayName())))
                        mob.returnToPokecube();
                }
            }
            return true;
        }
        return false;
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
                        if (id == -1 && !e.getPokemonAIState(IPokemob.TAMED) || all)
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
                cSender.addChatMessage(new ChatComponentText("Killed " + count));
                return true;
            }
            else
            {
                cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
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
                                    cSender.getPositionVector().zCoord) > Mod_Pokecube_Helper.mobDespawnRadius)
                                count2++;
                            else count1++;
                        }
                    }
                }
                cSender.addChatMessage(
                        new ChatComponentText("Found " + count1 + " in range and " + count2 + " out of range"));
                return true;
            }
            else
            {
                cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
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
                                    Mod_Pokecube_Helper.mobDespawnRadius) == null
                                    && !e.getPokemonAIState(IPokemob.TAMED))
                            {
                                ((Entity) e).setDead();
                                n++;
                            }
                        }
                    }
                }
                cSender.addChatMessage(new ChatComponentText("Culled " + n));
                return true;
            }
            else
            {
                cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
                return false;
            }
        }

        if (args[0].equalsIgnoreCase("items"))
        {

            WorldServer world = (WorldServer) cSender.getEntityWorld();
            List<Entity> items = world.getLoadedEntityList();
            for (Entity e : items)
            {
                if (e instanceof EntityItem) e.setDead();
            }
            return true;
        }
        return false;
    }

    private boolean doSettings(ICommandSender cSender, String[] args, boolean isOp, EntityPlayerMP[] targets)
    {
        if (args[0].equalsIgnoreCase("spawns"))
        {
            if (args.length == 1)
            {
                cSender.addChatMessage(new ChatComponentText("Pokemobs Spawning " + SpawnHandler.doSpawns));
                return true;
            }
            if (isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
            {
                if (args.length == 2)
                {
                    String temp = args[1];
                    boolean on = temp.equalsIgnoreCase("true") || temp.equalsIgnoreCase("on");
                    boolean off = temp.equalsIgnoreCase("false") || temp.equalsIgnoreCase("off");
                    if (on || off) SpawnHandler.doSpawns = on;
                    cSender.addChatMessage(new ChatComponentText("Pokemobs Spawning " + SpawnHandler.doSpawns));
                    ConfigHandler.saveConfig();
                    return true;
                }
            }
            else
            {
                cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
                return false;
            }
        }

        if (args[0].equalsIgnoreCase("spawn"))
        {
            if (args.length == 1)
            {
                cSender.addChatMessage(new ChatComponentText(
                        "Pokemob Spawn Info n:" + SpawnHandler.MAXNUM + ":d:" + Mod_Pokecube_Helper.mobDespawnRadius));
                return true;
            }
            if (isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
            {
                if (args.length == 3)
                {
                    SpawnHandler.MAXNUM = Integer.parseInt(args[1]);
                    Mod_Pokecube_Helper.mobDespawnRadius = Integer.parseInt(args[2]);
                    cSender.addChatMessage(new ChatComponentText("Pokemob Spawn Info n:" + SpawnHandler.MAXNUM + ":d:"
                            + Mod_Pokecube_Helper.mobDespawnRadius));
                    ConfigHandler.saveConfig();
                    return true;
                }
            }
            else
            {
                cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
                return false;
            }
        }

        if (args[0].equalsIgnoreCase("semihardmode") || args[0].equalsIgnoreCase("shm"))
        {
            if (args.length == 1)
            {
                cSender.addChatMessage(new ChatComponentText("SemiHardMode is set to " + PokecubeMod.pokemobsDamageBlocks));
                return true;
            }
            if (args.length == 2)
            {
                String temp = args[1];
                boolean on = temp.equalsIgnoreCase("true") || temp.equalsIgnoreCase("on");
                boolean off = temp.equalsIgnoreCase("false") || temp.equalsIgnoreCase("off");
                if (off || on)
                {
                    if (isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
                    {
                        PokecubeMod.pokemobsDamageBlocks = on;
                        cSender.addChatMessage(
                                new ChatComponentText("SemiHardMode is set to " + PokecubeMod.pokemobsDamageBlocks));
                        ConfigHandler.saveConfig();
                        return true;
                    }
                    else
                    {
                        cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
                        return false;
                    }
                }

            }
        }
        if (args[0].equalsIgnoreCase("hardmode") || args[0].equalsIgnoreCase("hm"))
        {
            if (args.length == 1)
            {
                cSender.addChatMessage(new ChatComponentText("HardMode is set to " + PokecubeMod.friendlyFire));
                return true;
            }
            if (args.length == 2)
            {
                String temp = args[1];
                boolean on = temp.equalsIgnoreCase("true") || temp.equalsIgnoreCase("on");
                boolean off = temp.equalsIgnoreCase("false") || temp.equalsIgnoreCase("off");
                if (off || on)
                {

                    if (isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
                    {
                        PokecubeMod.friendlyFire = on;
                        if (PokecubeMod.friendlyFire) PokecubeMod.pokemobsDamageBlocks = true;
                        cSender.addChatMessage(new ChatComponentText("HardMode is set to " + PokecubeMod.friendlyFire));
                        ConfigHandler.saveConfig();
                        return true;
                    }
                    else
                    {
                        cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
                        return false;
                    }

                }

            }
        }
        if (args[0].equalsIgnoreCase("explosions") || args[0].equalsIgnoreCase("boom"))
        {
            if (args.length == 1)
            {
                cSender.addChatMessage(new ChatComponentText("Explosion Damage is " + Mod_Pokecube_Helper.explosions));
                return true;
            }
            if (args.length == 2)
            {
                String temp = args[1];
                boolean on = temp.equalsIgnoreCase("true") || temp.equalsIgnoreCase("on");
                boolean off = temp.equalsIgnoreCase("false") || temp.equalsIgnoreCase("off");
                if (off || on)
                {
                    if (isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
                    {
                        Mod_Pokecube_Helper.explosions = on;
                        cSender.addChatMessage(
                                new ChatComponentText("Explosion Damage is " + Mod_Pokecube_Helper.explosions));
                        ConfigHandler.saveConfig();
                        return true;
                    }
                    else
                    {
                        cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private boolean doMake(ICommandSender cSender, String[] args, boolean isOp, EntityPlayerMP[] targets)
    {
        boolean deobfuscated = false;
        try
        {
            World.class.getDeclaredField("provider");
            deobfuscated = true;
        }
        catch (Exception e1)
        {
        }
        boolean commandBlock = !(cSender instanceof EntityPlayer);

        if ((deobfuscated || commandBlock) && args[0].equalsIgnoreCase("make"))
        {
            String name;
            IPokemob mob = null;
            if (args.length > 1)
            {
                int num = 1;
                int index = 2;
                EntityPlayer player = null;

                if (targets != null)
                {
                    num = targets.length;
                }
                for (int i = 0; i < num; i++)
                {
                    if (isOp || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
                    {
                        PokedexEntry entry = null;
                        try
                        {
                            int id = Integer.parseInt(args[1]);
                            entry = Database.getEntry(id);
                            name = entry.getName();
                        }
                        catch (NumberFormatException e)
                        {
                            name = args[1];
                            if (name.startsWith("\'"))
                            {

                                for (int j = 2; j < args.length; j++)
                                {
                                    name += " " + args[j];
                                    if (args[j].contains("\'"))
                                    {
                                        index = j + 1;
                                        break;
                                    }
                                }
                            }
                            entry = Database.getEntry(name);
                        }
                        if (entry == null)
                        {

                        }

                        mob = (IPokemob) PokecubeMod.core.createEntityByPokedexNb(entry.getPokedexNb(),
                                cSender.getEntityWorld());

                        if (mob == null)
                        {
                            cSender.addChatMessage(new ChatComponentText("Invalid mob name"));
                            return false;
                        }
                        mob.changeForme(name);
                        Vector3 offset = Vector3.getNewVector().set(0, 1, 0);

                        String owner = "";
                        boolean shiny = false;
                        boolean shadow = false;
                        int red, green, blue;
                        byte gender = -3;
                        red = green = blue = 255;
                        boolean ancient = false;
                        String ability = null;

                        int exp = 10;
                        int level = -1;
                        String[] moves = new String[4];
                        int mindex = 0;

                        if (index < args.length)
                        {
                            for (int j = index; j < args.length; j++)
                            {
                                String[] vals = args[j].split(":");
                                String arg = vals[0];
                                String val = "";
                                if (vals.length > 1) val = vals[1];
                                if (arg.equalsIgnoreCase("s"))
                                {
                                    shiny = true;
                                }
                                if (arg.equalsIgnoreCase("sh"))
                                {
                                    shadow = true;
                                }
                                else if (arg.equalsIgnoreCase("l"))
                                {
                                    level = Integer.parseInt(val);
                                    exp = Tools.levelToXp(mob.getExperienceMode(), level);
                                }
                                else if (arg.equalsIgnoreCase("x"))
                                {
                                    if (val.equalsIgnoreCase("f")) gender = IPokemob.FEMALE;
                                    if (val.equalsIgnoreCase("m")) gender = IPokemob.MALE;
                                }
                                else if (arg.equalsIgnoreCase("r"))
                                {
                                    red = Integer.parseInt(val);
                                }
                                else if (arg.equalsIgnoreCase("g"))
                                {
                                    green = Integer.parseInt(val);
                                }
                                else if (arg.equalsIgnoreCase("b"))
                                {
                                    blue = Integer.parseInt(val);
                                }
                                else if (arg.equalsIgnoreCase("o"))
                                {
                                    owner = val;
                                }
                                else if (arg.equalsIgnoreCase("a"))
                                {
                                    ability = val;
                                }
                                else if (arg.equalsIgnoreCase("m") && mindex < 4)
                                {
                                    moves[mindex] = val;
                                    mindex++;
                                }
                                else if (arg.equalsIgnoreCase("v"))
                                {
                                    String[] vec = val.split(",");
                                    offset.x = Double.parseDouble(vec[0].trim());
                                    offset.y = Double.parseDouble(vec[1].trim());
                                    offset.z = Double.parseDouble(vec[2].trim());
                                }
                            }
                        }

                        Vector3 temp = Vector3.getNewVector();
                        if (player != null)
                        {
                            offset = offset.add(temp.set(player.getLookVec()));
                        }
                        temp.set(cSender.getPosition()).addTo(offset);
                        temp.moveEntity((Entity) mob);

                        if (targets != null)
                        {
                            player = targets[i];
                            if (player != null)
                            {
                                owner = targets[i].getUniqueID().toString();
                            }
                            else
                            {
                                owner = "";
                            }
                        }
                        else
                        {
                            EntityPlayer p = cSender.getEntityWorld().getPlayerEntityByName(owner);
                            if (p != null) owner = p.getUniqueID().toString();
                        }

                        mob.setHp(((EntityLiving) mob).getMaxHealth());
                        if (!owner.equals(""))
                        {
                            mob.setPokemonOwnerByName(owner);
                            mob.setPokemonAIState(IPokemob.TAMED, true);
                        }
                        mob.setShiny(shiny);
                        if (gender != -3) mob.setSexe(gender);
                        if (mob instanceof IMobColourable) ((IMobColourable) mob).setRGBA(red, green, blue, 255);
                        if (shadow) mob.setShadow(shadow);
                        if (ancient) mob.setAncient(ancient);
                        mob.setExp(exp, true, true);
                        if (AbilityManager.abilityExists(ability)) mob.setAbility(AbilityManager.getAbility(ability));

                        for (int i1 = 0; i1 < 4; i1++)
                        {
                            if (moves[i1] != null)
                            {
                                String arg = moves[i1];
                                if (!arg.isEmpty())
                                {
                                    if (arg.equalsIgnoreCase("none"))
                                    {
                                        mob.setMove(i1, null);
                                    }
                                    else
                                    {
                                        mob.setMove(i1, arg);
                                    }
                                }
                            }
                        }
                        mob.specificSpawnInit();
                        if (mob != null)
                        {
                            ((Entity) mob).worldObj.spawnEntityInWorld((Entity) mob);
                        }
                        cSender.addChatMessage(new ChatComponentText("Spawned " + mob));
                        return true;
                    }
                }
            }

            return false;
        }
        return false;
    }

    private boolean doTM(ICommandSender cSender, String[] args, boolean isOp, EntityPlayerMP[] targets)
    {
        if (args[0].equalsIgnoreCase("tm"))
        {
            if (args.length == 1) { return false; }
            if (args.length >= 2)
            {
                String temp = args[1];

                boolean isMove = false;
                isMove = MovesUtils.isMoveImplemented(temp);

                if (isMove)
                {
                    int num = 1;
                    int index = 0;
                    String name = null;
                    EntityPlayer player = null;

                    WorldServer world = (WorldServer) cSender.getEntityWorld();
                    if (targets != null)
                    {
                        num = targets.length;
                    }
                    else if (args.length == 3)
                    {
                        name = args[2];
                        player = world.getPlayerEntityByName(name);
                    }

                    for (int i = 0; i < num; i++)
                    {
                        ItemStack tm = PokecubeItems.getStack("tm");
                        ItemTM.addMoveToStack(temp, tm);

                        if (targets != null)
                        {
                            player = targets[index];
                        }

                        if (player == null) player = world.getPlayerEntityByName(cSender.getName());

                        if (player != null && isOp
                                || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer())
                        {
                            player.inventory.addItemStackToInventory(tm);
                        }
                        else
                        {
                            cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
                            return false;
                        }
                    }
                    return true;

                }

            }
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
                    cSender.addChatMessage(new ChatComponentText("Reset Starter for " + player.getName()));
                    player.addChatMessage(new ChatComponentText("You may choose a new starter"));

                }
                else
                {
                    cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
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

                            cSender.addChatMessage(new ChatComponentText("Reset Starter for " + player.getName()));
                            player.addChatMessage(new ChatComponentText("You may choose a new starter"));
                        }
                    }
                    else
                    {
                        cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
                        return false;
                    }
                return true;
            }
        }
        return false;
    }

    private boolean doGift(ICommandSender cSender, String[] args, boolean isOp, EntityPlayerMP[] targets)
    {
        if (args[0].equalsIgnoreCase("gift") && cSender instanceof EntityPlayer)
        {
            if (!Mod_Pokecube_Helper.mysterygift)
            {
                cSender.addChatMessage(new ChatComponentText("Mysterygift is not enabled on this world"));
                return false;
            }
            if (args.length > 1)
            {
                String code = args[1];
                String giftSt = PokecubeMod.gifts.get(code);
                if (giftSt != null)
                {
                    EntityPlayer player = (EntityPlayer) cSender;

                    if (player.getEntityData().getString("code:" + code).equals(code))
                    {
                        cSender.addChatMessage(new ChatComponentText("You've already used this code!"));
                        return false;
                    }
                    String[] gift = giftSt.split(";");

                    String name = gift[0];

                    IPokemob mob = (IPokemob) PokecubeMod.core
                            .createEntityByPokedexNb(Database.getEntry(name).getPokedexNb(), cSender.getEntityWorld());

                    boolean shiny = false;
                    boolean shadow = false;
                    int red, green, blue;
                    byte gender = -3;
                    red = green = blue = 255;

                    int exp = 10;
                    int level = -1;
                    String ability = null;
                    String[] moves = new String[4];
                    int index = 0;
                    for (int i = 1; i < gift.length; i++)
                    {
                        String[] vals = gift[i].trim().split(":");
                        String arg = vals[0].trim();
                        String val = "";
                        if (vals.length > 1) val = vals[1];
                        if (arg.equalsIgnoreCase("s"))
                        {
                            shiny = true;
                        }
                        if (arg.equalsIgnoreCase("sh"))
                        {
                            shadow = true;
                        }
                        else if (arg.equalsIgnoreCase("l"))
                        {
                            level = Integer.parseInt(val);
                            exp = Tools.levelToXp(mob.getExperienceMode(), level);
                        }
                        else if (arg.equalsIgnoreCase("x"))
                        {
                            if (val.equalsIgnoreCase("f")) gender = IPokemob.FEMALE;
                            if (val.equalsIgnoreCase("m")) gender = IPokemob.MALE;
                        }
                        else if (arg.equalsIgnoreCase("r"))
                        {
                            red = Byte.parseByte(val);
                        }
                        else if (arg.equalsIgnoreCase("g"))
                        {
                            green = Byte.parseByte(val);
                        }
                        else if (arg.equalsIgnoreCase("b"))
                        {
                            blue = Byte.parseByte(val);
                        }
                        else if (arg.equalsIgnoreCase("a"))
                        {
                            ability = val;
                        }
                        else if (arg.equalsIgnoreCase("m") && index < 4)
                        {
                            moves[index] = val;
                            index++;
                        }
                        else if (arg.equalsIgnoreCase("n") && !val.isEmpty())
                        {
                            mob.setPokemonNickname(val);
                        }
                    }
                    mob.setOriginalOwnerUUID(new UUID(12345, 54321));
                    mob.setPokecubeId(13);
                    mob.setExp(exp, false, true);
                    mob.setShiny(shiny);
                    if (gender != -3) mob.setSexe(gender);
                    if (mob instanceof IMobColourable) ((IMobColourable) mob).setRGBA(red, green, blue, 255);
                    if (shadow) mob.setShadow(shadow);
                    if (AbilityManager.abilityExists(ability)) mob.setAbility(AbilityManager.getAbility(ability));
                    for (int i = 0; i < 4; i++)
                    {
                        if (moves[i] != null)
                        {
                            String arg = moves[i];
                            if (!arg.isEmpty())
                            {
                                if (arg.equalsIgnoreCase("none"))
                                {
                                    mob.setMove(i, null);
                                }
                                else
                                {
                                    mob.setMove(i, arg);
                                }
                            }
                        }
                    }
                    mob.setPokemonOwner(player);
                    mob.setHp(((EntityLiving) mob).getMaxHealth());
                    mob.returnToPokecube();

                    cSender.addChatMessage(new ChatComponentText("Congratulations!"));
                    player.getEntityData().setString("code:" + code, code);

                    return true;
                }
                cSender.addChatMessage(new ChatComponentText("The code " + args[1] + " is invalid!"));
                return false;
            }
            cSender.addChatMessage(new ChatComponentText("You need to enter a code!"));
            return false;
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
                cSender.addChatMessage(new ChatComponentText("Insufficient Permissions"));
                return false;
            }
        }
        return false;
    }

    @Override
    public int compareTo(ICommand arg0)
    {
        return 0;
    }
}
