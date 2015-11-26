package pokecube.core.events;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import net.minecraftforge.fml.common.eventhandler.Event;

public class ClassGenEvent extends Event
{
	public final ClassWriter writer;
	public final ClassNode node;
	public final int pokedexNb;
	public ClassGenEvent(ClassWriter cw, ClassNode n, int nb) {
		this.writer = cw;
		pokedexNb = nb;
		this.node = n;
	}

}
