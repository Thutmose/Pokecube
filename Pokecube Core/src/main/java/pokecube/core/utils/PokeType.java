package pokecube.core.utils;

import net.minecraft.util.StatCollector;
import pokecube.core.interfaces.Nature;

public enum PokeType
{
    // @formatter:off
    unknown		(	0, 		  "???"),
    normal		(	0xA8A878, "normal"),
    fighting	(	0xBF2F27, "fighting"),
    flying		(	0xA78FF0, "flying"),
    poison		( 	0x9F3F9F, "poison"),
    ground		( 	0xE0BF67, "ground"),
    rock		( 	0xB79F37, "rock"),
    bug			( 	0xA7B71F, "bug"),
    ghost		(	0x6F5797, "ghost"),
    steel		( 	0xB7B7D0, "steel"),
    fire		( 	0xF08030, "fire"),
    water		(	0x678FF0, "water"),
    grass		(	0x77C84F, "grass"),
    electric	(	0xF8D02F, "electric"),
    psychic		( 	0xF85787, "psychic"),
    ice			( 	0x97D8D8, "ice"),
    dragon		(	0x6F37F8, "dragon"),
    dark		( 	0x6F5747, "dark"),
    fairy		( 	0xE6A5E6, "fairy");
    // @formatter:on
    public final int colour;
    public final String name;

    private PokeType(int colour, String name)
    {
        this.colour = colour;
        this.name = name;
    }

    // public static final PokeType[] TYPES = { unknown,
    // normal, fighting, flying, poison, ground,
    // rock, bug, ghost, steel, fire, water, grass,
    // electric, psychic, ice, dragon, dark, fairy };

    private static float o = 0; // ineffective
    private static float n = 1F; // normal
    private static float v = 2F; // very effective
    private static float x = 0.5F; // not very effective
 // @formatter:off
    private static float[][] typeTable =
    {
//         unknown, normal, fighting, flying, poison, ground, rock, bug, ghost, steel, fire, water, grass, electric, psychic, ice, dragon, dark, fairy
/* unknown 	*/{ n,	n,		n,		  n,	  n,	  n,	  n,	n,	 n,		n,	   n,	 n,		n,	   n,	     n,		  n,	n,		n,   n  },

/* normal 	*/{ n,	n,		n,		  n,	  n,	  n,	  x,	n,	 o,		x,	   n,	 n,		n,	   n,	     n,		  n,	n,		n,   n  },
/* fighting */{ n,	v,		n,		  x,	  x,	  n,	  v,	x,	 o,		v,	   n,	 n,		n,	   n,	     x,		  v,	n,		v,	 x  },
/* flying 	*/{ n,	n,		v,		  n,	  n,	  n,	  x,	v,	 n,		x,	   n,	 n,		v,	   x,	     n,		  n,	n,		n,	 n  },
/* poison 	*/{ n,	n,		n,		  n,	  x,	  x,	  x,	n,	 x,		o,	   n,	 n,		v,	   n,	     n,		  n,	n,		n,	 v  },
/* ground 	*/{ n,	n,		n,		  o,	  v,	  n,	  v,	x,	 n,		v,	   v,	 n,		x,	   v,	     n,		  n,	n,		n,	 n  },
/* rock 	*/{ n,	n,		x,		  v,	  n,	  x,	  n,	v,	 n,		x,	   v,	 n,		n,	   n,	     n,		  v,	n,		n,	 n  },
/* bug 		*/{ n,	n,		x,		  x,	  x,	  n,	  n,	n,	 x,		x,	   x,	 n,		v,	   n,	     v,		  n,	n,		v,	 x  },
/* ghost 	*/{ n,	o,		n,		  n,	  n,	  n,	  n,	n,	 v,		n,	   n,	 n,		n,	   n,	     v,		  n,	n,		x,	 n  },
/* steel 	*/{ n,	n,		n,		  n,	  n,	  n,	  v,	n,	 n,		x,	   x,	 x,		n,	   x,	     n,		  v,	n,		n,	 v  },
/* fire 	*/{ n,	n,		n,		  n,	  n,	  n,	  x,	v,	 n,		v,	   x,	 x,		v,	   n,	     n,		  v,	x,		n,	 n  },
/* water 	*/{ n,	n,		n,		  n,	  n,	  v,	  v,	n,	 n,		n,	   v,	 x,		x,	   n,	     n,		  n,	x,		n,	 n  },
/* grass 	*/{ n,	n,		n,		  x,	  x,	  v,	  v,	x,	 n,		x,	   x,	 v,		x,	   n,	     n,		  n,	x,		n,	 n  },
/* electric */{ n,	n,		n,		  v,	  n,	  o,	  n,	n,	 n,		n,	   n,	 v,		x,	   x,	     n,		  n,	x,		n,	 n  },
/* psychic 	*/{ n,	n,		v,		  n,	  v,	  n,	  n,	n,	 n,		x,	   n,	 n,		n,	   n,	     x,		  n,	n,		o,	 n  },
/* ice 		*/{ n,	n,		n,		  v,	  n,	  v,	  n,	n,	 n,		x,	   x,	 x,		v,	   n,	     n,		  x,	v,		n,	 n  },
/* dragon 	*/{ n,	n,		n,		  n,	  n,	  n,	  n,	n,	 n,		x,	   n,	 n,		n,	   n,	     n,		  n,	v,		n,	 o  },
/* dark 	*/{ n,	n,		x,		  n,	  n,	  n,	  n,	n,	 v,		n,	   n,	 n,		n,	   n,	     v,		  n,	n,		x,	 x  },
/* fairy 	*/{ n,	n,		v,		  n,	  x,	  n,	  n,	n,	 n,		x,	   x,	 n,		n,	   n,	     n,		  n,	v,		v,	 n  }
    };
    // @formatter:on

    public static float getAttackEfficiency(PokeType type, PokeType defenseType1, PokeType defenseType2)
    {
        float multiplier = typeTable[type.ordinal()][defenseType1.ordinal()];

        if (defenseType2.compareTo(unknown) > 0)
        {
            multiplier *= typeTable[type.ordinal()][defenseType2.ordinal()];
        }

        return multiplier;
    }

    public static String getName(PokeType type)
    {
        return type.name;
    }

    public static String getTranslatedName(PokeType type)
    {
        String translated = StatCollector.translateToLocal("type." + getName(type));

        if (translated == null || translated.startsWith("type.")) { return getName(type); }

        return translated;
    }

    public static PokeType getType(int id)
    {
        for (PokeType type : values())
        {
            if (type.ordinal() == id) return type;
        }
        return unknown;
    }

    public static PokeType getType(String name)
    {
        name = name.toLowerCase().trim();
        for (PokeType type : values())
        {
            if (name.equalsIgnoreCase(type.name)) return type;
        }
        return unknown;
    }

    public static byte[] statsModFromNature(byte nature)
    {
        return Nature.values()[nature].getStatsMod();
    }

}
