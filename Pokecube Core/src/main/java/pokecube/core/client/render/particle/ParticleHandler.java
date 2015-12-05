package pokecube.core.client.render.particle;

import java.util.HashSet;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

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
    Map<Vector3, IParticle>        particles = Maps.newHashMap();
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

    public void addParticle(Vector3 location, IParticle particle)
    {
        particles.put(location, particle);
    }

    public void clear()
    {
        particles.clear();
    }

    @SubscribeEvent
    public void WorldUnloadEvent(Unload evt)
    {
        if (evt.world.provider.getDimensionId() == 0 && FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            clear();
        }

    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderWorldPost(RenderFogEvent event)
    {
        try
        {
            GL11.glPushMatrix();
            for (Vector3 location : particles.keySet())
            {
                IParticle particle = particles.get(location);

                if (particle.getDuration() < 0) continue;

                EntityPlayer player = Minecraft.getMinecraft().thePlayer;
                Vector3 source = Vector3.getNewVectorFromPool().set(player);
                GL11.glPushMatrix();

                source.x = location.x - source.x;
                source.y = location.y - source.y;
                source.z = location.z - source.z;

                GL11.glTranslated(source.x, source.y, source.z);
                // Clear out the jitteryness from rendering
                source.x = player.prevPosX - player.posX;
                source.y = player.prevPosY - player.posY;
                source.z = player.prevPosZ - player.posZ;
                source.scalarMultBy(event.renderPartialTicks);
                GL11.glTranslated(source.x, source.y, source.z);
                // TODO see about fixing the slight movement that occurs when
                // the player stops or starts moving
                particle.render(event.renderPartialTicks);
                GL11.glPopMatrix();
                source.freeVectorFromPool();
            }
            // particles.clear();
            GL11.glPopMatrix();
            for (Vector3 e : particles.keySet())
            {
                IParticle particle = this.particles.get(e);

                if (particle.lastTick() != event.entity.worldObj.getTotalWorldTime())
                {
                    particle.setDuration(particle.getDuration() - 1);
                    particle.setLastTick(event.entity.worldObj.getTotalWorldTime());
                }
            }
            HashSet<Vector3> toRemove = new HashSet<Vector3>();
            for (Vector3 e : particles.keySet())
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
                // o.freeVectorFromPool();
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
}
