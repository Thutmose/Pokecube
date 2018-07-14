package pokecube.modelloader.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.entity.RenderAdvancedPokemobModel;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.modelloader.CommonProxy;
import pokecube.modelloader.IMobProvider;
import pokecube.modelloader.ModPokecubeML;
import pokecube.modelloader.client.gui.GuiAnimate;
import pokecube.modelloader.client.render.AnimationLoader;
import pokecube.modelloader.client.render.TabulaPackLoader;
import pokecube.modelloader.common.Config;
import pokecube.modelloader.common.ExtraDatabase;
import pokecube.modelloader.items.ItemModelReloader;
import thut.core.client.render.model.ModelFactory;

public class ClientProxy extends CommonProxy
{

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        return new GuiAnimate();
    }

    @Override
    public void init()
    {
        super.init();
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ItemModelReloader.instance, 0,
                new ModelResourceLocation("pokecube_ml:modelreloader", "inventory"));
    }

    @Override
    public void populateModels()
    {
        TabulaPackLoader.clear();
        List<String> modList = Lists.newArrayList(modelProviders.keySet());
        // Sort to prioritise default mod
        Collections.sort(modList, Config.instance.modIdComparator);

        ProgressBar bar = ProgressManager.push("Model Locations", modList.size());
        for (String mod : modList)
        {
            bar.step(mod);
            IMobProvider provider = mobProviders.get(mod);
            ProgressBar bar2 = ProgressManager.push("Checking Pokemob Models", Database.getSortedFormes().size());
            for (PokedexEntry p : Database.getSortedFormes())
            {
                bar2.step(p.getName());
                String name = p.getTrimmedName().toLowerCase(Locale.ENGLISH);
                try
                {
                    ResourceLocation tex = new ResourceLocation(mod, provider.getModelDirectory(p) + name + ".xml");
                    IResource res = Minecraft.getMinecraft().getResourceManager().getResource(tex);
                    res.close();
                    ArrayList<String> models = modModels.get(mod);
                    if (models == null)
                    {
                        modModels.put(mod, models = new ArrayList<String>());
                    }
                    if (!models.contains(name)) models.add(name);
                }
                catch (Exception e)
                {
                    try
                    {
                        ResourceLocation tex = new ResourceLocation(mod, provider.getModelDirectory(p) + name + ".tbl");
                        IResource res = Minecraft.getMinecraft().getResourceManager().getResource(tex);
                        res.getInputStream().close();
                        res.close();
                        ArrayList<String> models = modModels.get(mod);
                        if (models == null)
                        {
                            modModels.put(mod, models = new ArrayList<String>());
                        }
                        if (!models.contains(name)) models.add(name);
                    }
                    catch (Exception e1)
                    {
                        List<String> extensions = Lists.newArrayList(ModelFactory.getValidExtensions());
                        Collections.sort(extensions, Config.instance.extensionComparator);
                        for (String ext : extensions)
                        {
                            try
                            {
                                ResourceLocation tex = new ResourceLocation(mod,
                                        provider.getModelDirectory(p) + name + "." + ext);
                                IResource res = Minecraft.getMinecraft().getResourceManager().getResource(tex);
                                res.getInputStream().close();
                                res.close();
                                ArrayList<String> models = modModels.get(mod);
                                if (models == null)
                                {
                                    modModels.put(mod, models = new ArrayList<String>());
                                }
                                if (!models.contains(name)) models.add(name);
                                break;
                            }
                            catch (Exception e2)
                            {

                            }
                        }
                    }
                }
            }
            ProgressManager.pop(bar2);
            if (modModels.containsKey(mod))
            {
                HashSet<String> alternateFormes = Sets.newHashSet();
                bar2 = ProgressManager.push("Pokemob Models Pass 1", modModels.get(mod).size());
                for (String s : modModels.get(mod))
                {
                    bar2.step(s);
                    PokedexEntry entry = Database.getEntry(s);
                    if (!AnimationLoader.initModel(provider, mod + ":" + provider.getModelDirectory(entry) + s,
                            alternateFormes))
                    {
                        TabulaPackLoader.loadModel(provider, mod + ":" + provider.getModelDirectory(entry) + s,
                                alternateFormes);
                    }
                }
                ProgressManager.pop(bar2);
                bar2 = ProgressManager.push("Pokemob Models Pass 2", alternateFormes.size());
                for (String s : alternateFormes)
                {
                    String[] args2 = s.toLowerCase(Locale.ENGLISH).split("/");
                    String name = args2[args2.length > 1 ? args2.length - 1 : 0];
                    bar2.step(name);
                    if (!AnimationLoader.initModel(provider, s, alternateFormes))
                    {
                        TabulaPackLoader.loadModel(provider, s, alternateFormes);
                    }
                }
                ProgressManager.pop(bar2);
            }
        }
        ProgressManager.pop(bar);
        TabulaPackLoader.postProcess();
        registerRenderInformation();

        if (AnimationLoader.loaded)
        {
            bar = ProgressManager.push("Preloading Models", Database.getSortedFormes().size());
            for (PokedexEntry entry : Database.getSortedFormes())
            {
                if ((ModPokecubeML.preload || Config.instance.toPreload.contains(entry.getName())))
                {
                    bar.step("Preloading " + entry.getName());
                    if (PokecubeMod.debug) PokecubeMod.log("Preloading model for " + entry);
                    ModPokecubeML.proxy.reloadModel(entry);
                }
                else bar.step("Skipping " + entry.getName());
            }
            ProgressManager.pop(bar);
        }
    }

    @Override
    public void reloadModel(PokedexEntry model)
    {
        modModels.clear();
        TabulaPackLoader.remove(model);
        List<String> modList = Lists.newArrayList(modelProviders.keySet());
        Collections.sort(modList, Config.instance.modIdComparator);
        boolean found = false;
        for (String mod : modList)
        {
            if (found) break;
            IMobProvider provider = mobProviders.get(mod);
            PokedexEntry p = model;
            String name = p.getTrimmedName();
            boolean validModel = false;
            try
            {
                // First check to see if it has an XML
                ResourceLocation tex = new ResourceLocation(mod, provider.getModelDirectory(p) + name + ".xml");
                IResource res = Minecraft.getMinecraft().getResourceManager().getResource(tex);
                res.close();
                ArrayList<String> models = modModels.get(mod);
                if (models == null)
                {
                    modModels.put(mod, models = new ArrayList<String>());
                }
                models.remove(name);
                if (!models.contains(name)) models.add(name);
            }
            catch (Exception e)
            {
                if (ExtraDatabase.resourceEntries.containsKey(name)) try
                {
                    // Then check to see if maybe the XML was packaged inside
                    // another.
                    ResourceLocation tex = new ResourceLocation(mod,
                            provider.getModelDirectory(p) + ExtraDatabase.resourceEntries.get(name) + ".xml");
                    IResource res = Minecraft.getMinecraft().getResourceManager().getResource(tex);
                    res.close();
                    ArrayList<String> models = modModels.get(mod);
                    if (models == null)
                    {
                        modModels.put(mod, models = new ArrayList<String>());
                    }
                    models.remove(name);
                    if (!models.contains(name)) models.add(name);
                    validModel = true;
                }
                catch (Exception e2)
                {
                }

                if (!validModel)
                {
                    name = p.getTrimmedName();
                    List<String> extensions = Lists.newArrayList(ModelFactory.getValidExtensions());
                    Collections.sort(extensions, Config.instance.extensionComparator);
                    for (String ext : extensions)
                    {
                        try
                        {
                            // Then look for an ModelFactory model
                            ResourceLocation tex = new ResourceLocation(mod,
                                    provider.getModelDirectory(p) + name + "." + ext);
                            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(tex);
                            res.close();
                            ArrayList<String> models = modModels.get(mod);
                            if (models == null)
                            {
                                modModels.put(mod, models = new ArrayList<String>());
                            }
                            validModel = true;
                            models.remove(name);
                            if (!models.contains(name)) models.add(name);
                            break;
                        }
                        catch (Exception e3)
                        {

                        }
                    }
                }

                if (!validModel)
                {
                    try
                    {
                        // finally look for an tbl model
                        ResourceLocation tex = new ResourceLocation(mod, provider.getModelDirectory(p) + name + ".tbl");
                        IResource res = Minecraft.getMinecraft().getResourceManager().getResource(tex);
                        res.close();
                        ArrayList<String> models = modModels.get(mod);
                        if (models == null)
                        {
                            modModels.put(mod, models = new ArrayList<String>());
                        }
                        models.remove(name);
                        if (!models.contains(name)) models.add(name);
                    }
                    catch (IOException e2)
                    {
                        if (PokecubeMod.debug) PokecubeMod
                                .log("No model for " + name + " in " + mod + " " + provider.getModelDirectory(p));
                        continue;
                    }
                }
            }
            if (modModels.containsKey(mod))
            {
                HashSet<String> alternateFormes = Sets.newHashSet();
                PokedexEntry entry = model;
                if (!AnimationLoader.initModel(provider, mod + ":" + provider.getModelDirectory(entry) + name,
                        alternateFormes))
                {
                    TabulaPackLoader.loadModel(provider, mod + ":" + provider.getModelDirectory(entry) + name,
                            alternateFormes);
                }
                for (String s : alternateFormes)
                {
                    String[] args2 = s.toLowerCase(Locale.ENGLISH).split("/");
                    name = args2[args2.length > 1 ? args2.length - 1 : 0];
                    if (!AnimationLoader.initModel(provider, s, alternateFormes))
                    {
                        TabulaPackLoader.loadModel(provider, s, alternateFormes);
                    }
                }
                found = true;
            }
        }
        TabulaPackLoader.postProcess();
        registerRenderInformation();
    }

    @Override
    public void postInit()
    {
        super.postInit();
        AnimationLoader.loaded = true;
        if (Config.instance.preload || !Config.instance.toPreload.isEmpty())
        {
            populateModels();
        }
    }

    @Override
    public void preInit()
    {
        super.preInit();
    }

    @Override
    public void registerRenderInformation()
    {
        for (String modid : modelProviders.keySet())
        {
            Object mod = modelProviders.get(modid);
            if (modModels.containsKey(modid))
            {
                for (final String s : modModels.get(modid))
                {
                    PokedexEntry entry = Database.getEntry(s);
                    if (entry == null) continue;
                    if (AnimationLoader.models.containsKey(entry.getTrimmedName())
                            || TabulaPackLoader.modelMap.containsKey(entry))
                    {
                        PokecubeCore.proxy.registerPokemobRenderer(s, new IRenderFactory<EntityLiving>()
                        {
                            @SuppressWarnings({ "rawtypes", "unchecked" })
                            @Override
                            public Render<? super EntityLiving> createRenderFor(RenderManager manager)
                            {
                                RenderAdvancedPokemobModel<?> renderer = new RenderAdvancedPokemobModel(
                                        entry.getTrimmedName(), manager, 1);
                                if (entry != null
                                        && (ModPokecubeML.preload || Config.instance.toPreload.contains(entry.getName())
                                                || Config.instance.toPreload.contains(entry.getTrimmedName())))
                                {
                                    renderer.preload();
                                }
                                return (Render<? super EntityLiving>) renderer;
                            }
                        }, mod);
                    }
                }
            }
        }
    }
}
