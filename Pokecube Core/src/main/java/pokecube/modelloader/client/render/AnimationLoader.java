package pokecube.modelloader.client.render;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.modelloader.IMobProvider;
import pokecube.modelloader.ModPokecubeML;
import pokecube.modelloader.common.Config;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationBuilder;
import thut.core.client.render.animation.AnimationRandomizer;
import thut.core.client.render.animation.AnimationRegistry;
import thut.core.client.render.animation.ModelHolder;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.IModelRenderer.Vector5;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.tabula.components.Animation;

public class AnimationLoader
{
    public static boolean                         loaded    = false;

    static String                                 file      = "";
    @SuppressWarnings("rawtypes")
    public static HashMap<String, IModelRenderer> modelMaps = new HashMap<String, IModelRenderer>();

    public static HashMap<String, ModelHolder>    models    = new HashMap<String, ModelHolder>();

    public static void addStrings(String key, Node node, Set<String> toAddTo)
    {
        if (node.getAttributes() == null) return;
        if (node.getAttributes().getNamedItem(key) != null)
        {
            String[] names = node.getAttributes().getNamedItem(key).getNodeValue().split(":");
            for (String s : names)
            {
                toAddTo.add(s);
            }
        }
    }

    public static void clear()
    {
        models.clear();
        modelMaps.clear();
    }

    public static void remove(PokedexEntry entry)
    {
        models.remove(entry.getName());
        modelMaps.remove(entry.getName());
    }

    public static int getIntValue(Node node, String key, int default_)
    {
        int ret = default_;
        if (node.getAttributes() == null) return ret;
        if (node.getAttributes().getNamedItem(key) != null)
        {
            ret = Integer.parseInt(node.getAttributes().getNamedItem(key).getNodeValue());
        }
        return ret;
    }

    public static IModelRenderer<?> getModel(String name)
    {
        IModelRenderer<?> ret = modelMaps.get(name);
        if (ret != null) return ret;
        ModelHolder model = models.get(name);
        if (model == null) { return null; }
        if ((ret = modelMaps.get(model.name)) != null) return ret;
        parse(model);
        if ((ret = modelMaps.get(model.name)) != null) return ret;
        return ret;
    }

    public static Vector3 getOffset(Node node, Vector3 default_)
    {
        if (node.getAttributes() == null) return default_;
        Vector3 vect = null;
        if (node.getAttributes().getNamedItem("offset") != null)
        {
            vect = Vector3.getNewVector();
            String shift;
            String[] r;
            shift = node.getAttributes().getNamedItem("offset").getNodeValue();
            r = shift.split(",");
            vect.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[1].trim()), Float.parseFloat(r[2].trim()));
            return vect;
        }
        return default_;
    }

    public static Vector5 getRotation(Node node, Vector5 default_)
    {
        if (node.getAttributes() == null) return default_;
        if (node.getAttributes().getNamedItem("rotation") != null)
        {
            String rotation;
            String time = "0";
            Vector4 ro = new Vector4();
            int t = 0;
            String[] r;
            rotation = node.getAttributes().getNamedItem("rotation").getNodeValue();
            if (node.getAttributes().getNamedItem("time") != null)
                time = node.getAttributes().getNamedItem("time").getNodeValue();
            r = rotation.split(",");
            t = Integer.parseInt(time);
            try
            {
                ro.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[1].trim()), Float.parseFloat(r[2].trim()),
                        Float.parseFloat(r[3].trim()));
            }
            catch (Exception e)
            {
                ro.set(0, 1, 0, 0);
            }
            return new Vector5(ro, t);
        }
        return default_;
    }

    public static Vector3 getScale(Node node, Vector3 default_)
    {
        if (node.getAttributes() == null) return default_;
        if (node.getAttributes().getNamedItem("scale") != null)
        {
            Vector3 vect = null;
            vect = Vector3.getNewVector();
            String shift;
            String[] r;
            shift = node.getAttributes().getNamedItem("scale").getNodeValue();
            r = shift.split(",");

            if (r.length == 3)
                vect.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[1].trim()), Float.parseFloat(r[2].trim()));
            else vect.set(Float.parseFloat(r[0].trim()), Float.parseFloat(r[0].trim()), Float.parseFloat(r[0].trim()));
            return vect;
        }
        return default_;
    }

    public static boolean initModel(IMobProvider provider, String s, HashSet<String> toReload)
    {
        ResourceLocation model = null;
        String anim = s + ".xml";

        String[] args = s.split(":");
        String[] args2 = args[1].split("/");
        String name = args2[args2.length > 1 ? args2.length - 1 : 0];
        PokedexEntry entry = Database.getEntry(name);

        ResourceLocation texture = new ResourceLocation(
                s.replace(provider.getModelDirectory(entry), provider.getTextureDirectory(entry)) + ".png");
        List<String> extensions = Lists.newArrayList(ModelFactory.getValidExtensions());
        Collections.sort(extensions, Config.instance.extensionComparator);
        for (String ext : extensions)
        {
            try
            {
                model = new ResourceLocation(s + "." + ext);
                IResource res = Minecraft.getMinecraft().getResourceManager().getResource(model);
                res.close();
                break;
            }
            catch (IOException e1)
            {
            }
            model = null;
        }
        try
        {
            ResourceLocation animation = null;
            try
            {
                animation = new ResourceLocation(anim);
                IResource res = Minecraft.getMinecraft().getResourceManager().getResource(animation);
                res.close();
            }
            catch (Exception e3)
            {
                if (entry.getBaseForme() != null)
                    animation = new ResourceLocation(anim.replace(entry.getTrimmedName().toLowerCase(Locale.ENGLISH),
                            entry.getBaseForme().getTrimmedName().toLowerCase(Locale.ENGLISH)));
                else PokecubeMod.log("Error with locating animation data for " + entry);
            }
            if (model != null)
            {
                if (entry != null)
                {
                    models.put(name, new ModelHolder(model, texture, animation, entry.getName()));
                    if (loaded) getModel(name);
                }
                else
                {
                    System.err.println("Attmpted to register a model for un-registered pokemob " + name);
                }
            }
            else
            {
                if (entry != null && entry.getBaseForme() != null)
                {
                    ModelHolder existing = models.get(entry.getBaseForme().getName());
                    if (existing == null)
                    {
                        toReload.add(s);
                    }
                    else
                    {
                        models.put(name, new ModelHolder(existing.model, texture, animation, entry.getName()));
                        if (loaded) getModel(name);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return model != null;
    }

    public static void load()
    {
        ModPokecubeML.proxy.populateModels();
        for (ModelHolder m : models.values())
        {
            parse(m);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void parse(ModelHolder model)
    {
        try
        {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(model.animation);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputStream stream = res.getInputStream();
            Document doc = dBuilder.parse(stream);
            res.close();
            doc.getDocumentElement().normalize();
            NodeList modelList = doc.getElementsByTagName("model");
            int headDir = 2;
            int headDir2 = 2;
            int headAxis = 2;
            int headAxis2 = 1;
            float[] headCaps = { -180, 180 };
            float[] headCaps1 = { -30, 70 };
            Vector3 offset = null;
            Vector5 rotation = null;
            Vector3 scale = null;
            TextureHelper texturer = null;
            PokemobAnimationChanger animator = null;
            Set<String> headNames = Sets.newHashSet();
            Set<String> shear = Sets.newHashSet();
            Set<String> dye = Sets.newHashSet();
            Set<Animation> tblAnims = Sets.newHashSet();
            HashMap<String, String> mergedAnimations = Maps.newHashMap();
            for (int i = 0; i < modelList.getLength(); i++)
            {
                Node modelNode = modelList.item(i);
                String modelName = model.name;
                HashMap<String, ArrayList<Vector5>> phaseList = new HashMap<String, ArrayList<Vector5>>();
                NodeList partsList = modelNode.getChildNodes();
                for (int j = 0; j < partsList.getLength(); j++)
                {
                    Node part = partsList.item(j);
                    if (part.getNodeName().equals("metadata"))
                    {
                        try
                        {
                            offset = getOffset(part, offset);
                            scale = getScale(part, scale);
                            rotation = getRotation(part, rotation);
                            headDir = getIntValue(part, "headDir", headDir);
                            headDir2 = getIntValue(part, "headDir2", headDir2);
                            headAxis = getIntValue(part, "headAxis", 2);
                            headAxis2 = getIntValue(part, "headAxis2", 0);
                            addStrings("head", part, headNames);
                            addStrings("shear", part, shear);
                            addStrings("dye", part, dye);
                            setHeadCaps(part, headCaps, headCaps1);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else if (part.getNodeName().equals("phase"))
                    {
                        Node phase = part.getAttributes().getNamedItem("name") == null
                                ? part.getAttributes().getNamedItem("type") : part.getAttributes().getNamedItem("name");
                        String phaseName = phase.getNodeValue();
                        boolean preset = false;
                        for (String s : AnimationRegistry.animations.keySet())
                        {
                            if (phaseName.equals(s))
                            {
                                tblAnims.add(AnimationRegistry.make(s, part.getAttributes(), null));
                                preset = true;
                            }
                        }
                        if (phaseName.equals("global"))
                        {
                            try
                            {
                                offset = getOffset(part, offset);
                                scale = getScale(part, scale);
                                rotation = getRotation(part, rotation);
                                headDir = getIntValue(part, "headDir", headDir);
                                headDir2 = getIntValue(part, "headDir2", headDir2);
                                headAxis = getIntValue(part, "headAxis", 2);
                                headAxis2 = getIntValue(part, "headAxis2", 0);
                                addStrings("head", part, headNames);
                                addStrings("shear", part, shear);
                                addStrings("dye", part, dye);
                                setHeadCaps(part, headCaps, headCaps1);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        // Texture Animation info
                        else if (phaseName.equals("textures"))
                        {
                            PokedexEntry entry = Database.getEntry(model.name);
                            setTextureDetails(part, entry);
                        }
                        else if (!preset)
                        {
                            Animation anim = AnimationBuilder.build(part, null);
                            if (anim != null)
                            {
                                Animation old = null;
                                for (Animation a : tblAnims)
                                {
                                    if (a.name.equals(anim.name))
                                    {
                                        old = a;
                                        break;
                                    }
                                }
                                if (old != null) AnimationBuilder.merge(anim, old);
                                else tblAnims.add(anim);
                            }
                        }
                    }
                    else if (part.getNodeName().equals("merges"))
                    {
                        String[] merges = part.getAttributes().getNamedItem("merge").getNodeValue().split("->");
                        mergedAnimations.put(merges[0], merges[1]);
                    }
                    else if (part.getNodeName().equals("customTex"))
                    {
                        texturer = new TextureHelper(part);
                    }
                    else if (part.getNodeName().equals("subAnims"))
                    {
                        animator = new PokemobAnimationChanger(new AnimationRandomizer(part));
                    }
                }

                IModelRenderer<?> renderer = modelMaps.get(modelName);
                DefaultIModelRenderer<?> loaded = null;
                if (renderer instanceof DefaultIModelRenderer) loaded = (DefaultIModelRenderer) renderer;
                if (loaded == null)
                {
                    loaded = new DefaultIModelRenderer(phaseList, model);
                }
                loaded.updateModel(phaseList, model);
                loaded.offset.set(offset);
                loaded.scale.set(scale);
                loaded.rotations = rotation;
                loaded.model.getHeadParts().addAll(headNames);
                for (Animation anim : tblAnims)
                {
                    if (anim != null)
                    {
                        if (!anim.sets.isEmpty()) loaded.animations.put(anim.name, anim);
                    }
                    else
                    {
                        new NullPointerException("Why is there a null animation?").printStackTrace();
                    }
                }
                for (String s : mergedAnimations.keySet())
                {
                    String toName = mergedAnimations.get(s);
                    Animation to = null;
                    Animation from = null;

                    for (Animation anim : loaded.animations.values())
                    {
                        if (s.equals(anim.name))
                        {
                            from = anim;
                        }
                        if (toName.equals(anim.name))
                        {
                            to = anim;
                        }
                        if (to != null && from != null) break;
                    }
                    if (from != null && to == null)
                    {
                        to = new Animation();
                        to.name = toName;
                        to.identifier = toName;
                        to.loops = from.loops;
                        loaded.animations.put(toName, to);
                    }
                    if (to != null && from != null)
                    {
                        AnimationBuilder.merge(from, to);
                    }
                }
                if (animator == null)
                {
                    animator = new PokemobAnimationChanger();
                }
                animator.dyeables.addAll(dye);
                animator.shearables.addAll(shear);
                Set<Animation> anims = Sets.newHashSet();
                anims.addAll(loaded.animations.values());
                animator.init(anims);

                loaded.setTexturer(texturer);
                loaded.setAnimationChanger(animator);

                if (loaded.model.imodel.getHeadInfo() != null)
                {
                    if (headDir != 2) loaded.model.imodel.getHeadInfo().yawDirection = headDir;
                    if (headDir2 != 2) loaded.model.imodel.getHeadInfo().pitchDirection = headDir2;
                    loaded.model.imodel.getHeadInfo().yawAxis = headAxis;
                    loaded.model.imodel.getHeadInfo().pitchAxis = headAxis2;
                    loaded.model.imodel.getHeadInfo().yawCapMin = headCaps[0];
                    loaded.model.imodel.getHeadInfo().yawCapMax = headCaps[1];
                    loaded.model.imodel.getHeadInfo().pitchCapMin = headCaps1[0];
                    loaded.model.imodel.getHeadInfo().pitchCapMax = headCaps1[1];
                }

                loaded.model.preProcessAnimations(loaded.animations.values());
                models.put(modelName, model);
                modelMaps.put(modelName, loaded);
            }

            stream.close();
        }
        catch (Exception e)
        {
            IModelRenderer<?> renderer = modelMaps.get(model.name);
            DefaultIModelRenderer<?> loaded = null;
            if (renderer instanceof DefaultIModelRenderer) loaded = (DefaultIModelRenderer) renderer;
            if (loaded == null)
            {
                loaded = new DefaultIModelRenderer(new HashMap<String, ArrayList<Vector5>>(), model);
            }
            else
            {
                loaded.updateModel(new HashMap<String, ArrayList<Vector5>>(), model);
            }
            models.put(model.name, model);
            modelMaps.put(model.name, loaded);
            System.err.println("No Animation found for " + model.name + " " + model.model);
            e.printStackTrace();
        }
    }

    public static void setHeadCaps(Node node, float[] toFill, float[] toFill1)
    {
        if (node.getAttributes() == null) return;
        if (node.getAttributes().getNamedItem("headCap") != null)
        {
            String shift;
            String[] r;
            shift = node.getAttributes().getNamedItem("headCap").getNodeValue();
            r = shift.split(",");
            toFill[0] = Float.parseFloat(r[0]);
            toFill[1] = Float.parseFloat(r[1]);
        }
        if (node.getAttributes().getNamedItem("headCap1") != null)
        {
            String shift;
            String[] r;
            shift = node.getAttributes().getNamedItem("headCap1").getNodeValue();
            r = shift.split(",");
            toFill1[0] = Float.parseFloat(r[0]);
            toFill1[1] = Float.parseFloat(r[1]);
        }
    }

    public static void setTextureDetails(Node node, PokedexEntry entry)
    {
        if (node.getAttributes() == null) return;
        String[] male = null, female = null;
        if (node.getAttributes().getNamedItem("male") != null)
        {
            String shift;
            shift = node.getAttributes().getNamedItem("male").getNodeValue();
            male = shift.split(",");
        }
        if (node.getAttributes().getNamedItem("female") != null)
        {
            String shift;
            shift = node.getAttributes().getNamedItem("female").getNodeValue();
            female = shift.split(",");
        }
        if (female == null && male != null)
        {
            female = male;
        }
        if (male != null)
        {
            entry.textureDetails[0] = male;
            entry.textureDetails[1] = female;
        }
    }
}
