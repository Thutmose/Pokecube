package toughasnails.temperature;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import toughasnails.api.TANPotions;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.config.TemperatureOption;
import toughasnails.api.stat.StatHandlerBase;
import toughasnails.api.stat.capability.ITemperature;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureScale;
import toughasnails.api.temperature.TemperatureScale.TemperatureRange;
import toughasnails.temperature.modifier.TemperatureModifier;
import toughasnails.temperature.modifier.TemperatureModifier.ExternalModifier;

public class TemperatureHandler extends StatHandlerBase implements ITemperature
{
    private int temperatureLevel;
    private int prevTemperatureLevel;
    private int temperatureTimer;
    private Map<String, TemperatureModifier.ExternalModifier> externalModifiers;
    
    public final TemperatureDebugger debugger = new TemperatureDebugger();

    static
    {
    }

    public TemperatureHandler()
    {
        this.temperatureLevel = TemperatureScale.getScaleTotal() / 2;
        this.prevTemperatureLevel = this.temperatureLevel;
        this.externalModifiers = Maps.newHashMap();
    }
    
    @Override
    public void update(EntityPlayer player, World world, Phase phase)
    {
        if (!SyncedConfig.getBooleanValue(TemperatureOption.ENABLE_TEMPERATURE))
            return;

        if (phase == Phase.END && !world.isRemote)
        {
            int targetTemperature = getPlayerTarget(player);
            int tempChangeTicks = TemperatureScale.getRateForTemperatures(temperatureLevel, targetTemperature);

            boolean incrementTemperature = ++temperatureTimer >= tempChangeTicks;
            boolean updateClient = ++debugger.debugTimer % 5 == 0;

            debugger.temperatureTimer = temperatureTimer;
            debugger.changeTicks = tempChangeTicks;

            Iterator<Map.Entry<String, ExternalModifier>> it = externalModifiers.entrySet().iterator();

            while (it.hasNext())
            {
                Map.Entry<String, ExternalModifier> entry = it.next();

                if (entry.getValue().getEndTime() < this.temperatureTimer)
                {
                    this.externalModifiers.remove(entry.getKey());
                }
            }

            if (incrementTemperature)
            {
                if (!player.isCreative())
                    this.addTemperature(new Temperature((int)Math.signum(targetTemperature - this.temperatureLevel)));
                // always reset the time when incrementing is supposed to happen
                // even in creative mode
                this.temperatureTimer = 0;
            }

            addPotionEffects(player);

            if (updateClient)
            {
                //This works because update is only called if !world.isRemote
                debugger.finalize((EntityPlayerMP)player);
            }
        }
    }

    @Override
    public int getPlayerTarget(EntityPlayer player)
    {
        return 0;
    }

    private void addPotionEffects(EntityPlayer player)
    {
        TemperatureRange range = TemperatureScale.getTemperatureRange(this.temperatureLevel);
        
        //The point from 0 to 1 at which potion effects begin in an extremity range
        float extremityDelta = (3.0F / 6.0F);
        
        //Start the hypo/hyperthermia slightly after the real ranges start
        int hypoRangeSize = (int)(TemperatureRange.ICY.getRangeSize() * extremityDelta);
        int hypoRangeStart = hypoRangeSize - 1;
        int hyperRangeSize = (int)(TemperatureRange.HOT.getRangeSize() * extremityDelta);
        int hyperRangeStart = (TemperatureScale.getScaleTotal() + 1) - hyperRangeSize;
        
        //Don't apply any negative effects whilst in creative mode
        if (!player.capabilities.isCreativeMode && (SyncedConfig.getBooleanValue(TemperatureOption.ENABLE_TEMPERATURE)))
        {
            if (this.temperatureLevel <= hypoRangeStart && (!player.isPotionActive(TANPotions.cold_resistance)) && (temperatureLevel < prevTemperatureLevel || !player.isPotionActive(TANPotions.hypothermia)))
            {
                player.removePotionEffect(TANPotions.hypothermia);
                player.addPotionEffect(new PotionEffect(TANPotions.hypothermia, 200, 0));
            }
            else if (this.temperatureLevel >= hyperRangeStart && (!player.isPotionActive(TANPotions.heat_resistance)) && (temperatureLevel > prevTemperatureLevel || !player.isPotionActive(TANPotions.hyperthermia)))
            {
                player.removePotionEffect(TANPotions.hyperthermia);
                player.addPotionEffect(new PotionEffect(TANPotions.hyperthermia, 200, 0));
            }
        }
    }
    
    @Override
    public boolean hasChanged()
    {
        return this.prevTemperatureLevel != this.temperatureLevel;
    }
    
    @Override
    public void onSendClientUpdate()
    {
        this.prevTemperatureLevel = this.temperatureLevel;
    }
    
    @Override
    public IMessage createUpdateMessage()
    {
        return null;
    }
    
    @Override
    public void setChangeTime(int ticks)
    {
        this.temperatureTimer = ticks;
    }
    
    @Override
    public int getChangeTime()
    {
        return this.temperatureTimer;
    }
    
    @Override
    public void setTemperature(Temperature temperature)
    {
        this.temperatureLevel = temperature.getRawValue();
    }
    
    @Override
    public void addTemperature(Temperature difference)
    {
        this.temperatureLevel = Math.max(Math.min(TemperatureScale.getScaleTotal(), this.temperatureLevel + difference.getRawValue()), 0);
    }
    
    @Override
    public void applyModifier(String name, int amount, int rate, int duration)
    {
        if (this.externalModifiers.containsKey(name))
        {
            ExternalModifier modifier = this.externalModifiers.get(name);
            modifier.setEndTime(this.temperatureTimer + duration);
        }
        else
        {
            TemperatureModifier.ExternalModifier modifier = new TemperatureModifier.ExternalModifier(name, amount, rate, this.temperatureTimer + duration);
            this.externalModifiers.put(name, modifier);
        }
    }
    
    @Override
    public boolean hasModifier(String name) 
    {
        return this.externalModifiers.containsKey(name);
    }
    
    @Override
    public ImmutableMap<String, ExternalModifier> getExternalModifiers() 
    {
        return ImmutableMap.copyOf(this.externalModifiers);
    }

    @Override
    public void setExternalModifiers(Map<String, ExternalModifier> externalModifiers) 
    {
        this.externalModifiers = externalModifiers;
    }
    
    @Override
    public Temperature getTemperature()
    {
        return new Temperature(this.temperatureLevel);
    }
}
