package com.mcf.davidee.nbteditpqb.forge;

import java.io.File;

import com.mcf.davidee.nbteditpqb.NBTEdit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommonProxy
{
    public void registerInformation()
    {

    }

    public File getMinecraftDirectory()
    {
        return new File(".");
    }

    public void openEditGUI(int entityID, NBTTagCompound tag)
    {

    }

    public void openEditGUI(int entityID, String customName, NBTTagCompound tag)
    {

    }

    public void openEditGUI(BlockPos pos, NBTTagCompound tag)
    {

    }

    public void sendMessage(EntityPlayer player, String message, TextFormatting color)
    {
        if (player != null)
        {
            ITextComponent component = new TextComponentString(message);
            component.getStyle().setColor(color);
            player.sendMessage(component);
        }
    }

    public boolean checkPermission(EntityPlayer player)
    {
        if (NBTEdit.opOnly ? player.canUseCommand(4, NBTEdit.MODID)
                : player.capabilities.isCreativeMode) { return true; }
        return false;
    }
}
