package pokecube.core.handlers;

import java.io.File;
import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.thread.aiRunnables.combat.AIFindTarget;
import pokecube.core.contributors.Contributor;
import pokecube.core.contributors.ContributorManager;
import pokecube.core.database.Database;
import pokecube.core.database.Database.EnumDatabase;
import pokecube.core.database.recipes.XMLRecipeHandler;
import pokecube.core.database.rewards.XMLRewardsHandler;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.events.handlers.EventsHandler;
import pokecube.core.events.handlers.SpawnHandler;
import pokecube.core.events.handlers.SpawnHandler.FunctionVariance;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.network.PokecubePacketHandler.StarterInfo;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.world.dimensions.PokecubeDimensionManager;
import pokecube.core.world.dimensions.secretpower.WorldProviderSecretBase;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;

public class Config extends ConfigBase
{
    public static final int              VERSION                      = 1;

    public static final String           spawning                     = "spawning";
    public static final String           database                     = "database";
    public static final String           world                        = "generation";
    public static final String           mobAI                        = "ai";
    public static final String           moves                        = "moves";
    public static final String           misc                         = "misc";
    public static final String           perms                        = "permissions";
    public static final String           client                       = "client";
    public static final String           advanced                     = "advanced";
    public static final String           healthbars                   = "healthbars";
    public static final String           genetics                     = "genetics";
    public static final String           items                        = "items";

    public static int                    GUICHOOSEFIRSTPOKEMOB_ID;
    public static int                    GUIDISPLAYPOKECUBEINFO_ID;
    public static int                    GUIDISPLAYTELEPORTINFO_ID;
    public static int                    GUIPOKECENTER_ID;
    public static int                    GUIPOKEDEX_ID;
    public static int                    GUIPOKEWATCH_ID;
    public static int                    GUIPOKEMOBSPAWNER_ID;
    public static int                    GUIPC_ID;
    public static int                    GUIPOKEMOB_ID;
    public static int                    GUIPOKEMOBAI_ID;
    public static int                    GUIPOKEMOBSTORE_ID;
    public static int                    GUIPOKEMOBROUTE_ID;
    public static int                    GUITRADINGTABLE_ID;
    public static int                    GUITMTABLE_ID;

    public static Config                 instance;

    private static Config                defaults                     = new Config();
    // Misc Settings
    @Configure(category = misc, needsMcRestart = true)
    public boolean                       default_contributors         = true;
    @Configure(category = misc, needsMcRestart = true)
    public String                        extra_contributors           = "";
    @Configure(category = misc, needsMcRestart = true)
    public boolean                       loginmessage                 = true;
    @Configure(category = misc)
    /** is there a choose first gui on login */
    public boolean                       guiOnLogin                   = true;
    @Configure(category = misc)
    /** does defeating a tame pokemob give exp */
    public boolean                       pvpExp                       = false;
    @Configure(category = misc)
    /** does defeating a tame pokemob give exp */
    public double                        pvpExpMultiplier             = 0.5;
    @Configure(category = misc)
    /** does defeating a tame pokemob give exp */
    public boolean                       trainerExp                   = true;
    @Configure(category = misc)
    public boolean                       mysterygift                  = true;
    @Configure(category = misc, needsMcRestart = true)
    public String                        defaultMobs                  = "";
    @Configure(category = misc)
    @SyncConfig
    public double                        scalefactor                  = 1;
    @Configure(category = misc)
    public boolean                       pcOnDrop                     = true;
    @Configure(category = misc)
    public float                         expScaleFactor               = 1;
    @Configure(category = misc)
    @SyncConfig
    public boolean                       pcHoldsOnlyPokecubes         = true;
    @Configure(category = misc)
    public String[]                      snagblacklist                = { "net.minecraft.entity.boss.EntityDragon",
            "net.minecraft.entity.boss.EntityWither" };
    @Configure(category = misc)
    public boolean                       defaultInteractions          = true;
    @Configure(category = misc)
    public boolean                       berryBreeding                = true;

    @Configure(category = perms)
    public boolean                       permsCapture                 = false;
    @Configure(category = perms)
    public boolean                       permsCaptureSpecific         = false;
    @Configure(category = perms)
    public boolean                       permsHatch                   = false;
    @Configure(category = perms)
    public boolean                       permsHatchSpecific           = false;
    @Configure(category = perms)
    public boolean                       permsSendOut                 = false;
    @Configure(category = perms)
    public boolean                       permsSendOutSpecific         = false;
    @Configure(category = perms)
    public boolean                       permsRide                    = false;
    @Configure(category = perms)
    public boolean                       permsRideSpecific            = false;
    @Configure(category = perms)
    public boolean                       permsFly                     = false;
    @Configure(category = perms)
    public boolean                       permsFlySpecific             = false;
    @Configure(category = perms)
    public boolean                       permsSurf                    = false;
    @Configure(category = perms)
    public boolean                       permsSurfSpecific            = false;
    @Configure(category = perms)
    public boolean                       permsDive                    = false;
    @Configure(category = perms)
    public boolean                       permsDiveSpecific            = false;
    @Configure(category = perms)
    public boolean                       permsMoveAction              = false;

    // Move Use related settings
    @Configure(category = moves)
    public double                        contactAttackDistance        = 0;
    @Configure(category = moves)
    public double                        rangedAttackDistance         = 16;
    @Configure(category = moves)
    /** Scaling factor for pokemob explosions */
    public double                        blastStrength                = 100;
    @Configure(category = moves)
    /** Scaling factor for pokemob explosions */
    public int                           blastRadius                  = 16;
    @Configure(category = moves)
    /** Capped damage to players by pok�mobs */
    public int                           maxWildPlayerDamage          = 10;
    @Configure(category = moves)
    /** Capped damage to players by pok�mobs */
    public int                           maxOwnedPlayerDamage         = 10;
    @Configure(category = moves)
    /** Capped damage to players by pok�mobs */
    public double                        wildPlayerDamageRatio        = 1;
    @Configure(category = moves)
    /** Capped damage to players by pok�mobs */
    public double                        wildPlayerDamageMagic        = 0.1;
    @Configure(category = moves)
    /** Capped damage to players by pok�mobs */
    public double                        ownedPlayerDamageRatio       = 1;
    @Configure(category = moves)
    /** Capped damage to players by pok�mobs */
    public double                        ownedPlayerDamageMagic       = 0.1;
    @Configure(category = moves)
    /** Scaling factor for damage against not pokemobs */
    public double                        pokemobToOtherMobDamageRatio = 1;
    @Configure(category = moves)
    /** Scaling factor for damage against NPCs */
    public double                        pokemobToNPCDamageRatio      = 1;
    @Configure(category = moves)
    public int                           baseSmeltingHunger           = 100;
    @Configure(category = moves)
    public boolean                       onlyPokemobsDamagePokemobs   = false;
    @Configure(category = moves)
    public float                         playerToPokemobDamageScale   = 1;
    @Configure(category = moves)
    public boolean                       defaultFireActions           = true;
    @Configure(category = moves)
    public boolean                       defaultWaterActions          = true;
    @Configure(category = moves)
    public boolean                       defaultElectricActions       = true;
    @Configure(category = moves)
    public boolean                       defaultIceActions            = true;

    // AI Related settings
    @Configure(category = mobAI)
    public int                           mateMultiplier               = 1;
    @Configure(category = mobAI)
    public float                         mateDensityWild              = 2;
    @Configure(category = mobAI)
    public float                         mateDensityPlayer            = 4;
    @Configure(category = mobAI)
    public int                           breedingDelay                = 4000;
    @Configure(category = mobAI)
    public int                           eggHatchTime                 = 10000;
    @Configure(category = mobAI)
    /** do wild pokemobs which leave cullDistance despawn immediately */
    @SyncConfig
    public boolean                       cull                         = false;
    /** distance for culling */
    @Configure(category = mobAI)
    @SyncConfig
    public int                           cullDistance                 = 96;
    @Configure(category = mobAI)
    @SyncConfig
    public boolean                       despawn                      = true;
    /** distance for culling */
    @Configure(category = mobAI)
    public int                           despawnTimer                 = 2000;
    @Configure(category = mobAI)
    /** Will lithovores eat gravel */
    public boolean                       pokemobsEatGravel            = false;
    @Configure(category = mobAI)
    /** Will lithovores eat rocks */
    public boolean                       pokemobsEatRocks             = true;
    @Configure(category = mobAI)
    /** Will herbivores eat plants */
    public boolean                       pokemobsEatPlants            = true;
    @Configure(category = mobAI)
    /** Is there a warning before a wild pok�mob attacks the player. */
    public boolean                       pokemobagresswarning         = true;
    @Configure(category = mobAI)
    @SyncConfig
    /** Distance to player needed to agress the player */
    public int                           mobAggroRadius               = 3;
    @Configure(category = mobAI)
    /** Approximately how many ticks between wild pokemobs running agro
     * checks. */
    public int                           mobAgroRate                  = 200;
    @Configure(category = mobAI)
    /** Approximate number of ticks before pok�mob starts taking hunger
     * damage */
    public int                           pokemobLifeSpan              = 8000;
    @Configure(category = mobAI)
    /** Warning time before a wild pok�mob attacks a player */
    public int                           pokemobagressticks           = 100;
    @Configure(category = mobAI)
    public boolean                       pokemobsDamageOwner          = false;
    @Configure(category = mobAI)
    public boolean                       pokemobsDamagePlayers        = true;
    @Configure(category = mobAI)
    public boolean                       pokemobsDamageBlocks         = false;
    @Configure(category = mobAI)
    public boolean                       pokemobsDropItems            = true;
    @Configure(category = mobAI)
    public float                         expFromDeathDropScale        = 1;
    @Configure(category = mobAI)
    /** Do explosions occur and cause damage */
    public boolean                       explosions                   = true;
    @Configure(category = mobAI)
    @SyncConfig
    public int                           attackCooldown               = 20;
    @Configure(category = mobAI)
    public int                           chaseDistance                = 32;
    @Configure(category = mobAI)
    public int                           combatDistance               = 8;
    @Configure(category = mobAI)
    public int                           aiDisableDistance            = 32;
    @Configure(category = mobAI)
    public int                           tameGatherDelay              = 20;
    @Configure(category = mobAI)
    public int                           wildGatherDelay              = 200;
    @Configure(category = mobAI)
    public int                           tameGatherDistance           = 16;
    @Configure(category = mobAI)
    public int                           wildGatherDistance           = 8;
    @Configure(category = mobAI)
    public boolean                       tameGather                   = true;
    @Configure(category = mobAI)
    public boolean                       wildGather                   = false;
    @Configure(category = mobAI)
    public boolean                       flyEnabled                   = true;
    @Configure(category = mobAI)
    public boolean                       surfEnabled                  = true;
    @Configure(category = mobAI)
    public boolean                       diveEnabled                  = true;
    @Configure(category = mobAI)
    public String[]                      dodgeSounds                  = { "entity.witch.throw" };
    @Configure(category = mobAI)
    public String[]                      leapSounds                   = { "entity.witch.throw" };
    @Configure(category = mobAI)
    public String[]                      guardBlacklistClass          = { "net.minecraft.entity.IMerchant",
            "net.minecraft.entity.INpc", "pokecube.core.items.pokemobeggs.EntityPokemobEgg",
            "net.minecraft.entity.IProjectile" };
    @Configure(category = mobAI)
    public String[]                      guardBlacklistId             = {};
    @Configure(category = mobAI)
    public float                         interactHungerScale          = 1;
    @Configure(category = mobAI)
    public float                         interactDelayScale           = 1;
    @Configure(category = mobAI)
    public boolean                       pokemobsOnShoulder           = true;
    @Configure(category = mobAI)
    public int                           fishHookBaitRange            = 16;

    // ridden Speed multipliers
    @Configure(category = mobAI)
    @SyncConfig
    public float                         flySpeedFactor               = 1;
    @Configure(category = mobAI)
    @SyncConfig
    public float                         surfSpeedFactor              = 1;
    @Configure(category = mobAI)
    @SyncConfig
    public float                         groundSpeedFactor            = 1;

    // Used by pathfinder's movehelper for scaling speed in air and water.
    @Configure(category = mobAI)
    public float                         flyPathingSpeedFactor        = 1.25f;
    @Configure(category = mobAI)
    public float                         swimPathingSpeedFactor       = 1.25f;
    @Configure(category = mobAI)
    public boolean                       pokemobCollisions            = true;
    @Configure(category = mobAI)
    public int                           captureDelayTicks            = 0;
    @Configure(category = mobAI)
    public boolean                       captureDelayTillAttack       = true;

    public SoundEvent[]                  dodges                       = {};
    public SoundEvent[]                  leaps                        = {};

    // World Gen and World effect settings
    @Configure(category = world)
    /** do meteors fall. */
    public boolean                       meteors                      = true;
    @Configure(category = world)
    public int                           meteorDistance               = 3000;
    @Configure(category = world)
    public int                           meteorRadius                 = 64;
    @Configure(category = world)
    public boolean                       doSpawnBuilding              = true;
    @Configure(category = world)
    public boolean                       basesLoaded                  = true;
    @Configure(category = world)
    public boolean                       autoPopulateLists            = true;
    @Configure(category = world)
    public boolean                       refreshSubbiomes             = false;
    @Configure(category = world, needsMcRestart = true)
    public String[]                      blocksStones                 = { "minecraft:stone variant=stone",
            "minecraft:stone variant=granite", "minecraft:stone variant=diorite", "minecraft:stone variant=andesite",
            "minecraft:netherrack", "minecraft:sandstone type=sandstone", "minecraft:red_sandstone type=red_sandstone",
            "minecraft:cobblestone" };
    @Configure(category = world, needsMcRestart = true)
    public String[]                      blocksOre                    = { ".*:.*_ore", ".*:ore*", ".*:ore" };
    @Configure(category = world, needsMcRestart = true)
    public String[]                      blocksGround                 = { "minecraft:sand", "minecraft:gravel",
            "minecraft:stained_hardened_clay", "minecraft:hardened_clay", "minecraft:dirt", "minecraft:grass" };
    @Configure(category = world, needsMcRestart = true)
    public String[]                      blocksWood                   = {};
    @Configure(category = world, needsMcRestart = true)
    public String[]                      blocksLeaves                 = {};
    @Configure(category = world, needsMcRestart = true)
    public String[]                      blocksPlants                 = { "minecraft:double_plant",
            "minecraft:red_flower", "minecraft:yellow_flower", "minecraft:tallgrass", "minecraft:deadbush",
            "minecraft:wheat", "minecraft:carrots", "minecraft:potatoes", "pokecube:berryfruit" };
    @Configure(category = world, needsMcRestart = true)
    public String[]                      blocksFruits                 = { "minecraft:wheat age=7",
            "minecraft:nether_wart age=3", "minecraft:carrots age=7", "minecraft:potatoes age=7",
            "minecraft:melon_block", "minecraft:pumpkin", "pokecube:berryfruit" };
    @Configure(category = world, needsMcRestart = true)
    public String[]                      blocksTerrain                = {};
    @Configure(category = world, needsMcRestart = true)
    public String[]                      blocksIndustrial             = { "minecraft:redstone_block",
            "minecraft:furnace", "minecraft:lit_furnace", "minecraft:piston", "minecraft:sticky_piston",
            "minecraft:dispenser", "minecraft:dropper", "minecraft:hopper", "minecraft:anvil" };
    @Configure(category = world)
    public boolean                       autoAddNullBerries           = false;
    @Configure(category = world)
    public int                           cropGrowthTicks              = 2500;
    @Configure(category = world)
    public int                           leafBerryTicks               = 7500;
    @Configure(category = world)
    public boolean                       autoDetectSubbiomes          = true;
    @Configure(category = world)
    public boolean                       generateFossils              = true;
    @Configure(category = world)
    public boolean                       villagePokecenters           = true;
    @Configure(category = world)
    public boolean                       chunkLoadPokecenters         = true;

    @Configure(category = world)
    public String                        baseSizeFunction             = "8 + c/10 + h/10 + k/20";
    @Configure(category = world)
    public int                           baseMaxSize                  = 1;
    @Configure(category = world, needsMcRestart = true)
    public String[]                      structureSubiomes            = { "Stronghold:ruin", "Mineshaft:ruin",
            "Temple:ruin", "EndCity:ruin", "Fortress:ruin", "Mansion:ruin", "Monument:monument", "Village:village" };
    @Configure(category = world, needsMcRestart = true)
    public String[]                      extraWorldgenDatabases       = {};
    @Configure(category = world)
    public int                           spawnDimension               = 0;
    // Mob Spawning settings
    @Configure(category = spawning, needsMcRestart = true)
    /** Do monsters not spawn. */
    public boolean                       deactivateMonsters           = false;
    @Configure(category = spawning)
    /** do monster spawns get swapped with shadow pokemobs */
    public boolean                       disableVanillaMonsters       = false;
    @Configure(category = spawning)
    public boolean                       disableVanillaAnimals        = false;
    @Configure(category = spawning, needsMcRestart = true)
    /** do animals not spawn */
    public boolean                       deactivateAnimals            = true;
    @Configure(category = spawning, needsMcRestart = true)
    /** do Pokemobs spawn */
    public boolean                       pokemonSpawn                 = true;
    @Configure(category = spawning)
    @SyncConfig
    /** This is also the radius which mobs spawn in. Is only despawn radius if
     * cull is true */
    public int                           maxSpawnRadius               = 32;
    @Configure(category = spawning)
    @SyncConfig
    /** closest distance to a player the pokemob can spawn. */
    public int                           minSpawnRadius               = 16;
    @Configure(category = spawning)
    /** Minimum level legendaries can spawn at. */
    public int                           minLegendLevel               = 1;
    @Configure(category = spawning)
    /** Will nests spawn */
    public boolean                       nests                        = false;
    @Configure(category = spawning)
    /** number of nests per chunk */
    public int                           nestsPerChunk                = 1;
    @Configure(category = spawning)
    /** To be used for nest retrogen. */
    public boolean                       refreshNests                 = false;
    @Configure(category = spawning)
    public int                           mobSpawnNumber               = 10;
    @Configure(category = spawning)
    public double                        mobDensityMultiplier         = 1;
    @Configure(category = spawning)
    @SyncConfig
    public int                           levelCap                     = 50;
    @Configure(category = spawning)
    public boolean                       shouldCap                    = true;
    @Configure(category = spawning)
    @SyncConfig
    @Versioned
    String[]                             spawnLevelFunctions          = { //@formatter:off
            "-1:abs((25)*(sin(x*8*10^-3)^3 + sin(y*8*10^-3)^3)):false:false",
            "0:abs((25)*(sin(x*10^-3)^3 + sin(y*10^-3)^3)):false:false",
            "1:1+r/200:true:true"
            };//@formatter:on
    @Configure(category = spawning)
    @SyncConfig
    public boolean                       expFunction                  = false;
    @Configure(category = spawning)
    @SyncConfig
    public String                        spawnLevelVariance           = "x + ceil(5*rand())";
    @Configure(category = spawning)
    public int[]                         dimensionBlacklist           = {};
    @Configure(category = spawning)
    public int[]                         dimensionWhitelist           = {};
    @Configure(category = spawning)
    public boolean                       whiteListEnabled             = false;
    @Configure(category = spawning)
    /** Spawns run once every this many ticks.. */
    public int                           spawnRate                    = 20;

    // Gui/client settings
    @Configure(category = client)
    public String                        guiRef                       = "top_left";
    @Configure(category = client)
    public String                        messageRef                   = "right_middle";
    @Configure(category = client)
    public String                        targetRef                    = "top_right";
    @Configure(category = client)
    public String                        teleRef                      = "top_right";
    @Configure(category = client)
    public int[]                         guiPos                       = { 0, 0 };
    @Configure(category = client)
    public float                         guiSize                      = 1;
    @Configure(category = client)
    public int[]                         telePos                      = { 89, 17 };
    @Configure(category = client)
    public float                         teleSize                     = 1;
    @Configure(category = client)
    public int[]                         targetPos                    = { 147, -42 };
    @Configure(category = client)
    public float                         targetSize                   = 1;
    @Configure(category = client)
    public int[]                         messagePos                   = { -150, -100 };
    @Configure(category = client)
    public int                           messageWidth                 = 150;;
    @Configure(category = client)
    public int[]                         messagePadding               = { 0, 0 };
    @Configure(category = client)
    public float                         messageSize                  = 1;
    @Configure(category = client)
    public boolean                       guiDown                      = true;
    @Configure(category = client)
    public boolean                       guiAutoScale                 = false;
    @Configure(category = client)
    public boolean                       autoSelectMoves              = false;
    @Configure(category = client)
    public boolean                       autoRecallPokemobs           = false;
    @Configure(category = client)
    public int                           autoRecallDistance           = 32;
    @Configure(category = client)
    public boolean                       riddenMobsTurnWithLook       = true;
    @Configure(category = client)
    public boolean                       extraberries                 = false;
    @Configure(category = client)
    public boolean                       battleLogInChat              = false;

    @Configure(category = advanced)
    String[]                             mystLocs                     = {};
    @Configure(category = advanced)
    boolean                              resetTags                    = false;
    @Configure(category = advanced)
    // TODO find more internal variables to add to this.
    String[]                             extraVars                    = { "jc:" + EventsHandler.juiceChance,
            "rc:" + EventsHandler.candyChance, "eggDpl:" + ItemPokemobEgg.PLAYERDIST,
            "eggDpm:" + ItemPokemobEgg.MOBDIST };
    @Configure(category = advanced)
    public boolean                       debug                        = false;
    @Configure(category = advanced)
    public String[]                      damageBlocksWhitelist        = { "flash", "teleport", "dig", "cut",
            "rocksmash", "secretpower" };
    @Configure(category = advanced)
    public String[]                      damageBlocksBlacklist        = {};
    @Configure(category = advanced)
    @SyncConfig
    public int                           evolutionTicks               = 50;
    @Configure(category = advanced)
    @SyncConfig
    public int                           baseRadarRange               = 64;
    @Configure(category = advanced)
    public String                        nonPokemobExpFunction        = "h*(a+1)";
    @Configure(category = advanced)
    public boolean                       nonPokemobExp                = false;
    @Configure(category = advanced)
    public int[]                         teleDimBlackList             = {};
    @Configure(category = advanced)
    @SyncConfig
    public int                           telePearlsCostSameDim        = 0;
    @Configure(category = advanced)
    @SyncConfig
    public int                           telePearlsCostOtherDim       = 16;
    @Configure(category = advanced)
    /** This is the version to match in configs, this is set after loading the
     * configs to VERSION, and uses -1 as a "default" to ensure this has
     * changed. */
    public int                           version                      = -1;

    @Configure(category = genetics)
    public String                        epigeneticEVFunction         = GeneticsManager.epigeneticFunction;
    @Configure(category = genetics)
    String[]                             mutationRates                = GeneticsManager.getMutationConfig();

    @Configure(category = database, needsMcRestart = true)
    boolean                              forceDatabase                = true;
    @Configure(category = database, needsMcRestart = true)
    boolean                              forceRecipes                 = true;
    @Configure(category = database, needsMcRestart = true)
    boolean                              forceRewards                 = true;
    @Configure(category = database, needsMcRestart = true)
    public boolean                       forceBerries                 = true;
    @Configure(category = database, needsMcRestart = true)
    public boolean                       useCache                     = true;

    @Configure(category = database, needsMcRestart = true)
    public String[]                      configDatabases              = { "", "", "" };

    @Configure(category = database, needsMcRestart = true)
    String[]                             recipeDatabases              = { "recipes" };
    @Configure(category = database, needsMcRestart = true)
    String[]                             rewardDatabases              = { "rewards" };

    @Configure(category = healthbars)
    public boolean                       doHealthBars                 = true;
    @Configure(category = healthbars)
    public int                           maxDistance                  = 24;
    @Configure(category = healthbars)
    public boolean                       renderInF1                   = false;
    @Configure(category = healthbars)
    public double                        heightAbove                  = 0.6;
    @Configure(category = healthbars)
    public boolean                       drawBackground               = true;
    @Configure(category = healthbars)
    public int                           backgroundPadding            = 2;
    @Configure(category = healthbars)
    public int                           backgroundHeight             = 6;
    @Configure(category = healthbars)
    public int                           barHeight                    = 4;
    @Configure(category = healthbars)
    public int                           plateSize                    = 25;
    @Configure(category = healthbars)
    public boolean                       showHeldItem                 = true;
    @Configure(category = healthbars)
    public boolean                       showArmor                    = true;
    @Configure(category = healthbars)
    public boolean                       groupArmor                   = true;
    @Configure(category = healthbars)
    public int                           hpTextHeight                 = 14;
    @Configure(category = healthbars)
    public boolean                       showOnlyFocused              = false;
    @Configure(category = healthbars)
    public boolean                       enableDebugInfo              = true;
    @Configure(category = healthbars)
    public int                           ownedNameColour              = 0x55FF55;
    @Configure(category = healthbars)
    public int                           otherOwnedNameColour         = 0xFF5555;
    @Configure(category = healthbars)
    public int                           caughtNamedColour            = 0x5555FF;
    @Configure(category = healthbars)
    public int                           scannedNameColour            = 0x88FFFF;
    @Configure(category = healthbars)
    public int                           unknownNameColour            = 0x888888;

    @Configure(category = items, needsMcRestart = true)
    public String[]                      customHeldItems              = {};
    @Configure(category = items, needsMcRestart = true)
    public String[]                      customFossils                = {};

    /** List of blocks to be considered for the floor of a cave. */
    private List<Predicate<IBlockState>> caveBlocks                   = Lists.newArrayList();
    /** List of blocks to be considered for the surface. */
    private List<Predicate<IBlockState>> surfaceBlocks                = Lists.newArrayList();
    /** List of blocks to be considered to be rocks for the purpose of rocksmash
     * and lithovore eating */
    private List<Predicate<IBlockState>> rocks                        = Lists.newArrayList();
    /** List of blocks to be considered to be generic terrain, for dig to reduce
     * drop rates for */
    private List<Predicate<IBlockState>> terrain                      = Lists.newArrayList();
    private List<Predicate<IBlockState>> woodTypes                    = Lists.newArrayList();
    private List<Predicate<IBlockState>> plantTypes                   = Lists.newArrayList();
    private List<Predicate<IBlockState>> fruitTypes                   = Lists.newArrayList();
    private List<Predicate<IBlockState>> dirtTypes                    = Lists.newArrayList();
    private List<Predicate<IBlockState>> industrial                   = Lists.newArrayList();

    private Config()
    {
        super(null);
    }

    public Config(File path)
    {
        super(path, defaults);
        populateSettings();
        applySettings();
        save();
        if (path.getName().endsWith(".dummy")) return;
        if (instance != null) MinecraftForge.EVENT_BUS.unregister(instance);
        MinecraftForge.EVENT_BUS.register(instance = this);
    }

    @Override
    public void applySettings()
    {
        if (PokecubeCore.core.getConfig() == this) initDefaultStarts();

        boolean toSave = false;
        if (version != VERSION)
        {
            toSave = true;
            version = VERSION;
            for (Field f : Config.class.getDeclaredFields())
            {
                Versioned conf = f.getAnnotation(Versioned.class);
                if (conf != null)
                {
                    try
                    {
                        f.setAccessible(true);
                        f.set(this, f.get(defaults));
                    }
                    catch (IllegalArgumentException | IllegalAccessException e)
                    {
                        PokecubeMod.log(Level.WARNING, "Error updating " + f.getName(), e);
                    }
                }
            }
        }

        WorldProviderSecretBase.init(baseSizeFunction);
        for (String s : structureSubiomes)
        {
            String[] args = s.split(":");
            PokecubeTerrainChecker.structureSubbiomeMap.put(args[0], args[1]);
        }
        if (attackCooldown <= 0) attackCooldown = 1;
        if (spawnRate <= 0) spawnRate = 0;

        SpawnHandler.MAX_DENSITY = mobDensityMultiplier;
        SpawnHandler.MAXNUM = mobSpawnNumber;
        if (breedingDelay < 600) breedingDelay = 1000;

        SpawnHandler.doSpawns = pokemonSpawn;
        SpawnHandler.lvlCap = shouldCap;
        SpawnHandler.capLevel = levelCap;
        SpawnHandler.expFunction = expFunction;
        SpawnHandler.loadFunctionsFromStrings(spawnLevelFunctions);
        SpawnHandler.refreshSubbiomes = refreshSubbiomes;
        SpawnHandler.DEFAULT_VARIANCE = new FunctionVariance(spawnLevelVariance);

        PokecubeSerializer.MeteorDistance = meteorDistance * meteorDistance;
        PokecubeMod.debug = debug;
        for (String loc : mystLocs)
        {
            PokecubeMod.giftLocations.add(loc);
        }
        if (Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION))
        {
            AIFindTarget.initIDs();
        }
        for (String s : recipeDatabases)
            XMLRecipeHandler.recipeFiles.add(s);
        for (String s : rewardDatabases)
            XMLRewardsHandler.recipeFiles.add(s);
        if (extraVars.length != defaults.extraVars.length)
        {
            String[] old = extraVars.clone();
            extraVars = defaults.extraVars.clone();
            for (int i = 0; i < extraVars.length; i++)
            {
                String[] args1 = extraVars[i].split(":");
                String key1 = args1[0];
                for (String s : old)
                {
                    String[] args2 = s.split(":");
                    String key2 = args2[0];
                    if (key1.equals(key2))
                    {
                        extraVars[i] = s;
                        break;
                    }
                }
            }
            toSave = true;
        }
        // TODO more internal variables
        for (String s : extraVars)
        {
            String[] args = s.split(":");
            String key = args[0];
            String value = args[1];
            if (key.equals("jc"))
            {
                EventsHandler.juiceChance = Double.parseDouble(value);
                continue;
            }
            if (key.equals("rc"))
            {
                EventsHandler.candyChance = Double.parseDouble(value);
                continue;
            }
            if (key.equals("eggDpl"))
            {
                ItemPokemobEgg.PLAYERDIST = Double.parseDouble(value);
                continue;
            }
            if (key.equals("eggDpm"))
            {
                ItemPokemobEgg.MOBDIST = Double.parseDouble(value);
                continue;
            }
        }

        if (mutationRates.length != defaults.mutationRates.length)
        {
            String[] old = mutationRates.clone();
            mutationRates = defaults.mutationRates.clone();
            for (int i = 0; i < mutationRates.length; i++)
            {
                String[] args1 = mutationRates[i].split(" ");
                String key1 = args1[0];
                for (String s : old)
                {
                    String[] args2 = s.split(" ");
                    String key2 = args2[0];
                    if (key1.equals(key2))
                    {
                        mutationRates[i] = s;
                        break;
                    }
                }
            }
            toSave = true;
        }

        for (String s : mutationRates)
        {
            String[] args = s.split(" ");
            String key = args[0];
            try
            {
                Float value = Float.parseFloat(args[1]);
                ResourceLocation loc = new ResourceLocation(key);
                GeneticsManager.mutationRates.put(loc, value);
            }
            catch (Exception e)
            {
                System.err.println("Error with mutation rate for " + s);
            }
        }

        PokecubeItems.resetTimeTags = resetTags;
        if (resetTags) get(advanced, "resetTags", false).set(false);

        Database.FORCECOPY = forceDatabase;
        Database.FORCECOPYRECIPES = forceRecipes;
        Database.FORCECOPYREWARDS = forceRewards;

        if (configDatabases.length != EnumDatabase.values().length)
        {
            configDatabases = new String[] { "", "", "" };
            toSave = true;
        }
        SpawnHandler.dimensionBlacklist.clear();
        for (int i : dimensionBlacklist)
        {
            SpawnHandler.dimensionBlacklist.add(i);
        }
        SpawnHandler.dimensionWhitelist.clear();
        for (int i : dimensionWhitelist)
        {
            SpawnHandler.dimensionWhitelist.add(i);
        }
        boolean failed = false;
        if (dodgeSounds.length == 0) failed = true;
        else
        {
            dodges = new SoundEvent[dodgeSounds.length];
            for (int i = 0; i < dodgeSounds.length; i++)
            {
                String s = dodgeSounds[i];
                try
                {
                    SoundEvent e = getRegisteredSoundEvent(s);
                    dodges[i] = e;
                }
                catch (Exception e)
                {
                    PokecubeCore.log(Level.WARNING, "No Sound for " + s, e);
                    failed = true;
                    break;
                }
            }
        }

        if (failed)
        {
            dodges = new SoundEvent[] { SoundEvents.ENTITY_GENERIC_SMALL_FALL };
        }

        failed = false;
        if (leapSounds.length == 0) failed = true;
        else
        {
            leaps = new SoundEvent[leapSounds.length];
            for (int i = 0; i < leapSounds.length; i++)
            {
                String s = leapSounds[i];
                try
                {
                    SoundEvent e = getRegisteredSoundEvent(s);
                    leaps[i] = e;
                }
                catch (Exception e)
                {
                    PokecubeCore.log("No Sound for " + s);
                    failed = true;
                    break;
                }
            }
        }

        if (failed)
        {
            leaps = new SoundEvent[] { SoundEvents.ENTITY_GENERIC_SMALL_FALL };
        }

        if (PokecubeDimensionManager.SECRET_BASE_TYPE != null)
        {
            PokecubeDimensionManager.SECRET_BASE_TYPE.setLoadSpawn(basesLoaded);
        }

        if (toSave)
        {
            this.save();
        }
    }

    @Override
    public Property get(String category, String key, String defaultValue, String comment, Property.Type type)
    {
        Property prop = super.get(category, key, defaultValue, comment, type);
        requiresRestart(prop);
        return prop;
    }

    public List<Predicate<IBlockState>> getCaveBlocks()
    {
        return caveBlocks;
    }

    public List<Predicate<IBlockState>> getRocks()
    {
        return rocks;
    }

    public List<Predicate<IBlockState>> getSurfaceBlocks()
    {
        return surfaceBlocks;
    }

    public List<Predicate<IBlockState>> getTerrain()
    {
        return terrain;
    }

    public List<Predicate<IBlockState>> getWoodTypes()
    {
        return woodTypes;
    }

    public List<Predicate<IBlockState>> getPlantTypes()
    {
        return plantTypes;
    }

    public List<Predicate<IBlockState>> getFruitTypes()
    {
        return fruitTypes;
    }

    public List<Predicate<IBlockState>> getDirtTypes()
    {
        return dirtTypes;
    }

    public List<Predicate<IBlockState>> getIndustrial()
    {
        return industrial;
    }

    public void initDefaultStarts()
    {
        FMLCommonHandler.callFuture(new FutureTask<Object>(new Callable<Object>()
        {
            @Override
            public Object call() throws Exception
            {
                try
                {
                    ContributorManager.instance().loadContributors();
                    List<String> args = Lists.newArrayList();
                    for (Contributor c : ContributorManager.instance().contributors.contributors)
                    {
                        if (!c.legacy.isEmpty())
                        {
                            args.add(c.name + ";" + c.legacy);
                        }
                    }
                    StarterInfo.infos = args.toArray(new String[0]);
                    if (Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION))
                        StarterInfo.processStarterInfo();
                }
                catch (Exception e)
                {
                    if (e instanceof UnknownHostException)
                    {
                        PokecubeMod.log(Level.WARNING, "Error loading contributors, unknown host");
                    }
                    else PokecubeMod.log(Level.WARNING, "Error loading contributors", e);
                }
                return null;
            }
        }));
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
    {
        if (eventArgs.getModID().equals(PokecubeMod.ID))
        {
            populateSettings();
            applySettings();
            save();
        }
    }

    public void requiresRestart(Property property)
    {
        Class<?> me = getClass();
        Configure c;
        for (Field f : me.getDeclaredFields())
        {
            if (f.getName().equals(property.getName()))
            {
                c = f.getAnnotation(Configure.class);
                if (c != null)
                {
                    boolean needsMcRestart = c.needsMcRestart();
                    property.setRequiresMcRestart(needsMcRestart);
                }
                break;
            }
        }
    }

    @Override
    public void save()
    {
        List<Integer> dims = Lists.newArrayList(SpawnHandler.dimensionBlacklist);
        Collections.sort(dims);
        dimensionBlacklist = new int[dims.size()];
        for (int i = 0; i < dims.size(); i++)
        {
            dimensionBlacklist[i] = dims.get(i);
        }
        dims = Lists.newArrayList(SpawnHandler.dimensionWhitelist);
        Collections.sort(dims);
        dimensionWhitelist = new int[dims.size()];
        for (int i = 0; i < dims.size(); i++)
        {
            dimensionWhitelist[i] = dims.get(i);
        }
        super.save();
    }

    public void seenMessage()
    {
        load();
        get(misc, "loginmessage", false).set(false);
        get(misc, "version", PokecubeMod.VERSION).set(PokecubeMod.VERSION);
        save();
    }

    public void setSettings()
    {
        load();
        populateSettings(true);
        applySettings();
        save();
    }

    private static SoundEvent getRegisteredSoundEvent(String id)
    {
        SoundEvent soundevent = (SoundEvent) SoundEvent.REGISTRY.getObject(new ResourceLocation(id));
        if (soundevent == null)
        {
            throw new IllegalStateException("Invalid Sound requested: " + id);
        }
        else
        {
            return soundevent;
        }
    }
}
