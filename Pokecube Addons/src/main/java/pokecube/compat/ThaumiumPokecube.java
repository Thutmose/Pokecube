package pokecube.compat;

import static pokecube.core.PokecubeItems.register;
import static pokecube.core.interfaces.PokecubeMod.creativeTabPokecubes;

import net.minecraft.entity.Entity;
import pokecube.core.PokecubeItems;
import pokecube.core.events.CaptureEvent.Post;
import pokecube.core.events.CaptureEvent.Pre;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.Pokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class ThaumiumPokecube// extends Mod_Pokecube_Helper
{
			public void addThaumiumPokecube(){
			 Pokecube thaumiumpokecube = new CompatPokecubes();
		        thaumiumpokecube.setUnlocalizedName("thaumiumpokecube").setCreativeTab(creativeTabPokecubes);
		        register(thaumiumpokecube);
		        
		        PokecubeItems.addCube(98, new Object[] {thaumiumpokecube});
		        
		        PokecubeBehavior thaumic = new PokecubeBehavior(){

					@Override
					public void onPreCapture(Pre evt) {
						EntityPokecube cube = (EntityPokecube) evt.pokecube;
						IPokemob mob = (IPokemob) PokecubeMod.core.createEntityByPokedexNb(evt.caught.getPokedexNb(), cube.worldObj);
						int i = 1;
						int m = 1;
						String tag = cube.getEntityData().getString("Aspect");
						
						//TODO Make a better way to do this
						if(mob.getType1() == PokeType.bug && tag == "HUNGER"){m=m+3;}
						if(mob.getType2() == PokeType.bug && tag == "HUNGER"){m=m+3;}
						if(mob.getType1() == PokeType.dark && tag == "DARKNESS"){m=m+3;}
						if(mob.getType2() == PokeType.dark && tag == "DARKNESS"){m=m+3;}
						if(mob.getType1() == PokeType.dragon && tag == "WEATHER"){m=m+3;}
						if(mob.getType2() == PokeType.dragon && tag == "WEATHER"){m=m+3;}
						if(mob.getType1() == PokeType.electric && tag == "ENERGY"){m=m+3;}
						if(mob.getType2() == PokeType.electric && tag == "ENERGY"){m=m+3;}
						if(mob.getType1() == PokeType.fairy && tag == "AURA"){m=m+3;}
						if(mob.getType2() == PokeType.fairy && tag == "AURA"){m=m+3;}
						if(mob.getType1() == PokeType.fighting && tag == "WEAPON"){m=m+3;}
						if(mob.getType2() == PokeType.fighting && tag == "WEAPON"){m=m+3;}
						if(mob.getType1() == PokeType.fire && tag == "FIRE"){m=m+3;}
						if(mob.getType2() == PokeType.fire && tag == "FIRE"){m=m+3;}
						if(mob.getType1() == PokeType.flying && tag == "FLIGHT"){m=m+3;}
						if(mob.getType2() == PokeType.flying && tag == "FLIGHT"){m=m+3;}
						if(mob.getType1() == PokeType.ghost && tag == "SOUL"){m=m+3;}
						if(mob.getType2() == PokeType.ghost && tag == "SOUL"){m=m+3;}
						if(mob.getType1() == PokeType.grass && tag == "PLANT"){m=m+3;}
						if(mob.getType2() == PokeType.grass && tag == "PLANT"){m=m+3;}
						if(mob.getType1() == PokeType.ground && tag == "EARTH"){m=m+3;}
						if(mob.getType2() == PokeType.ground && tag == "EARTH"){m=m+3;}
						if(mob.getType1() == PokeType.ice && tag == "COLD"){m=m+3;}
						if(mob.getType2() == PokeType.ice && tag == "COLD"){m=m+3;}
						if(mob.getType1() == PokeType.normal && tag == "ORDER"){m=m+3;}
						if(mob.getType2() == PokeType.normal && tag == "ORDER"){m=m+3;}
						if(mob.getType1() == PokeType.poison && tag == "POISON"){m=m+3;}
						if(mob.getType2() == PokeType.poison && tag == "POISON"){m=m+3;}
						if(mob.getType1() == PokeType.psychic && tag == "MIND"){m=m+3;}
						if(mob.getType2() == PokeType.psychic && tag == "MIND"){m=m+3;}
						if(mob.getType1() == PokeType.rock && tag == "MINE"){m=m+3;}
						if(mob.getType2() == PokeType.rock && tag == "MINE"){m=m+3;}
						if(mob.getType1() == PokeType.steel && tag == "METAL"){m=m+3;}
						if(mob.getType2() == PokeType.steel && tag == "METAL"){m=m+3;}
						if(mob.getType1() == PokeType.unknown && tag == "ELDRITCH"){m=m+3;}
						if(mob.getType2() == PokeType.unknown && tag == "ELDRITCH"){m=m+3;}
						if(mob.getType1() == PokeType.water && tag == "WATER"){m=m+3;}
						if(mob.getType2() == PokeType.water && tag == "WATER"){m=m+3;}
					
						
						Vector3 v = Vector3.getNewVectorFromPool();
						Entity thrower = cube.shootingEntity;
						double rate = m;
		                cube.tilt = Tools.computeCatchRate(mob, rate);
						cube.time = cube.tilt * 4;
		                evt.caught.setPokecubeId(PokecubeItems.getCubeId(evt.filledCube));
		                cube.setEntityItemStack(PokecubeManager.pokemobToItem(evt.caught));
		                PokecubeManager.setTilt(cube.getEntityItem(), cube.tilt);
		                v.set(evt.caught).moveEntity(cube);
		                v.freeVectorFromPool();
		                ((Entity) evt.caught).setDead();
		                cube.setVelocity(0, 0.1, 0);
		                cube.worldObj.spawnEntityInWorld(cube.copy());
		                evt.setCanceled(true);
		                evt.pokecube.setDead();
						}
						
					

					@Override
					public void onPostCapture(Post evt) {
						
						
					}
		        };
		        PokecubeBehavior.addCubeBehavior(98, thaumic);
			 }
			 
	}
