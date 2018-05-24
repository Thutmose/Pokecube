package pokecube.core.utils;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.moves.MovesUtils;

/** This is a list of permissions nodes, as well as information about what they
 * do, and what they are for. All of these are registered, after postinit, with
 * the listed default levels. */
public class Permissions
{
    public static void register()
    {
        IPermissionHandler handler = PermissionAPI.getPermissionHandler();
        handler.registerNode(CATCHPOKEMOB, DefaultPermissionLevel.ALL, "can catch a mob?");
        handler.registerNode(HATCHPOKEMOB, DefaultPermissionLevel.ALL, "can hatch a mob?");
        handler.registerNode(SENDOUTPOKEMOB, DefaultPermissionLevel.ALL, "can send out a mob?");

        handler.registerNode(RIDEPOKEMOB, DefaultPermissionLevel.ALL, "can ride a mob?");
        handler.registerNode(FLYPOKEMOB, DefaultPermissionLevel.ALL, "can fly a mob?");
        handler.registerNode(SURFPOKEMOB, DefaultPermissionLevel.ALL, "can surf a mob?");
        handler.registerNode(DIVEPOKEMOB, DefaultPermissionLevel.ALL, "can dive a mob?");

        for (PokedexEntry entry : Database.getSortedFormes())
        {
            String catcha = CATCHPOKEMOB + "." + entry.getTrimmedName();
            String hatcha = HATCHPOKEMOB + "." + entry.getTrimmedName();
            String senda = SENDOUTPOKEMOB + "." + entry.getTrimmedName();
            String ridea = RIDEPOKEMOB + "." + entry.getTrimmedName();
            String flya = FLYPOKEMOB + "." + entry.getTrimmedName();
            String surfa = SURFPOKEMOB + "." + entry.getTrimmedName();
            String divea = DIVEPOKEMOB + "." + entry.getTrimmedName();

            handler.registerNode(catcha, DefaultPermissionLevel.ALL, "can catch a " + entry + "?");
            handler.registerNode(hatcha, DefaultPermissionLevel.ALL, "can hatch a " + entry + "?");
            handler.registerNode(senda, DefaultPermissionLevel.ALL, "can send out a " + entry + "?");

            handler.registerNode(ridea, DefaultPermissionLevel.ALL, "can ride a " + entry + "?");
            handler.registerNode(flya, DefaultPermissionLevel.ALL, "can fly a " + entry + "?");
            handler.registerNode(surfa, DefaultPermissionLevel.ALL, "can surf a " + entry + "?");
            handler.registerNode(divea, DefaultPermissionLevel.ALL, "can dive a " + entry + "?");

            CATCHSPECIFIC.put(entry, catcha);
            CATCHSPECIFIC.put(entry, hatcha);
            CATCHSPECIFIC.put(entry, senda);

            RIDESPECIFIC.put(entry, ridea);
            FLYSPECIFIC.put(entry, flya);
            SURFSPECIFIC.put(entry, surfa);
            DIVESPECIFIC.put(entry, divea);
        }

        for (String s : MovesUtils.moves.keySet())
        {
            String move = "pokecube.move.action." + s;
            handler.registerNode(move, DefaultPermissionLevel.ALL, "can use " + move + " out of battle?");
        }
    }

    /** Can the player ride a pokemob. Default to ALL */
    public static final String                    RIDEPOKEMOB     = "pokecube.ride";
    /** Can the player ride a specific pokemob, checked after checking
     * RIDEPOKEMOB, only if it is allowed, has format "pokecube.ride.<trimmed
     * entry name>" Default to ALL */
    public static final Map<PokedexEntry, String> RIDESPECIFIC    = Maps.newHashMap();

    /** Can the player surf a pokemob. Default to ALL */
    public static final String                    SURFPOKEMOB     = "pokecube.surf";
    /** Can the player surf a specific pokemob, checked after checking
     * SURFPOKEMOB, only if it is allowed, has format "pokecube.surf.<trimmed
     * entry name>" Default to ALL */
    public static final Map<PokedexEntry, String> SURFSPECIFIC    = Maps.newHashMap();

    /** Can the player surf a pokemob. Default to ALL */
    public static final String                    DIVEPOKEMOB     = "pokecube.dive";
    /** Can the player surf a specific pokemob, checked after checking
     * DIVEPOKEMOB, only if it is allowed, has format "pokecube.dive.<trimmed
     * entry name>" Default to ALL */
    public static final Map<PokedexEntry, String> DIVESPECIFIC    = Maps.newHashMap();

    /** Can the player fly a pokemob. Default to ALL */
    public static final String                    FLYPOKEMOB      = "pokecube.fly";
    /** Can the player fly a specific pokemob, checked after checking
     * FLYPOKEMOB, only if it is allowed, has format "pokecube.fly.<trimmed
     * entry name>" Default to ALL */
    public static final Map<PokedexEntry, String> FLYSPECIFIC     = Maps.newHashMap();

    /** can the player use the specified world action, format is
     * "pokecube.move.action.<move name>, Default to ALL */
    public static final Map<String, String>       MOVEWORLDACTION = Maps.newHashMap();

    /** Can the player catch a pokemob. If not, the pokecube will bounce off,
     * similar to legendary conditions. Default to ALL */
    public static final String                    CATCHPOKEMOB    = "pokecube.catch";
    /** Can the player catch a specific pokemob, checked after checking
     * CATCHPOKEMOB, has format "pokecube.catch.<trimmed entry name>" Default to
     * ALL */
    public static final Map<PokedexEntry, String> CATCHSPECIFIC   = Maps.newHashMap();

    /** Can the player send out pokemobs, if false, it returns the cube to their
     * inventory (or sends to pc). Default to ALL */
    public static final String                    SENDOUTPOKEMOB  = "pokecube.sendout";
    /** Can the player send out specific pokemob, if false, it returns the cube
     * to their inventory (or sends to pc), checked after checking
     * SENDOUTPOKEMOB, has format "pokecube.sendout.<trimmed entry name>"
     * Default to ALL */
    public static final Map<PokedexEntry, String> SENDOUTSPECIFIC = Maps.newHashMap();

    /** Can the player hatch a egg, if not, the egg will hatch as a wild pokemob
     * instead. Default to ALL */
    public static final String                    HATCHPOKEMOB    = "pokecube.hatch";
    /** Can the player hatch a specific pokemob, checked after checking
     * HATCHPOKEMOB, has format "pokecube.hatch.<trimmed entry name>" Default to
     * ALL */
    public static final Map<PokedexEntry, String> HATCHSPECIFIC   = Maps.newHashMap();

}
