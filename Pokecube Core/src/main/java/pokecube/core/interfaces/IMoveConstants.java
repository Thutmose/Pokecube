/**
 * 
 */
package pokecube.core.interfaces;

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
     * Flavours
     */
    byte             SPICY                = 0;       // red
    byte             DRY                  = 1;       // blue
    byte             SWEET                = 2;       // pink
    byte             BITTER               = 3;       // green
    byte             SOUR                 = 4;       // yellow

    /** Is the pokemob currently sitting */
    @Deprecated
    @OldAI
    static final int SITTING              = 1 << 0;
    /** Is the pokemob angry at something */
    @Deprecated
    @OldAI
    static final int ANGRY                = 1 << 1;
    /** Does the pokemob have an owner */
    @Deprecated
    @OldAI
    static final int TAMED                = 1 << 2;
    /** A Guarding pokemon will attack any strangers nearby */
    @Deprecated
    @OldAI
    static final int GUARDING             = 1 << 3;
    /** A Hunting pokemon will look for food to eat, Either prey or berries. */
    @Deprecated
    @OldAI
    static final int HUNTING              = 1 << 4;
    /** A Staying pokemon will act like a wild pokemon. */
    @Deprecated
    @OldAI
    static final int STAYING              = 1 << 5;
    /** A sleeping pokemon will try to sit at its home location */
    @Deprecated
    @OldAI
    static final int SLEEPING             = 1 << 6;
    /** Indicates that the pokemon is going to execute a move. */
    @Deprecated
    @OldAI
    static final int EXECUTINGMOVE        = 1 << 7;
    /** Indeicates that there is a new move to use. */
    @Deprecated
    @OldAI
    static final int NEWEXECUTEMOVE       = 1 << 8;
    /** Pokemon cannot have item used on it */
    @Deprecated
    @OldAI
    static final int NOITEMUSE            = 1 << 9;
    /** Pokemon is forbidden from swapping move */
    @Deprecated
    @OldAI
    static final int NOMOVESWAP           = 1 << 10;
    /** Pokemon is idle. */
    @Deprecated
    @OldAI
    static final int IDLE                 = 1 << 11;
    /** Has the Pokemon been traded */
    @Deprecated
    @OldAI
    static final int TRADED               = 1 << 12;
    /** is the pokemob prevented from moving (ie from ingrain, etc) */
    @Deprecated
    @OldAI
    static final int NOPATHING            = 1 << 13;
    /** is the pokemon leaping, used for the leap AI */
    @Deprecated
    @OldAI
    static final int LEAPING              = 1 << 14;
    /** in the process of dodging, used to determine if to use the old attack
     * location, or new */
    @Deprecated
    @OldAI
    static final int DODGING              = 1 << 15;
    /** Pokemon is fighting over mate, should stop when hp hits 50%. */
    @Deprecated
    @OldAI
    static final int MATEFIGHT            = 1 << 16;
    /** Is the pokemon just exiting the pokecube */
    @Deprecated
    @OldAI
    static final int EXITINGCUBE          = 1 << 17;
    /** Is the pokemob currently trying to mate */
    @Deprecated
    @OldAI
    static final int MATING               = 1 << 18;
    /** Prevented from flying or floating. */
    @Deprecated
    @OldAI
    static final int GROUNDED             = 1 << 19;
    // /** is the pokemob currently pathing somewhere */
    @Deprecated
    @OldAI
    static final int PATHING              = 1 << 20;
    /** is the pokemob jumping */
    @Deprecated
    @OldAI
    static final int JUMPING              = 1 << 21;
    /** is the pokemob in lava */
    @Deprecated
    @OldAI
    static final int INLAVA               = 1 << 22;
    /** is the pokemob in water */
    @Deprecated
    @OldAI
    static final int INWATER              = 1 << 23;
    /** is the pokemob tired */
    @Deprecated
    @OldAI
    static final int TIRED                = 1 << 24;
    /** is the pokemob evolving */
    @Deprecated
    @OldAI
    static final int EVOLVING             = 1 << 25;
    /** is the pokemob sheared */
    @Deprecated
    @OldAI
    static final int SHEARED              = 1 << 26;
    /** is the pokemob megaevolved */
    @Deprecated
    @OldAI
    static final int MEGAFORME            = 1 << 27;
    /** has the pokemob used a zmove this "battle" */
    @Deprecated
    @OldAI
    static final int USEDZMOVE            = 1 << 28;
    /** should capture be denied for this pokemob. */
    @Deprecated
    @OldAI
    static final int DENYCAPTURE          = 1 << 29;
    /** is the pokemob's movement being controlled. */
    @Deprecated
    @OldAI
    static final int CONTROLLED           = 1 << 30;

    public static enum AIRoutine
    {
        //@formatter:off
        GATHER,         //Does the pokemob gather item drops and harvest crops.
        STORE(false),   //Does the pokemob store its inventory when full.
        WANDER,         //Does the pokemob wander around randomly
        MATE,           //Does the pokemob breed.
        FOLLOW,         //Does the pokemob follow its owner.
        AGRESSIVE,      //Does the pokemob find targets to attack.
        AIRBORNE;       //Does the pokemob fly around, or can it only walk.
        //@formatter:on

        private final boolean default_;

        private AIRoutine()
        {
            default_ = true;
        }

        private AIRoutine(boolean value)
        {
            default_ = value;
        }

        /** @return default state for this routine. */
        public boolean getDefault()
        {
            return default_;
        }
    }
}
