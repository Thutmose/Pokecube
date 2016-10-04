package pokecube.core.client.render.particle;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.maths.Vector3;

public class ParticleHandler
{
    private static ParticleHandler instance;

    public static ParticleHandler Instance()
    {
        if (instance == null)
        {
            instance = new ParticleHandler();
            MinecraftForge.EVENT_BUS.register(instance);
        }
        return instance;
    }

    Map<Vector3, IParticle> particles = Maps.newHashMap();

    public void addParticle(Vector3 location, IParticle particle)
    {
        synchronized (particles)
        {
            particles.put(location, particle);
        }
    }

    public void clear()
    {
        particles.clear();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderWorldPost(RenderFogEvent event)
    {
        try
        {
            synchronized (particles)
            {
                GL11.glPushMatrix();
                Set<Vector3> locations = Sets.newHashSet(particles.keySet());
                for (Vector3 target : locations)
                {
                    IParticle particle = particles.get(target);
                    if (particle.getDuration() < 0) continue;
                    EntityPlayer player = Minecraft.getMinecraft().thePlayer;
                    Vector3 source = Vector3.getNewVector().set(player.lastTickPosX, player.lastTickPosY,
                            player.lastTickPosZ);
                    GL11.glPushMatrix();
                    source.set(target.subtract(source));
                    // System.out.println(source);
                    GL11.glTranslated(source.x, source.y, source.z);
                    double d0 = (-player.posX + player.lastTickPosX) * event.getRenderPartialTicks();
                    double d1 = (-player.posY + player.lastTickPosY) * event.getRenderPartialTicks();
                    double d2 = (-player.posZ + player.lastTickPosZ) * event.getRenderPartialTicks();
                    source.set(d0, d1, d2);
                    GL11.glTranslated(source.x, source.y, source.z);
                    particle.render(event.getRenderPartialTicks());
                    GL11.glPopMatrix();
                }
                // particles.clear();
                GL11.glPopMatrix();
                for (Vector3 e : locations)
                {
                    IParticle particle = this.particles.get(e);

                    if (particle.lastTick() != event.getEntity().getEntityWorld().getTotalWorldTime())
                    {
                        particle.setDuration(particle.getDuration() - 1);
                        particle.setLastTick(event.getEntity().getEntityWorld().getTotalWorldTime());
                    }
                }
                HashSet<Vector3> toRemove = new HashSet<Vector3>();
                for (Vector3 e : locations)
                {
                    IParticle particle = this.particles.get(e);
                    if (particle.getDuration() < 0)
                    {
                        toRemove.add(e);
                    }
                }
                for (Vector3 o : toRemove)
                {
                    particles.get(o).kill();
                }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void WorldUnloadEvent(Unload evt)
    {
        if (evt.getWorld().provider.getDimension() == 0
                && FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            clear();
        }

    }
}
