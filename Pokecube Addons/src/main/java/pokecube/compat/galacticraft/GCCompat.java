package pokecube.compat.galacticraft;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IRETURN;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.biome.BiomeGenBase;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.events.ClassGenEvent;
import pokecube.core.events.handlers.SpawnHandler;

public class GCCompat {

	private static HashMap<String, Float> moonmon = new HashMap();

	static {
		moonmon.put("clefairy", 0.1f);
		moonmon.put("clefable", 0.0f);
		moonmon.put("lunatone", 0.5f);
	}
	private static HashMap<String, Float> roidmon = new HashMap();

	static {
		roidmon.put("lunatone", 0.5f);
		roidmon.put("solrock", 0.5f);
	}
	private static HashMap<String, Float> spacemon = new HashMap();

	static {
		spacemon.put("deoxys", 0.001f);
	}
	
	
	private HashSet<Integer> spaceNums = new HashSet();
	boolean init = false;

	public void register() {
		addToMoons();
		addToMars();
		addToSpace();
		addToAsteroids();
		SpawnHandler.sortSpawnables();
	}

	private void addToMoons() {
		try {
			Class<?> biomeClass = Class
					.forName("micdoodle8.mods.galacticraft.core.world.gen.BiomeGenBaseMoon");
			if (biomeClass != null) {
				for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray()) {
					if (biomeClass.isInstance(b)) {
						SpawnHandler.spawns.remove(b.biomeID);
						for (String s : moonmon.keySet()) {
							PokedexEntry entry = Database.getEntry(s);
							if (entry != null) {
								if(entry.getSpawnData() == null)
								{
									entry.setSpawnData(new SpawnData());
								}
								entry.getSpawnData()
										.addBiome(b, moonmon.get(s));
								SpawnHandler.addSpawn(entry, b);
								System.out.println("Registered " + s
										+ " for the Moon " + b.biomeID);
							}
						}
					}
				}
			}
		} catch (ClassNotFoundException e) {
		}
	}

	private void addToMars() {

	}

	private void addToSpace() {
		try {
			Class<?> biomeClass = Class
					.forName("micdoodle8.mods.galacticraft.core.world.gen.BiomeGenBaseOrbit");
			if (biomeClass != null) {
				for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray()) {
					if (biomeClass.isInstance(b)) {
						SpawnHandler.spawns.remove(b.biomeID);
						for (String s : spacemon.keySet()) {
							PokedexEntry entry = Database.getEntry(s);
							if (entry != null) {
								if(entry.getSpawnData() == null)
								{
									entry.setSpawnData(new SpawnData());
								}
								entry.getSpawnData()
										.addBiome(b, spacemon.get(s));
								SpawnHandler.addSpawn(entry, b);
								System.out.println("Registered " + s
										+ " for Orbit " + b.biomeID);
							}
						}
					}
				}
			}
		} catch (ClassNotFoundException e) {
		}
	}

	private void addToAsteroids() {
		try {
			Class<?> biomeClass = Class
					.forName("micdoodle8.mods.galacticraft.planets.asteroids.world.gen.BiomeGenBaseAsteroids");
			if (biomeClass != null) {
				for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray()) {
					if (biomeClass.isInstance(b)) {
						SpawnHandler.spawns.remove(b.biomeID);
						for (String s : roidmon.keySet()) {
							PokedexEntry entry = Database.getEntry(s);
							if (entry != null) {
								if(entry.getSpawnData() == null)
								{
									entry.setSpawnData(new SpawnData());
								}
								entry.getSpawnData()
										.addBiome(b, roidmon.get(s));
								SpawnHandler.addSpawn(entry, b);
								System.out.println("Registered " + s
										+ " for Asteroids " + b.biomeID);
							}
						}
					}
				}
			}
		} catch (ClassNotFoundException e) {
		}
	}

	@SubscribeEvent
	public void makeVacuumSafe(ClassGenEvent evt) {
		if (!init) {
			for (String s : moonmon.keySet()) {
				PokedexEntry entry = Database.getEntry(s);
				if (entry != null) {
					spaceNums.add(entry.getPokedexNb());
				}
			}
			for (String s : roidmon.keySet()) {
				PokedexEntry entry = Database.getEntry(s);
				if (entry != null) {
					spaceNums.add(entry.getPokedexNb());
				}
			}
			for (String s : spacemon.keySet()) {
				PokedexEntry entry = Database.getEntry(s);
				if (entry != null) {
					spaceNums.add(entry.getPokedexNb());
				}
			}
			init = true;
		}
		if (spaceNums.contains(evt.pokedexNb)) {
			try {
				Class<?> inter = Class
						.forName("micdoodle8.mods.galacticraft.api.entity.IEntityBreathable");

				ClassWriter cw = evt.writer;
				
				Field interfaces = ClassWriter.class.getDeclaredField("interfaces");
				Field count = ClassWriter.class.getDeclaredField("interfaceCount");
				interfaces.setAccessible(true);
				count.setAccessible(true);
				
				int[] ints = (int[]) interfaces.get(cw);
				int n = count.getInt(cw);
				
				int[] newints = new int[ints!=null?ints.length+1:1];
				n = newints.length;
				count.set(cw, n);
				if(ints!=null)
				for(int i = 0; i<ints.length; i++)
				{
					newints[i] = ints[i];
				}
				newints[n-1] = cw.newClass(Type.getInternalName(inter));
				interfaces.set(cw, newints);
				
				MethodVisitor mv;
				{
					mv = cw.visitMethod(ACC_PUBLIC, "canBreath", "()Z", null, null);
					mv.visitCode();
					Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitInsn(ICONST_1);
					mv.visitInsn(IRETURN);
					Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitMaxs(1, 1);
					mv.visitEnd();
				}
				
			} catch (Throwable t) {
				
			}
		}
	}
}
