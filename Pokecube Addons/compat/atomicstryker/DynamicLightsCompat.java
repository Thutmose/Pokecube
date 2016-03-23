package pokecube.compat.atomicstryker;

import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;

public class DynamicLightsCompat
{
    public static class PokemobLightSource implements atomicstryker.dynamiclights.client.IDynamicLightSource
    {
        final IPokemob pokemob;
        public PokemobLightSource(IPokemob pokemob)
        {
            this.pokemob = pokemob;
        }
        
        @Override
        /**
         * Entity the Dynamic Light Source is associated with.
         * The Light will always be centered on this Entity and move with it.
         * Any Entity can only be associated with a single Light!
         * If the Entity is dead (eg. Entity.isDead() returns true), the Light will be removed aswell.
         */
        public Entity getAttachmentEntity()
        {
            return (Entity) pokemob;
        }
           
        @Override
        /**
         * Values above 15 will not be considered, 15 is the MC max level. Values below 1 are considered disabled.
         * Values can be changed on the fly.
         * @return int value of Minecraft Light level at the Dynamic Light Source
         */
        public int getLightLevel()
        {
            return 15;
        }
    }
    Method addLightSource;
    
    Method removeLightSource;
    
    public DynamicLightsCompat()
    {
        try
        {
            Class<?> inter = Class.forName("atomicstryker.dynamiclights.client.DynamicLights");
            addLightSource = inter.getMethod("addLightSource", IDynamicLightSource.class);
            removeLightSource = inter.getMethod("removeLightSource", IDynamicLightSource.class);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void DeathEvent(LivingDeathEvent evt)
    {
        if(evt.entity instanceof IPokemob && ((IPokemob)evt.entity).isType(PokeType.fire))
        {
            try
            {
                removeLightSource.invoke(null, new PokemobLightSource((IPokemob) evt.entity));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    
    @SubscribeEvent
    public void JoinWorldEvent(EntityJoinWorldEvent evt)
    {
        if(evt.entity instanceof IPokemob && ((IPokemob)evt.entity).isType(PokeType.fire))
        {
            try
            {
                addLightSource.invoke(null, new PokemobLightSource((IPokemob) evt.entity));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
