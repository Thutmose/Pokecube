package pokecube.core.database.moves;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import pokecube.core.database.moves.json.JsonMoves;
import pokecube.core.database.moves.json.JsonMoves.MoveJsonEntry;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.utils.PokeType;

public class MoveEntry implements IMoveConstants
{
    public static enum Category
    {
        OTHER, SPECIAL, PHYSICAL;
    }

    private static HashMap<String, MoveEntry> movesNames       = new HashMap<String, MoveEntry>();
    public static HashSet<String>             protectionMoves  = new HashSet<String>();
    public static HashSet<String>             unBlockableMoves = new HashSet<String>();
    public static HashSet<String>             oneHitKos        = new HashSet<String>();

    public static int                         TOTALHP          = 1;
    public static int                         DAMAGEDEALT      = 2;
    public static int                         RELATIVEHP       = 4;
    public static int                         MISS             = 8;

    public static int                         NODAMAGE         = -2;
    public static int                         FULLHP           = -1;
    public static int                         LEVEL            = -5;
    public static int                         SPECIAL          = -4;
    public static int                         FLEE             = -3;

    static
    {
        MoveEntry confusion = new MoveEntry("pokemob.status.confusion", -1);
        confusion.type = PokeType.unknown;
        confusion.category = PHYSICAL;
        confusion.attackCategory = CATEGORY_CONTACT + CATEGORY_SELF;
        confusion.power = 40;
        confusion.protect = false;
        confusion.magiccoat = false;
        confusion.snatch = false;
        confusion.kingsrock = false;
        confusion.notIntercepable = true;
        confusion.baseEntry = new MoveJsonEntry();
    }

    public static MoveEntry get(String name)
    {
        return movesNames.get(name);
    }

    public static Collection<MoveEntry> values()
    {
        return movesNames.values();
    }

    public final String            name;
    public final int               index;
    public PokeType                type;
    /** Distance, contact, etc. */
    public int                     attackCategory;
    public int                     power                    = 0;
    public int                     accuracy;
    public int                     pp;
    public byte                    statusChange;
    public float                   statusChance;
    public byte                    change                   = CHANGE_NONE;
    public float                   chanceChance             = 0;
    public int[]                   attackerStatModification = { 0, 0, 0, 0, 0, 0, 0, 0 };
    public float                   attackerStatModProb      = 1;
    public int[]                   attackedStatModification = { 0, 0, 0, 0, 0, 0, 0, 0 };
    public float                   attackedStatModProb      = 1;
    public float                   damageHeal               = 0;
    public float                   selfHealRatio            = 0;
    private boolean                multiTarget;
    private boolean                notIntercepable;
    public boolean                 protect;
    public boolean                 magiccoat;
    public boolean                 snatch;
    public boolean                 kingsrock;
    public int                     crit;
    public boolean                 soundType                = false;
    public boolean                 isPunch                  = false;
    public boolean                 fixed                    = false;
    public float                   selfDamage               = 0;
    public int                     selfDamageType;
    public int                     priority                 = 0;
    public boolean                 defrosts                 = false;
    public boolean                 mirrorcoated             = false;

    /** Whether the move requires recharging after. If a move wants to have a
     * delay beforehand, it should have that sorted in its animation. */
    public boolean                 delayAfter               = false;

    /** Status, Special, Physical */
    public byte                    category                 = -1;
    public String                  animDefault              = "none";
    public JsonMoves.MoveJsonEntry baseEntry;

    public MoveEntry(String name, int index)
    {
        this.name = name;
        this.index = index;
        movesNames.put(name, this);
    }

    public boolean isMultiTarget()
    {
        if (baseEntry != null) return baseEntry.multiTarget;
        return multiTarget;
    }

    public boolean isNotIntercepable()
    {
        if (baseEntry != null) return !baseEntry.interceptable;
        return notIntercepable;
    }

    public void setNotIntercepable(boolean b)
    {
        if (baseEntry != null) baseEntry.interceptable = !b;
        else notIntercepable = b;
    }

}
