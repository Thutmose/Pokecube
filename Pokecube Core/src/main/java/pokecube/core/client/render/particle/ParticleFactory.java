package pokecube.core.client.render.particle;

import pokecube.core.utils.Vector4;
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
            particle.setLifetime(32);
            ret = particle;
        }
        else if(name.equalsIgnoreCase("leaf"))
        {
            ParticleOrientable particle = new ParticleOrientable(2, 2);
            particle.setLifetime(20);
            particle.setVelocity(velocity);
            particle.size = 0.25;
            if(velocity!=null)
            {
                Vector3 normal = velocity.normalize().copy();
                Vector4 v3 = new Vector4(0,1,0,(float) (90-normal.toSpherical().z * 180/Math.PI));
                Vector4 v2 = new Vector4(1,0,0,(float) (90+(normal.y * 180/Math.PI)));
                ((ParticleOrientable)particle).setOrientation(v3.addAngles(v2));
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
