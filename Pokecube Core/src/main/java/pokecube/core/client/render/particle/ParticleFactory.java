package pokecube.core.client.render.particle;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import pokecube.core.PokecubeCore;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;

public class ParticleFactory
{
    static final Map<Integer, IParticleFactory>        particleTypes = ReflectionHelper.getPrivateValue(
            net.minecraft.client.particle.ParticleManager.class, Minecraft.getMinecraft().effectRenderer,
            "field_178932_g", "particleTypes");
    static final Object                                lock          = ReflectionHelper.getPrivateValue(
            net.minecraft.client.particle.ParticleManager.class, Minecraft.getMinecraft().effectRenderer,
            "field_187241_h", "queueEntityFX");

    private static final Map<String, IParticleFactory> factories     = Maps.newHashMap();

    public static void initVanillaParticles()
    {
        for (Integer i : particleTypes.keySet())
        {
            EnumParticleTypes vanilla = EnumParticleTypes.getParticleFromId(i);
            if (vanilla != null)
            {
                factories.put(vanilla.getParticleName(), particleTypes.get(i));
            }
        }
    }
    
    public static void initDefaultParticles()
    {
        
    }

    public static void registerFactory(String name, IParticleFactory factory)
    {
        factories.put(name, factory);
    }

    public static IParticle makeParticle(String name, Vector3 location, Vector3 velocity, int... args)
    {
        IParticleFactory fact = factories.get(name);
        if (fact != null)
        {
            int id = 0;
            EnumParticleTypes vanilla = null;
            vanilla = EnumParticleTypes.getByName(name);
            if (vanilla == null)
            {
                if (name.contains("smoke"))
                    vanilla = name.contains("large") ? EnumParticleTypes.SMOKE_LARGE : EnumParticleTypes.SMOKE_NORMAL;
            }
            if (vanilla != null) id = vanilla.getParticleID();
            Particle par = fact.createParticle(id, PokecubeCore.getWorld(), location.x, location.y, location.z, velocity.x,
                    velocity.y, velocity.z, args);
            if (par != null)
            {
                if (args.length > 1) par.setMaxAge(Math.max(2, args[1]));
                synchronized (lock)
                {
                    Minecraft.getMinecraft().effectRenderer.addEffect(par);
                }
                return null;
            }
        }
        IParticle ret = null;
        if (name.equalsIgnoreCase("string"))
        {
            ParticleNoGravity particle = new ParticleNoGravity(8, 5);
            particle.setVelocity(velocity);
            ret = particle;
        }
        else if (name.equalsIgnoreCase("aurora"))
        {
            ParticleNoGravity particle = new ParticleNoGravity(0, 0);
            particle.setVelocity(velocity);
            int[][] textures = new int[2][2];
            textures[0][0] = 2;
            textures[0][1] = 4;
            textures[1][0] = 1;
            textures[1][1] = 4;
            particle.setTex(textures);
            particle.name = "aurora";
            int life = 32;
            if (args.length > 1) life = args[1];
            particle.setStartTime((int) (PokecubeCore.getWorld().getWorldTime() % 1000));
            particle.setAnimSpeed(1);
            particle.setLifetime(life);
            particle.setSize(0.1f);
            ret = particle;
        }
        else if (name.equalsIgnoreCase("misc"))
        {
            ParticleNoGravity particle = new ParticleNoGravity(0, 0);
            particle.setVelocity(velocity);
            int[][] textures = new int[2][2];
            textures[0][0] = 2;
            textures[0][1] = 4;
            textures[1][0] = 1;
            textures[1][1] = 4;
            particle.setTex(textures);
            particle.name = "misc";
            int life = 32;
            if (args.length > 0) particle.setColour(args[0]);
            if (args.length > 1) life = args[1];
            particle.setLifetime(life);
            particle.setSize(0.15f);
            ret = particle;
        }
        else if (name.equalsIgnoreCase("powder"))
        {
            ParticleNoGravity particle = new ParticleNoGravity(0, 0);
            particle.setVelocity(velocity);
            int[][] textures = new int[7][2];
            textures[0][0] = 0;
            textures[0][1] = 0;
            textures[1][0] = 1;
            textures[1][1] = 0;
            textures[2][0] = 2;
            textures[2][1] = 0;
            textures[3][0] = 3;
            textures[3][1] = 0;
            textures[4][0] = 4;
            textures[4][1] = 0;
            textures[5][0] = 5;
            textures[5][1] = 0;
            textures[6][0] = 6;
            textures[6][1] = 0;
            particle.setTex(textures);
            particle.setSize(0.125f);
            particle.name = "powder";
            int life = 32;
            if (args.length > 0) particle.setColour(args[0]);
            if (args.length > 1) life = args[1];
            particle.setLifetime(life);
            ret = particle;
        }
        else if (name.equalsIgnoreCase("leaf"))
        {
            ParticleOrientable particle = new ParticleOrientable(2, 2);
            particle.setLifetime(20);
            particle.setVelocity(velocity);
            particle.size = 0.25;
            if (velocity != null)
            {
                Vector3 normal = velocity.normalize().copy();
                Vector4 v3 = new Vector4(0, 1, 0, (float) (90 - normal.toSpherical().z * 180 / Math.PI));
                Vector4 v2 = new Vector4(1, 0, 0, (float) (90 + (normal.y * 180 / Math.PI)));
                particle.setOrientation(v3.addAngles(v2));
            }
            ret = particle;
        }
        else
        {
            ParticleNoGravity particle = new ParticleNoGravity(0, 0);
            particle.setVelocity(velocity);
            ret = particle;
        }
        return ret;
    }
}
