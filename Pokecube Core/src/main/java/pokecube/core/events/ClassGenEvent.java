package pokecube.core.events;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import net.minecraftforge.fml.common.eventhandler.Event;

/** This is event is sent for each generic pokemob class generated.<br>
 * It is mostly sent during the pre-init phase, when the main classes are being
 * made.<br>
 * It is sent right after the node has accepted the classwriter. */
public class ClassGenEvent extends Event
{
    public final ClassWriter writer;
    public final ClassNode   node;
    public final int         pokedexNb;

    public ClassGenEvent(ClassWriter cw, ClassNode n, int nb)
    {
        this.writer = cw;
        pokedexNb = nb;
        this.node = n;
    }

}
