/*******************************************************************************
 * Copyright 2016, the Biomes O' Plenty Team
 * 
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 * 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/.
 ******************************************************************************/
package toughasnails.api;

import java.util.UUID;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.EnumDifficulty;

public class HealthHelper 
{
    public static final UUID STARTING_HEALTH_MODIFIER_ID = UUID.fromString("050F240E-868F-4164-A67E-374084DACA71");
    public static final UUID LIFEBLOOD_HEALTH_MODIFIER_ID = UUID.fromString("B04DB09D-ED8A-4B82-B1EF-ADB425174925");

    public static IHeartAmountProvider heartProvider;

    public static int getActiveHearts(EntityPlayer player)
    {
        return Math.min((int)(player.getMaxHealth() / 2), 10);
    }
    
    public static int getInactiveHearts(EntityPlayer player)
    {
        return Math.max(10 - (int)(player.getMaxHealth() / 2), 0);
    }
    
    public static int getLifebloodHearts(EntityPlayer player)
    {
        IAttributeInstance maxHealthInstance = player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH);
        AttributeModifier modifier = maxHealthInstance.getModifier(HealthHelper.LIFEBLOOD_HEALTH_MODIFIER_ID);
        
        if (modifier != null)
        {
            return (int)(modifier.getAmount() / 2.0D);
        }

        return 0;
    }
    
    /**Returns true if successful*/
    public static boolean addActiveHearts(EntityPlayer player, int hearts)
    {
        IAttributeInstance maxHealthInstance = player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MAX_HEALTH);
        AttributeModifier modifier = maxHealthInstance.getModifier(HealthHelper.LIFEBLOOD_HEALTH_MODIFIER_ID);
        float newHealth = player.getMaxHealth() + (hearts * 2);
        double existingHearts = modifier != null ? modifier.getAmount() : 0.0D;
        
        return false;
    }


    public static int getStartingHearts(EnumDifficulty difficulty)
    {
        return heartProvider.getStartingHearts(difficulty);
    }

    public interface IHeartAmountProvider
    {
        int getMaxHearts();
        int getStartingHearts(EnumDifficulty difficulty);
    }
}
