package pokecube.core;

import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import net.minecraftforge.common.MinecraftForge;
import pokecube.core.entity.pokemobs.GenericPokemob;
import pokecube.core.events.ClassGenEvent;

/** This class generates the pokemob classes for each pokemob. It works by
 * copying the byte array from GenericPokemob.class, then modifiying it
 * accordingly for each pokemob class it makes.
 * 
 * @author Thutmose */
public class ByteClassLoader extends ClassLoader
{
    String resName = "GenericPokemob.class";

    public ByteClassLoader(ClassLoader ucl)
    {
        super(ucl);
    }

    public Class<?> loadClass(String name, byte[] jarBytes, boolean resolve) throws ClassNotFoundException
    {
        name = name.replace("/", ".");
        Class<?> clazz = null;
        try
        {
            clazz = super.loadClass(name, false);
        }
        catch (Exception e1)
        {

        }
        if (clazz == null)
        {
            try
            {
                byte[] bytes = jarBytes;
                clazz = defineClass(name, bytes, 0, bytes.length);
                if (resolve)
                {
                    resolveClass(clazz);
                }
            }
            catch (Exception e)
            {
                clazz = super.loadClass(name, resolve);
            }
        }
        return clazz;
    }

    private static byte[] genericMobBytes;

    public Class<?> generatePokemobClass(int pokedexNb) throws ClassNotFoundException
    {
        try
        {
            InputStream is = GenericPokemob.class.getResourceAsStream(resName);
            ClassReader reader = new ClassReader(is);
            genericMobBytes = reader.b.clone();
        }
        catch (Exception e)
        {

            e.printStackTrace();
        }

        ClassReader reader = new ClassReader(genericMobBytes);
        ClassWriter writer;
        byte[] genericMob = reader.b.clone();
        int num = pokedexNb;
        ClassNode changer = new ClassNode();
        reader.accept(changer, 0);

        changer.sourceFile = changer.sourceFile.replace(".java", "") + num + ".java";
        changer.name = changer.name + num;

        writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        changer.accept(writer);
        ClassGenEvent evt = new ClassGenEvent(writer, changer, num);
        MinecraftForge.EVENT_BUS.post(evt);
        writer.visitEnd();
        genericMob = writer.toByteArray();

        ClassReader cr = new ClassReader(genericMob);
        ClassNode classNode = new ClassNode();

        cr.accept(classNode, 0);

        Class<?> c = loadClass(classNode.name, genericMob, true);
        return c;

    }

}
