package pokecube.compat.waila;

import java.awt.Dimension;
import java.util.List;

import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import mcp.mobius.waila.api.IWailaTooltipRenderer;
import mcp.mobius.waila.api.SpecialChars;
import mcp.mobius.waila.api.impl.ModuleRegistrar;
import mcp.mobius.waila.overlay.DisplayUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.EggStats;
import pokecube.core.database.stats.KillStats;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.PokeType;

public class WailaCompat implements IWailaEntityProvider
{
    public static class TTRenderString implements IWailaTooltipRenderer
    {
        
        public TTRenderString(){
        }
        
        @Override
        public void draw(String[] params, IWailaCommonAccessor accessor) {
            
            String total = "";
            String name = params[0];
            total += name;
            int i = Integer.parseInt(params[1]);
            DisplayUtil.drawString(name, 0, 0, i, true);
            if(params.length == 4)
            {
                name += " ";
                total += " ";
                int l = Minecraft.getMinecraft().fontRendererObj.getStringWidth(name);
                name = params[2];
                total += name +" ";
                i = Integer.parseInt(params[3]);
                DisplayUtil.drawString(name, l, 0, i, true);
            }
            Entity entity = accessor.getEntity();
            if(entity instanceof IPokemob)
            {
                IPokemob pokemob = (IPokemob) entity;
                int l = Minecraft.getMinecraft().fontRendererObj.getStringWidth(total);
                int caught = CaptureStats.getTotalNumberOfPokemobCaughtBy(accessor.getPlayer().getUniqueID().toString(), pokemob.getPokedexEntry());
                int hatched = EggStats.getTotalNumberOfPokemobHatchedBy(accessor.getPlayer().getUniqueID().toString(), pokemob.getPokedexEntry());
                int number = caught + hatched;
                int killed = KillStats.getTotalNumberOfPokemobKilledBy(accessor.getPlayer().getUniqueID().toString(), pokemob.getPokedexEntry());
                DisplayUtil.drawString(number+"", l, 0, PokeType.grass.colour, true);
                l += Minecraft.getMinecraft().fontRendererObj.getStringWidth(number+"");
                DisplayUtil.drawString("/", l, 0, PokeType.normal.colour, true);
                l += Minecraft.getMinecraft().fontRendererObj.getStringWidth("/");
                DisplayUtil.drawString(killed+"", l, 0, PokeType.fighting.colour, true);
            }
        }

        @Override
        public Dimension getSize(String[] params, IWailaCommonAccessor accessor) {
            return new Dimension(DisplayUtil.getDisplayWidth(""),  8);
        }
    }

    public WailaCompat()
    {
        ModuleRegistrar.instance().registerBodyProvider(this, EntityPokemob.class);
        ModuleRegistrar.instance().registerTailProvider(this, EntityPokemob.class);
        ModuleRegistrar.instance().registerNBTProvider(this, EntityPokemob.class);
//        ModuleRegistrar.instance().registerOverrideEntityProvider(this, EntityPokemob.class);
        ModuleRegistrar.instance().registerTooltipRenderer("pokecube", new TTRenderString());
    }

    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP arg0, Entity arg1, NBTTagCompound arg2, World arg3)
    {
        return arg2;
    }

    @Override
    public List<String> getWailaBody(Entity arg0, List<String> arg1, IWailaEntityAccessor arg2,
            IWailaConfigHandler arg3)
    {
        if(arg0 instanceof IPokemob)
        {
            IPokemob pokemob = (IPokemob) arg0;
            
            String test = "";
            PokeType type = pokemob.getType1();
            if(pokemob.getType2() == PokeType.unknown)
            {
                test = SpecialChars.getRenderString("pokecube", PokeType.getTranslatedName(type), type.colour+"");
            }
            else
            {
                PokeType type2 = pokemob.getType2();
                test = SpecialChars.getRenderString("pokecube", PokeType.getTranslatedName(type), type.colour+"", PokeType.getTranslatedName(type2), type2.colour+"");
            }

            arg1.add(test);
        }
        
        return arg1;
    }

    @Override
    public List<String> getWailaHead(Entity arg0, List<String> arg1, IWailaEntityAccessor arg2,
            IWailaConfigHandler arg3)
    {
        return arg1;
    }

    @Override
    public Entity getWailaOverride(IWailaEntityAccessor arg0, IWailaConfigHandler arg1)
    {
        return arg0.getEntity();
    }
    
    @Override
    public List<String> getWailaTail(Entity arg0, List<String> arg1, IWailaEntityAccessor arg2,
            IWailaConfigHandler arg3)
    {
        if(arg0 instanceof IPokemob)
        {
            IPokemob pokemob = (IPokemob) arg0;
            PokedexEntry entry = pokemob.getPokedexEntry();
            if(entry.getBaseName() != entry.getName())
            {
                arg1.add(entry.getTranslatedName());
            }
        }
        return arg1;
    }
}
