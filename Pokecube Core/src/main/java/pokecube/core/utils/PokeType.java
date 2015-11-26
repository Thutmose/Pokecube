package pokecube.core.utils;

import static pokecube.core.interfaces.IMoveConstants.ADAMANT;
import static pokecube.core.interfaces.IMoveConstants.BOLD;
import static pokecube.core.interfaces.IMoveConstants.BRAVE;
import static pokecube.core.interfaces.IMoveConstants.CALM;
import static pokecube.core.interfaces.IMoveConstants.CAREFUL;
import static pokecube.core.interfaces.IMoveConstants.GENTLE;
import static pokecube.core.interfaces.IMoveConstants.HASTY;
import static pokecube.core.interfaces.IMoveConstants.IMPISH;
import static pokecube.core.interfaces.IMoveConstants.JOLLY;
import static pokecube.core.interfaces.IMoveConstants.LAX;
import static pokecube.core.interfaces.IMoveConstants.LONELY;
import static pokecube.core.interfaces.IMoveConstants.MILD;
import static pokecube.core.interfaces.IMoveConstants.MODEST;
import static pokecube.core.interfaces.IMoveConstants.NAIVE;
import static pokecube.core.interfaces.IMoveConstants.NAUGHTY;
import static pokecube.core.interfaces.IMoveConstants.QUIET;
import static pokecube.core.interfaces.IMoveConstants.RASH;
import static pokecube.core.interfaces.IMoveConstants.RELAXED;
import static pokecube.core.interfaces.IMoveConstants.SASSY;
import static pokecube.core.interfaces.IMoveConstants.TIMID;

import net.minecraft.util.StatCollector;

public enum PokeType {
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
    
    public final int colour;
    public final String name;
    
    private PokeType(int colour, String name)
    {
    	this.colour = colour;
    	this.name = name;
    }
    
//    public static final PokeType[] TYPES = { unknown, 
//    		normal, fighting, flying, poison, ground,
//    		rock, bug, ghost, steel, fire, water, grass,
//			electric, psychic, ice, dragon, dark, fairy };
    
    private static float o = 0;		// ineffective
    private static float n = 1F; 	// normal
    private static float v = 2F; 	// very effective
    private static float x = 0.5F; 	// not very effective
    
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
        String translated =  StatCollector.translateToLocal("type." + getName(type));

        if (translated == null || translated.startsWith("type."))
        {
            return getName(type);
        }

        return translated;
    }
    
    public static PokeType getType(int id)
    {
    	for(PokeType type: values())
    	{
    		if(type.ordinal() == id)
    			return type;
    	}
    	return unknown;
    }
    
    public static PokeType getType(String name)
    {
    	name = name.toLowerCase().trim();
    	for(PokeType type: values())
    	{
    		if(name.equalsIgnoreCase(type.name))
    			return type;
    	}
    	return unknown;
    }
    
	public static byte[] statsModFromNature(byte nature)
	{
		switch(nature)
		{
		//Attack
		case(LONELY):	return new byte[]{0,1,-1,0,0,0};
		case(BRAVE):	return new byte[]{0,1,0,0,0,-1};
		case(ADAMANT):	return new byte[]{0,1,0,-1,0,0};
		case(NAUGHTY):	return new byte[]{0,1,0,0,-1,0};
		//Defense
		case(BOLD):		return new byte[]{0,-1,1,0,0,0};
		case(RELAXED):	return new byte[]{0,0,1,0,0,-1};
		case(IMPISH):	return new byte[]{0,0,1,-1,0,0};
		case(LAX):		return new byte[]{0,0,1,0,-1,0};
		//Speed
		case(TIMID):	return new byte[]{0,-1,0,0,0,1};
		case(HASTY):	return new byte[]{0,0,-1,0,0,1};
		case(JOLLY):	return new byte[]{0,0,0,-1,0,1};
		case(NAIVE):	return new byte[]{0,0,0,0,-1,1};
		//SpAtk
		case(MODEST):	return new byte[]{0,-1,0,1,0,0};
		case(MILD):		return new byte[]{0,0,-1,1,0,0};
		case(QUIET):	return new byte[]{0,0,0,1,0,-1};
		case(RASH):		return new byte[]{0,0,0,1,-1,0};
		//SpDef
		case(CALM):		return new byte[]{0,-1,0,0,1,0};
		case(GENTLE):	return new byte[]{0,0,-1,0,1,0};
		case(SASSY):	return new byte[]{0,0,0,0,1,-1};
		case(CAREFUL):	return new byte[]{0,0,0,-1,1,0};
		
		default: 	return new byte[]{0,0,0,0,0,0};
		}
	}
    
}


