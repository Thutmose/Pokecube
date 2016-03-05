package pokecube.core.database;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.utils.PokeType;

public class MoveEntry implements IMoveConstants
{

    private static HashMap<String, MoveEntry> movesNames               = new HashMap<String, MoveEntry>();
    public static HashSet<String>             protectionMoves          = new HashSet<String>();
    public static HashSet<String>             unBlockableMoves         = new HashSet<String>();
    public static HashSet<String>             oneHitKos                = new HashSet<String>();

    public static int                         TOTALHP                  = 1;
    public static int                         DAMAGEDEALT              = 2;
    public static int                         RELATIVEHP               = 4;
    public static int                         MISS                     = 8;

    public static int                         NODAMAGE                 = -2;
    public static int                         FULLHP                   = -1;
    public static int                         LEVEL                    = -5;
    public static int                         SPECIAL                  = -4;
    public static int                         FLEE                     = -3;

    public final String                       name;
    public final int                          index;

    public PokeType                           type;
    /** Distance, contact, etc. */
    public int                                attackCategory;
    public int                                power                    = 0;
    public int                                accuracy;
    public int                                pp;
    public byte                               statusChange;
    public float                              statusChance;
    public byte                               change                   = CHANGE_NONE;
    public int                                chanceChance             = 0;
    public int[]                              attackerStatModification = { 0, 0, 0, 0, 0, 0, 0, 0 };
    public int                                attackerStatModProb      = 100;
    public int[]                              attackedStatModification = { 0, 0, 0, 0, 0, 0, 0, 0 };
    public int                                attackedStatModProb      = 100;
    public float                              damageHealRatio          = 0;
    public float                              selfHealRatio            = 0;
    public boolean                            multiTarget;
    public boolean                            notIntercepable;
    public boolean                            protect;
    public boolean                            magiccoat;
    public boolean                            snatch;
    public boolean                            kingsrock;
    public int                                crit;
    public float                              selfDamage               = 0;
    public int                                selfDamageType;
    /** Status, Special, Physical */
    public byte                               category                 = -1;

    public String                             animDefault              = "none";

    static
    {
        MoveEntry confusion = new MoveEntry("pokemob.status.confusion", -1);
        confusion.type = PokeType.unknown;
        confusion.category = PHYSICAL;
        confusion.attackCategory = CATEGORY_CONTACT;
        confusion.power = 40;
        confusion.protect = false;
        confusion.magiccoat = false;
        confusion.snatch = false;
        confusion.kingsrock = false;
        confusion.notIntercepable = true;
        confusion.multiTarget = false;
    }

    public MoveEntry(String name, int index)
    {
        this.name = name;
        this.index = index;
        movesNames.put(name, this);
    }

    public static MoveEntry get(String name)
    {
        return movesNames.get(name);
    }

    public static Collection<MoveEntry> values()
    {
        return movesNames.values();
    }

}
