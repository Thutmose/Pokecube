/**
 * 
 */
package pokecube.core.interfaces;

import pokecube.core.utils.PokeType;

/** @author Manchou */
public interface IMoveConstants extends IMoveNames
{
    /*
     * exclusive Status Effects
     */
    byte             STATUS_NON           = 0;
    byte             STATUS_BRN           = 1;
    byte             STATUS_FRZ           = 2;
    byte             STATUS_PAR           = 4;
    byte             STATUS_PSN           = 8;
    byte             STATUS_PSN2          = 24;
    byte             STATUS_SLP           = 32;

    /*
     * Stats Modifiers
     */
    byte             ATTACK               = 1;
    byte             DEFENSE              = 2;
    byte             SPATACK              = 4;
    byte             SPDEFENSE            = 8;
    byte             VIT                  = 16;
    byte             ACCURACY             = 32;
    byte             EVASION              = 64;

    /*
     * Stats Changes
     */
    byte             HARSH                = -2;
    byte             FALL                 = -1;
    byte             RAISE                = 1;
    byte             SHARP                = 2;
    byte             DRASTICALLY          = 3;

    /*
     * non-exclusive status effects
     */
    byte             CHANGE_NONE          = 0;
    byte             CHANGE_CONFUSED      = 1;
    byte             CHANGE_FLINCH        = 2;
    byte             CHANGE_CURSE         = 4;

    /*
     * Move Categories
     */
    byte             CATEGORY_CONTACT     = 1;
    byte             CATEGORY_DISTANCE    = 2;
    byte             CATEGORY_SELF        = 4;
    byte             CATEGORY_SELF_EFFECT = 8;

    /*
     * Move damage category
     */
    byte             SPECIAL              = 1;
    byte             PHYSICAL             = 2;

    // Special Moves, ie ones needed for specific logic
    // No move move for just sitting there
    String           MOVE_NONE            = "none";
    String           DEFAULT_MOVE         = "tackle";

    /*
     * Types
     */
    PokeType         unknown              = PokeType.unknown;
    PokeType         normal               = PokeType.normal;
    PokeType         fighting             = PokeType.fighting;
    PokeType         flying               = PokeType.flying;
    PokeType         poison               = PokeType.poison;
    PokeType         ground               = PokeType.ground;
    PokeType         rock                 = PokeType.rock;
    PokeType         bug                  = PokeType.bug;
    PokeType         ghost                = PokeType.ghost;
    PokeType         steel                = PokeType.steel;
    PokeType         fire                 = PokeType.fire;
    PokeType         water                = PokeType.water;
    PokeType         grass                = PokeType.grass;
    PokeType         electric             = PokeType.electric;
    PokeType         psychic              = PokeType.psychic;
    PokeType         ice                  = PokeType.ice;
    PokeType         dragon               = PokeType.dragon;
    PokeType         dark                 = PokeType.dark;
    PokeType         fairy                = PokeType.fairy;

    /*
     * Flavours
     */
    byte             SPICY                = 0;                // red
    byte             DRY                  = 1;                // blue
    byte             SWEET                = 2;                // pink
    byte             BITTER               = 3;                // green
    byte             SOUR                 = 4;                // yellow

    /** Is the pokemob currently sitting */
    static final int SITTING              = 1 << 0;
    /** Is the pokemob angry at something */
    static final int ANGRY                = 1 << 1;
    /** Does the pokemob have an owner */
    static final int TAMED                = 1 << 2;
    /** A Guarding pokemon will attack any strangers nearby */
    static final int GUARDING             = 1 << 3;
    /** A Hunting pokemon will look for food to eat, Either prey or berries. */
    static final int HUNTING              = 1 << 4;
    /** A Staying pokemon will act like a wild pokemon. */
    static final int STAYING              = 1 << 5;
    /** A sleeping pokemon will try to sit at its home location */
    static final int SLEEPING             = 1 << 6;
    /** Indicates that the pokemon is going to execute a utility move. */
    static final int EXECUTINGMOVE        = 1 << 7;
    /** Indeicates that there is a new utility move to use. */
    static final int NEWEXECUTEMOVE       = 1 << 8;
    /** Pokemon is held by the player. */
    static final int NOITEMUSE            = 1 << 9;
    /** Pokemon is on the player's shoulder */
    static final int NOMOVESWAP           = 1 << 10;
    /** Pokemon is idle. */
    static final int IDLE                 = 1 << 11;
    /** Has the Pokemon been traded */
    static final int TRADED               = 1 << 12;
    /** Does the pokemon have a saddle on it */
    static final int SADDLED              = 1 << 13;
    /** is the pokemon leaping, used for the leap AI */
    static final int LEAPING              = 1 << 14;
    /** in the process of dodging, used to determine if to use the old attack
     * location, or new */
    static final int DODGING              = 1 << 15;
    /** Pokemon is fighting over mate, should stop when hp hits 50%. */
    static final int MATEFIGHT            = 1 << 16;
    /** Is the pokemon just exiting the pokecube */
    static final int EXITINGCUBE          = 1 << 17;
    /** Is the pokemob currently trying to mate */
    static final int MATING               = 1 << 18;
    /** does the pokemob have a new move to learn */
    static final int LEARNINGMOVE         = 1 << 19;
    // /** is the pokemob pathing to owner */
    static final int PATHING              = 1 << 20;
    /** is the pokemob jumping */
    static final int JUMPING              = 1 << 21;
    /** is the pokemob in lava */
    static final int INLAVA               = 1 << 22;
    /** is the pokemob in water */
    static final int INWATER              = 1 << 23;
    /** is the pokemob tired */
    static final int TIRED                = 1 << 24;
    /** is the pokemob evolving */
    static final int EVOLVING             = 1 << 25;
    /** is the pokemob sheared */
    static final int SHEARED              = 1 << 26;
    /** is the pokemob megaevolved */
    static final int MEGAFORME            = 1 << 27;
    /** has the pokemob used a zmove this "battle" */
    static final int USEDZMOVE            = 1 << 28;

}
