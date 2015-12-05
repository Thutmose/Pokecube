package pokecube.core.client.render.particle;

import thut.api.maths.Vector3;

public class ParticleFactory
{
    public static IParticle makeParticle(String name, Vector3 velocity)
    {
        IParticle ret = null;
        if(name.equalsIgnoreCase("string"))
        {
            ParticleNoGravity particle = new ParticleNoGravity(8, 5);
            particle.setVelocity(velocity);
            ret = particle;
        }
        else if(name.equalsIgnoreCase("aurora"))
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
            particle.setDuration(32);
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
