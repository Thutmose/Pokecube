package pokecube.adventures;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import pokecube.adventures.blocks.legendary.BlockLegendSpawner;
import pokecube.core.database.Database;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.ISpecialCaptureCondition;
import pokecube.core.database.stats.ISpecialSpawnCondition;
import pokecube.core.database.stats.KillStats;
import pokecube.core.database.stats.SpecialCaseRegister;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;

public class LegendaryConditions
{

    public static BlockLegendSpawner spawner1;
    /** This should be increased if any more are registerd. */
    public static final int          SPAWNER1COUNT = 7;

    static boolean isBlock(World world, ArrayList<Vector3> blocks, Block toTest)
    {
        for (Vector3 v : blocks)
        {
            if (v.getBlock(world) != toTest) { return false; }
        }
        return true;
    }

    /** @param world
     * @param blocks
     * @param material
     * @param bool
     *            if true, looks for matches, if false looks for anything that
     *            doesn't match.
     * @return */
    static boolean isMaterial(World world, ArrayList<Vector3> blocks, Material material, boolean bool)
    {
        boolean ret = true;
        if (bool)
        {
            for (Vector3 v : blocks)
            {
                if (v.getBlockMaterial(world) != material) return false;
            }
        }
        else
        {
            for (Vector3 v : blocks)
            {
                if (v.getBlockMaterial(world) == material) return false;
            }
        }
        return ret;
    }

    static void registerBeasts()
    {
        ISpecialCaptureCondition suicuneCapture = new ISpecialCaptureCondition()
        {

            @Override
            public boolean canCapture(Entity trainer)
            {
                return false;
            }

            @Override
            public boolean canCapture(Entity trainer, IPokemob pokemon)
            {

                int count = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("suicune"));
                if (count > 0) return false;
                int count1 = CaptureStats.getTotalUniqueOfTypeCaughtBy(trainer.getUniqueID().toString(),
                        PokeType.water);
                int count3 = SpecialCaseRegister.countSpawnableTypes(PokeType.water);
                if (((double) count1) / ((double) count3) >= 0.5) { return true; }
                if (pokemon != null && !trainer.worldObj.isRemote)
                {
                    String message = "msg.nosuicunetrust.txt";
                    message = I18n.translateToLocal(message);
                    trainer.addChatMessage(new TextComponentString(message));
                    return false;
                }
                return false;
            }

        };

        ISpecialCaptureCondition enteiCapture = new ISpecialCaptureCondition()
        {

            @Override
            public boolean canCapture(Entity trainer)
            {
                return false;
            }

            @Override
            public boolean canCapture(Entity trainer, IPokemob pokemon)
            {

                int count = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("entei"));
                if (count > 0) return false;
                int count1 = CaptureStats.getTotalUniqueOfTypeCaughtBy(trainer.getUniqueID().toString(), PokeType.fire);
                int count3 = SpecialCaseRegister.countSpawnableTypes(PokeType.fire);
                if (((double) count1) / ((double) count3) >= 0.5) { return true; }
                if (pokemon != null && !trainer.worldObj.isRemote)
                {
                    String message = "msg.noenteitrust.txt";
                    message = I18n.translateToLocal(message);
                    trainer.addChatMessage(new TextComponentString(message));
                    return false;
                }
                return false;
            }
        };

        ISpecialCaptureCondition raikouCapture = new ISpecialCaptureCondition()
        {

            @Override
            public boolean canCapture(Entity trainer)
            {
                return false;
            }

            @Override
            public boolean canCapture(Entity trainer, IPokemob pokemon)
            {

                int count1 = CaptureStats.getTotalUniqueOfTypeCaughtBy(trainer.getUniqueID().toString(),
                        PokeType.electric);
                int count3 = SpecialCaseRegister.countSpawnableTypes(PokeType.electric);

                int count = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("raikou"));
                if (count > 0) return false;

                if (((double) count1) / ((double) count3) >= 0.5) { return true; }
                if (pokemon != null && !trainer.worldObj.isRemote)
                {
                    String message = "msg.noraikoutrust.txt";
                    message = I18n.translateToLocal(message);
                    trainer.addChatMessage(new TextComponentString(message));
                    return false;
                }
                return false;
            }

        };

        SpecialCaseRegister.register("raikou", raikouCapture);
        SpecialCaseRegister.register("entei", enteiCapture);
        SpecialCaseRegister.register("suicune", suicuneCapture);
    }

    static void registerGen2Legends()
    {
        ISpecialSpawnCondition celebiSpawn = new ISpecialSpawnCondition()
        {

            @Override
            public boolean canSpawn(Entity trainer)
            {
                if (CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("celebi")) > 0)
                    return false;

                TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(trainer);
                Vector3 v = Vector3.getNewVector().set(trainer);
                if (SpawnHandler.canSpawn(t, Database.getEntry("celebi").getSpawnData(), v, trainer.worldObj))
                {
                    boolean hasCelebi = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                            Database.getEntry("celebi")) > 0;
                    boolean hasKilled = KillStats.getTotalNumberOfPokemobKilledBy(trainer.getUniqueID().toString(),
                            Database.getEntry("celebi")) > 2;

                    if (hasKilled || hasCelebi) { return false; }
                    boolean celebiHere = Tools.countPokemon(v, trainer.worldObj, 32, Database.getEntry("celebi")) > 0;
                    if (celebiHere) return false;
                    return SpecialCaseRegister.getCaptureCondition("celebi").canCapture(trainer);
                }
                else
                {
                    String message = "msg.nocelebihere.txt";
                    message = I18n.translateToLocal(message);
                    trainer.addChatMessage(new TextComponentString(message));
                    return false;
                }
            }

            @Override
            public boolean canSpawn(Entity trainer, Vector3 location)
            {
                return canSpawn(trainer);
            }

            @Override
            public void onSpawn(IPokemob mob)
            {
                mob.setExp(54500, true, true);
            }
        };

        ISpecialSpawnCondition hoohSpawn = new ISpecialSpawnCondition()
        {

            @Override
            public boolean canSpawn(Entity trainer)
            {
                if (CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("hooh")) > 0)
                    return false;
                Vector3 v = Vector3.getNewVector().set(trainer);

                boolean raikou = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("raikou")) > 0;
                boolean suicune = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("suicune")) > 0;
                boolean entei = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("entei")) > 0;
                if (!(raikou && entei && suicune))
                {
                    String message = "msg.nohoohtrust.txt";
                    message = I18n.translateToLocal(message);
                    trainer.addChatMessage(new TextComponentString(message));
                    return false;
                }
                int biomeId = v.getBiomeID(trainer.worldObj);
                boolean biome = Database.getEntry("hooh").getSpawnData().isValid(biomeId);
                if (biome)
                {
                    boolean here = Tools.countPokemon(v, trainer.worldObj, 32, Database.getEntry("hooh")) > 0;
                    if (here) return false;

                    return true;
                }
                else
                {
                    String message = "msg.nohoohhere.txt";
                    message = I18n.translateToLocal(message);
                    trainer.addChatMessage(new TextComponentString(message));
                    return false;
                }

            }

            @Override
            public boolean canSpawn(Entity trainer, Vector3 location)
            {

                if (location.y < 150)
                {
                    String message = "msg.nohoohhere.txt";
                    message = I18n.translateToLocal(message);
                    trainer.addChatMessage(new TextComponentString(message));
                    return false;
                }

                if (CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("hooh")) > 0)
                    return false;

                for (int i = -5; i <= 5; i++)
                {
                    for (int k = -5; k <= 5; k++)
                    {

                        if (location.add(i, -1, k).getBlockState(trainer.worldObj).getMaterial() == Material.air)
                        {
                            String message = "msg.nohoohhere.txt";
                            message = I18n.translateToLocal(message);
                            trainer.addChatMessage(new TextComponentString(message));
                            return false;
                        }
                    }

                }
                return canSpawn(trainer);
            }

            @Override
            public void onSpawn(IPokemob mob)
            {
                mob.setExp(160000, true, true);
            }

        };

        ISpecialCaptureCondition celebiCapture = new ISpecialCaptureCondition()
        {

            @Override
            public boolean canCapture(Entity trainer)
            {
                return canCapture(trainer, null);
            }

            @Override
            public boolean canCapture(Entity trainer, IPokemob pokemon)
            {
                int count1 = CaptureStats.getTotalUniqueOfTypeCaughtBy(trainer.getUniqueID().toString(),
                        PokeType.grass);
                int count2 = KillStats.getTotalUniqueOfTypeKilledBy(trainer.getUniqueID().toString(), PokeType.grass);
                int count3 = SpecialCaseRegister.countSpawnableTypes(PokeType.grass);
                double captureFactor = (double) count1 / (double) count3;

                if (captureFactor >= 0.75 && count1 >= count2) { return true; }
                // if(pokemon!=null)
                if (trainer instanceof ICommandSender && !trainer.worldObj.isRemote)
                {
                    if (captureFactor < 0.75)
                    {
                        String message = "msg.nocelebitrust.txt";
                        message = I18n.translateToLocal(message);
                        trainer.addChatMessage(new TextComponentString(message));
                    }
                    else if (count1 < count2)
                    {
                        String message = "msg.celebiangry.txt";
                        message = I18n.translateToLocal(message);
                        trainer.addChatMessage(new TextComponentString(message));
                    }
                }
                return false;
            }
        };

        ISpecialCaptureCondition hoohCapture = new ISpecialCaptureCondition()
        {

            @Override
            public boolean canCapture(Entity trainer)
            {
                return true;
            }

            @Override
            public boolean canCapture(Entity trainer, IPokemob pokemon)
            {
                boolean hasCelebi = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("hooh")) > 0;
                boolean hasKilled = KillStats.getTotalNumberOfPokemobKilledBy(trainer.getUniqueID().toString(),
                        Database.getEntry("hooh")) > 2;

                boolean raikou = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("raikou")) > 0;
                boolean suicune = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("suicune")) > 0;
                boolean entei = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("entei")) > 0;
                if (!(raikou && entei && suicune)) return false;

                if (hasKilled || hasCelebi) return false;

                return true;
            }

        };

        ISpecialCaptureCondition lugiaCapture = new ISpecialCaptureCondition()
        {

            @Override
            public boolean canCapture(Entity trainer)
            {
                return true;
            }

            @Override
            public boolean canCapture(Entity trainer, IPokemob pokemon)
            {
                boolean hasLugia = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("lugia")) > 0;
                boolean hasKilled = KillStats.getTotalNumberOfPokemobKilledBy(trainer.getUniqueID().toString(),
                        Database.getEntry("lugia")) > 2;

                boolean articuno = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("articuno")) > 0;
                boolean zapdos = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("zapdos")) > 0;
                boolean moltres = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("moltres")) > 0;
                if (!(articuno && moltres && zapdos))
                {
                    String message = "msg.nolugiatrust.txt";
                    message = I18n.translateToLocal(message);
                    trainer.addChatMessage(new TextComponentString(message));
                    return false;
                }

                if (hasKilled || hasLugia) return false;

                return true;
            }

        };

        SpecialCaseRegister.register("celebi", celebiSpawn);
        SpecialCaseRegister.register("hooh", hoohSpawn);

        SpecialCaseRegister.register("celebi", celebiCapture);
        SpecialCaseRegister.register("hooh", hoohCapture);
        SpecialCaseRegister.register("lugia", lugiaCapture);
    }

    static void registerGen3Legends()
    {
        ISpecialSpawnCondition kyogreSpawn = new ISpecialSpawnCondition()
        {

            @Override
            public boolean canSpawn(Entity trainer)
            {
                if (CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("kyogre")) > 0)
                    return false;
                Vector3 v = Vector3.getNewVector().set(trainer);
                TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(trainer);

                if (SpawnHandler.canSpawn(t, Database.getEntry("kyogre").getSpawnData(), v, trainer.worldObj))
                {
                    boolean here = Tools.countPokemon(v, trainer.worldObj, 32, Database.getEntry("kyogre")) > 0;
                    return !here;
                }
                else
                {
                    String message = "msg.nokyogrehere.txt";
                    message = I18n.translateToLocal(message);
                    trainer.addChatMessage(new TextComponentString(message));
                    return false;
                }
            }

            @Override
            public boolean canSpawn(Entity trainer, Vector3 location)
            {
                return canSpawn(trainer);
            }

            @Override
            public void onSpawn(IPokemob mob)
            {
                mob.setExp(54500, true, true);
            }
        };

        ISpecialSpawnCondition groudonSpawn = new ISpecialSpawnCondition()
        {

            @Override
            public boolean canSpawn(Entity trainer)
            {
                if (CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("groudon")) > 0)
                    return false;
                Vector3 v = Vector3.getNewVector().set(trainer);
                TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(trainer);

                if (SpawnHandler.canSpawn(t, Database.getEntry("groudon").getSpawnData(), v, trainer.worldObj))
                {
                    boolean here = Tools.countPokemon(v, trainer.worldObj, 32, Database.getEntry("groudon")) > 0;
                    return !here;
                }
                else
                {
                    String message = "msg.nogroudonhere.txt";
                    message = I18n.translateToLocal(message);
                    trainer.addChatMessage(new TextComponentString(message));
                    return false;
                }
            }

            @Override
            public boolean canSpawn(Entity trainer, Vector3 location)
            {
                return canSpawn(trainer);
            }

            @Override
            public void onSpawn(IPokemob mob)
            {
                mob.setExp(54500, true, true);
            }
        };

        ISpecialCaptureCondition kyogreCapture = new ISpecialCaptureCondition()
        {

            @Override
            public boolean canCapture(Entity trainer)
            {
                return canCapture(trainer, null);
            }

            @Override
            public boolean canCapture(Entity trainer, IPokemob pokemon)
            {
                int count1 = CaptureStats.getTotalUniqueOfTypeCaughtBy(trainer.getUniqueID().toString(),
                        PokeType.water);
                int count2 = KillStats.getTotalUniqueOfTypeKilledBy(trainer.getUniqueID().toString(), PokeType.ground);
                int count3 = SpecialCaseRegister.countSpawnableTypes(PokeType.water);
                int count4 = SpecialCaseRegister.countSpawnableTypes(PokeType.ground);
                double captureFactor = (double) count1 / (double) count3;
                double killFactor = (double) count2 / (double) count4;
                if (killFactor >= 0.5 && captureFactor >= 0.5) { return true; }
                if (pokemon != null && !trainer.worldObj.isRemote)
                {
                    String message = "msg.nokyogretrust.txt";
                    message = I18n.translateToLocal(message);
                    trainer.addChatMessage(new TextComponentString(message));
                }
                return false;
            }
        };

        ISpecialCaptureCondition groudonCapture = new ISpecialCaptureCondition()
        {

            @Override
            public boolean canCapture(Entity trainer)
            {
                return canCapture(trainer, null);
            };

            @Override
            public boolean canCapture(Entity trainer, IPokemob pokemon)
            {
                int count1 = CaptureStats.getTotalUniqueOfTypeCaughtBy(trainer.getUniqueID().toString(),
                        PokeType.ground);
                int count2 = KillStats.getTotalUniqueOfTypeKilledBy(trainer.getUniqueID().toString(), PokeType.water);
                int count4 = SpecialCaseRegister.countSpawnableTypes(PokeType.water);
                int count3 = SpecialCaseRegister.countSpawnableTypes(PokeType.ground);
                double captureFactor = (double) count1 / (double) count3;
                double killFactor = (double) count2 / (double) count4;
                if (killFactor >= 0.5 && captureFactor >= 0.5) { return true; }
                if (pokemon != null && !trainer.worldObj.isRemote)
                {
                    String message = "msg.nogroudontrust.txt";
                    message = I18n.translateToLocal(message);
                    trainer.addChatMessage(new TextComponentString(message));
                }
                return false;
            }
        };

        SpecialCaseRegister.register("kyogre", kyogreSpawn);
        SpecialCaseRegister.register("groudon", groudonSpawn);

        SpecialCaseRegister.register("kyogre", kyogreCapture);
        SpecialCaseRegister.register("groudon", groudonCapture);
    }

    public static void registerPreInit()
    {
        spawner1.registerType("registeel");
        spawner1.registerType("regice");
        spawner1.registerType("regirock");
        spawner1.registerType("celebi");
        spawner1.registerType("hooh");
        spawner1.registerType("kyogre");
        spawner1.registerType("groudon");
    }

    static void registerRegis()
    {

        ISpecialSpawnCondition regirockSpawn = new ISpecialSpawnCondition()
        {

            @Override
            public boolean canSpawn(Entity trainer)
            {
                Vector3 v = Vector3.getNewVector().set(trainer);

                boolean relicanth = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("relicanth")) > 0;
                boolean wailord = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("wailord")) > 0;

                TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(trainer);
                boolean biome = SpawnHandler.canSpawn(t, Database.getEntry("regirock").getSpawnData(), v,
                        trainer.worldObj);
                if (relicanth && wailord && biome) { return true; }
                String message = "msg.noregirockhere.txt";
                message = I18n.translateToLocal(message);
                trainer.addChatMessage(new TextComponentString(message));
                return false;

            }

            @Override
            public boolean canSpawn(Entity trainer, Vector3 location)
            {
                ArrayList<Vector3> locations = new ArrayList<Vector3>();
                boolean check = false;
                World world = trainer.worldObj;

                locations.add(location.add(0, -1, 0));
                locations.add(location.add(0, -2, 0));
                locations.add(location.add(-1, -1, 0));
                locations.add(location.add(1, -1, 0));
                check = isBlock(world, locations, Blocks.obsidian);
                if (check)
                {
                    locations.clear();
                    locations.add(location.add(-1, 0, 0));
                    locations.add(location.add(1, 0, 0));
                    check = isBlock(world, locations, Blocks.hardened_clay);
                }
                else
                {
                    locations.clear();
                    locations.add(location.add(0, -1, 0));
                    locations.add(location.add(0, -2, 0));
                    locations.add(location.add(0, -1, 1));
                    locations.add(location.add(0, -1, -1));
                    check = isBlock(world, locations, Blocks.obsidian);
                    if (check)
                    {
                        locations.clear();
                        locations.add(location.add(0, 0, 1));
                        locations.add(location.add(0, 0, -1));
                        check = isBlock(world, locations, Blocks.hardened_clay);
                    }

                }

                if (!check)
                {
                    String message = "msg.reginotlookright.txt";
                    message = I18n.translateToLocal(message);
                    trainer.addChatMessage(new TextComponentString(message));
                    return false;
                }
                return canSpawn(trainer);
            }

            @Override
            public void onSpawn(IPokemob mob)
            {
                mob.setExp(54500, true, true);
                Vector3 location = Vector3.getNewVector().set(mob).addTo(0, -1, 0);

                ArrayList<Vector3> locations = new ArrayList<Vector3>();
                World world = ((Entity) mob).worldObj;

                locations.add(location.add(0, -1, 0));
                locations.add(location.add(0, -2, 0));
                locations.add(location.add(1, -1, 0));
                locations.add(location.add(-1, -1, 0));
                locations.add(location.add(0, -1, -1));
                locations.add(location.add(0, -1, 1));
                locations.add(location.add(0, 0, -1));
                locations.add(location.add(0, 0, 1));
                locations.add(location.add(1, 0, 0));
                locations.add(location.add(-1, 0, 0));

                for (Vector3 v : locations)
                {
                    v.setAir(world);
                }
                location.setAir(world);
            }

        };

        ISpecialSpawnCondition regiceSpawn = new ISpecialSpawnCondition()
        {

            @Override
            public boolean canSpawn(Entity trainer)
            {
                Vector3 v = Vector3.getNewVector().set(trainer);
                boolean relicanth = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("relicanth")) > 0;
                boolean wailord = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("wailord")) > 0;

                TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(trainer);

                if (relicanth && wailord && SpawnHandler.canSpawn(t, Database.getEntry("regice").getSpawnData(), v,
                        trainer.worldObj)) { return true; }
                String message = "msg.noregicehere.txt";
                message = I18n.translateToLocal(message);
                trainer.addChatMessage(new TextComponentString(message));
                return false;

            }

            @Override
            public boolean canSpawn(Entity trainer, Vector3 location)
            {

                ArrayList<Vector3> locations = new ArrayList<Vector3>();
                boolean check = false;
                World world = trainer.worldObj;

                locations.add(location.add(0, -1, 0));
                locations.add(location.add(0, -2, 0));
                locations.add(location.add(-1, -1, 0));
                locations.add(location.add(1, -1, 0));
                check = isBlock(world, locations, Blocks.ice) || isBlock(world, locations, Blocks.packed_ice);
                if (check)
                {
                    Block b = location.add(0, -1, 1).getBlock(world);
                    check = b == Blocks.ice || b == Blocks.packed_ice;
                    if (!check)
                    {
                        b = location.add(0, -1, -1).getBlock(world);
                        check = b == Blocks.ice || b == Blocks.packed_ice;
                    }
                }
                else
                {
                    locations.clear();
                    locations.add(location.add(0, -1, 0));
                    locations.add(location.add(0, -2, 0));
                    locations.add(location.add(0, -1, 1));
                    locations.add(location.add(0, -1, -1));
                    check = isBlock(world, locations, Blocks.ice) || isBlock(world, locations, Blocks.packed_ice);
                    if (check)
                    {
                        Block b = location.add(1, -1, 0).getBlock(world);
                        check = b == Blocks.ice || b == Blocks.packed_ice;
                        if (!check)
                        {
                            b = location.add(-1, -1, 0).getBlock(world);
                            check = b == Blocks.ice || b == Blocks.packed_ice;
                        }
                    }
                }

                if (!check)
                {
                    String message = "msg.reginotlookright.txt";
                    message = I18n.translateToLocal(message);
                    trainer.addChatMessage(new TextComponentString(message));
                    return false;
                }
                return canSpawn(trainer);
            }

            @Override
            public void onSpawn(IPokemob mob)
            {
                mob.setExp(54500, true, true);
                Vector3 location = Vector3.getNewVector().set(mob).add(0, -1, 0);

                ArrayList<Vector3> locations = new ArrayList<Vector3>();
                World world = ((Entity) mob).worldObj;

                locations.add(location.add(0, -1, 0));
                locations.add(location.add(0, -2, 0));
                locations.add(location.add(1, -1, 0));
                locations.add(location.add(-1, -1, 0));
                locations.add(location.add(0, -1, -1));
                locations.add(location.add(0, -1, 1));

                for (Vector3 v : locations)
                {
                    v.setAir(world);
                }
                location.setAir(world);
            }

        };

        ISpecialSpawnCondition registeelSpawn = new ISpecialSpawnCondition()
        {

            @Override
            public boolean canSpawn(Entity trainer)
            {
                Vector3 v = Vector3.getNewVector().set(trainer);

                boolean relicanth = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("relicanth")) > 0;
                boolean wailord = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID().toString(),
                        Database.getEntry("wailord")) > 0;

                TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(trainer);

                if (relicanth && wailord && SpawnHandler.canSpawn(t, Database.getEntry("registeel").getSpawnData(), v,
                        trainer.worldObj)) { return true; }

                String message = "msg.noregisteelhere.txt";
                message = I18n.translateToLocal(message);
                trainer.addChatMessage(new TextComponentString(message));
                return false;

            }

            @Override
            public boolean canSpawn(Entity trainer, Vector3 location)
            {

                ArrayList<Vector3> locations = new ArrayList<Vector3>();
                boolean check = false;
                World world = trainer.worldObj;

                locations.add(location.add(0, -1, -1));
                locations.add(location.add(0, -1, +1));
                locations.add(location.add(0, -1, 0));
                locations.add(location.add(0, -2, 0));

                check = isBlock(world, locations, Blocks.iron_block);
                if (!check)
                {
                    locations.clear();
                    locations.add(location.add(-1, -1, 0));
                    locations.add(location.add(1, -1, 0));
                    locations.add(location.add(0, -1, 0));
                    locations.add(location.add(0, -2, 0));

                    check = isBlock(world, locations, Blocks.iron_block);
                }
                if (!check)
                {
                    String message = "msg.reginotlookright.txt";
                    message = I18n.translateToLocal(message);
                    trainer.addChatMessage(new TextComponentString(message));
                    return false;
                }
                return canSpawn(trainer);
            }

            @Override
            public void onSpawn(IPokemob mob)
            {
                mob.setExp(54500, true, true);
                Vector3 location = Vector3.getNewVector().set(mob).add(0, -1, 0);

                ArrayList<Vector3> locations = new ArrayList<Vector3>();
                boolean check = false;
                World world = ((Entity) mob).worldObj;

                locations.add(location.add(0, -1, -1));
                locations.add(location.add(0, -1, +1));
                locations.add(location.add(0, -1, 0));
                locations.add(location.add(0, -2, 0));

                check = isBlock(world, locations, Blocks.iron_block);
                if (!check)
                {
                    locations.clear();
                    locations.add(location.add(-1, -1, 0));
                    locations.add(location.add(1, -1, 0));
                    locations.add(location.add(0, -1, 0));
                    locations.add(location.add(0, -2, 0));

                    check = isBlock(world, locations, Blocks.iron_block);
                    if (check)
                    {
                        for (Vector3 v : locations)
                        {
                            v.setAir(world);
                        }
                        location.setAir(world);
                    }
                }
                else
                {
                    for (Vector3 v : locations)
                    {
                        v.setAir(world);
                    }
                    location.setAir(world);
                }

            }

        };

        SpecialCaseRegister.register("registeel", registeelSpawn);
        SpecialCaseRegister.register("regice", regiceSpawn);
        SpecialCaseRegister.register("regirock", regirockSpawn);
    }

    public static void registerSpecialConditions()
    {
        registerBeasts();
        registerGen2Legends();
        registerGen3Legends();
        registerRegis();
    }

}
